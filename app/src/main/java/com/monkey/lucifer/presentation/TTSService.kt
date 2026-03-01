package com.monkey.lucifer.presentation

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * OpenAI Text-to-Speech Service
 *
 * Provides high-quality voice generation using OpenAI's TTS API.
 * Supports multiple languages with male/female voice options.
 */
class TTSService(
    private val apiKey: String,
    private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)  // Faster connection
        .readTimeout(20, TimeUnit.SECONDS)     // Reduced read timeout for faster streaming
        .writeTimeout(10, TimeUnit.SECONDS)    // Faster write timeout
        .retryOnConnectionFailure(true)
        .build()

    private var mediaPlayer: MediaPlayer? = null
    private var ttsAudioFile: File? = null

    companion object {
        private const val TAG = "TTSService"

        // OpenAI TTS Model - Using gpt-4o-mini-tts for faster generation
        private const val TTS_MODEL = "gpt-4o-mini-tts"  // Fast and high quality

        // Voice: alloy (calm, deep, intelligent male voice - perfect for Lucifer)
        private const val VOICE = "alloy"

        private const val API_ENDPOINT = "https://api.openai.com/v1/audio/speech"
    }

    /**
     * Speak text using OpenAI TTS
     *
     * @param text Text to speak
     * @param languageCode Language code (kept for compatibility but not used for voice selection)
     * @param isMaleVoice Kept for compatibility but always uses alloy voice
     */
    suspend fun speak(
        text: String,
        languageCode: String = "en",
        isMaleVoice: Boolean = true
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎤 OpenAI TTS: Speaking with Alloy voice")
            Log.d(TAG, "Text: $text")

            // Stop any existing playback
            stopSpeaking()

            // Get audio from OpenAI TTS
            val audioBytes = getAudioFromOpenAI(text)

            if (audioBytes == null) {
                Log.e(TAG, "Failed to get audio from OpenAI TTS")
                return@withContext
            }

            // Save audio to temporary file
            val audioFile = File(context.cacheDir, "tts_${System.currentTimeMillis()}.mp3")
            audioFile.writeBytes(audioBytes)
            ttsAudioFile = audioFile

            Log.d(TAG, "✅ Audio file saved: ${audioFile.absolutePath} (${audioFile.length()} bytes)")

            // Play audio
            playAudio(audioFile)

        } catch (e: Exception) {
            Log.e(TAG, "TTS Error: ${e.message}", e)
        }
    }

    /**
     * Get audio from OpenAI TTS API with optimized settings for speed
     */
    private suspend fun getAudioFromOpenAI(text: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject()
            jsonBody.put("model", TTS_MODEL)
            jsonBody.put("input", text)
            jsonBody.put("voice", VOICE)
            jsonBody.put("response_format", "mp3")  // MP3 format for smaller size and faster streaming
            jsonBody.put("speed", 1.15)  // Faster speaking for quicker delivery

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(API_ENDPOINT)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            Log.d(TAG, "🔊 Calling OpenAI TTS API with $VOICE voice (speed: 1.15x)")

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Log.e(TAG, "TTS API Error: ${response.code} - $errorBody")
                    return@withContext null
                }

                val audioData = response.body?.bytes()
                Log.d(TAG, "✅ Received ${audioData?.size ?: 0} bytes from OpenAI TTS")
                audioData
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling OpenAI TTS API: ${e.message}", e)
            null
        }
    }


    /**
     * Play audio file
     */
    private fun playAudio(audioFile: File) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .build()
                )
                setDataSource(audioFile.absolutePath)
                prepare()
                start()

                setOnCompletionListener {
                    Log.d(TAG, "✅ TTS playback completed")
                    // Clean up temp file after playback
                    audioFile.delete()
                }

                Log.d(TAG, "▶️ Playing audio: ${audioFile.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio: ${e.message}", e)
        }
    }

    /**
     * Stop speaking
     */
    fun stopSpeaking() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                    Log.d(TAG, "⏹️ MediaPlayer stopped (was playing)")
                }
                player.reset()
                player.release()
                Log.d(TAG, "⏹️ MediaPlayer released")
            }
            mediaPlayer = null

            // Clean up temp file
            ttsAudioFile?.let { file ->
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "🗑️ Deleted temp TTS file: ${file.name}")
                }
            }
            ttsAudioFile = null

            Log.d(TAG, "⏹️ TTS stopped completely")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback: ${e.message}", e)
            // Force cleanup even on error
            try {
                mediaPlayer?.release()
                mediaPlayer = null
                ttsAudioFile?.delete()
                ttsAudioFile = null
            } catch (cleanupError: Exception) {
                Log.e(TAG, "Error in forced cleanup: ${cleanupError.message}")
            }
        }
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    /**
     * Release resources
     */
    fun release() {
        stopSpeaking()
        mediaPlayer = null
        ttsAudioFile?.delete()
        ttsAudioFile = null
    }
}




