package com.example.thepinkforum

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.network.CourseData
import com.example.thepinkforum.network.UsersViewModel
import com.example.thepinkforum.ui.EmptyState
import com.example.thepinkforum.ui.LoadingState
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.PinkSoftCard
import com.example.thepinkforum.ui.theme.TextDark
import com.example.thepinkforum.ui.theme.TextMuted

@Composable
fun AdminCoursesScreen(
    navController : NavController,
    modifier : Modifier = Modifier,
    viewModel: UsersViewModel = viewModel()
){
    val context = LocalContext.current
    val courses by viewModel.courses
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    val deleteSuccess by viewModel.successMessage

    var showCourseDialog by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableIntStateOf(0) }


    if(showCourseDialog){
        AddCourseDialog(
            onDismiss = {showCourseDialog = false},
            onCreateClick = { title, desc, cat, teacher, location, programmeTime, status ->
                showCourseDialog = false
                // The creator ID (last param) is handled inside the ViewModel now
                viewModel.addCourse(context, title, desc, cat, teacher, location, programmeTime, status, 0)
                refreshKey++
            },
            viewModel = viewModel
        )
    }

    val adminScreens = listOf(
        NavigationItems.AdminHome,
        NavigationItems.AdminFiles,
        NavigationItems.AdminUpload,
        NavigationItems.AdminCourses,
        NavigationItems.AdminInventory
    )



    LaunchedEffect(refreshKey) {
        viewModel.fetchCourses(context)
    }

    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = adminScreens)
        },
        containerColor = PinkBackground
    ){innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(PinkBackground)
                .padding(innerPadding)
                .padding(16.dp)
        ){
            Text(
                text = "System Course Management",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = modifier.height(10.dp))

            Text(
                text = "Review and manage all courses across the platform",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = {showCourseDialog = true},
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
            ){
                Text(
                    text = "Add System Course",
                    color = Color.White
                )
            }

            Spacer(modifier = modifier.height(25.dp))

            Text(
                text = "All Platform Courses",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = modifier.height(10.dp))

            if (isLoading) {
                LoadingState()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else if (courses.isEmpty()) {
                EmptyState(
                    message = "No Courses Found",
                    subMessage = "There are currently no courses in the system."
                )
            } else {
                var editingCourse by remember { mutableStateOf<CourseData?>(null) }
                
                if (editingCourse != null) {
                    EditCourseDialog(
                        course = editingCourse!!,
                        onDismiss = { editingCourse = null },
                        onUpdateClick = { title, desc, cat, loc ->
                            viewModel.editCourse(
                                context, 
                                editingCourse!!.id, 
                                title, 
                                desc, 
                                cat, 
                                editingCourse!!.teacherId ?: 0,
                                loc,
                                editingCourse!!.programmeTime ?: "",
                                editingCourse!!.status
                            )
                            editingCourse = null
                        }
                    )
                }

                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    items(courses){course ->
                        AdminCourseCard(
                            course = course,
                            onEditClick = { editingCourse = course }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onCreateClick: (title: String, desc: String, cat: String, teacherId: Int, loc: String, time: String, status: String) -> Unit,
    viewModel: UsersViewModel
){
    val context = LocalContext.current
    var courseName by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }
    var courseCategory by remember { mutableStateOf("") }

    var courseLocation by remember { mutableStateOf("") }
    var courseTime by remember { mutableStateOf("") }
    
    val allUsers by viewModel.allUsers
    val isLoadingUsers by viewModel.isLoading
    
    val educators = allUsers.filter { 
        it.role?.lowercase() == "educator" || it.role?.lowercase() == "teacher" 
    }
    
    var selectedTeacherName by remember { mutableStateOf("Select Teacher") }
    var selectedTeacherId by remember { mutableStateOf<Int?>(null) }
    var teacherExpanded by remember { mutableStateOf(false) }

    var courseStatus by remember { mutableStateOf("Ongoing") }
    var statusExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Ongoing", "Upcoming", "Completed")

    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers(context)
    }

    Dialog(onDismissRequest = {onDismiss()}){
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ){
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Add New Course",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = courseName,
                    onValueChange = {courseName = it},
                    label = { Text("Course Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = courseDescription,
                    onValueChange = {courseDescription = it},
                    label = { Text("Course Description") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(95.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = courseCategory,
                    onValueChange = {courseCategory = it},
                    label = { Text("Course Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = courseLocation,
                    onValueChange = {courseLocation = it},
                    label = { Text("Course Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Assign Teacher Dropdown
                ExposedDropdownMenuBox(
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = !teacherExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedTeacherName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign Teacher") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = PinkBackground,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = teacherExpanded,
                        onDismissRequest = { teacherExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Select Teacher") },
                            onClick = {
                                teacherExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                        
                        if (isLoadingUsers) {
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Loading teachers...")
                                    }
                                },
                                onClick = { },
                                enabled = false
                            )
                        } else if (educators.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No teachers found", color = Color.Gray) },
                                onClick = { },
                                enabled = false
                            )
                        } else {
                            educators.forEach { teacher ->
                                DropdownMenuItem(
                                    text = { Text(teacher.name ?: "Unknown") },
                                    onClick = {
                                        selectedTeacherName = teacher.name ?: "Unknown"
                                        selectedTeacherId = teacher.id
                                        teacherExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = courseTime,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            courseTime = input
                        }
                    },
                    label = { Text("Course Time (hours)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Course Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = courseStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = PinkBackground,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    courseStatus = option
                                    statusExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    TextButton(onClick = { onDismiss() }){
                        Text("Cancel", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if(courseName.isNotBlank() && courseDescription.isNotBlank() && courseCategory.isNotBlank()){
                                android.util.Log.d("ADMIN_COURSES", "SENDING: TeacherId=$selectedTeacherId, Status=$courseStatus")
                                onCreateClick(
                                    courseName, 
                                    courseDescription, 
                                    courseCategory, 
                                    selectedTeacherId ?: 0, 
                                    courseLocation, 
                                    courseTime, 
                                    courseStatus
                                )
                            }
                            refreshKey++
                        },
                        enabled = courseName.isNotBlank() && courseDescription.isNotBlank() && courseCategory.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                    ){
                        Text("Add Course", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCourseCard(
    course: CourseData,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = viewModel()
) {
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    if(showDeleteDialog){
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Course", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure? This action cannot be undone and you will lose all your data.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCourse(context, course.id)
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Course Icon Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PinkSoftCard,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Book,
                            contentDescription = null,
                            tint = PinkPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Course Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Course Category: ${course.category}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEditClick() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Edit", fontSize = 12.sp)
                }
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EditCourseDialog(
    course: CourseData,
    onDismiss: () -> Unit,
    onUpdateClick: (title: String, desc: String, cat: String, loc: String) -> Unit
) {
    var courseName by remember { mutableStateOf(course.title) }
    var courseDescription by remember { mutableStateOf(course.description) }
    var courseCategory by remember { mutableStateOf(course.category) }
    var courseLocation by remember { mutableStateOf(course.location ?: "") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Course",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = courseDescription,
                    onValueChange = { courseDescription = it },
                    label = { Text("Course Description") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(95.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = courseCategory,
                    onValueChange = { courseCategory = it },
                    label = { Text("Course Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = courseLocation,
                    onValueChange = { courseLocation = it },
                    label = { Text("Course Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (courseName.isNotBlank() && courseDescription.isNotBlank() && courseCategory.isNotBlank()) {
                                onUpdateClick(courseName, courseDescription, courseCategory, courseLocation)
                            }
                        },
                        enabled = courseName.isNotBlank() && courseDescription.isNotBlank() && courseCategory.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                    ) {
                        Text("Update Course", color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminCoursesScreenPreview(){
    AdminCoursesScreen(modifier = Modifier, navController = NavController(LocalContext.current))
}