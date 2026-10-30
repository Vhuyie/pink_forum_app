package com.example.thepinkforum.network

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel(){
    val errorMessage = mutableStateOf<String?>(null)

    val dashboardUserName = mutableStateOf("")
    val dashboardUserEmail = mutableStateOf("")
    val dashboardUserRole = mutableStateOf("")
    val dashboardUserCount = mutableStateOf<Int?>(0)
    val dashboardStudentCount = mutableStateOf<Int?>(0)

    val dashboardFileCount = mutableStateOf<Int?>(0)

    // Educator States
    val educatorStats = mutableStateOf<EducatorStats?>(null)
    val educatorModules = mutableStateOf<List<EducatorModule>>(emptyList())

    // Admin States
    val adminStats = mutableStateOf<AdminStats?>(null)

    val profileUpdateSuccess = mutableStateOf(false)

    fun getUserDetails(context: Context){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.getService(context).getUsers()

                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()
                    dashboardUserName.value = body?.name ?: ""
                    dashboardUserEmail.value = body?.email ?: ""
                    dashboardUserRole.value = body?.role ?: ""
                } else {
                    errorMessage.value = response.body()?.message ?: "Failed to load user details."
                }
            }catch(e: com.google.gson.JsonSyntaxException){
                errorMessage.value = "Server error: Invalid data format."
                e.printStackTrace()
            }catch(e: Exception){
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            }
        }
    }

    fun updateProfile(context: Context, name: String, email: String) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            try {
                val request = UpdateProfileRequest(fullName = name, email = email)
                val response = ApiClient.getService(context).updateProfile(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    dashboardUserName.value = name
                    dashboardUserEmail.value = email
                    TokenManager(context).saveUserName(name)
                    profileUpdateSuccess.value = true
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val parsed = com.google.gson.Gson().fromJson(errorBody, UpdateProfileResponse::class.java)
                        parsed?.message ?: response.body()?.message ?: "Failed to update profile."
                    } catch (_: Exception) {
                        response.body()?.message ?: "Failed to update profile."
                    }
                    errorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            }
        }
    }

    fun getUserCount(context: Context){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.getService(context).getUserCount()

                if (response.isSuccessful && response.body()?.success == true) {
                    dashboardUserCount.value = response.body()?.count ?: 0

                }else{
                    errorMessage.value = response.body()?.message ?: "Failed to load user count."
                }
            }catch(e: com.google.gson.JsonSyntaxException){
                errorMessage.value = "Server error: Invalid data format."
                e.printStackTrace()
            }catch(e: Exception){
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            }
        }
    }

    fun getStudentCount(context: Context){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.getService(context).getStudentCount()

                if(response.isSuccessful && response.body()?.success == true){
                    dashboardStudentCount.value = response.body()?.count ?: 0
                }else{
                    errorMessage.value = response.body()?.message ?: "Failed to load student count."
                }

            }catch(e: com.google.gson.JsonSyntaxException){
                errorMessage.value = "Server error: Invalid data format."
                e.printStackTrace()
            }catch(e: Exception){
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            }
        }
    }

    fun getFileCount(context: Context){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.getService(context).getFileCount()

                if(response.isSuccessful && response.body()?.success == true){
                    dashboardFileCount.value = response.body()?.count ?: 0
                }else{
                    errorMessage.value = response.body()?.message ?: "Failed to load file count."
                }
            }catch(e: com.google.gson.JsonSyntaxException){
                errorMessage.value = "Server error: Invalid data format."
                e.printStackTrace()
            }catch(e: Exception){
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            }
        }
    }

    fun getEducatorDashboardData(context: Context) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            try {
                val statsResponse = ApiClient.getService(context).getEducatorStats()
                if (statsResponse.isSuccessful && statsResponse.body()?.success == true) {
                    educatorStats.value = statsResponse.body()
                } else if (statsResponse.code() == 404 || statsResponse.code() == 403) {
                    educatorStats.value = null
                }

                val modulesResponse = ApiClient.getService(context).getEducatorModules()
                if (modulesResponse.isSuccessful && modulesResponse.body()?.success == true) {
                    educatorModules.value = modulesResponse.body()?.modules ?: emptyList()
                } else if (modulesResponse.code() == 404 || modulesResponse.code() == 403) {
                    educatorModules.value = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("EDUCATOR_DASHBOARD", "Error fetching educator data", e)
            }
        }
    }

    fun getAdminDashboardData(context: Context) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.getService(context).getAdminStats()
                if (response.isSuccessful && response.body()?.success == true) {
                    adminStats.value = response.body()
                }
            } catch (e: Exception) {
                errorMessage.value = "Error loading admin data."
                e.printStackTrace()
            }
        }
    }
}