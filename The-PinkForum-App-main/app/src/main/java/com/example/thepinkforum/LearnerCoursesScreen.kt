package com.example.thepinkforum

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.network.CourseData
import com.example.thepinkforum.network.UsersViewModel
import com.example.thepinkforum.ui.EmptyState
import com.example.thepinkforum.ui.LoadingState
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkDark
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.PinkSoftCard
import com.example.thepinkforum.ui.theme.TextDark


@Composable
fun LearnerCoursesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = viewModel()
) {
    val context = LocalContext.current
    val courses by viewModel.courses

    val enrollments by viewModel.enrolments
    val isLoading by viewModel.isLoading

    val errorMessage by viewModel.errorMessage
    val enrolledSuccess by viewModel.successMessage

    var filterType by remember { mutableStateOf("All") } // "All" or "My"
    var isSortAscending by remember { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchCourses(context)
        viewModel.getEnrolledCourses(context)

        if(enrolledSuccess != null){
            Toast.makeText(context, "Course enrolled successfully!", Toast.LENGTH_SHORT).show()
            viewModel.successMessage.value = null
        }
    }

    val studentScreens = listOf(
        NavigationItems.StudentHome,
        NavigationItems.StudentFiles,
        NavigationItems.StudentCourses,
        NavigationItems.StudentDownloaded
    )

    // Dummy course list matching your backend structure
    // (We will now use the courses from viewModel)
    
    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = studentScreens)
        },
        containerColor = PinkBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(PinkBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Courses",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Learning",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.weight(1f))

                Card(
                    onClick = { isSortAscending = !isSortAscending },
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.SortByAlpha,
                            contentDescription = "Sort Icon",
                            tint = if (isSortAscending) PinkPrimary else Color.Black,
                        )
                    }
                }
            }

            // Continue Learning Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PinkPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Arrow Down Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Continue Learning",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Module: Computer Basics",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Navigate to ongoing module */ },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(text = "Continue", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedIconButton(
                    onClick = { filterType = "All" },
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = if (filterType == "All") Color.White else TextDark,
                        containerColor = if (filterType == "All") PinkPrimary else PinkSoftCard
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.6.dp, SolidColor(PinkDark)),
                ) {
                    Text(text = "All Courses", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                OutlinedIconButton(
                    onClick = { filterType = "My" },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (filterType == "My") Color.White else TextDark,
                        containerColor = if (filterType == "My") PinkPrimary else PinkSoftCard
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.6.dp, SolidColor(PinkDark))
                ) {
                    Text(text = "My Courses", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Replace single static Card with LazyColumn to render course list
            if (isLoading) {
                LoadingState()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else {
                val displayList = if (filterType == "My") {
                    // Filter the master list of courses by comparing IDs with the enrollment list
                    val enrolledIds = enrollments.map { it.id }.toSet()
                    courses.filter { it.id in enrolledIds }
                } else {
                    courses
                }

                val filteredCourses = if (isSortAscending) {
                    displayList.sortedBy { it.title }
                } else {
                    displayList.sortedByDescending { it.title }
                }

                if (filteredCourses.isEmpty()) {
                    EmptyState(
                        message = if (filterType == "My") "No Enrolled Courses" else "No Courses Found",
                        subMessage = if (filterType == "My") "Enroll in a module to see it here." else "Start your journey by enrolling in a new module."
                    )
                } else {
                    val enrolledIds = enrollments.map { it.id }.toSet()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCourses) { course ->
                            CourseCard(
                                course = course,
                                isEnrolled = course.id in enrolledIds,
                                onEnrollClick = { courseId ->
                                    viewModel.courseEnrol(context, course.id)
                                },
                                onContinueClick = { courseId ->
                                    // TODO: Navigate to specific course screen
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(
    course: CourseData,
    isEnrolled: Boolean,
    onEnrollClick: (String) -> Unit,
    onContinueClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var courseButton by remember { mutableStateOf(false) }
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
                        text = "Category: ${course.category ?: "General"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isEnrolled) PinkSoftCard else Color.LightGray.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (isEnrolled) PinkPrimary.copy(alpha = 0.3f) else Color.Transparent)
                    ) {
                        Text(
                            text = if (isEnrolled) "Enrolled" else (course.status ?: "Active"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnrolled) PinkPrimary else Color.Gray,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isEnrolled) {
                Button(
                    onClick = { onContinueClick(course.id.toString()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Continue Learning", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                OutlinedButton(
                    onClick = { onEnrollClick(course.id.toString()) },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, PinkPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White, containerColor = PinkPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Enroll Now", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LearnerCoursesScreenPreview() {
    LearnerCoursesScreen(navController = NavController(LocalContext.current))
}