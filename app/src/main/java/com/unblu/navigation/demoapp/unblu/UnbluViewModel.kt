package com.unblu.navigation.demoapp.unblu

import android.content.Context
import android.view.View
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.unblu.navigation.usability.unblu.UnbluConstants

class UnbluViewModel : ViewModel(){
    private val _initializing = mutableStateOf(false)
    val initializing : State<Boolean> = _initializing

    private val _endpoint = mutableStateOf(UnbluConstants.ENDPOINT_URL)
    val endpoint : State<String> = _endpoint

    private val _apiKey = mutableStateOf(UnbluConstants.ENDPOINT_API_KEY)

    private val _view = mutableStateOf<View?>(null)
    val view : State<View?> = _view

    val apiKey : State<String> = _apiKey

    fun setApiKey(apiKey : String){
        _apiKey.value = apiKey
    }

    fun setEndpoint(endpoint : String){
        _endpoint.value = endpoint
    }

    fun setView(view: View){
        _view.value = view
    }

    fun setInitializing(context : Context, initializing: Boolean, result: () -> Unit) {
        _initializing.value = initializing
        if(initializing)
            UnbluHandler.initUnblu(context){
                _initializing.value = false
                result()
            }
    }
}