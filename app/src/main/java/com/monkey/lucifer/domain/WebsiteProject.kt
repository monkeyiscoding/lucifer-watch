package com.monkey.lucifer.domain

import android.graphics.Bitmap

/**
 * Represents a website project created by the AI
 */
data class WebsiteProject(
    val id: String,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val htmlContent: String,
    val firebaseStorageUrl: String? = null,
    val githubUrl: String? = null,
    val storagePath: String? = null,
    val qrCodeBitmap: Bitmap? = null,
    val status: ProjectStatus = ProjectStatus.CREATING
)

enum class ProjectStatus {
    CREATING,
    UPLOADING,
    COMPLETE,
    FAILED,
    CANCELLED
}

/**
 * Build steps for progress tracking
 */
sealed class BuildStep(val message: String, val progress: Float) {
    object Analyzing : BuildStep("Analyzing your requirements...", 0.1f)
    object CreatingProject : BuildStep("Creating project structure...", 0.2f)
    object GeneratingHTML : BuildStep("Generating HTML structure...", 0.4f)
    object StylingWebsite : BuildStep("Adding responsive CSS styles...", 0.6f)
    object AddingInteractivity : BuildStep("Adding JavaScript features...", 0.75f)
    object UploadingToCloud : BuildStep("Uploading to Firebase...", 0.85f)
    object GeneratingQR : BuildStep("Generating QR code...", 0.95f)
    object Complete : BuildStep("Website ready, Sir!", 1.0f)
}

/**
 * Website details extracted from user command
 */
data class WebsiteDetails(
    val name: String,
    val description: String = "",
    val additionalFeatures: List<String> = emptyList()
)
