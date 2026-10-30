package com.example.thepinkforum

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thepinkforum.network.TokenManager
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.ThePinkforumTheme
import com.google.firebase.messaging.FirebaseMessaging

// Make sure to import your screens here if they are in a different package!
// import com.example.thepinkforum.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThePinkforumTheme {
                //Deals with the navigation controller and the screens
                val navController = rememberNavController()

                window.navigationBarColor = PinkBackground.toArgb()
                window.statusBarColor = PinkBackground.toArgb()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PinkBackground
                ){
                    val context = LocalContext.current
                    val tokenManager = TokenManager(context)

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        Log.d("PINK_FORUM_FCM", "POST_NOTIFICATIONS Granted: $isGranted")
                    }

                    LaunchedEffect(Unit) {
                        try {
                            FirebaseMessaging.getInstance().subscribeToTopic("all_learners")
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("PINK_FORUM_FCM", "Subscribed to all_learners topic successfully!")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("PINK_FORUM_FCM", "Error subscribing to all_learners topic: ${e.message}")
                                }
                        } catch (e: Exception) {
                            Log.e("PINK_FORUM_FCM", "FCM Subscription error: ${e.message}")
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } catch (e: Exception) {
                                Log.e("PINK_FORUM_FCM", "Permission request error: ${e.message}")
                            }
                        }
                    }

                    val startDestination = when {
                        tokenManager.isFirstTime() -> "get_started"
                        tokenManager.isTokenValid(tokenManager.getToken()) -> {
                            when (tokenManager.getUserRole()) {
                                "admin" -> "admin_home"
                                "educator" -> "educator_home"
                                else -> "student_home"
                            }
                        }
                        else -> "login_as"
                    }

                    PinkForumApp(navController = navController, startDestination = startDestination)

                }
            }
        }
    }
}

@Composable
fun PinkForumApp(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = "get_started"
) {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    //The NavHost is the container that swaps out your screens
    NavHost(
        navController = navController,
        startDestination = startDestination, // The dynamic start destination
        modifier = modifier,
    ) {
        // Route 1: Get Started Screen
        composable("get_started") {
            GetStartedScreen(
                onGetStartedClick = {
                    tokenManager.setFirstTime(false)
                    // Tell the controller to go to the register_as route!
                    navController.navigate("register_as"){
                        popUpTo("get_started") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    tokenManager.setFirstTime(false)
                    // navController.navigate("login_as") for users who already have an account
                    navController.navigate("login_as"){
                        popUpTo("get_started") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Route 2: Register As Screen
        composable("register_as") {
            RegisterAsScreen(
                onRoleSelected = { role ->
                    // We can pass the selected role as a variable in the route!
                    navController.navigate("register/$role")
                }
            )
        }

        // Route 3: Register Form Screen
        composable("register/{role}") { backStackEntry ->
            // Extract the role from the route link (defaults to student if null)
            val userRole = backStackEntry.arguments?.getString("role") ?: "student"

            RegisterScreen(
                userRole = userRole,
                navController = navController
            )
        }

        composable("verify_email/{userId}/{role}") { backStackEntry ->
            val userIdStr = backStackEntry.arguments?.getString("userId") ?: "0"
            val userId = userIdStr.toIntOrNull() ?: 0
            val userRole = backStackEntry.arguments?.getString("role") ?: "student"

            VerifyEmailScreen(
                userId = userId,
                userRole = userRole,
                navController = navController
            )
        }

        composable("login/{role}"){ backStackEntry ->
            val userRole = backStackEntry.arguments?.getString("role") ?: "student"

            LoginScreen(
                userRole = userRole,
                navController = navController
            )
        }

        composable("login_as"){
            LoginAsScreen(
                onRoleSelected = { role ->
                    navController.navigate("login/$role")
                }
            )
        }

        composable("student_home"){
            LearnerHomeScreen(
                onCourseClick = {courseId ->},
                navController = navController
            )
        }

        composable("educator_home"){
            EducatorHomeScreen(
                onCreateCourseClick = {navController.navigate("educator_courses")},
                onUploadResourceClick = {navController.navigate("educator_upload")},
                navController = navController
            )
        }

        composable("admin_home"){
            AdminHomeScreen(
                navController = navController,
                inventoryItem = NavigationItems.AdminInventory,
                fileItems = NavigationItems.AdminFiles,
                usersItem = NavigationItems.AdminUsers
            )
        }

        composable("admin_users"){
            AdminUsersScreen(
                navController = navController
            )
        }

        composable("student_files"){
            LearnerFilesScreen(
                navController = navController
            )
        }

        composable("educator_files"){
            EducatorFilesScreen(
                navController = navController
            )
        }

        composable("educator_upload") {
            EducatorFilesScreen(
                navController = navController
            )
        }

        composable("admin_files"){
            AdminFilesScreen(
                navController = navController
            )
        }

        composable("admin_upload") {
            AdminFilesScreen(
                navController = navController
            )
        }

        composable("student_courses"){
            LearnerCoursesScreen(
                navController = navController
            )
        }

        composable("student_downloaded_files"){
            LearnerDownloadedFilesScreen(
                navController = navController
            )
        }

        composable("educator_courses"){
            EducatorCoursesScreen(
                navController = navController
            )
        }

        composable("educator_attendance"){
            EducatorAttendanceScreen(
                navController = navController
            )
        }

        composable("admin_courses"){
            AdminCoursesScreen(
                navController = navController
            )
        }

        composable("admin_attendance"){
            AdminAttendanceScreen(
                navController = navController
            )
        }

        composable("educator_inventory"){
            EducatorInventoryScreen(
                navController = navController
            )
        }

        composable("admin_inventory"){
            AdminInventoryScreen(
                navController = navController
            )
        }

        composable("settings/{role}") { backStackEntry ->
            val userRole = backStackEntry.arguments?.getString("role") ?: "student"

            SettingsScreen(
                userRole = userRole,
                navController = navController
            )
        }

        composable("profile/{role}") { backStackEntry ->
            val userRole = backStackEntry.arguments?.getString("role") ?: "student"

            ProfileScreen(
                userRole = userRole,
                navController = navController
            )
        }
    }
}


