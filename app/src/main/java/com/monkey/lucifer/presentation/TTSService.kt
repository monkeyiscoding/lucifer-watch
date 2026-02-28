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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var mediaPlayer: MediaPlayer? = null
    private var ttsAudioFile: File? = null

    companion object {
        private const val TAG = "TTSService"

        // OpenAI TTS Models and Voices
        // Models: tts-1 (faster), tts-1-hd (better quality)
        // Voices for different languages:
        // - alloy: Neutral, versatile (English focused)
        // - echo: Deep male voice (English focused)
        // - fable: Expressive, good for non-English languages
        // - onyx: Male voice (English focused)
        // - nova: Warm, female voice (better for Indian languages)
        // - shimmer: Clear female voice (English focused)

        private const val TTS_MODEL = "tts-1-hd"  // High quality

        // Language-specific voice selection
        // Hindi needs: nova (warm, natural prosody) or fable (expressive)
        private const val HINDI_VOICE = "nova"        // Best for Hindi - warm, natural
        private const val HINDI_VOICE_ALTERNATE = "fable"  // Fallback - expressive
        private const val DEFAULT_MALE_VOICE = "alloy"     // For other languages
        private const val DEFAULT_FEMALE_VOICE = "nova"    // For other languages

        private const val API_ENDPOINT = "https://api.openai.com/v1/audio/speech"
    }

    /**
     * Speak text using OpenAI TTS
     *
     * @param text Text to speak
     * @param languageCode Language code (e.g., "hi" for Hindi, "en" for English)
     * @param isMaleVoice Use male voice (true) or female voice (false) - NOTE: OpenAI voices are gender-neutral
     */
    suspend fun speak(
        text: String,
        languageCode: String = "en",
        isMaleVoice: Boolean = true
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎤 OpenAI TTS: Speaking in $languageCode (${if (isMaleVoice) "Male" else "Female"})")
            Log.d(TAG, "Text: $text")

            // Stop any existing playback
            stopSpeaking()

            // Get audio from OpenAI TTS
            val audioBytes = getAudioFromOpenAI(text, languageCode, isMaleVoice)

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
     * Get audio from OpenAI TTS API
     * Selects appropriate voice based on language
     */
    private suspend fun getAudioFromOpenAI(
        text: String,
        languageCode: String = "en",
        isMaleVoice: Boolean = true
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            // Select voice based on language
            val voice = selectVoiceForLanguage(languageCode, isMaleVoice)

            val jsonBody = JSONObject()
            jsonBody.put("model", TTS_MODEL)
            jsonBody.put("input", text)
            jsonBody.put("voice", voice)
            jsonBody.put("response_format", "mp3")  // MP3 format for smaller size
            jsonBody.put("speed", 1.0)  // Normal speed

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(API_ENDPOINT)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            Log.d(TAG, "🔊 Calling OpenAI TTS API with voice: $voice for language: $languageCode")

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
     * Select the best voice for a given language
     *
     * Voice Selection Strategy:
     * - Hindi: nova (warm, natural prosody for Indian languages)
     * - Other languages: alloy (male) or nova (female)
     */
    private fun selectVoiceForLanguage(languageCode: String, isMaleVoice: Boolean): String {
        return when (languageCode.lowercase()) {
            "hi", "hindi" -> {
                // Hindi: Use nova for warm, natural accent
                Log.d(TAG, "🇮🇳 Hindi detected: Using 'nova' voice (warm, natural prosody)")
                HINDI_VOICE  // nova - best for Hindi
            }
            "en", "english" -> {
                // English: Use alloy for male, nova for female
                if (isMaleVoice) DEFAULT_MALE_VOICE else DEFAULT_FEMALE_VOICE
            }
            "ta", "tamil" -> {
                // Tamil: Use nova (similar to Hindi)
                Log.d(TAG, "🇮🇳 Tamil detected: Using 'nova' voice")
                HINDI_VOICE
            }
            "te", "telugu" -> {
                // Telugu: Use nova (similar to Hindi)
                Log.d(TAG, "🇮🇳 Telugu detected: Using 'nova' voice")
                HINDI_VOICE
            }
            "gu", "gujarati" -> {
                // Gujarati: Use nova (similar to Hindi)
                Log.d(TAG, "🇮🇳 Gujarati detected: Using 'nova' voice")
                HINDI_VOICE
            }
            "mr", "marathi" -> {
                // Marathi: Use nova (similar to Hindi)
                Log.d(TAG, "🇮🇳 Marathi detected: Using 'nova' voice")
                HINDI_VOICE
            }
            "pa", "punjabi" -> {
                // Punjabi: Use nova (similar to Hindi)
                Log.d(TAG, "🇮🇳 Punjabi detected: Using 'nova' voice")
                HINDI_VOICE
            }
            "bn", "bengali" -> {
                // Bengali: Use nova (similar to Hindi)
                Log.d(TAG, "🇮🇳 Bengali detected: Using 'nova' voice")
                HINDI_VOICE
            }
            else -> {
                // Default: Use alloy for male, nova for female
                Log.d(TAG, "Default voice selection for: $languageCode")
                if (isMaleVoice) DEFAULT_MALE_VOICE else DEFAULT_FEMALE_VOICE
            }
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
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            Log.d(TAG, "⏹️ TTS stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback: ${e.message}")
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




