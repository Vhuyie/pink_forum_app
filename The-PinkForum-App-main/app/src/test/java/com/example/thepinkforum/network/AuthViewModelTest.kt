package com.example.thepinkforum.network

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: AuthViewModel
    private val apiService = mockk<ApiService>()
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel()
        
        // Mock Android Log
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0
        
        // Mock the ApiClient object to return our mocked ApiService
        mockkObject(ApiClient)
        every { ApiClient.getService(any()) } returns apiService
        
        // Mock NetworkUtils to always return true for internet availability
        mockkObject(NetworkUtils)
        every { NetworkUtils.isInternetAvailable(any()) } returns true
        
        // Mock TokenManager constructor
        mockkConstructor(TokenManager::class)
        every { anyConstructed<TokenManager>().saveToken(any()) } just Runs
        every { anyConstructed<TokenManager>().saveUserId(any()) } just Runs
        every { anyConstructed<TokenManager>().saveUserName(any()) } just Runs
        every { anyConstructed<TokenManager>().saveUserRole(any()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `login success sets loginSuccess to true`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password"
        val role = "student"
        val mockUser = UserData(id = 1, name = "Test User", email = email, role = role)
        val mockResponse = AuthResponse(success = true, message = "Success", token = "fake-token", user = mockUser)
        
        coEvery { 
            apiService.studentLogin(role, any()) 
        } returns Response.success(mockResponse)

        // Act
        viewModel.login(context, email, password, role)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.loginSuccess.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `login failure sets errorMessage`() = runTest {
        // Arrange
        val email = "wrong@example.com"
        val password = "wrong"
        val role = "student"
        val mockResponse = AuthResponse(success = false, message = "Invalid credentials")
        
        coEvery { 
            apiService.studentLogin(role, any()) 
        } returns Response.success(mockResponse)

        // Act
        viewModel.login(context, email, password, role)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertFalse(viewModel.loginSuccess.value)
        assertEquals("Invalid credentials", viewModel.errorMessage.value)
    }

    @Test
    fun `register success sets registerSuccess to true`() = runTest {
        // Arrange
        val name = "New User"
        val email = "new@example.com"
        val password = "Password123!"
        val role = "student"
        val mockResponse = AuthResponse(success = true, message = "User registered")
        
        coEvery { 
            apiService.studentRegister(role, any()) 
        } returns Response.success(mockResponse)

        // Act
        viewModel.register(context, name, email, password, role)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.registerSuccess.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `register failure sets errorMessage`() = runTest {
        // Arrange
        val name = "New User"
        val email = "exists@example.com"
        val password = "Password123!"
        val role = "student"
        val mockResponse = AuthResponse(success = false, message = "Email already exists")
        
        coEvery { 
            apiService.studentRegister(role, any()) 
        } returns Response.success(mockResponse)

        // Act
        viewModel.register(context, name, email, password, role)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertFalse(viewModel.registerSuccess.value)
        assertEquals("Email already exists", viewModel.errorMessage.value)
    }

    @Test
    fun `register success with verification required sets pendingUserId and verificationRequired`() = runTest {
        // Arrange
        val name = "New User"
        val email = "new@example.com"
        val password = "Password123!"
        val role = "student"
        val mockResponse = AuthResponse(
            success = true,
            message = "User registered. Check your email for code.",
            userId = 123,
            verificationRequired = true
        )

        coEvery { 
            apiService.studentRegister(role, any()) 
        } returns Response.success(mockResponse)

        // Act
        viewModel.register(context, name, email, password, role)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.registerSuccess.value)
        assertTrue(viewModel.verificationRequired.value)
        assertEquals(123, viewModel.pendingUserId.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `verifyEmail success sets verifySuccess to true`() = runTest {
        // Arrange
        val userId = 123
        val code = "1234"
        val mockResponse = VerifyEmailResponse(success = true, message = "Email verified successfully.")

        coEvery { 
            apiService.verifyEmail(VerifyEmailRequest(userId, code)) 
        } returns Response.success(mockResponse)

        // Act
        viewModel.verifyEmail(context, userId, code)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.verifySuccess.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `verifyEmail failure sets errorMessage`() = runTest {
        // Arrange
        val userId = 123
        val code = "9999"
        val mockResponse = VerifyEmailResponse(success = false, message = "That code is invalid, expired, or has reached the attempt limit.")

        coEvery { 
            apiService.verifyEmail(VerifyEmailRequest(userId, code)) 
        } returns Response.success(mockResponse)

        // Act
        viewModel.verifyEmail(context, userId, code)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertFalse(viewModel.verifySuccess.value)
        assertEquals("That code is invalid, expired, or has reached the attempt limit.", viewModel.errorMessage.value)
    }
}
