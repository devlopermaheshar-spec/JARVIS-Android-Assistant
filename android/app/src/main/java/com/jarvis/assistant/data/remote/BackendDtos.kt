package com.jarvis.assistant.data.remote

import com.google.gson.annotations.SerializedName

data class HistoryItem(
    val role: String, // "user" or "assistant"
    val content: String
)

data class ChatRequest(
    val message: String,
    val history: List<HistoryItem> = emptyList(),
    val language: String = "hinglish", // "hindi", "hinglish", "english"
    val systemContext: Map<String, Any?> = emptyMap()
)

data class ActionPayloadDto(
    @SerializedName("action") val action: String, // "launch_app", "search_files", "battery_status", etc.
    @SerializedName("app_name") val appName: String? = null,
    @SerializedName("query") val query: String? = null,
    @SerializedName("file_type") val fileType: String? = null,
    @SerializedName("date_filter") val dateFilter: String? = null,
    @SerializedName("setting_name") val settingName: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("time_string") val timeString: String? = null,
    @SerializedName("question") val question: String? = null,
    @SerializedName("options") val options: List<String>? = null
)

data class ChatResponse(
    @SerializedName("reply") val reply: String,
    @SerializedName("action") val action: ActionPayloadDto? = null,
    @SerializedName("error") val error: String? = null
)

data class SummarizeRequest(
    val documentTitle: String,
    val text: String
)

data class SummarizeResponse(
    val summary: String,
    val keyPoints: List<String> = emptyList()
)
