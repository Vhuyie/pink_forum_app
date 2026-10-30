package com.example.thepinkforum

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thepinkforum.ui.theme.*
@Composable
fun RegisterAsScreen(
    onRoleSelected: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf("student") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PinkBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Join The Pink Forum",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select how you would like to register",
                fontSize = 14.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Role Card 1: Student
            RoleSelectionCard(
                title = "Student",
                description = "Access educational resources and community programs",
                isSelected = selectedRole == "student",
                onClick = { selectedRole = "student" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role Card 2: Educator
            RoleSelectionCard(
                title = "Educator",
                description = "Provide training, manage courses and support learners",
                isSelected = selectedRole == "educator",
                onClick = { selectedRole = "educator" }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Continue Button
            Button(
                onClick = { onRoleSelected(selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RoleSelectionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PinkSoftCard else Color.White
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) PinkPrimary else FieldBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row{
                Icon(
                    painter = painterResource(id = R.drawable.account),
                    contentDescription = "user icon",
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) PinkDark else TextDark
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = TextMuted
            )
        }
    }
}