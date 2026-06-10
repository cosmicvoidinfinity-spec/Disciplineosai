package com.example.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getCoachResponse(
        userMessage: String,
        coachPersona: String,
        contextStr: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured of valid. Returning mock response.")
            return@withContext getFallbackCoachResponse(userMessage, coachPersona)
        }

        val systemPrompt = when (coachPersona) {
            "STRICT_SERGEANT" -> """
                You are Sergeant Goggins. You are a highly strict, blunt, and aggressive military drill instructor. 
                You tolerate ZERO excuses. If the user reports weakness, tell them to wake up, stay hard, and run through a brick wall. Keep it gritty, real, highly motivational, and under 100 words.
            """.trimIndent()
            "BUSINESS_MASTER" -> """
                You are a business mentor and strategic systems architect like Ray Dalio. 
                Focus purely on systems, leverage, execution velocity, and analytical tracking of habits. 
                Give the user practical actionable frameworks. Keep it under 100 words.
            """.trimIndent()
            "WISE_MONK" -> """
                You are a tranquil Stoic philosopher and Wise Monk. 
                Focus on mindfulness, breath, intentional pauses, and accepting obstacles as the path. 
                Keep it serene, peaceful, profound, and under 100 words.
            """.trimIndent()
            else -> """
                You are a highly supportive, compassionate, and warm life coach. 
                Celebrate the user's micro-progress, support them through struggles, and offer practical, friendly guidance. Keep it comforting, encouraging, and under 100 words.
            """.trimIndent()
        }

        val parsedContextPrompt = "Current User Context: $contextStr\n\nUser Message: $userMessage"

        try {
            // Build direct JSON payload
            val root = JSONObject()
            
            // System instruction
            val systemInstructionObj = JSONObject()
            val systemPartsArray = JSONArray()
            systemPartsArray.put(JSONObject().put("text", systemPrompt))
            systemInstructionObj.put("parts", systemPartsArray)
            root.put("systemInstruction", systemInstructionObj)

            // Contents array
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", parsedContextPrompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            // Generation config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            root.put("generationConfig", generationConfig)

            val requestBodyStr = root.toString()
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyStr.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API Request failed: ${response.code} -> $errorBody")
                    return@withContext getFallbackCoachResponse(userMessage, coachPersona)
                }

                val responseBodyStr = response.body?.string() ?: return@withContext "API connection lost."
                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.optJSONObject(0).optString("text", "Let's double check your discipline rules.")
                    }
                }
                return@withContext "No response. Ensure your focus holds."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception calling Gemini: ${e.localizedMessage}")
            return@withContext getFallbackCoachResponse(userMessage, coachPersona)
        }
    }

    suspend fun getNoteAISummary(title: String, content: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "AI Offline: Summarized internally as focus logging on $title."
        }

        val prompt = "Analyze the following personal note and summarize it in exactly two short sentences, extracting key action items or core insights:\n\nTitle: $title\nContent: $content"

        try {
            val root = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            val requestBodyStr = root.toString()
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyStr.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val candidates = JSONObject(bodyStr).optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.optJSONObject(0)
                        val parts = firstCandidate?.optJSONObject("content")?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.optJSONObject(0).optString("text", "Summarized successfully.")
                        }
                    }
                }
            }
            return@withContext "Note captured in workspace secure. Action items identified."
        } catch (e: Exception) {
            return@withContext "Saved Note Offline. Ready for tactical synchronization."
        }
    }

    private fun getFallbackCoachResponse(userMessage: String, coachPersona: String): String {
        return when (coachPersona) {
            "STRICT_SERGEANT" -> {
                "YOU ARE TELLING ME YOU CAN'T? NO EXCUSES! Cold showers, heavy sets, and early alarms build champions. WAKE UP AND GET TO WORK! STAY HARD!"
            }
            "BUSINESS_MASTER" -> {
                "Analysis of your statement reveals leverage optimization potential. Structure your calendar with hard time blocks, cut out non-essential interactions, and trace your KPIs down to the minute. Build a machine, don't just work in it."
            }
            "WISE_MONK" -> {
                "The obstacle is the way. Feel the resistance in your mind right now, take a conscious breath, and let it pass. Action should flow like rainwater—natural, unbothered, persistent."
            }
            else -> {
                "Hey there, I hear you. It's totally okay to feel tired or faced by barriers. Why don't we focus on just one tiny task right now, complete it, and get some water? You're doing better than you of yesterday."
            }
        }
    }
}
