package com.unblu.navigation.demoapp

import android.util.Log
import com.unblu.navigation.demoapp.unblu.UnbluSingleton
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.application.UnbluApplication
import com.unblu.sdk.core.debug.LogLevel

class UApplication : UnbluApplication() {
    override fun onCreate() {
        super.onCreate()
        UnbluSingleton.createSharedPreferences(this)
        Unblu
            .onUiVisibilityRequest()
            .subscribe {
                Log.d("UnbluUsabilityApp","SDK show ui request triggered, reason: ${it.reason}")
                UnbluSingleton.setRequestedUiShow()
            }
        Unblu.setLogLevel(LogLevel.DEBUG)
        Unblu.enableDebugOutput = true
    }
}