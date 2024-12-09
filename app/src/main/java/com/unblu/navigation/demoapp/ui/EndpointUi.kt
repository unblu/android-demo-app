package com.unblu.navigation.demoapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unblu.navigation.demoapp.ui.theme.Purple80
import com.unblu.navigation.demoapp.unblu.UnbluSingleton
import com.unblu.navigation.demoapp.unblu.UnbluViewModel

@Composable
fun EndpointScreen(unbluViewModel: UnbluViewModel, onClick: () -> Unit = {}) {
    val initializing by remember { unbluViewModel.initializing }
    val endpoint by remember { unbluViewModel.endpoint }
    val apiKey by remember { unbluViewModel.apiKey }
    Box(Modifier
        .background(Color.White)
        .fillMaxSize())
    {
        Column(Modifier
            .align(Alignment.Center)) {
            Spacer(modifier = Modifier.height(8.dp))
            InputField("Endpoint", endpoint){
                unbluViewModel.setEndpoint(it)
            }
            InputField("Password", apiKey){
                unbluViewModel.setApiKey(it)
            }
            Button(
                enabled = !initializing,
                onClick = {
                    onClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = Purple80),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if(UnbluSingleton.getUnbluUi()!= null) "Show" else "Init",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}


