package com.monkey.lucifer.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.monkey.lucifer.domain.BuildStep
import com.monkey.lucifer.domain.ProjectStatus
import com.monkey.lucifer.presentation.WebsiteBuilderViewModel

@Composable
fun WebsiteBuildingScreen(
    viewModel: WebsiteBuilderViewModel,
    onBuildComplete: () -> Unit
) {
    val isBuilding by viewModel.isBuilding.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val completedSteps by viewModel.completedSteps.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val showStopDialog by viewModel.showStopDialog.collectAsState()

    // Auto-navigate when complete
    LaunchedEffect(currentProject?.status) {
        if (currentProject?.status == ProjectStatus.COMPLETE) {
            kotlinx.coroutines.delay(1000)
            onBuildComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: Stop button
            if (isBuilding) {
                Chip(
                    onClick = { viewModel.showStopDialog() },
                    modifier = Modifier.padding(top = 8.dp),
                    label = { Text("Stop", fontSize = 10.sp, color = Color.White) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop building",
                            tint = Color.Red
                        )
                    },
                    colors = ChipDefaults.primaryChipColors(
                        backgroundColor = Color(0xFF1E1E1E)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Middle: Progress
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isBuilding) {
                    BuildingProgress(
                        currentStep = currentStep,
                        completedSteps = completedSteps
                    )
                }
            }
        }

        // Stop confirmation dialog
        if (showStopDialog) {
            StopConfirmationDialog(
                onConfirm = { viewModel.stopBuilding() },
                onDismiss = { viewModel.dismissStopDialog() }
            )
        }
    }
}

@Composable
fun BuildingProgress(
    currentStep: BuildStep,
    completedSteps: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular progress indicator
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            CircularProgressIndicator(
                progress = currentStep.progress,
                modifier = Modifier.fillMaxSize(),
                indicatorColor = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeWidth = 8.dp
            )

            Text(
                text = "${(currentStep.progress * 100).toInt()}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Current step message
        Text(
            text = currentStep.message,
            fontSize = 12.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun StopConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            onClick = {},
            backgroundPainter = CardDefaults.cardBackgroundPainter(Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Stop Building?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "All progress will be lost and project data will be deleted.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Text("Cancel", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF3A3A3A)
                        )
                    ) {
                        Text("Stop", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}



