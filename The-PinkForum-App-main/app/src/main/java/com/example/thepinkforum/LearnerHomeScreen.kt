package com.example.thepinkforum


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.network.DashboardViewModel
import com.example.thepinkforum.network.TokenManager
import com.example.thepinkforum.network.UsersViewModel
import com.example.thepinkforum.ui.EmptyState
import com.example.thepinkforum.ui.LoadingState
import com.example.thepinkforum.ui.theme.*

@Composable
fun LearnerHomeScreen(
    navController: NavController,
    onCourseClick: (String) -> Unit = {},
    viewModel: UsersViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val userName = remember { TokenManager(context).getUserName() }

    val enrolments by viewModel.enrolments
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchCourses(context)
        dashboardViewModel.getUserDetails(context)
        viewModel.getEnrolledCourses(context)
    }

    val studentScreens = listOf(
        NavigationItems.StudentHome,
        NavigationItems.StudentFiles,
        NavigationItems.StudentCourses,
        NavigationItems.StudentDownloaded
    )

    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = studentScreens)
        },
        containerColor = PinkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back, 👋",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                            Text(
                                text = dashboardViewModel.dashboardUserName.value.ifBlank { userName ?: "User" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                        Card(
                            shape = RoundedCornerShape(50.dp),
                            colors = CardDefaults.cardColors(containerColor = PinkSoftCard),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                IconButton(onClick = {navController.navigate("settings/student")}) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Settings",
                                        tint = PinkDark
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Featured Announcement Banner
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PinkPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "NEW WORKSHOP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Digital Literacy & Web Safety",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Join our live session this Friday to learn essential online skills.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Quick Modules Title
                item {
                    Text(
                        text = "Your Student Modules",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Learning Cards
                item {
                    if (isLoading) {
                        LoadingState(modifier = Modifier.height(200.dp))
                    } else if (errorMessage != null) {
                        Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
                    } else if (enrolments.isEmpty()) {
                        EmptyState(
                            message = "No Courses Available",
                            subMessage = "You haven't enrolled in any modules yet.",
                            modifier = Modifier.height(200.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            enrolments.forEach { enrolment ->
                                LearnerCourseCard(
                                    title = enrolment.title,
                                    subtitle = "${enrolment.category} • ${enrolment.location}"
                                ) { onCourseClick(enrolment.id.toString()) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LearnerCourseCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LearnerHomeScreenPreview(){
    LearnerHomeScreen(navController = NavController(androidx.compose.ui.platform.LocalContext.current))
}