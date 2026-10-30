package com.example.thepinkforum

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.thepinkforum.network.FileItem
import com.example.thepinkforum.network.UsersViewModel
import com.example.thepinkforum.ui.EmptyState
import com.example.thepinkforum.ui.LoadingState
import com.example.thepinkforum.ui.theme.LightGreen
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.PinkSoftCard
import com.example.thepinkforum.ui.theme.SearchBar
import com.example.thepinkforum.ui.theme.TextDark
import com.example.thepinkforum.ui.theme.TextMuted

@Composable
fun LearnerDownloadedFilesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = viewModel()
){
    val files by viewModel.downloadedFiles
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    val context = LocalContext.current

    var fileToDelete by remember { mutableStateOf<FileItem?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchLocalDownloads(context)
    }

    if (fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${fileToDelete!!.title}' from your device?") },
            confirmButton = {
                Button(
                    onClick = {
                        val item = fileToDelete!!
                        fileToDelete = null
                        viewModel.deleteDownloadedFile(context, item)
                        Toast.makeText(context, "File removed from device", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel", color = TextDark)
                }
            }
        )
    }

    val studentScreens = listOf(
        NavigationItems.StudentHome,
        NavigationItems.StudentFiles,
        NavigationItems.StudentCourses,
        NavigationItems.StudentDownloaded
    )

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = studentScreens)
        },
        containerColor = PinkBackground
    ){innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(PinkBackground)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Downloaded Files",
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Search Bar at the top of the screen
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {searchQuery = it},
                placeholder = { Text("Search Files") },
                leadingIcon = {Icon(Icons.Outlined.Search, contentDescription = "Search Icon")},
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

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = modifier.fillMaxWidth().height(55.dp)
                .padding(top = 5.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightGreen)
            ){
                Row(
                    modifier = modifier.fillMaxWidth().padding(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    Icon(
                        imageVector = Icons.Filled.Done,
                        //Icon for Downloaded
                        contentDescription = "Done Icon"
                    )

                    //Text that specifies where learner downloaded files are
                    Text(
                        text = "Downloaded Files are found here",
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "My Files",
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(17.dp))

            if (isLoading) {
                LoadingState()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else {
                val filteredFiles = files.filter { it.title.contains(searchQuery, ignoreCase = true) }

                if (filteredFiles.isEmpty()) {
                    EmptyState(
                        message = if (searchQuery.isEmpty()) "No Downloaded Files" else "No matches found",
                        subMessage = if (searchQuery.isEmpty()) "Files you download will appear here." else "Try a different search term."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ){
                        items(filteredFiles){downloadedFiles ->
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
                                            text = downloadedFiles.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = downloadedFiles.category,
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }

                                    IconButton(onClick = { fileToDelete = downloadedFiles }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete",
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
fun LearnerDownloadedFilesScreenPreview(){
    LearnerDownloadedFilesScreen(navController = NavController(LocalContext.current), modifier = Modifier)
}