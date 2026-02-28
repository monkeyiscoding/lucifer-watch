package com.monkey.lucifer.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.monkey.lucifer.domain.WebsiteProject

@Composable
fun WebsitePreviewScreen(
    project: WebsiteProject,
    onClose: () -> Unit
) {
    // Simplified QR Code Display - Only QR and Close Button
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Success message at top
            Text(
                text = "Website is ready, sir!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Green,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // QR Code - Center of screen
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                project.qrCodeBitmap?.let { qrBitmap ->
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(140.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Close button at bottom
            Chip(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(40.dp),
                label = {
                    Text(
                        "Close",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = ChipDefaults.primaryChipColors(
                    backgroundColor = Color(0xFF3A3A3A)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}





