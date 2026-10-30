package com.example.thepinkforum

import android.util.Patterns
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.network.AuthViewModel
import com.example.thepinkforum.ui.theme.*
@Composable
fun RegisterScreen(
    navController: NavController,
    userRole: String, // "learner", "educator", or "admin"
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Input States
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation Error States
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // ViewModel States
    val isLoading by authViewModel.isLoading
    val apiErrorMessage by authViewModel.errorMessage
    val registerSuccess by authViewModel.registerSuccess
    val verificationRequired by authViewModel.verificationRequired

    // Navigation on Success
    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            val pendingUserId = authViewModel.pendingUserId.value
            if (verificationRequired && pendingUserId != null) {
                Toast.makeText(context, "Check your email for the verification code.", Toast.LENGTH_LONG).show()
                val userId = pendingUserId
                authViewModel.resetState()
                navController.navigate("verify_email/$userId/$userRole") {
                    popUpTo("register/$userRole") { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                authViewModel.resetState()
                navController.navigate("login/$userRole") {
                    popUpTo("register/$userRole") { inclusive = true }
                }
            }
        }
    }

    // Validation Logic
    fun validateInputs(): Boolean {
        var isValid = true
        nameError = null
        emailError = null
        passwordError = null

        val specialCharRegex = Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?]")

        if (name.isBlank()) { nameError = "Name is required"; isValid = false }

        if (email.isBlank()) {
            emailError = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid email format"
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else {
            val hasUppercase = password.any { it.isUpperCase() }
            val hasLowercase = password.any { it.isLowerCase() }
            val hasDigit = password.any { it.isDigit() }
            val hasSpecialChar = specialCharRegex.containsMatchIn(password)
            val isLongEnough = password.length >= 8

            if (!isLongEnough || !hasUppercase || !hasLowercase || !hasDigit || !hasSpecialChar) {
                passwordError = "Password must be 8+ chars, with uppercase, lowercase, number, and symbol"
                isValid = false
            }
        }
        return isValid
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
                    text = "${userRole.replaceFirstChar { it.uppercase() }} Registration",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(24.dp))

                PinkInputField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = "Name",
                    placeholder = "Enter Your Name",
                    errorMessage = nameError
                )


                PinkInputField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = "Email",
                    placeholder = "Enter Your Email",
                    keyboardType = KeyboardType.Email,
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

                // Show API Errors (e.g., "Email already exists")
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
                                authViewModel.register(
                                    context, 
                                    name.trim(),
                                    email.trim(), 
                                    password.trim(), 
                                    userRole
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Register", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { navController.navigate("login/$userRole") }) {
                    Text("Already have an account? Log In", color = PinkDark, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PinkInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray, fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = errorMessage != null, // Turns border red if there is an error
            supportingText = {
                if (errorMessage != null) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { onPasswordToggle?.invoke() }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = PinkPrimary
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PinkPrimary,
                unfocusedBorderColor = FieldBorder,
                focusedContainerColor = PinkBackground,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
