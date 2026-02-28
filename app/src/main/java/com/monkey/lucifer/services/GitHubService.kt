package com.monkey.lucifer.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Base64
import java.util.concurrent.TimeUnit

class GitHubService(
    private val token: String = "", // TODO: Pass token as parameter or load from secure config
    private val username: String = "monkeyiscoding",
    private val repoName: String = "lucifer-websites"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.github.com"
    private val authHeader = "token $token"

    /**
     * Check if repository exists, create if it doesn't
     */
    suspend fun ensureRepositoryExists(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // First, check if repo exists
            val checkUrl = "$baseUrl/repos/$username/$repoName"
            val checkRequest = Request.Builder()
                .url(checkUrl)
                .header("Authorization", authHeader)
                .header("Accept", "application/vnd.github.v3+json")
                .get()
                .build()

            val checkResponse = client.newCall(checkRequest).execute()

            if (checkResponse.isSuccessful) {
                Log.d("GitHubService", "Repository already exists")
                val responseBody = checkResponse.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val repoUrl = jsonResponse.getString("html_url")
                return@withContext Result.success(repoUrl)
            }

            // Repository doesn't exist, create it
            Log.d("GitHubService", "Creating repository: $repoName")
            val createUrl = "$baseUrl/user/repos"

            val requestBody = JSONObject().apply {
                put("name", repoName)
                put("description", "AI-Generated Websites using Lucifer")
                put("private", false)
                put("auto_init", true)
            }.toString()

            val createRequest = Request.Builder()
                .url(createUrl)
                .header("Authorization", authHeader)
                .header("Accept", "application/vnd.github.v3+json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val createResponse = client.newCall(createRequest).execute()

            if (createResponse.isSuccessful) {
                val responseBody = createResponse.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val repoUrl = jsonResponse.getString("html_url")
                Log.d("GitHubService", "Repository created successfully: $repoUrl")
                Result.success(repoUrl)
            } else {
                val error = "Failed to create repository: ${createResponse.code} - ${createResponse.body?.string()}"
                Log.e("GitHubService", error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("GitHubService", "Error ensuring repository exists", e)
            Result.failure(e)
        }
    }

    /**
     * Upload website files to GitHub repository
     * Creates folder structure: websites/<projectId>/
     */
    suspend fun uploadWebsite(
        projectId: String,
        websiteName: String,
        files: Map<String, String>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // First ensure repository exists
            val repoCheckResult = ensureRepositoryExists()
            if (repoCheckResult.isFailure) {
                return@withContext Result.failure(repoCheckResult.exceptionOrNull() ?: Exception("Failed to ensure repository"))
            }

            val repoUrl = repoCheckResult.getOrNull()!!
            Log.d("GitHubService", "Uploading ${files.size} files for project: $projectId")

            // Upload each file
            for ((fileName, content) in files) {
                val filePath = "websites/$projectId/$fileName"
                val uploadResult = uploadFile(filePath, content)

                if (uploadResult.isFailure) {
                    Log.e("GitHubService", "Failed to upload file: $fileName")
                    return@withContext Result.failure(uploadResult.exceptionOrNull() ?: Exception("Upload failed"))
                }

                Log.d("GitHubService", "Uploaded: $filePath")
            }

            // Ensure GitHub Pages is enabled (first time can take a few minutes to propagate)
            val pagesResult = enableGitHubPages()
            if (pagesResult.isFailure) {
                Log.w("GitHubService", "GitHub Pages enable failed: ${pagesResult.exceptionOrNull()?.message}")
            }

            // Return the GitHub Pages URL for the index.html
            val gitHubPagesUrl = getGitHubPagesUrl(projectId)
            Log.d("GitHubService", "Website available at: $gitHubPagesUrl")
            Result.success(gitHubPagesUrl)
        } catch (e: Exception) {
            Log.e("GitHubService", "Error uploading website", e)
            Result.failure(e)
        }
    }

    /**
     * Upload a single file to GitHub
     */
    private suspend fun uploadFile(filePath: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/repos/$username/$repoName/contents/$filePath"

            // Encode content in base64
            val encodedContent = Base64.getEncoder().encodeToString(content.toByteArray())

            val requestBody = JSONObject().apply {
                put("message", "Add $filePath via Lucifer AI")
                put("content", encodedContent)
                put("branch", "main")
            }.toString()

            val request = Request.Builder()
                .url(url)
                .header("Authorization", authHeader)
                .header("Accept", "application/vnd.github.v3+json")
                .put(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d("GitHubService", "File uploaded successfully: $filePath")
                Result.success(Unit)
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e("GitHubService", "Upload failed for $filePath: ${response.code} - $errorBody")

                // Check if it's a conflict (file already exists) - that's okay
                if (response.code == 409) {
                    Log.d("GitHubService", "File already exists, updating...")
                    updateFile(filePath, content)
                } else {
                    Result.failure(Exception("Upload failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e("GitHubService", "Error uploading file: $filePath", e)
            Result.failure(e)
        }
    }

    /**
     * Update an existing file in GitHub
     */
    private suspend fun updateFile(filePath: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/repos/$username/$repoName/contents/$filePath"

            // First, get the current file to get its SHA
            val getRequest = Request.Builder()
                .url(url)
                .header("Authorization", authHeader)
                .header("Accept", "application/vnd.github.v3+json")
                .get()
                .build()

            val getResponse = client.newCall(getRequest).execute()
            val currentFile = JSONObject(getResponse.body?.string() ?: "{}")
            val sha = currentFile.optString("sha", "")

            if (sha.isEmpty()) {
                return@withContext Result.failure(Exception("Could not get file SHA"))
            }

            // Update the file
            val encodedContent = Base64.getEncoder().encodeToString(content.toByteArray())
            val requestBody = JSONObject().apply {
                put("message", "Update $filePath via Lucifer AI")
                put("content", encodedContent)
                put("sha", sha)
                put("branch", "main")
            }.toString()

            val updateRequest = Request.Builder()
                .url(url)
                .header("Authorization", authHeader)
                .header("Accept", "application/vnd.github.v3+json")
                .put(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val updateResponse = client.newCall(updateRequest).execute()

            if (updateResponse.isSuccessful) {
                Log.d("GitHubService", "File updated successfully: $filePath")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Update failed: ${updateResponse.code}"))
            }
        } catch (e: Exception) {
            Log.e("GitHubService", "Error updating file: $filePath", e)
            Result.failure(e)
        }
    }

    /**
     * Enable GitHub Pages for the repository
     */
    suspend fun enableGitHubPages(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/repos/$username/$repoName/pages"

            val requestBody = JSONObject().apply {
                put("source", JSONObject().apply {
                    put("branch", "main")
                    put("path", "/")
                })
                put("public", true)
            }.toString()

            val request = Request.Builder()
                .url(url)
                .header("Authorization", authHeader)
                .header("Accept", "application/vnd.github.v3+json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful || response.code == 409) {  // 409 = already enabled
                val gitHubPagesUrl = "https://$username.github.io/$repoName"
                Log.d("GitHubService", "GitHub Pages enabled: $gitHubPagesUrl")
                Result.success(gitHubPagesUrl)
            } else {
                Result.failure(Exception("Failed to enable GitHub Pages: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("GitHubService", "Error enabling GitHub Pages", e)
            Result.failure(e)
        }
    }

    /**
     * Get the GitHub Pages URL for a website
     */
    fun getGitHubPagesUrl(projectId: String): String {
        return "https://$username.github.io/$repoName/websites/$projectId/index.html"
    }

    /**
     * Get the GitHub repository URL
     */
    fun getRepositoryUrl(): String {
        return "https://github.com/$username/$repoName"
    }
}
