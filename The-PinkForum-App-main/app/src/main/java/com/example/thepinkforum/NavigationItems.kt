package com.example.thepinkforum

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItems(val title: String, val route: String, val SelectedIcon: ImageVector, val UnselectedIcon: ImageVector){
    data object StudentHome: NavigationItems("Home", "student_home", Icons.Filled.Home, Icons.Outlined.Home)
    data object StudentFiles: NavigationItems("Files", "student_files", Icons.Filled.FileCopy, Icons.Outlined.FileCopy)
    data object StudentCourses: NavigationItems("Courses", "student_courses", Icons.Filled.Book, Icons.Outlined.Book)
    data object StudentDownloaded: NavigationItems("Downloaded", "student_downloaded_files", Icons.Filled.Download, Icons.Outlined.Download)

    data object EducatorHome: NavigationItems("Home", "educator_home", Icons.Filled.Home, Icons.Outlined.Home)
    data object EducatorFiles: NavigationItems("Files", "educator_files", Icons.Filled.FileCopy, Icons.Outlined.FileCopy)
    data object EducatorCourses: NavigationItems("Courses", "educator_courses", Icons.Filled.Book, Icons.Outlined.Book)
    data object EducatorUpload: NavigationItems("Upload", "educator_upload", Icons.Filled.UploadFile, Icons.Outlined.UploadFile)
    data object EducatorAttendance: NavigationItems("Attendance", "educator_attendance", Icons.Filled.People, Icons.Outlined.People)
    data object EducatorInventory: NavigationItems("Inventory", "educator_inventory", Icons.Filled.Inventory, Icons.Outlined.Inventory)

    data object AdminHome: NavigationItems("Home", "admin_home", Icons.Filled.Home, Icons.Outlined.Home)
    data object AdminFiles: NavigationItems("Files", "admin_files", Icons.Filled.FileCopy, Icons.Outlined.FileCopy)
    data object AdminCourses: NavigationItems("Courses", "admin_courses", Icons.Filled.Book, Icons.Outlined.Book)
    data object AdminUpload: NavigationItems("Upload", "admin_upload", Icons.Filled.UploadFile, Icons.Outlined.UploadFile)
    data object AdminAttendance: NavigationItems("Attendance", "admin_attendance", Icons.Filled.People, Icons.Outlined.People)
    data object AdminUsers: NavigationItems("Users", "admin_users", Icons.Filled.People, Icons.Outlined.People)
    data object AdminInventory: NavigationItems("Inventory", "admin_inventory", Icons.Filled.Inventory, Icons.Outlined.Inventory)
}