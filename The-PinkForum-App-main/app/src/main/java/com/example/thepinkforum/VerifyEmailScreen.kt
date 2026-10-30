package com.example.thepinkforum

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.network.AuthViewModel
import com.example.thepinkforum.ui.theme.*

@Composable
fun VerifyEmailScreen(
    userId: Int,
    userRole: String,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    var code by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }

    val isLoading by authViewModel.isLoading
    val apiErrorMessage by authViewModel.errorMessage
    val verifySuccess by authViewModel.verifySuccess

    LaunchedEffect(verifySuccess) {
        if (verifySuccess) {
            Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_SHORT).show()
            authViewModel.resetState()
            navController.navigate("login/$userRole") {
                popUpTo("verify_email/$userId/$userRole") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PinkBackground)
            .padding(20.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Email Verification",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Please enter the 4-digit code sent to your email address to complete registration.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Verification Code",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { newValue ->
                            if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                code = newValue
                                codeError = null
                            }
                        },
                        placeholder = { Text("1234", color = Color.LightGray, fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = codeError != null,
                        supportingText = {
                            if (codeError != null) {
                                Text(text = codeError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkPrimary,
                            unfocusedBorderColor = FieldBorder,
                            focusedContainerColor = PinkBackground,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                apiErrorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isLoading) {
                    CircularProgressIndicator(color = PinkPrimary)
                } else {
                    Button(
                        onClick = {
                            if (code.length != 4) {
                                codeError = "Please enter a valid 4-digit code"
                            } else if (userId <= 0) {
                                Toast.makeText(context, "Invalid user ID. Please try registering again.", Toast.LENGTH_SHORT).show()
                            } else {
                                authViewModel.verifyEmail(context, userId, code)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Verify Code", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    authViewModel.resetState()
                    navController.navigate("login/$userRole") {
                        popUpTo("verify_email/$userId/$userRole") { inclusive = true }
                    }
                }) {
                    Text("Back to Log In", color = PinkDark, fontSize = 14.sp)
                }
            }
        }
    }
}
