package com.monkey.lucifer.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults

@Composable
fun SimpleWebsiteBuildingScreen(
    completedSteps: List<String>,
    buildError: String?,
    onClose: (() -> Unit)? = null
) {
    val listState = rememberScalingLazyListState()
    val progress = if (completedSteps.isEmpty()) 0f else (completedSteps.size * 20f).coerceAtMost(100f) / 100f
    val progressValue by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Text(
                    text = "Building Website",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                // Progress bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressValue)
                                .background(Color(0xFF4CAF50))
                        )
                    }
                    Text(
                        text = "${(progressValue * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(completedSteps.size) { index ->
                AnimatedContent(
                    targetState = completedSteps[index],
                    label = "step_$index"
                ) { stepText ->
                    Text(
                        text = stepText,
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            if (buildError != null) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    Text(
                        text = "❌ Error: $buildError",
                        color = Color(0xFFFF6B6B),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (onClose != null) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    item {
                        Button(
                            onClick = onClose,
                            modifier = Modifier.size(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFFF6B6B)
                            )
                        ) {
                            Text("Close", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            } else if (progressValue >= 0.95f) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Complete",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}


