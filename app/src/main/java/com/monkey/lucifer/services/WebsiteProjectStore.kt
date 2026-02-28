package com.monkey.lucifer.services

import android.util.Log
import com.monkey.lucifer.domain.WebsiteProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebsiteProjectStore {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val firebaseApiKey = "AIzaSyDER86nAMW9YjbJupojXcAzj5J0gLVij-o"
    private val firebaseProjectId = "lucifer-97501"
    private val firestoreBase = "https://firestore.googleapis.com/v1"

    suspend fun saveProject(project: WebsiteProject): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$firestoreBase/projects/$firebaseProjectId/databases/(default)/documents/WebsiteProjects/${project.id}?key=$firebaseApiKey"

            val document = JSONObject().apply {
                put("fields", JSONObject().apply {
                    put("id", JSONObject().put("stringValue", project.id))
                    put("name", JSONObject().put("stringValue", project.name))
                    put("description", JSONObject().put("stringValue", project.description))
                    put("created_at", JSONObject().put("integerValue", project.createdAt.toString()))
                    put("storage_path", JSONObject().put("stringValue", project.storagePath ?: ""))
                    put("firebase_url", JSONObject().put("stringValue", project.firebaseStorageUrl ?: ""))
                    put("github_url", JSONObject().put("stringValue", project.githubUrl ?: ""))
                    put("status", JSONObject().put("stringValue", project.status.name))
                })
            }

            val request = Request.Builder()
                .url(url)
                .patch(document.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val success = response.isSuccessful
                if (!success) {
                    Log.e("WebsiteProjectStore", "Save failed: ${response.code} ${response.body?.string()}")
                }
                success
            }
        } catch (e: Exception) {
            Log.e("WebsiteProjectStore", "Error saving project", e)
            false
        }
    }

    suspend fun deleteProject(projectId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$firestoreBase/projects/$firebaseProjectId/databases/(default)/documents/WebsiteProjects/$projectId?key=$firebaseApiKey"
            val request = Request.Builder()
                .url(url)
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("WebsiteProjectStore", "Error deleting project", e)
            false
        }
    }
}

