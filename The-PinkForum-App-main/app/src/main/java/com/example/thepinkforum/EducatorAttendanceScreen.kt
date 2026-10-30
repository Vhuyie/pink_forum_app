package com.example.thepinkforum

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.navigation.NavController
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.PinkSoftCard

// --- Data Models (Ready for API Integration) ---
data class AttendanceSessionData(
    val sessionId: String,
    val moduleName: String,
    val date: String,
    val presentCount: Int,
    val totalLearners: Int
)

data class LearnerAttendanceData(
    val recordId: String,
    val learnerName: String,
    val status: String // "Present", "Absent", or "Late"
)

@Composable
fun EducatorAttendanceScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Standard Educator Navigation Bottom Bar Tabs
    val educatorScreens = listOf(
        NavigationItems.EducatorHome,
        NavigationItems.EducatorFiles,
        NavigationItems.EducatorUpload,
        NavigationItems.EducatorCourses,
        NavigationItems.EducatorInventory
    )

    // State to track if we are viewing the sessions list or a specific session's details
    var selectedSession by remember { mutableStateOf<AttendanceSessionData?>(null) }

    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = educatorScreens)
        },
        containerColor = PinkBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedSession == null) {
                // View 1: List of all sessions
                SessionListView(
                    onSessionClick = { session -> selectedSession = session }
                )
            } else {
                // View 2: Specific learners in the clicked session
                LearnerRecordsView(
                    session = selectedSession!!,
                    onBackClick = { selectedSession = null }
                )
            }
        }
    }
}

@Composable
fun SessionListView(onSessionClick: (AttendanceSessionData) -> Unit) {
    // Dummy Data: Replace with HTTP GET request to handleAttendanceSessionAPI later
    val sessions = listOf(
        AttendanceSessionData("1", "Intro to Web Safety", "16 Aug 2026", 18, 20),
        AttendanceSessionData("2", "Advanced Financial Literacy", "14 Aug 2026", 20, 20),
        AttendanceSessionData("3", "Computer Basics", "10 Aug 2026", 15, 20)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attendance",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Button to trigger handleAttendanceRegisterAPI (POST)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recent Sessions",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sessions) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSessionClick(session) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PinkSoftCard,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = PinkPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.moduleName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = session.date,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${session.presentCount}/${session.totalLearners}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PinkPrimary
                            )
                            Text(
                                text = "Present",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LearnerRecordsView(
    session: AttendanceSessionData,
    onBackClick: () -> Unit
) {
    // Dummy Data: Replace with HTTP GET request for specific session records later
    val records = listOf(
        LearnerAttendanceData("101", "Alice Johnson", "Present"),
        LearnerAttendanceData("102", "Michael Smith", "Present"),
        LearnerAttendanceData("103", "Sarah Williams", "Absent"),
        LearnerAttendanceData("104", "David Brown", "Late")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = session.moduleName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = session.date,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Records List
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.LightGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = record.learnerName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )

                        // Status Badge
                        val (statusColor, statusIcon) = when (record.status) {
                            "Present" -> Color(0xFF4CAF50) to Icons.Default.CheckCircle // Green
                            "Absent" -> Color(0xFFF44336) to Icons.Default.Cancel // Red
                            else -> Color(0xFFFF9800) to Icons.Default.CheckCircle // Orange for Late
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = statusColor.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = record.status,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
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
fun EducatorAttendancePreview() {
    EducatorAttendanceScreen(navController = NavController(LocalContext.current))
}