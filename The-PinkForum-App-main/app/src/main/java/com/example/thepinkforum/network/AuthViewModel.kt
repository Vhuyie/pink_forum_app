package com.example.thepinkforum.network

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // UI State variables that Compose will observe
    val isLoading = mutableStateOf(false)

    val errorMessage = mutableStateOf<String?>(null)

    val loginSuccess = mutableStateOf(false)

    val registerSuccess = mutableStateOf(false)

    val deleteSuccess = mutableStateOf(false)

    val pendingUserId = mutableStateOf<Int?>(null)

    val verificationRequired = mutableStateOf(false)

    val verifySuccess = mutableStateOf(false)

    // Add this function below your login function
    fun register(context: Context, name: String, email: String, password: String, role: String) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection. Please check your settings."
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                // Prepare the JSON body
                val request = RegisterRequest(name, email, password)

                // Call the correct register endpoint based on role
                val apiService = ApiClient.getService(context)
                val response = when (role.lowercase()) {
                    "educator" -> apiService.educatorRegister(role, request)
                    else -> apiService.studentRegister(role, request)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()
                    if (body?.verificationRequired == true && body.userId != null) {
                        pendingUserId.value = body.userId
                        verificationRequired.value = true
                    }
                    registerSuccess.value = true
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val parsedError = Gson().fromJson(errorBody, AuthResponse::class.java)
                        parsedError?.message ?: response.body()?.message ?: "Registration failed."
                    } catch (_: Exception) {
                        response.body()?.message ?: "Registration failed."
                    }
                    errorMessage.value = errorMsg
                }
            } catch (e: com.google.gson.JsonSyntaxException) {
                errorMessage.value = "Server Error: The server returned an invalid response. Check your PHP logs."
                e.printStackTrace()
            } catch (e: Exception) {
                // Catches no-internet errors or server timeout issues
                errorMessage.value = "Network error: Make sure your WAMP server is running."
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun login(context: Context, email: String, password: String, role: String) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection. Please check your settings."
            return
        }

        // Launch a coroutine so the network call doesn't freeze the app UI
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                // 1. Prepare the JSON request body
                val request = LoginRequest(email, password)

                // 2. Make the HTTP POST call based on role
                val apiService = ApiClient.getService(context)
                val response = when (role.lowercase()) {
                    "admin" -> apiService.adminLogin(role, request)
                    "educator" -> apiService.educatorLogin(role, request)
                    else -> apiService.studentLogin(role, request)
                }

                android.util.Log.d("AUTH_DEBUG", "Server Response: ${response.body()}")

                // 3. Handle the response from PHP
                if (response.isSuccessful && response.body()?.success == true) {
                    val token = response.body()?.token
                    val userId = response.body()?.user?.id
                    val name = response.body()?.user?.name
                    val email = response.body()?.user?.email

                    if (token != null) {
                        // Save the JWT locally
                        val tokenManager = TokenManager(context)
                        tokenManager.saveToken(token)

                        userId?.let{ tokenManager.saveUserId(it) }

                        name?.let { tokenManager.saveUserName(it) }

                        email?.let { tokenManager.saveUserEmail(it) }

                        tokenManager.saveUserRole(role.lowercase())

                        // Trigger navigation in the UI
                        loginSuccess.value = true
                    } else {
                        errorMessage.value = "Login succeeded, but no token was received."
                    }
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val parsedError = Gson().fromJson(errorBody, AuthResponse::class.java)
                        parsedError?.message ?: response.body()?.message ?: "Invalid credentials or role mismatch."
                    } catch (_: Exception) {
                        response.body()?.message ?: "Invalid credentials or role mismatch."
                    }
                    errorMessage.value = errorMsg
                }
            } catch (e: com.google.gson.JsonSyntaxException) {
                errorMessage.value = "Server Error: The server returned an invalid response. Check your PHP logs."
                e.printStackTrace()
            } catch (e: Exception) {
                // Catches no-internet errors or server timeout issues
                errorMessage.value = "Network error: Make sure your WAMP server is running."
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun logout(context: Context){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection. Unable to logout"
            return
        }

        val tokenManager = TokenManager(context)
        tokenManager.clearToken()
    }

    fun deleteAccount(context: Context){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection. Unable to delete account"
            return
        }

        val tokenManager = TokenManager(context)
        val userId = tokenManager.getUserId()
        val role = tokenManager.getUserRole()

        if (userId == null) {
            errorMessage.value = "User ID not found. Please log in again."
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            deleteSuccess.value = false

            try{
                val apiService = ApiClient.getService(context)
                val response = when(role?.lowercase()){
                    "educator" -> apiService.deleteEducatorAccount(userId)
                    "admin" -> apiService.deleteAdminAccount(userId)
                    else -> apiService.deleteStudentAccount(userId)
                }

                if(response.isSuccessful){
                    logout(context)
                    deleteSuccess.value = true
                }else{
                    errorMessage.value = "Failed to delete account."
                }
            }catch(e: Exception){
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun verifyEmail(context: Context, userId: Int, code: String) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection. Please check your settings."
            return
        }

        if (code.length != 4 || !code.all { it.isDigit() }) {
            errorMessage.value = "A valid 4-digit code is required."
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val request = VerifyEmailRequest(userId, code)
                val apiService = ApiClient.getService(context)
                val response = apiService.verifyEmail(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    verifySuccess.value = true
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val parsedError = Gson().fromJson(errorBody, VerifyEmailResponse::class.java)
                        parsedError?.message ?: response.body()?.message ?: "Verification failed."
                    } catch (_: Exception) {
                        response.body()?.message ?: "Verification failed."
                    }
                    errorMessage.value = errorMsg
                }
            } catch (e: com.google.gson.JsonSyntaxException) {
                errorMessage.value = "Server Error: The server returned an invalid response."
                e.printStackTrace()
            } catch (e: Exception) {
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    // Reset states if needed (e.g., when leaving the screen)
    fun resetState() {
        errorMessage.value = null
        loginSuccess.value = false
        registerSuccess.value = false
        deleteSuccess.value = false
        pendingUserId.value = null
        verificationRequired.value = false
        verifySuccess.value = false
    }
}