package com.monkey.lucifer.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Text
import com.monkey.lucifer.presentation.screens.WebsiteCommandPreviewScreen
import com.monkey.lucifer.presentation.screens.SimpleWebsiteBuildingScreen
import com.monkey.lucifer.presentation.screens.WebsiteQRCodeScreen

@Composable
fun HomePage(viewModel: HomeViewModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val status by viewModel.status.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val aiText by viewModel.aiText.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val error by viewModel.error.collectAsState()

    // Website building states
    val showCommandPreview by viewModel.showCommandPreview.collectAsState()
    val lastCommand by viewModel.lastCommand.collectAsState()
    val parsedWebsiteName by viewModel.parsedWebsiteName.collectAsState()
    val isBuilding by viewModel.isBuilding.collectAsState()
    val completedSteps by viewModel.completedSteps.collectAsState()
    val buildError by viewModel.buildError.collectAsState()
    val showQRCode by viewModel.showQRCode.collectAsState()
    val qrCodeUrl by viewModel.qrCodeUrl.collectAsState()
    val shouldAutoStart by viewModel.shouldAutoStart.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startRecording(context)
        } else {
            // Permission denied
            viewModel.stopRecordingAndProcess()
        }
    }

    val autoStartPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.autoStartRecording(context)
        } else {
            // Don't call clear(), just stop
            viewModel.stopRecordingAndProcess()
        }
    }

    // Auto-start listening when app opens or resumes
    LaunchedEffect(shouldAutoStart) {
        if (shouldAutoStart) {
            autoStartPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Show appropriate screen based on state
    when {
        showCommandPreview -> {
            WebsiteCommandPreviewScreen(
                command = lastCommand,
                parsedWebsiteName = parsedWebsiteName,
                onConfirm = { viewModel.buildWebsite() },
                onCancel = { viewModel.hideCommandPreview() }
            )
        }
        showQRCode -> {
            WebsiteQRCodeScreen(
                websiteName = parsedWebsiteName,
                qrCodeUrl = qrCodeUrl,
                onClose = { viewModel.closeQRCode() }
            )
        }
        isBuilding || buildError != null -> {
            // Show building screen while building OR if there's an error
            SimpleWebsiteBuildingScreen(
                completedSteps = completedSteps,
                buildError = buildError,
                onClose = if (buildError != null) {
                    { viewModel.clearBuildError() }
                } else null
            )
        }
        else -> {
            // Main home page UI
            HomePageUI(
                status = status,
                recognizedText = recognizedText,
                aiText = aiText,
                isRecording = isRecording,
                error = error,
                permissionLauncher = permissionLauncher,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun HomePageUI(
    status: String,
    recognizedText: String,
    aiText: String,
    isRecording: Boolean,
    error: String,
    permissionLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Boolean>,
    viewModel: HomeViewModel
) {

    val micInteraction = remember { MutableInteractionSource() }
    val micScale by animateFloatAsState(
        targetValue = if (isRecording) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 220),
        label = "micScale"
    )

    val statusText = when {
        isRecording -> "Lucifer is listening"
        status.contains("Thinking", ignoreCase = true) || status.contains("Transcribing", ignoreCase = true) -> "Lucifer is thinking"
        else -> "Lucifer is ready"
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 65%: scrollable text area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = statusText,
                        label = "status"
                    ) { target ->
                        Text(
                            text = target,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (error.isNotBlank()) {
                        Text(
                            text = error,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    if (recognizedText.isNotBlank() && !recognizedText.equals("You", ignoreCase = true)) {
                        Text(
                            text = "You said: $recognizedText",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    if (aiText.isNotBlank()) {
                        Text(
                            text = "AI: $aiText",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 35%: fixed mic area with responsive layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isRecording) Color(0xFFFF6B6B).copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.12f),
                                CircleShape
                            )
                            .clickable(
                                interactionSource = micInteraction,
                                indication = null
                            ) {
                                if (isRecording) {
                                    viewModel.stopRecordingAndProcess()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                            .scale(micScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap to talk",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}