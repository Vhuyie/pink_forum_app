package com.example.thepinkforum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.network.FileItem
import com.example.thepinkforum.network.UsersViewModel
import com.example.thepinkforum.ui.EmptyState
import com.example.thepinkforum.ui.LoadingState
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.PinkSoftCard
import com.example.thepinkforum.ui.theme.SearchBar
import com.example.thepinkforum.ui.theme.TextDark
import com.example.thepinkforum.ui.theme.TextMuted

@Composable
fun EducatorFilesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = viewModel()
) {
    val context = LocalContext.current
    val files by viewModel.allFiles
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var showUploadDialog by remember { mutableStateOf(false) }

    var fileToDelete by remember { mutableStateOf<FileItem?>(null) }

    var refreshKey by remember { mutableIntStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(refreshKey) {
        viewModel.fetchFiles(context)
    }

    val educatorScreens = listOf(
        NavigationItems.EducatorHome,
        NavigationItems.EducatorFiles,
        NavigationItems.EducatorUpload,
        NavigationItems.EducatorCourses,
        NavigationItems.EducatorInventory
    )

    if (showUploadDialog) {
        UploadFileDialog(
            onDismiss = { showUploadDialog = false },
            userRole = "educator",
            onUploadClick = {filename, category,uri, role ->
                showUploadDialog = false
                viewModel.uploadFile(context, filename, category, uri, role)
                refreshKey++
            }
        )
    }

    if (fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val target = fileToDelete!!
                        fileToDelete = null
                        viewModel.deleteFile(context, target.id)
                        refreshKey++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel", color = TextDark)
                }
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = educatorScreens)
        },

        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {showUploadDialog = true},
                containerColor = PinkPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Upload Resource") }
            )
        },
        containerColor = PinkBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Manage File Content",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Upload and organize resources for learners",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search files...") },
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
                val filteredFiles = files.filter { it.title.contains(searchQuery, ignoreCase = true) }

                if (filteredFiles.isEmpty()) {
                    EmptyState(
                        message = if (searchQuery.isEmpty()) "No Files in Library" else "No matches found",
                        subMessage = if (searchQuery.isEmpty()) "Check back later for module resources." else "Try a different search term."
                    )
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        items(filteredFiles) { file ->
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
                                                imageVector = Icons.Outlined.Description,
                                                contentDescription = null,
                                                tint = PinkPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = file.category,
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }

                                    IconButton(onClick = { fileToDelete = file }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}