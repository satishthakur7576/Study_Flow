package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Data models for parsing with Moshi
    data class Part(val text: String)
    data class Content(val parts: List<Part>)
    data class RequestBody(
        val contents: List<Content>,
        val systemInstruction: Content? = null
    )

    data class Candidate(val content: Content)
    data class ResponseBody(val candidates: List<Candidate>?)

    /**
     * Checks if the API key is a valid-looking key and not the default placeholder.
     */
    fun isApiKeyConfigured(): Boolean {
        val key = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.contains("PLACEHOLDER")
    }

    /**
     * Sends a chat prompt to the Gemini API with detailed context.
     */
    suspend fun generateAcademicAdvice(
        prompt: String,
        studentName: String,
        todayFocusMinutes: Int,
        currentStreak: Int,
        tasksCompleted: Int,
        tasksTotal: Int,
        totalHabits: Int,
        totalClasses: Int,
        chatHistory: List<Pair<String, String>> // List of (Sender, Message) to provide conversation history
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API_KEY_MISSING"
        }

        // Build the conversation history contents list
        val contents = mutableListOf<Content>()
        
        // Add previous history turns
        chatHistory.takeLast(10).forEach { (sender, text) ->
            contents.add(
                Content(
                    parts = listOf(Part(text = "[$sender]: $text"))
                )
            )
        }

        // Add the current user prompt
        contents.add(
            Content(
                parts = listOf(Part(text = "[User]: $prompt"))
            )
        )

        // System instructions passing live student metrics
        val systemPrompt = """
            You are FocusBot AI, a warm, professional, highly encouraging academic study companion and productivity coach for the student named $studentName.
            Current student metrics to help you frame your suggestions:
            - Today's focus minutes: $todayFocusMinutes minutes
            - Current overall focus/habit streak: $currentStreak days
            - Today's task checklist completion: $tasksCompleted completed out of $tasksTotal total tasks
            - Total habits tracked: $totalHabits
            - Active classes in timetable: $totalClasses

            Guidelines:
            1. Speak in a friendly, supportive, yet professional and structured tone. Keep formatting elegant (use markdown lists and bold text where appropriate).
            2. Help the student design focus sessions, manage time, recover from procrastination, build habits, and organize assignments.
            3. Reference their live stats occasionally (e.g. "I see you've already completed $tasksCompleted tasks today!") to make the response highly relevant.
            4. Keep responses concise, practical, and highly actionable (within 2-3 short paragraphs).
        """.trimIndent()

        val requestPayload = RequestBody(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val jsonAdapter = moshi.adapter(RequestBody::class.java)
            val jsonString = jsonAdapter.toJson(requestPayload)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini API error: Status code ${response.code}, Body: $errBody")
                    return@withContext "Error: Failed to fetch insights from FocusBot (HTTP ${response.code}). Please verify your network and Gemini API Key configuration."
                }

                val responseString = response.body?.string()
                if (responseString.isNullOrBlank()) {
                    return@withContext "FocusBot encountered an empty response. Please try re-framing your query!"
                }

                val responseAdapter = moshi.adapter(ResponseBody::class.java)
                val responseObj = responseAdapter.fromJson(responseString)
                val generatedText = responseObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!generatedText.isNullOrBlank()) {
                    generatedText
                } else {
                    "FocusBot is framing insights, but the response was unreadable. Let's try again!"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception calling Gemini API", e)
            "Error: ${e.localizedMessage ?: "Unable to contact FocusBot's AI brain"}. Check your internet connection."
        }
    }
}
