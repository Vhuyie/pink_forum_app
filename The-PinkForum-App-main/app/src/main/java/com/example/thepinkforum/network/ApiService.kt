package com.example.thepinkforum.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
    // Expects URL like: /api/register.php?role=learner
    @POST("mobile_learner_register.php")
    suspend fun studentRegister(
        @Query("role") role: String,
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("mobile_educator_register.php")
    suspend fun educatorRegister(
        @Query("role") role: String,
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("mobile_verify_email.php")
    suspend fun verifyEmail(
        @Body request: VerifyEmailRequest
    ): Response<VerifyEmailResponse>

    // Expects URL like: /api/login.php?role=learner
    @POST("mobile_learner_login.php")
    suspend fun studentLogin(
        @Query("role") role: String,
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("mobile_educator_login.php")
    suspend fun educatorLogin(
        @Query("role") role: String,
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("mobile_admin_login.php")
    suspend fun adminLogin(
        @Query("role") role: String,
        @Body request: LoginRequest
    ): Response<AuthResponse>

    // --- Learner Endpoints ---

    @GET("mobile_files.php")
    suspend fun getFiles(): Response<FileListResponse>

    @Streaming
    @GET
    suspend fun downloadFile(@Url fileUrl: String): Response<ResponseBody>

    @Multipart
    @POST("mobile_educator_upload.php")
    suspend fun educatorUploadFile(
        @Part("user_id") userId: RequestBody,
        @Part("user_name") userName: RequestBody,
        @Part("file_name") fileName: RequestBody,
        @Part("file_category") category: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @Multipart
    @POST("mobile_admin_upload.php")
    suspend fun adminUploadFile(
        @Part("user_id") userId: RequestBody,
        @Part("user_name") userName: RequestBody,
        @Part("file_name") fileName: RequestBody,
        @Part("file_category") category: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @GET("mobile_users.php")
    suspend fun getUsers(): Response<UserResponse>

    @GET("mobile_users.php")
    suspend fun getAllUsers(): Response<UserList>

    @GET("mobile_total_users.php")
    suspend fun getUserCount(): Response<UserCount>

    @GET("mobile_total_learners.php")
    suspend fun getStudentCount(): Response<StudentUserCount>

    @GET("mobile_educator_stats.php")
    suspend fun getEducatorStats(): Response<EducatorStats>

    @GET("mobile_educator_modules.php")
    suspend fun getEducatorModules(): Response<EducatorModulesResponse>

    @GET("mobile_admin_stats.php")
    suspend fun getAdminStats(): Response<AdminStats>

    @GET("mobile_total_files.php")
    suspend fun getFileCount(): Response<FileCount>

    @POST("mobile_learner_delete_account.php")
    suspend fun deleteStudentAccount(@Query("user_id") userId: String): Response<ResponseBody>

    @POST("mobile_educator_delete_account.php")
    suspend fun deleteEducatorAccount(@Query("user_id") userId: String): Response<ResponseBody>

    @POST("mobile_admin_delete_account.php")
    suspend fun deleteAdminAccount(@Query("user_id") userId: String): Response<ResponseBody>

    @GET("mobile_admin_inventory_read_item.php")
    suspend fun getInventory(): Response<InventoryResponse>

    @POST("mobile_add_course.php")
    suspend fun addCourse(
        @Query("user_id") userId: Int,
        @Query("title") title: String,
        @Query("description") description: String,
        @Query("category") category: String,
        @Query("teacher_id") teacher: Int,
        @Query("location") location: String,
        @Query("programme_time") programmeTime: String,
        @Query("status") status: String,
        @Query("created_by") createdBy: Int?
    ): Response<CourseCreationResponse>

    @GET("mobile_courses.php")
    suspend fun getCourses(): Response<CourseResponse>

    @POST("mobile_learner_enrol.php")
    suspend fun courseEnrol(@Body request: EnrolRequest): Response<ResponseBody>

    @GET("mobile_learner_courses.php")
    suspend fun getEnrolledCourses(): Response<EnrolledCoursesResponse>

    @POST("mobile_course_delete.php")
    suspend fun courseDelete(@Query("id") courseId: Int): Response<ResponseBody>

    @POST("mobile_course_edit.php")
    suspend fun courseEdit(
        @Query("id") courseId: Int,
        @Query("title") title: String,
        @Query("description") description: String,
        @Query("category") category: String,
        @Query("teacher_id") teacherId: Int,
        @Query("location") location: String,
        @Query("programme_time") programmeTime: String,
        @Query("status") status: String
    ): Response<ResponseBody>

    @POST("mobile_admin_delete.php")
    suspend fun adminDeleteFile(@Query("file_id") fileId: Int): Response<ResponseBody>

    @POST("mobile_educator_delete.php")
    suspend fun educatorDeleteFile(@Query("file_id") fileId: Int): Response<ResponseBody>

    @GET("mobile_educator_courses.php")
    suspend fun getEducatorCourses(): Response<CourseResponse>

    @POST("mobile_update_profile.php")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>
}