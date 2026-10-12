/**
 * J.A.R.V.I.S. Secure AI Backend Server
 * Handles conversational NLP, Hinglish/Hindi/English parsing, tool/action extraction,
 * and document summarization with Google Gemini API.
 */

const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 4000;
const HOST = process.env.HOST || '0.0.0.0';
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || '';
const GEMINI_MODEL = process.env.GEMINI_MODEL || 'gemini-1.5-flash';

// Security middleware
app.use(helmet());
app.use(cors({ origin: '*' }));
app.use(express.json({ limit: '10mb' }));

// Rate limiting to protect API quotas
const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 200,
    message: { error: 'Too many requests. Please slow down.' }
});
app.use('/api/', apiLimiter);

// System prompt defining J.A.R.V.I.S. persona and tool calling format
const JARVIS_SYSTEM_INSTRUCTION = `
You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the iconic personal AI assistant inspired by Iron Man's JARVIS.
You are running on the user's Android phone.

CRITICAL INSTRUCTIONS:
1. Address the user respectfully as "Sir" (or "Ma'am" if instructed).
2. TONE: Calm, refined, polite, British wit, concise, and highly competent.
3. LANGUAGE CAPABILITY:
   - Understand Hindi, Hinglish, and English seamlessly.
   - Reply in the language the user speaks: if spoken in Hinglish (e.g., "YouTube kholo"), reply in natural fluent Hinglish (e.g., "Certainly, sir. Launching YouTube.").
   - If spoken in pure Hindi, reply in respectful Hindi.
   - If spoken in English, reply in classic JARVIS British English.
4. ACTION EXTRACTION (TOOL CALLING):
   Whenever the user's command requires an Android action, return a strictly structured JSON response in the following schema:
   {
     "reply": "Spoken/text response to user",
     "action": {
       "action": "ACTION_NAME",
       ...parameters
     }
   }

SUPPORTED ACTIONS AND PARAMETERS:
- "launch_app": { "action": "launch_app", "app_name": "YouTube" | "WhatsApp" | "Camera" | "Chrome" | ... }
- "search_files": { "action": "search_files", "query": "filename or keyword", "file_type": "PDF"|"DOCUMENT"|"IMAGE"|"AUDIO"|"VIDEO"|null, "date_filter": "TODAY"|"YESTERDAY"|"WEEK"|null }
- "battery_status": { "action": "battery_status" }
- "storage_status": { "action": "storage_status" }
- "device_info": { "action": "device_info" }
- "open_settings": { "action": "open_settings", "setting_name": "wifi"|"bluetooth"|"location"|"sound"|"display" }
- "create_reminder": { "action": "create_reminder", "title": "Meeting with team", "time_string": "in 30 minutes" }
- "create_note": { "action": "create_note", "title": "Note Title", "content": "Note body" }
- "ask_clarification": { "action": "ask_clarification", "question": "Follow-up question", "options": ["Option 1", "Option 2"] }

IMPORTANT RULES:
- Never hallucinate completed actions. If an action is required, produce the tool call so the Android app can execute it natively.
- If a command is ambiguous (e.g., "Documents dikhao" without timeframe or format), use "ask_clarification" to politely ask follow-up questions with 2-3 clickable choices.
- Output ONLY valid JSON matching this schema. No markdown backticks or commentary outside JSON.
`;

/**
 * Health check endpoint
 */
app.get('/health', (req, res) => {
    res.json({
        status: 'online',
        service: 'JARVIS-Android-Backend',
        geminiConfigured: Boolean(GEMINI_API_KEY && GEMINI_API_KEY !== 'your_gemini_api_key_here'),
        timestamp: new Date().toISOString()
    });
});

/**
 * POST /api/chat
 * Multi-turn conversational endpoint with tool calling
 */
app.post('/api/chat', async (req, res) => {
    const { message, history = [], language = 'hinglish', systemContext = {} } = req.body;

    if (!message || typeof message !== 'string') {
        return res.status(400).json({ error: 'Message field is required.' });
    }

    // If Gemini API Key is not configured yet, provide a smart fallback response
    if (!GEMINI_API_KEY || GEMINI_API_KEY === 'your_gemini_api_key_here') {
        const fallback = generateLocalRuleFallback(message);
        return res.json(fallback);
    }

    try {
        // Build prompt with conversation history and live telemetry context
        const contextSummary = Object.entries(systemContext)
            .map(([k, v]) => `${k}: ${v}`)
            .join(', ');

        const contents = [];

        // Add history
        for (const h of history) {
            contents.push({
                role: h.role === 'assistant' ? 'model' : 'user',
                parts: [{ text: h.content }]
            });
        }

        // Add current user message with system context
        contents.push({
            role: 'user',
            parts: [{
                text: `[Device Telemetry: ${contextSummary || 'Normal'}]\n[Preferred Language: ${language}]\nUser says: ${message}`
            }]
        });

        const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${GEMINI_API_KEY}`;

        const payload = {
            systemInstruction: {
                parts: [{ text: JARVIS_SYSTEM_INSTRUCTION }]
            },
            contents,
            generationConfig: {
                temperature: 0.4,
                responseMimeType: 'application/json'
            }
        };

        const response = await fetch(geminiUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errText = await response.text();
            console.error('Gemini API Error:', response.status, errText);
            return res.status(response.status).json({
                reply: `Sir, our cloud neural uplink encountered an error (${response.status}). Operating on local telemetry fallback.`,
                error: errText
            });
        }

        const data = await response.json();
        const candidate = data.candidates?.[0]?.content?.parts?.[0]?.text;

        if (!candidate) {
            return res.json({
                reply: "Sir, I couldn't formulate a response. How else may I assist you?"
            });
        }

        try {
            const parsed = JSON.parse(candidate);
            return res.json(parsed);
        } catch (parseErr) {
            // Clean markdown code blocks if any
            const cleaned = candidate.replace(/```json/g, '').replace(/```/g, '').trim();
            const parsed = JSON.parse(cleaned);
            return res.json(parsed);
        }

    } catch (err) {
        console.error('Error handling chat:', err);
        return res.status(500).json({
            reply: 'Sir, an internal exception occurred in the neural network.',
            error: err.message
        });
    }
});

/**
 * POST /api/summarize
 * Document summarization endpoint
 */
app.post('/api/summarize', async (req, res) => {
    const { documentTitle, text } = req.body;

    if (!text || text.length === 0) {
        return res.status(400).json({ error: 'Text content is required for summarization.' });
    }

    if (!GEMINI_API_KEY || GEMINI_API_KEY === 'your_gemini_api_key_here') {
        return res.json({
            summary: `Sir, here is a concise briefing of '${documentTitle || 'document'}': The text consists of ${text.length} characters. Direct cloud summarization requires a configured Gemini API key in backend/.env.`,
            keyPoints: ["Document loaded successfully", `Length: ${text.length} characters`]
        });
    }

    try {
        const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${GEMINI_API_KEY}`;

        const prompt = `
Summarize the following document titled "${documentTitle || 'Untitled'}".
Tone: J.A.R.V.I.S. (formal, articulate, concise).
Provide a concise executive summary paragraph, followed by 3-5 bullet key points.

Document Content:
${text.slice(0, 15000)}
`;

        const payload = {
            contents: [{ parts: [{ text: prompt }] }],
            generationConfig: { temperature: 0.3 }
        };

        const response = await fetch(geminiUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        const output = data.candidates?.[0]?.content?.parts?.[0]?.text || 'No summary could be generated.';

        res.json({
            summary: output,
            keyPoints: []
        });

    } catch (err) {
        console.error('Summarize error:', err);
        res.status(500).json({ error: err.message });
    }
});

/**
 * Smart rule-based fallback when GEMINI_API_KEY is not set
 */
function generateLocalRuleFallback(input) {
    const lower = input.toLowerCase();

    if (lower.includes('youtube')) {
        return {
            reply: "Certainly, sir. Launching YouTube.",
            action: { action: "launch_app", app_name: "YouTube" }
        };
    }
    if (lower.includes('battery')) {
        return {
            reply: "Checking your current battery telemetry, sir.",
            action: { action: "battery_status" }
        };
    }
    if (lower.includes('kal') && (lower.includes('document') || lower.includes('pdf'))) {
        return {
            reply: "Retrieving documents modified yesterday, sir.",
            action: { action: "search_files", query: "", file_type: "DOCUMENT", date_filter: "YESTERDAY" }
        };
    }
    if (lower.includes('download') && lower.includes('pdf')) {
        return {
            reply: "Scanning your Downloads directory for PDF files, sir.",
            action: { action: "search_files", query: "pdf", file_type: "PDF" }
        };
    }
    if (lower.includes('camera')) {
        return {
            reply: "Activating camera sensors, sir.",
            action: { action: "launch_app", app_name: "Camera" }
        };
    }
    if (lower.includes('reminder')) {
        return {
            reply: "Scheduling your reminder now, sir.",
            action: { action: "create_reminder", title: "User Reminder", time_string: "30 minutes" }
        };
    }

    return {
        reply: "Greetings Sir. JARVIS is online. You may instruct me with commands such as 'YouTube kholo', 'Mere downloads mein PDF dhundo', or 'Meri battery kitni hai?'."
    };
}

// Start server
app.listen(PORT, HOST, () => {
    console.log(`===============================================`);
    console.log(`⚡ J.A.R.V.I.S. Secure AI Backend Online`);
    console.log(`   Host: http://${HOST}:${PORT}`);
    console.log(`   Gemini Status: ${GEMINI_API_KEY && GEMINI_API_KEY !== 'your_gemini_api_key_here' ? 'CONNECTED' : 'UNCONFIGURED (Running Fallback)'}`);
    console.log(`===============================================`);
});
