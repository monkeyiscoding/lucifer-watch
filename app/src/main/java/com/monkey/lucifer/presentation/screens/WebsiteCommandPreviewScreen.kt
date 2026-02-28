package com.monkey.lucifer.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.wear.compose.material.*

@Composable
fun WebsiteCommandPreviewScreen(
    command: String,
    parsedWebsiteName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = 24.dp,
                bottom = 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            item {
                Text(
                    text = "Website Preview",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Your command
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    onClick = {},
                    backgroundPainter = CardDefaults.cardBackgroundPainter(Color(0xFF1E1E1E))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Your Command:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = command,
                            fontSize = 11.sp,
                            color = Color.White,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            // Website name
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    onClick = {},
                    backgroundPainter = CardDefaults.cardBackgroundPainter(Color(0xFF2A4A2A))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Website Name:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Green.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = parsedWebsiteName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        )
                    }
                }
            }

            // Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel button
                    Chip(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        label = {
                            Text(
                                "Cancel",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        },
                        colors = ChipDefaults.primaryChipColors(
                            backgroundColor = Color(0xFF3A3A3A)
                        )
                    )

                    // Build button
                    Chip(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        label = {
                            Text(
                                "Build",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Build website",
                                tint = Color.Green
                            )
                        },
                        colors = ChipDefaults.primaryChipColors(
                            backgroundColor = Color(0xFF2A4A2A)
                        )
                    )
                }
            }
        }
    }
}





