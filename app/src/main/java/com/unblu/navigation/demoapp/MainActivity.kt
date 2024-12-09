package com.unblu.navigation.demoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.unblu.navigation.demoapp.ui.EndpointScreen
import com.unblu.navigation.demoapp.ui.UnbluSheet
import com.unblu.navigation.demoapp.ui.theme.UnbluVisitorBasicSetupTheme
import com.unblu.navigation.demoapp.unblu.UnbluSingleton
import com.unblu.navigation.demoapp.unblu.UnbluViewModel
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.application.UnbluApplicationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val unbluViewModel =
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                .create(UnbluViewModel::class.java)
        observeView(unbluViewModel)
        setContent {
            var openBottomSheet by rememberSaveable { mutableStateOf(false) }
            val mainView by remember { unbluViewModel.view }
            val context = LocalContext.current
            UnbluVisitorBasicSetupTheme {
                DisposableEffect(key1 = Unit) {
                    val disposable =
                        UnbluSingleton.hasUiShowRequest().subscribe {
                            if (UnbluSingleton.getClient() != null) openBottomSheet = true
                            else
                                unbluViewModel.setInitializing(context, true) {
                                    openBottomSheet = true
                                }
                        }

                    onDispose { disposable.dispose() }
                }
                BackHandler {
                    openBottomSheet = false
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EndpointScreen(unbluViewModel) {
                        unbluViewModel.setInitializing(context, true) { openBottomSheet = true }
                    }
                    if (openBottomSheet) {
                        UnbluSheet(mainView) { openBottomSheet = false }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        UnbluApplicationHelper.onNewIntent(intent?.extras)
    }

    private fun observeView(unbluViewModel: UnbluViewModel) {
        Unblu.onVisitorInitialized()
            .map { it.mainView }
            .subscribe { view -> unbluViewModel.setView(view) }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UnbluVisitorBasicSetupTheme {
        val unbluViewModel = UnbluViewModel()
        EndpointScreen(unbluViewModel)
    }
}
