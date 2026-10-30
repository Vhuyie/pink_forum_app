package com.example.thepinkforum

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.PinkSoftCard
import com.example.thepinkforum.ui.theme.SearchBar
import com.example.thepinkforum.ui.theme.TextMuted

@Composable
fun LearnerFilesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = viewModel()
) {
    val context = LocalContext.current
    val files by viewModel.allFiles
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    val downloadState = viewModel.downloadState
    androidx.compose.runtime.LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            is com.example.thepinkforum.network.DownloadState.Downloading -> {
                android.widget.Toast.makeText(context, "Downloading file...", android.widget.Toast.LENGTH_SHORT).show()
            }
            is com.example.thepinkforum.network.DownloadState.Downloaded -> {
                android.widget.Toast.makeText(context, "File downloaded successfully!", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.downloadState = com.example.thepinkforum.network.DownloadState.Idle
            }
            is com.example.thepinkforum.network.DownloadState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.downloadState = com.example.thepinkforum.network.DownloadState.Idle
            }
            else -> {}
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchFiles(context)
    }

    var searchQuery by remember { mutableStateOf("") }

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
    ) {innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(PinkBackground)
                .padding(16.dp)
                .padding(innerPadding)
        ) {
            Text(
                text = "Resource Library",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Download guides and module materials",
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

                                    IconButton(onClick = { 
                                        val fileName = file.url.split("/").last()
                                        viewModel.downloadFile(context, file.id, file.title, fileName) 
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Download,
                                            contentDescription = "Download",
                                            tint = PinkPrimary
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LearnerFilesScreenPreview(){
    LearnerFilesScreen(navController = NavController(LocalContext.current), modifier = Modifier)
}