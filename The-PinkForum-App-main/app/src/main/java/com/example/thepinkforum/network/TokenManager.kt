package com.example.thepinkforum.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import org.json.JSONObject


class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PinkForumPrefs", Context.MODE_PRIVATE)

    // Save the token when login is successful
    fun saveToken(token: String) {
        prefs.edit().putString("JWT_TOKEN", token).apply()
    }

    // Retrieve the token to attach to future API calls
    fun getToken(): String? {
        return prefs.getString("JWT_TOKEN", null)
    }

    fun saveUserId(userId: Int){
        prefs.edit().putString("USER_ID", userId.toString()).apply()
    }

    fun getUserId(): String?{
        return prefs.getString("USER_ID", null)
    }

    // Save the users name when login is successful
    fun saveUserName(name: String) {
        prefs.edit().putString("USER_NAME", name).apply()
    }

    fun getUserName(): String? {
        return prefs.getString("USER_NAME", "User") // "User" is the default fallback
    }

    fun saveUserEmail(email: String){
        prefs.edit().putString("USER_EMAIL", email).apply()
    }

    fun getUserEmail(): String?{
        return prefs.getString("USER_EMAIL", null)
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString("USER_ROLE", role).apply()
    }

    fun getUserRole(): String? {
        return prefs.getString("USER_ROLE", null)
    }

    fun isFirstTime(): Boolean {
        return prefs.getBoolean("IS_FIRST_TIME", true)
    }

    fun setFirstTime(isFirstTime: Boolean) {
        prefs.edit().putBoolean("IS_FIRST_TIME", isFirstTime).apply()
    }

    // Clear the token when the user logs out
    fun clearToken() {
        prefs.edit().remove("JWT_TOKEN").remove("USER_ROLE").remove("USER_ID").remove("USER_NAME").apply()
    }

    fun isTokenValid(token: String?): Boolean {
        // 1. If there's no token, they definitely aren't logged in
        if (token.isNullOrEmpty()) return false

        return try {
            // 2. Split the token into Header, Payload, and Signature
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("PINK_FORUM_JWT", "Invalid JWT format")
                return false
            }

            // 3. Decode the Payload with CRITICAL FIX: Add NO_PADDING and NO_WRAP
            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            val payloadString = String(payloadBytes, Charsets.UTF_8)
            val jsonObject = JSONObject(payloadString)

            // 4. Extract the 'exp' claim (JWT expiration is in seconds)
            if (!jsonObject.has("exp")) {
                Log.w("PINK_FORUM_JWT", "No 'exp' claim found in token")
                return true // Assuming valid if the backend didn't give it an expiration
            }

            // Multiply by 1000L to match Android's System.currentTimeMillis()
            val expirationTime = jsonObject.getLong("exp") * 1000L

            // 5. Compare times
            val isValid = System.currentTimeMillis() < expirationTime

            Log.d("PINK_FORUM_JWT", "Is Token Valid? $isValid")
            isValid

        } catch (e: Exception) {
            // If literally anything goes wrong, silently catch it and force a re-login
            Log.e("PINK_FORUM_JWT", "Token parse failed: ${e.message}")
            false
        }
    }
}