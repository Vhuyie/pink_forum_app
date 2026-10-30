package com.example.thepinkforum

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.network.DashboardViewModel
import com.example.thepinkforum.network.TokenManager
import com.example.thepinkforum.network.UsersViewModel
import com.example.thepinkforum.ui.theme.*

@Composable
fun EducatorHomeScreen(
    navController: NavController,
    onCreateCourseClick: () -> Unit,
    onUploadResourceClick: () -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel(),
    viewModel: UsersViewModel = viewModel(),
) {
    val context = LocalContext.current
    val errorMessage by dashboardViewModel.errorMessage
    val totalStudents by dashboardViewModel.dashboardStudentCount
    val totalFiles by dashboardViewModel.dashboardFileCount

    val educatorCourses by viewModel.courses


    LaunchedEffect(Unit) {
        dashboardViewModel.getUserDetails(context)
        dashboardViewModel.getStudentCount(context)
        dashboardViewModel.getFileCount(context)
        dashboardViewModel.getEducatorDashboardData(context)
        viewModel.getEducatorCourses(context)
    }

    val educatorScreens = listOf(
        NavigationItems.EducatorHome,
        NavigationItems.EducatorFiles,
        NavigationItems.EducatorUpload,
        NavigationItems.EducatorCourses,
        NavigationItems.EducatorInventory
    )

    val userName = remember { TokenManager(context).getUserName() }

    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = educatorScreens)
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
                    ){
                        Column{
                            Text(text = "Educator Portal 👩‍🏫", fontSize = 14.sp, color = PinkDark, fontWeight = FontWeight.Bold)
                            Text(text = "Hello, ${dashboardViewModel.dashboardUserName.value.ifBlank { userName ?: "User" }}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage.toString(),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(50.dp),
                            colors = CardDefaults.cardColors(containerColor = PinkSoftCard),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                IconButton(onClick = {navController.navigate("settings/educator")}) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Settings",
                                        tint = PinkDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Educator Stat Cards Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EducatorStatBox("Total Files", (totalFiles ?: 0).toString(), Modifier.weight(1f))
                        EducatorStatBox("Total Students", (totalStudents ?: 0).toString(), Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }

                // Quick Action Buttons
                item {
                    Text(text = "Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onCreateCourseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("+ View Created Courses", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onUploadResourceClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkDark),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("📎 Upload Material / Document", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

}

@Composable
fun EducatorStatBox(title: String, count: String, modifier: Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = count, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PinkPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 13.sp, color = TextMuted)
        }
    }
}

@Composable
fun EducatorModuleRow(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = subtitle, fontSize = 12.sp, color = TextMuted)
            }
            Text(text = "Manage >", fontSize = 13.sp, color = PinkDark, fontWeight = FontWeight.Bold)
        }
    }
}