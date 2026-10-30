package com.example.thepinkforum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.network.UsersViewModel
import com.example.thepinkforum.ui.EmptyState
import com.example.thepinkforum.ui.LoadingState
import com.example.thepinkforum.ui.theme.*

@Composable
fun AdminUsersScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = viewModel()
) {
    val context = LocalContext.current
    val users by viewModel.allUsers
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers(context)
    }

    val adminScreens = listOf(
        NavigationItems.AdminHome,
        NavigationItems.AdminFiles,
        NavigationItems.AdminUpload,
        NavigationItems.AdminCourses,
        NavigationItems.AdminInventory
    )

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = adminScreens)
        },
        containerColor = PinkBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(PinkBackground)
                .padding(16.dp)
                .padding(innerPadding)
        ) {
            Text(
                text = "User Management",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "View and manage platform users and roles",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or email...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = SearchBar
                )
            )

            if (isLoading) {
                LoadingState()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else {
                val filteredUsers = users.filter { 
                    (it.name?.contains(searchQuery, ignoreCase = true) ?: false) || 
                    (it.email?.contains(searchQuery, ignoreCase = true) ?: false) 
                }

                if (filteredUsers.isEmpty()) {
                    EmptyState(
                        message = if (searchQuery.isEmpty()) "No Users Found" else "No matches found",
                        subMessage = if (searchQuery.isEmpty()) "There are no users registered on the platform." else "Try a different search term."
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredUsers) { user ->
                            UserListItem(user = user)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: com.example.thepinkforum.network.UserData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PinkSoftCard,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = PinkPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name ?: "Unknown",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Email, 
                        contentDescription = null, 
                        modifier = Modifier.size(12.dp), 
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.email ?: "No email",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Badge, 
                        contentDescription = null, 
                        modifier = Modifier.size(12.dp), 
                        tint = PinkPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.role?.replaceFirstChar { it.uppercase() } ?: "User",
                        fontSize = 12.sp,
                        color = PinkPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminUsersScreenPreview(){
    AdminUsersScreen(modifier = Modifier, navController = NavController(LocalContext.current))
}
