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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.thepinkforum.network.DashboardViewModel
import com.example.thepinkforum.ui.theme.*

@Composable
fun AdminHomeScreen(
    navController: NavController,
    fileItems: NavigationItems,
    inventoryItem: NavigationItems,
    usersItem: NavigationItems,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val totalStudents = dashboardViewModel.dashboardStudentCount.value
    val errorMessage = dashboardViewModel.errorMessage.value
    val totalFiles = dashboardViewModel.dashboardFileCount.value

    LaunchedEffect(Unit) {
        dashboardViewModel.getUserCount(context)
        dashboardViewModel.getStudentCount(context)
        dashboardViewModel.getFileCount(context)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val adminScreens = listOf(
        NavigationItems.AdminHome,
        NavigationItems.AdminFiles,
        NavigationItems.AdminUpload,
        NavigationItems.AdminCourses,
        NavigationItems.AdminInventory
    )
    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = adminScreens)
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
                            Text(text = "SYSTEM ADMINISTRATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PinkDark, letterSpacing = 1.sp)
                            Text(text = "Control Center", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
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
                                IconButton(onClick = {navController.navigate("settings/admin")}) {
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

                // Platform Stats Overview
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminTile("Total Users", dashboardViewModel.dashboardUserCount.value.toString(), Modifier.weight(1f))
                            AdminTile("Students", totalStudents.toString(), Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminTile("Total Files", totalFiles.toString(), Modifier.weight(1f))
                            AdminTile("Flagged Items", 0.toString(), Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Management Controls
                item {
                    Text(text = "Admin Controls", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    AdminActionButton("👤 Manage Users & Roles", {
                        if(currentRoute != usersItem.route){
                            navController.navigate(usersItem.route){
                                popUpTo(navController.graph.startDestinationId){saveState = true}
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    })
                    Spacer(modifier = Modifier.height(10.dp))
                    AdminActionButton("📑 Review Uploaded Documents", {
                        if(currentRoute != fileItems.route){
                            navController.navigate(fileItems.route){
                                popUpTo(navController.graph.startDestinationId){saveState = true}
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    })

                    Spacer(modifier = Modifier.height(10.dp))
                    AdminActionButton("👤 View Inventory", {
                        if (currentRoute != inventoryItem.route) {
                            navController.navigate(inventoryItem.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    })
                }
            }
        }
    }

}

@Composable
fun AdminTile(label: String, value: String, modifier: Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = label, fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
fun AdminActionButton(title: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextDark)
            Text(text = "→", fontSize = 18.sp, color = PinkDark, fontWeight = FontWeight.Bold)
        }
    }
}