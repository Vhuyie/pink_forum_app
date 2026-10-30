package com.example.thepinkforum.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import androidx.core.net.toUri

sealed interface DownloadState{
    object Idle: DownloadState
    object Downloading: DownloadState
    data class Downloaded(val filePath: String): DownloadState
    data class Error(val message: String): DownloadState
}

class UsersViewModel : ViewModel() {

    // UI States
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val successMessage = mutableStateOf<String?>(null)

    // Data Lists
    val courses = mutableStateOf<List<CourseData>>(emptyList())
    val enrolments = mutableStateOf<List<EnrolledCourses>>(emptyList())
    val allFiles = mutableStateOf<List<FileItem>>(emptyList())
    val downloadedFiles = mutableStateOf<List<FileItem>>(emptyList())
    val allUsers = mutableStateOf<List<UserData>>(emptyList())
    val inventoryItems = mutableStateOf<List<InventoryData>>(emptyList())
    var downloadState by mutableStateOf<DownloadState>(DownloadState.Idle)
    var uploadState = MutableStateFlow("Idle")




    // Fetch All Courses
    fun fetchCourses(context: Context) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val response = ApiClient.getService(context).getCourses()
                if (response.isSuccessful && response.body()?.success == true) {
                    courses.value = response.body()?.courses ?: emptyList()
                } else if (response.code() == 404) {
                    courses.value = emptyList()
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val parsed = com.google.gson.Gson().fromJson(errorBody, CourseResponse::class.java)
                        parsed?.message ?: "Failed to load courses."
                    } catch (_: Exception) {
                        "Failed to load courses."
                    }
                    errorMessage.value = errorMsg
                }
            } catch (e: com.google.gson.JsonSyntaxException) {
                errorMessage.value = "Server error: Invalid data format."
                e.printStackTrace()
            } catch (e: Exception) {
                errorMessage.value = "Network error/Routing: Connection failed.(Check your server or API)"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    // Fetch Library Files
    fun fetchFiles(context: Context) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val response = ApiClient.getService(context).getFiles()
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("PINK_FORUM_DEBUG", "GET SUCCESS! Raw Data: ${response.body()}")
                    allFiles.value = response.body()?.files ?: emptyList()
                } else {
                    Log.e("PINK_FORUM_DEBUG", "GET FAILED. Code: ${response.code()}")
                    errorMessage.value = response.body()?.message ?: "Failed to load files."
                }
            } catch (e: com.google.gson.JsonSyntaxException) {
                errorMessage.value = "Server error: Invalid data format."
                e.printStackTrace()
            } catch (e: Exception) {
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    // Fetch Downloaded Files from LOCAL device storage using DownloadManager
    fun fetchLocalDownloads(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            errorMessage.value = null
            try {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
                val cursor = downloadManager.query(query)
                
                val downloadedList = mutableListOf<FileItem>()
                
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                        val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val idIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                        
                        if (titleIndex != -1 && localUriIndex != -1 && idIndex != -1) {
                            val title = cursor.getString(titleIndex)
                            val uriString = cursor.getString(localUriIndex)
                            val id = cursor.getLong(idIndex)
                            
                            // Only add if it's a file path we can actually use or display
                            if (title != null && uriString != null) {
                                downloadedList.add(
                                    FileItem(
                                        id = id.toInt(),
                                        title = title,
                                        category = "Downloaded",
                                        url = uriString,
                                        uploader = "System"
                                    )
                                )
                            }
                        }
                    }
                    cursor.close()
                }
                
                // Fallback: Also scan the app's private external downloads folder
                val appDownloads = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                appDownloads?.listFiles()?.forEach { file ->
                    if (file.isFile && downloadedList.none { it.title == file.name }) {
                        downloadedList.add(
                            FileItem(
                                id = file.hashCode(),
                                title = file.name,
                                category = "Local File",
                                url = file.absolutePath,
                                uploader = "Me"
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    downloadedFiles.value = downloadedList
                    Log.d("PINK_FORUM_DEBUG", "Fetched ${downloadedList.size} local downloads")
                }
            } catch (e: Exception) {
                Log.e("PINK_FORUM_DEBUG", "Error reading local downloads", e)
                withContext(Dispatchers.Main) {
                    errorMessage.value = "Could not read local downloads."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                }
            }
        }
    }

    fun deleteDownloadedFile(context: Context, fileItem: FileItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (fileItem.url.isNotBlank()) {
                    val rawPath = if (fileItem.url.startsWith("file://")) {
                        fileItem.url.substring(7)
                    } else {
                        fileItem.url
                    }
                    val file = File(rawPath)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                try {
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    downloadManager.remove(fileItem.id.toLong())
                } catch (_: Exception) {}

                fetchLocalDownloads(context)
            } catch (e: Exception) {
                Log.e("PINK_FORUM_DEBUG", "Error deleting downloaded file", e)
            }
        }
    }

    fun fetchAllUsers(context: Context) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val response = ApiClient.getService(context).getAllUsers()
                Log.d("USERS_VM", "Fetch All Users RAW: ${response.body()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    allUsers.value = response.body()?.users ?: emptyList()
                    Log.d("USERS_VM", "Updated allUsers with ${allUsers.value.size} users")
                } else {
                    errorMessage.value = "Failed to fetch users."
                }
            } catch (e: Exception) {
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun fetchAllInventory(context: Context){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch{
            isLoading.value = true
            errorMessage.value = null
            try{
                val response = ApiClient.getService(context).getInventory()
                
                // CRITICAL DEBUG: Log the raw body as string to see what the server is actually sending
                Log.d("PINK_FORUM_DEBUG", "RAW INVENTORY RESPONSE: ${response.body()}")

                if(response.isSuccessful && response.body()?.success == true){
                    val items = response.body()?.items
                    Log.d("PINK_FORUM_DEBUG", "INVENTORY SUCCESS! Items List: $items")
                    Log.d("PINK_FORUM_DEBUG", "Items size: ${items?.size}")
                    inventoryItems.value = items ?: emptyList()
                }else{
                    Log.e("PINK_FORUM_DEBUG", "INVENTORY FAILED. Code: ${response.code()}, Msg: ${response.body()?.message}")
                    errorMessage.value = response.body()?.message ?: "Failed to load inventory items."
                }
            }catch(e: com.google.gson.JsonSyntaxException){
                Log.e("PINK_FORUM_DEBUG", "INVENTORY JSON ERROR", e)
                errorMessage.value = "Server error: Invalid data format."
            }catch(e: Exception){
                Log.e("PINK_FORUM_DEBUG", "INVENTORY NETWORK ERROR", e)
                errorMessage.value = "Network error: Connection failed."
            } finally {
                isLoading.value = false
            }
        }
    }

    fun downloadFile(context: Context, fileId: Int, fileTitle: String, fileName: String){
        val cleanFileName = fileName.trim()

        if (!NetworkUtils.isInternetAvailable(context)) {
            downloadState = DownloadState.Error("No internet connection.")
            return
        }

        viewModelScope.launch(Dispatchers.IO){
            try{
                downloadState = DownloadState.Downloading

                val tokenManager = TokenManager(context)
                val token = tokenManager.getToken()

                val baseUrl = "https://the-pinkforum-website-production-8217.up.railway.app/api/mobile_download.php"
                val fullUrl = "$baseUrl?file_id=$fileId"

                val request = DownloadManager.Request(fullUrl.toUri()).apply {
                    setTitle(fileTitle.trim())
                    setDescription("Downloading from The Pink Forum...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setAllowedOverMetered(true)
                    
                    setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, cleanFileName)
                    
                    if (cleanFileName.endsWith(".pdf", ignoreCase = true)) {
                        setMimeType("application/pdf")
                    } else {
                        setMimeType("application/octet-stream")
                    }
                    
                    if (token != null) {
                        addRequestHeader("Authorization", "Bearer $token")
                    }
                }

                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)

                withContext(Dispatchers.Main) {
                    downloadState = DownloadState.Downloaded("Saved to Downloads folder")
                }

            } catch (e: Exception){
                Log.e("PINK_FORUM_DEBUG", "DOWNLOAD ERROR: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    downloadState = DownloadState.Error("Failed to start download.")
                }
            }
        }
    }

    fun uploadFile(context: Context, fileName: String, category: String, uri: Uri, role : String){
        if (!NetworkUtils.isInternetAvailable(context)) {
            uploadState.value = "Error: No internet connection."
            return
        }

        viewModelScope.launch(Dispatchers.IO){
            uploadState.value = "Uploading"
            isLoading.value = true

            val tokenManager = TokenManager(context)

            // 1. Get user details (Update getUserId to match how you saved it!)
            val userId = tokenManager.getUserId()
            val userName = tokenManager.getUserName() ?: "Educator"

            if (userId == null) {
                uploadState.value = "Error: User session not found. Please log in again."
                return@launch
            }

            //Deal with file content safely
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "pdf"


            val tempFileName = "${File(fileName)}.$extension"

            val tempFile = File(context.cacheDir, tempFileName)

            contentResolver.openInputStream(uri)?.use{inputStream ->
                FileOutputStream(tempFile).use{outputStream ->
                    inputStream.copyTo(outputStream)
                }

                val requestFile = tempFile.asRequestBody("media/formdata".toMediaTypeOrNull())
                val idPart = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                val namePart = userName.toRequestBody("text/plain".toMediaTypeOrNull())
                val fileTitlePart = fileName.toRequestBody("text/plain".toMediaTypeOrNull())

                val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("attachment", tempFile.name, requestFile)

                try{
                    val apiService = ApiClient.getService(context)
                    val response = if(role == "educator"){
                        apiService.educatorUploadFile(idPart, namePart, fileTitlePart, categoryPart, filePart)
                    }else{
                        apiService.adminUploadFile(idPart, namePart, fileTitlePart, categoryPart, filePart)

                    }

                    if(response.isSuccessful){
                        Log.d("PINK_FORUM_DEBUG", "UPLOAD SUCCESS! Code: ${response.code()}")
                        uploadState.value = "Success: ${response.code()}"
                        fetchFiles(context)
                    }else{
                        Log.e("PINK_FORUM_DEBUG", "UPLOAD FAILED. Code: ${response.code()}, WAMP Says: $errorMessage")
                        uploadState.value = "Error: ${response.code()}"
                    }
                }catch (e: Exception){
                    Log.e("PINK_FORUM_DEBUG", "ANDROID CRASHED: ${e.message}", e)
                    uploadState.value = "Network error: Connection failed. ${e.message}"
                    e.printStackTrace()
                }finally{
                    isLoading.value = false
                }
            }
        }
    }

    fun addCourse(context: Context, title: String, description: String, category: String, teacher: Int, location: String, programmeTime: String, status: String, createdBy: Int){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch{
            isLoading.value = true
            errorMessage.value = null

            val tokenManager = TokenManager(context)
            val userId = tokenManager.getUserId()
            val userIdInt = userId?.toIntOrNull() ?: 0

            if(userIdInt == 0){
                errorMessage.value = "User session not found. Please log in again."
                return@launch
            }

            try{
                // Both user_id and created_by should be the Admin's ID
                val response = ApiClient.getService(context).addCourse(
                    userIdInt, 
                    title, 
                    description, 
                    category, 
                    teacher, 
                    location,
                    programmeTime, 
                    status, 
                    userIdInt // Pass Admin ID as createdBy
                )

                if(response.isSuccessful){
                    successMessage.value = "Course added successfully!"
                }else{
                    val errorBody = response.errorBody()?.string()
                    Log.e("ADD_COURSE", "Server Error: $errorBody")
                    errorMessage.value = "Failed to add course: $errorBody"
                }
            }catch(e: Exception){
                Log.e("ADD_COURSE", "Exception: ${e.message}", e)
                errorMessage.value = "Network error: Connection failed."
            } finally {
                isLoading.value = false
            }
        }
    }

    fun courseEnrol(context: Context, programmeId: Int){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch{
            isLoading.value = true
            errorMessage.value = null
            
            val tokenManager = TokenManager(context)
            val token = tokenManager.getToken()
            
            Log.d("ENROL_DEBUG", "Attempting to enrol in programme: $programmeId")
            Log.d("ENROL_DEBUG", "Using token: ${token?.take(10)}...")

            try{
                val response = ApiClient.getService(context).courseEnrol(EnrolRequest(programmeId))

                if(response.isSuccessful){
                    successMessage.value = "Course enrolled successfully!"
                }else{
                    val errorBody = response.errorBody()?.string()
                    Log.e("ENROL_DEBUG", "Enrol failed: $errorBody")
                    errorMessage.value = "Failed to enrol course: $errorBody"
                }
            }catch(e: Exception){
                Log.e("ENROL_DEBUG", "Enrol exception", e)
                errorMessage.value = "Network error: Connection failed."
            } finally {
                isLoading.value = false
            }
        }
    }

    fun getEnrolledCourses(context: Context){
        if (!NetworkUtils.isInternetAvailable(context)) {
            return
        }

        viewModelScope.launch{
            try{
                val response = ApiClient.getService(context).getEnrolledCourses()

                if(response.isSuccessful && response.body()?.success == true) {
                    enrolments.value = response.body()?.courses ?: emptyList()
                } else {
                    Log.w("PINK_FORUM_DEBUG", "getEnrolledCourses response code: ${response.code()}")
                    enrolments.value = emptyList()
                }

            }catch(e: Exception){
                Log.e("PINK_FORUM_DEBUG", "GET ENROLLED COURSES ERROR", e)
                enrolments.value = emptyList()
            }
        }
    }

    fun deleteCourse(context: Context, courseId: Int){
        if(!NetworkUtils.isInternetAvailable(context)){
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch{
            isLoading.value = true
            errorMessage.value = null

            try{
                val response = ApiClient.getService(context).courseDelete(courseId)

                if(response.isSuccessful){
                    successMessage.value = "Course deleted successfully!"
                    fetchCourses(context) // Refresh list
                }else{
                    errorMessage.value = "Failed to delete course."
                }
            }catch(e: Exception){
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun editCourse(
        context: Context, 
        courseId: Int, 
        title: String, 
        description: String, 
        category: String, 
        teacherId: Int,
        location: String, 
        time: String,
        status: String
    ){
        if(!NetworkUtils.isInternetAvailable(context)){
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch{
            isLoading.value = true
            errorMessage.value = null

            try{
                val response = ApiClient.getService(context).courseEdit(
                    courseId, 
                    title, 
                    description, 
                    category, 
                    teacherId,
                    location,
                    time,
                    status
                )

                if(response.isSuccessful){
                    successMessage.value = "Course updated successfully!"
                    fetchCourses(context) // Refresh list
                }else{
                    errorMessage.value = "Failed to update course."
                }

            }catch(e: Exception){
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()

            }finally{
                isLoading.value = false
            }
        }
    }

    fun deleteFile(context: Context, fileId: Int){
        if(!NetworkUtils.isInternetAvailable(context)){
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try{
                val tokenManager = TokenManager(context)
                val response = if(tokenManager.getUserRole() == "educator"){
                    ApiClient.getService(context).educatorDeleteFile(fileId)
                }else{
                    ApiClient.getService(context).adminDeleteFile(fileId)
                }

                if(response.isSuccessful){
                    successMessage.value = "File deleted successfully!"
                    fetchFiles(context)
                }else{
                    errorMessage.value = "Failed to delete file."
                }

            }catch(e: Exception){
                errorMessage.value = "Network error: Connection failed."
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun getEducatorCourses(context : Context){
        if (!NetworkUtils.isInternetAvailable(context)) {
            errorMessage.value = "No internet connection."
            return
        }

        viewModelScope.launch{
            isLoading.value = true
            errorMessage.value = null

            try{
                val response = ApiClient.getService(context).getEducatorCourses()

                if(response.isSuccessful && response.body()?.success == true){
                    val fetchedCourses = response.body()?.courses ?: emptyList()
                    Log.d("EDUCATOR_COURSES", "Fetched ${fetchedCourses.size} courses for educator")
                    courses.value = fetchedCourses
                } else if (response.code() == 404) {
                    Log.d("EDUCATOR_COURSES", "404 - No assigned courses found for this educator")
                    courses.value = emptyList()
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val parsed = com.google.gson.Gson().fromJson(errorBody, CourseResponse::class.java)
                        parsed?.message ?: "Failed to load courses."
                    } catch (_: Exception) {
                        "Failed to load courses."
                    }
                    Log.e("EDUCATOR_COURSES", "Failed code: ${response.code()}, msg: $errorMsg")
                }
            } catch(e: Exception) {
                Log.e("EDUCATOR_COURSES", "Error loading educator courses", e)
            } finally {
                isLoading.value = false
            }
        }
    }
}
