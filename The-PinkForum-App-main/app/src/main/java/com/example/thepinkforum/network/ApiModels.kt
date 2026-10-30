package com.example.thepinkforum.network

import com.google.gson.annotations.SerializedName

// --- Requests (Sent to PHP) ---
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserData(
    @SerializedName("id", alternate = ["user_id"])
    val id: Int,
    @SerializedName("fullname")
    val name: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("role")
    val role: String?
)

// --- Responses (Received from PHP) ---
data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String? = null,
    val user: UserData? = null,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("verification_required")
    val verificationRequired: Boolean? = null
)

data class VerifyEmailRequest(
    @SerializedName("user_id")
    val userId: Int,
    val code: String
)

data class VerifyEmailResponse(
    val success: Boolean,
    val message: String?
)

data class UpdateProfileRequest(
    @SerializedName("fullname")
    val fullName: String,
    val email: String
)

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String?
)

// --- Learner Data Models ---

data class InventoryData(
    @SerializedName("item_id")
    val id : Int,
    @SerializedName("item_name")
    val name : String,
    @SerializedName("item_category")
    val category : String,
    @SerializedName("item_quantity")
    val quantity : Int
)

data class InventoryResponse(
    val success: Boolean,
    val message: String? = null,
    val items: List<InventoryData>? = null
)

data class CourseData(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("category")
    val category: String = "",
    @SerializedName("teacher_id")
    val teacherId: Int? = 0,
    @SerializedName("location")
    val location: String? = "",
    @SerializedName("programme_time")
    val programmeTime: String? = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("created_by")
    val createdBy: String = "",
)

data class CourseResponse(
    val success: Boolean,
    val message: String? = null,
    val courses: List<CourseData>? = null
)

data class FileItem(
    @SerializedName("file_id")
    val id: Int,
    @SerializedName("file_name")
    val title: String,
    @SerializedName("file_category")
    val category: String,
    @SerializedName("file_path")
    val url: String,
    @SerializedName("uploaded_by")
    val uploader: String,
)

data class FileListResponse(
    val success: Boolean,
    val message: String? = null,
    val files: List<FileItem>? = null
)

data class UploadResponse(
    val status: Boolean = false,
    val message: String = ""
)

data class CourseCreationResponse(
    val success: Boolean,
    val message: String? = null,
)

data class EnrolRequest(
    @SerializedName("programme_id")
    val programmeId: Int
)

data class EnrolledCourses(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("category")
    val category: String = "",
    @SerializedName("location")
    val location: String = ""
)

data class EnrolledCoursesResponse(
    val success: Boolean,
    val message: String? = null,
    val courses: List<EnrolledCourses>? = null
)

data class UserResponse(
    val success: Boolean,
    val id: Int,
    val name: String?,
    val surname: String?,
    val email: String?,
    val role: String?,
    val message : String?
)

data class UserCount(
    val success : Boolean,
    val count : Int? = 0,
    val message : String
)

data class UserList(
    val success : Boolean,
    val message : String? = null,
    val users: List<UserData>? = null
)

data class StudentUserCount(
    val success : Boolean,
    val count : Int? = 0,
    val message : String
)

data class FileCount(
    val success : Boolean,
    val count : Int? = 0,
    val message : String
)

data class EducatorStats(
    val success: Boolean = false,
    @SerializedName("active_courses")
    val activeCourses: Int? = 0,
    @SerializedName("total_students")
    val totalStudents: Int? = 0,
    val message: String? = null
)

data class EducatorModule(
    val id: Int = 0,
    val title: String = "",
    @SerializedName("enrolled_students")
    val enrolledStudents: Int? = 0
)

data class EducatorModulesResponse(
    val success: Boolean = false,
    val modules: List<EducatorModule>? = null,
    val message: String? = null
)

data class AdminStats(
    val success: Boolean,
    val total_users: Int,
    val educators: Int,
    val pending_files: Int,
    val flagged_items: Int,
    val message: String? = null
)
