package com.example.thepinkforum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.TextDark
import com.example.thepinkforum.ui.theme.TextMuted


@Composable
fun LoginAsScreen(
    onRoleSelected: (String) -> Unit
){
    var selectedRole by remember { mutableStateOf("student") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PinkBackground)
            .padding(24.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login to The Pink Forum",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select how you would like to login",
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

            Spacer(modifier = Modifier.height(16.dp))

            RoleSelectionCard(
                title = "Admin",
                description = "Manage everything from educational resources to users",
                isSelected = selectedRole == "admin",
                onClick = { selectedRole = "admin" }
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