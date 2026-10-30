package com.example.thepinkforum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.ui.theme.*
import com.example.thepinkforum.network.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    userRole: String,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading

    val errorMessage by authViewModel.errorMessage
    val deleteSuccess by authViewModel.deleteSuccess

    LaunchedEffect(Unit){
        authViewModel.resetState()
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            navController.navigate("login_as") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    // State to control the Delete Account confirmation popup
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },

                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PinkBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PinkBackground)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    color = PinkPrimary
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Profile Section
            SettingsItem(
                icon = Icons.Default.Person,
                text = "My Profile",
                onClick = {
                    navController.navigate("profile/$userRole")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Logout Action
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                text = "Logout",
                onClick = {
                    authViewModel.logout(context)
                    navController.navigate("login_as") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Delete Account Action (Trigger Dialog)
            SettingsItem(
                icon = Icons.Default.Delete,
                text = "Delete Account",
                textColor = MaterialTheme.colorScheme.error,
                iconColor = MaterialTheme.colorScheme.error,
                onClick = { showDeleteDialog = true }
            )
        }

        // --- Delete Account Confirmation Dialog ---
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
                text = { Text("Are you absolutely sure? This action cannot be undone and you will lose all your data.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            // TODO: Add authViewModel.deleteAccount(context) here later
                            authViewModel.deleteAccount(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = TextDark)
                    }
                }
            )
        }
    }
}

// Reusable UI Component for Settings Rows
@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    textColor: Color = TextDark,
    iconColor: Color = PinkPrimary,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}