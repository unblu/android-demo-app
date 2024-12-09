package com.unblu.navigation.demoapp.unblu

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import com.unblu.sdk.core.callback.InitializeExceptionCallback
import com.unblu.sdk.core.errortype.UnbluClientErrorType
import com.unblu.sdk.core.visitor.UnbluVisitorClient

object UnbluHandler {
    var initializing = false
    private fun doInitialize(activity: Activity, result: (UnbluVisitorClient?) -> Unit) {
        if (initializing) return
        initializing = true
        if (UnbluSingleton.getClient() != null) {
            result(UnbluSingleton.getClient())
            return
        }
        UnbluSingleton.start(
            activity.application,
            activity,
            {
                result(it)
                initializing = false
            },
            deinitializeExceptionCallback = object : InitializeExceptionCallback {
                override fun onConfigureNotCalled() {
                    initializing = false
                    result(null)
                }

                override fun onInErrorState() {
                    initializing = false
                    result(null)
                }

                override fun onInitFailed(errorType: UnbluClientErrorType, details: String?) {
                    initializing = false
                    result(null)
                }
            })
    }

    fun initUnblu(
        context: Context,
        success: (UnbluVisitorClient?) -> Unit
    ) {
        UnbluSingleton.getClient()?.let { client ->
            if (UnbluSingleton.getUnbluUi() != null) {
                    success(client)
                return@let
            }
        } ?: run {
            val activity = context as? ComponentActivity
            activity?.let { activity ->
                doInitialize(activity) {
                    it?.let { client ->
                            success(client)
                    } ?: run {
                        success(null)
                    }
                }
            } ?: success(null)
        }
    }
}
