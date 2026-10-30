package com.example.thepinkforum

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.thepinkforum.ui.theme.PinkBackground

@Composable
fun AdminAttendanceScreen(
    navController: NavController,
    modifier: Modifier = Modifier
){
    val adminScreens = listOf(
        NavigationItems.AdminHome,
        NavigationItems.AdminFiles,
        NavigationItems.AdminUpload,
        NavigationItems.AdminCourses,
        NavigationItems.AdminInventory
    )

    // State to track if we are viewing the sessions list or a specific session's details
    var selectedSession by remember { mutableStateOf<AttendanceSessionData?>(null) }

    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = adminScreens)
        },
        containerColor = PinkBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedSession == null) {
                // View 1: List of all sessions
                SessionListView(
                    onSessionClick = { session -> selectedSession = session }
                )
            } else {
                // View 2: Specific learners in the clicked session
                LearnerRecordsView(
                    session = selectedSession!!,
                    onBackClick = { selectedSession = null }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminAttendanceScreenPreview(){
    AdminAttendanceScreen(modifier = Modifier, navController = NavController(LocalContext.current))
}