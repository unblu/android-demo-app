package com.unblu.navigation.demoapp.ui

import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex

import com.unblu.sdk.core.Unblu
import kotlinx.coroutines.launch
import kotlin.math.ceil

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UnbluSheet(mainView: View?, onDismissRequest: () -> Unit) {
    var targetOffsetY by remember { mutableStateOf(0f) }
    val offsetY by animateFloatAsState(targetOffsetY, label = "offsetY anim")
    val coroutineScope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Expanded,
        skipHalfExpanded = true,
        confirmValueChange = { state ->
            when (state) {
                ModalBottomSheetValue.Hidden -> {
                    onDismissRequest()
                    true
                }

                ModalBottomSheetValue.Expanded -> {
                    true
                }

                ModalBottomSheetValue.HalfExpanded -> {
                    false
                }
            }
        })

    DisposableEffect(key1 = Unit) {
        Log.d("UnbluModalSheet", "Launching effect")
        val disposable = Unblu.onUiHideRequest()
            .subscribe {
                Log.d("UnbluModalSheet", "onUiHideRequest")
                coroutineScope.launch {
                    onDismissRequest()
                    modalBottomSheetState.hide()
                }
            }

        onDispose {
            Log.d("UnbluModalSheet", "disposing effect")
            disposable.dispose()
        }
    }

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        modifier = Modifier.offset(y = offsetY.toDp(LocalContext.current).dp),
        sheetShape = RoundedCornerShape(4.dp),
        sheetGesturesEnabled = false,
        sheetBackgroundColor = Color.Transparent,
        sheetContentColor = Color.Transparent,
        scrimColor = if (offsetY != 0f) Color.Transparent else Color.Black.copy(alpha = 0.3f),
        sheetContent = {
            val maxHeight =
                LocalConfiguration.current.screenHeightDp.dp - (LocalConfiguration.current.screenHeightDp * 0.05f).dp
            val context = LocalContext.current
            Box {
                DragHandle(
                    Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(1f), onDrag = { dragAmount ->
                        targetOffsetY = maxOf(0f, targetOffsetY + dragAmount)
                    }) { shouldDismiss ->
                    if (shouldDismiss) {
                        targetOffsetY = maxHeight.value.toPx(context).toFloat()
                        coroutineScope.launch {
                            modalBottomSheetState.hide()
                        }
                    } else {
                        targetOffsetY = 0f
                    }
                }
                var selectedTabIndex by remember { mutableStateOf(0) }
                val tabs = listOf("Unblu", "More")

                Column(
                    modifier = Modifier
                        .zIndex(0f)
                        .imePadding()
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .heightIn(max = maxHeight)
                        .focusable(true)
                ) {
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    when (selectedTabIndex) {
                        0 -> {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { context ->
                                    mainView?.let { it } ?: View(context)
                                }
                            )
                        }
                        1 -> {
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("More...")
                            }
                        }
                    }
                }
            }
        },
        content = {
        }
    )
}

@Composable
fun DragHandle(
    modifier: Modifier = Modifier,
    onDrag: (Float) -> Unit,
    onDragEnded: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var totalDragDistance by remember { mutableStateOf(0f) }
    val dragThreshold = 400f
    val pointerInputModifier = modifier
        .pointerInput(Unit) {
            detectVerticalDragGestures(onVerticalDrag = { _, dragAmount ->
                coroutineScope.launch {
                    totalDragDistance += dragAmount
                    onDrag(dragAmount)
                }
            }, onDragEnd = {
                val dismiss = totalDragDistance > dragThreshold
                onDragEnded(dismiss)
                totalDragDistance = 0f
            }, onDragCancel = {
                onDragEnded(false)
            })
        }

    Box(
        modifier = pointerInputModifier
            .height(25.dp)
            .background(
                Color.Transparent,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
            .fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(4.dp)
                .height(4.dp)
                .width(40.dp)
                .background(
                    Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
                .focusable(true)
        )
    }
}

fun Number.toPx(context: Context): Int {
    val logicalDensity: Float = context.resources.displayMetrics.density
    return ceil(this.toInt() * logicalDensity).toInt()
}

fun Number.toDp(context: Context): Int {
    return this.toInt() / context.resources.displayMetrics.density.toInt()
}