package com.monkey.lucifer.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class FirebaseStorageService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val firebaseApiKey = "AIzaSyDER86nAMW9YjbJupojXcAzj5J0gLVij-o"
    private val firebaseStorageBucket = "lucifer-97501.firebasestorage.app"

    // Use correct REST API endpoint with full bucket name for Firebase Storage
    private val storageBaseUrl = "https://firebasestorage.googleapis.com/v0/b/$firebaseStorageBucket/o"

    /**
     * Upload website files (HTML, CSS, JS, etc) to Firebase Storage
     * Returns the public URL to access the main index.html file
     *
     * @param projectId Unique project identifier
     * @param websiteName Name of the website (used for folder structure)
     * @param files Map of file paths to content (e.g., "index.html", "css/styles.css")
     */
    suspend fun uploadWebsiteFiles(
        projectId: String,
        websiteName: String,
        files: Map<String, String>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            var mainUrl = ""

            // Create a clean folder name from website name (remove special chars, spaces -> underscores)
            val folderName = websiteName
                .replace(Regex("[^A-Za-z0-9\\s-]"), "")
                .trim()
                .replace(Regex("\\s+"), " ")

            Log.d("FirebaseStorage", "Uploading website: $folderName (${files.size} files)")

            // Upload each file to: websites/<projectId>/<fileName>
            for ((fileName, content) in files) {
                // Keep all files in the same folder as index.html
                val filePath = "websites/$projectId/$fileName"

                val encodedPath = URLEncoder.encode(filePath, "UTF-8")
                val url = "$storageBaseUrl/$encodedPath?uploadType=media&key=$firebaseApiKey"

                Log.d("FirebaseStorage", "Uploading $fileName to: $filePath")

                // Determine content type based on file extension
                val contentType = when {
                    fileName.endsWith(".html") -> "text/html; charset=utf-8"
                    fileName.endsWith(".css") -> "text/css; charset=utf-8"
                    fileName.endsWith(".js") -> "application/javascript; charset=utf-8"
                    fileName.endsWith(".json") -> "application/json; charset=utf-8"
                    fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
                    fileName.endsWith(".png") -> "image/png"
                    fileName.endsWith(".gif") -> "image/gif"
                    fileName.endsWith(".svg") -> "image/svg+xml"
                    fileName.endsWith(".webp") -> "image/webp"
                    else -> "text/plain; charset=utf-8"
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", contentType)
                    .post(content.toRequestBody(contentType.toMediaType()))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    if (fileName == "index.html") {
                        mainUrl = "https://firebasestorage.googleapis.com/v0/b/$firebaseStorageBucket/o/$encodedPath?alt=media"
                        Log.d("FirebaseStorage", "✅ Upload successful for $fileName: $mainUrl")
                    } else {
                        Log.d("FirebaseStorage", "✅ Upload successful for $fileName -> $filePath")
                    }
                } else {
                    val errorBody = response.body?.string() ?: ""
                    val errorMsg = "Upload failed for $fileName: ${response.code} - ${response.message}\n$errorBody"
                    Log.e("FirebaseStorage", errorMsg)
                    return@withContext Result.failure(Exception(errorMsg))
                }
            }

            if (mainUrl.isEmpty()) {
                return@withContext Result.failure(Exception("No index.html found in files"))
            }

            Result.success(mainUrl)
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Upload error", e)
            Result.failure(e)
        }
    }

    /**
     * Upload HTML content to Firebase Storage using REST API
     * Returns the public URL to access the file
     */
    suspend fun uploadWebsite(
        projectId: String,
        htmlContent: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val filePath = "websites/$projectId/index.html"
            val encodedPath = URLEncoder.encode(filePath, "UTF-8")

            // Upload with authorization
            val url = "$storageBaseUrl/$encodedPath?uploadType=media&key=$firebaseApiKey"

            Log.d("FirebaseStorage", "Uploading website to: $url")

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "text/html; charset=utf-8")
                .post(htmlContent.toRequestBody("text/html; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                // Parse response to get the download URL
                try {
                    val responseBody = response.body?.string()
                    Log.d("FirebaseStorage", "Response body: $responseBody")
                    val jsonResponse = JSONObject(responseBody ?: "{}")

                    // Extract the name field which contains the file path
                    val fileName = jsonResponse.optString("name", "")

                    if (fileName.isNotEmpty()) {
                        // Construct the public URL using correct bucket name
                        val publicUrl = "https://firebasestorage.googleapis.com/v0/b/$firebaseStorageBucket/o/${URLEncoder.encode(fileName, "UTF-8")}?alt=media"
                        Log.d("FirebaseStorage", "Upload successful: $publicUrl")
                        Result.success(publicUrl)
                    } else {
                        // Fallback URL construction from the path we uploaded
                        val publicUrl = "https://firebasestorage.googleapis.com/v0/b/$firebaseStorageBucket/o/$encodedPath?alt=media"
                        Log.d("FirebaseStorage", "Upload successful (fallback): $publicUrl")
                        Result.success(publicUrl)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseStorage", "Error parsing response", e)
                    // Still return success with constructed URL since the upload succeeded
                    val publicUrl = "https://firebasestorage.googleapis.com/v0/b/$firebaseStorageBucket/o/$encodedPath?alt=media"
                    Result.success(publicUrl)
                }
            } else {
                val errorBody = response.body?.string() ?: ""
                val errorMsg = "Upload failed: ${response.code} - ${response.message}\n$errorBody"
                Log.e("FirebaseStorage", errorMsg)
                Log.e("FirebaseStorage", "URL attempted: $url")
                Log.e("FirebaseStorage", "File path: $filePath")
                Log.e("FirebaseStorage", "Content length: ${htmlContent.length}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Upload error", e)
            Result.failure(e)
        }
    }

    /**
     * Delete website files from Firebase Storage
     */
    suspend fun deleteWebsite(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val filePath = "websites/$projectId/index.html"
            val encodedPath = URLEncoder.encode(filePath, "UTF-8")
            val url = "$storageBaseUrl/$encodedPath?key=$firebaseApiKey"

            Log.d("FirebaseStorage", "Deleting website from: $url")

            val request = Request.Builder()
                .url(url)
                .delete()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d("FirebaseStorage", "Delete successful")
                Result.success(Unit)
            } else {
                val error = "Delete failed: ${response.code}"
                Log.e("FirebaseStorage", error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Delete error", e)
            Result.failure(e)
        }
    }
}
