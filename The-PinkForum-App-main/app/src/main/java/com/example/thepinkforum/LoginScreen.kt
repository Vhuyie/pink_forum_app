package com.example.thepinkforum

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.thepinkforum.network.AuthViewModel
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkDark
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.TextDark


@Composable
fun LoginScreen(
    navController: NavController,
    userRole: String,
    authViewModel: AuthViewModel = viewModel()
){
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val isLoading by authViewModel.isLoading
    val apiErrorMessage by authViewModel.errorMessage
    val loginSuccess by authViewModel.loginSuccess

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            authViewModel.resetState()

            val destination = when (userRole) {
                "educator" -> "educator_home"
                "admin" -> "admin_home"
                else -> "student_home"
            }

            navController.navigate(destination) {
                popUpTo("login/$userRole") { inclusive = true }
            }
        }
    }

    fun validateInputs(): Boolean {
        var isValid = true
        emailError = null
        passwordError = null

        if (email.isBlank()) { emailError = "Email is required"; isValid = false }
        if (password.isBlank()) { passwordError = "Password is required"; isValid = false }

        return isValid
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PinkBackground)
            .padding(20.dp)
    ){
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth() // Changed from fixed size to fillMaxWidth so errors don't clip
                .align(Alignment.Center)
        ){
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "${userRole.replaceFirstChar { it.uppercase() }} Login",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(24.dp))

                PinkInputField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = "Email",
                    placeholder = "Enter Your Email",
                    errorMessage = emailError
                )

                PinkInputField(
                    value = password,
                    onValueChange = { password = it; passwordError = null },
                    label = "Password",
                    placeholder = "Enter Your Password",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible },
                    errorMessage = passwordError
                )

                Spacer(modifier = Modifier.height(16.dp))

                apiErrorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isLoading) {
                    CircularProgressIndicator(color = PinkPrimary)
                } else {
                    Button(
                        onClick = {
                            if (validateInputs()) {
                                authViewModel.login(
                                    context, 
                                    email.trim(), 
                                    password.trim(), 
                                    userRole
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ){
                        Text("Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if(userRole == "student" || userRole == "educator"){
                    TextButton(onClick = { navController.navigate("register/$userRole") }) {
                        Text("Don't have an account? Register", color = PinkDark, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}