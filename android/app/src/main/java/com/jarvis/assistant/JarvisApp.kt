package com.jarvis.assistant

import android.app.Application
import android.util.Log

class JarvisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("JarvisApp", "J.A.R.V.I.S. Android Operating Core initialized.")
    }
}
