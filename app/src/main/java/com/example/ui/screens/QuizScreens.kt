package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.local.*
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import java.io.File
import java.io.FileOutputStream
import android.content.Intent
import androidx.core.content.FileProvider
import android.net.Uri
import android.os.Environment

// NDC Brand Colors
val NDCGreen = Color(0xFF007A33)   // Rich revolutionary land
val NDCRed = Color(0xFFD22630)     // Sacrificial blood of martyrs
val NDCOffWhite = Color(0xFFF8FAFC)
val NDCCharcoal = Color(0xFF1E293B)  // Premium container
val NDCDarkBg = Color(0xFF0F172A)    // Elegant dark
val NDCGold = Color(0xFFEAB308)      // Victory accent

@Composable
fun NDCLogo(modifier: Modifier = Modifier, size: Int = 80) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(NDCGreen, CircleShape)
            .border(2.dp, NDCGold, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Simple canvas drawing representing umbrella Akatamanso + Eagle
        Canvas(modifier = Modifier.size((size * 0.7f).dp)) {
            // Draw umbrella dome
            drawArc(
                color = Color.White,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
            // Umbrella ribs / handle
            drawLine(
                color = Color.White,
                start = Offset(size.dp.toPx() * 0.35f, size.dp.toPx() * 0.35f),
                end = Offset(size.dp.toPx() * 0.35f, size.dp.toPx() * 0.55f),
                strokeWidth = 6f
            )
            // Rib 2 hook
            drawArc(
                color = Color.White,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 6f),
                topLeft = Offset(size.dp.toPx() * 0.3f, size.dp.toPx() * 0.53f),
                size = androidx.compose.ui.geometry.Size(15f, 15f)
            )
            // Color segments
            drawCircle(color = NDCRed, radius = 8f, center = Offset(size.dp.toPx() * 0.15f, size.dp.toPx() * 0.25f))
            drawCircle(color = NDCGold, radius = 8f, center = Offset(size.dp.toPx() * 0.35f, size.dp.toPx() * 0.10f))
            drawCircle(color = Color.Black, radius = 8f, center = Offset(size.dp.toPx() * 0.55f, size.dp.toPx() * 0.25f))
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onSplashFinished: () -> Unit
) {
    val user by authViewModel.currentUser.collectAsStateWithLifecycle()
    val onboardingCompleted by authViewModel.onboardingCompleted.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(2500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NDCCharcoal),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NDCLogo(size = 120)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "NDC GHANA QUIZ",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )
            Text(
                text = "Unity, Stability, and Development",
                fontSize = 14.sp,
                color = NDCGreen,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = NDCGold,
                strokeWidth = 4.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(
    authViewModel: AuthViewModel,
    onOnboardingFinished: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NDCCharcoal),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    authViewModel.completeOnboarding()
                    onOnboardingFinished()
                }) {
                    Text("SKIP", color = NDCGold, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "OnboardingStep"
            ) { currentStep ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    when (currentStep) {
                        1 -> {
                            NDCLogo(size = 100)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "Welcome Comrade!",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Join the National Democratic Congress quiz league. Test your understanding of Ghana's progressive party history and democratic transitions.",
                                fontSize = 15.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                        2 -> {
                            Icon(Icons.Default.School, contentDescription = "School", tint = NDCGold, modifier = Modifier.size(100.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "How the Quiz Works",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Select a category, unlock access utilizing custom passcode where required, and answer questions within the countdown timer limits.",
                                fontSize = 15.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                        3 -> {
                            Icon(Icons.Default.EmojiEvents, contentDescription = "Prizes", tint = NDCGreen, modifier = Modifier.size(100.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "Compete & Win Prizes",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Earn points, level up on constituency leaderboards, and stand a chance to win handsome prizes sponsored by party leaders and patriots.",
                                fontSize = 15.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // Bottom elements
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Carousel Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(width = if (step == index + 1) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (step == index + 1) NDCGreen else Color.Gray)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            authViewModel.completeOnboarding()
                            onOnboardingFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(if (step == 3) "GET STARTED" else "NEXT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 3. LOGIN SCREEN
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (UserEntity) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdminLogin by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val authError by authViewModel.authError.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NDCCharcoal)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NDCLogo(size = 90)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isAdminLogin) "NDC ADMIN PORTAL" else "NDC QUIZ COMBAT",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Enter your credential details to log in",
                fontSize = 14.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Inputs card
            Card(
                colors = CardDefaults.cardColors(containerColor = NDCDarkBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Admin Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sign in as Admin", color = Color.White, fontSize = 14.sp)
                        Switch(
                            checked = isAdminLogin,
                            onCheckedChange = { isAdminLogin = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NDCGreen,
                                checkedTrackColor = NDCGreen.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        label = { Text("Email or Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("username_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NDCCharcoal,
                            unfocusedContainerColor = NDCCharcoal,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("password_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NDCCharcoal,
                            unfocusedContainerColor = NDCCharcoal,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    authError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = NDCGold,
                            modifier = Modifier.align(Alignment.CenterHorizontally).size(28.dp)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (emailOrPhone.isEmpty() || password.isEmpty()) {
                                    Toast.makeText(context, "Credentials cannot be empty", Toast.LENGTH_SHORT).show()
                                } else {
                                    authViewModel.login(emailOrPhone, password) { user ->
                                        if (isAdminLogin && user.role == "User") {
                                            Toast.makeText(context, "Unauthorized. Admin role required.", Toast.LENGTH_LONG).show()
                                        } else {
                                            onLoginSuccess(user)
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isAdminLogin) NDCRed else NDCGreen),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_button")
                        ) {
                            Text(if (isAdminLogin) "ACCESS ADMIN CONSOLE" else "LOG IN", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register Here", color = NDCGold, fontWeight = FontWeight.SemiBold)
            }
        }
}

// 4. REGISTRATION SCREEN
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: (UserEntity) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val fullName by authViewModel.regFullName.collectAsStateWithLifecycle()
    val email by authViewModel.regEmail.collectAsStateWithLifecycle()
    val phone by authViewModel.regPhone.collectAsStateWithLifecycle()
    val region by authViewModel.regRegion.collectAsStateWithLifecycle()
    val constituency by authViewModel.regConstituency.collectAsStateWithLifecycle()
    val password by authViewModel.regPassword.collectAsStateWithLifecycle()

    val regionsList = authViewModel.regionsList
    var dropdownExpanded by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val authError by authViewModel.authError.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NDCCharcoal)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NDCLogo(size = 80)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Comrade Registration", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Enter details to create your secure ID", fontSize = 14.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = NDCDarkBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = fullName,
                        onValueChange = { authViewModel.regFullName.value = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_name"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = email,
                        onValueChange = { authViewModel.regEmail.value = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_email"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = phone,
                        onValueChange = { authViewModel.regPhone.value = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("reg_phone"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Region Selector Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(region.ifEmpty { "Select Region" }, color = Color.White)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(NDCCharcoal).fillMaxWidth(0.8f)
                        ) {
                            regionsList.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r, color = Color.White) },
                                    onClick = {
                                        authViewModel.regRegion.value = r
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = constituency,
                        onValueChange = { authViewModel.regConstituency.value = it },
                        label = { Text("Constituency") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_constituency"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = password,
                        onValueChange = { authViewModel.regPassword.value = it },
                        label = { Text("Hashed Security Code / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_password"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    authError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = NDCGold,
                            modifier = Modifier.align(Alignment.CenterHorizontally).size(28.dp)
                        )
                    } else {
                        Button(
                            onClick = {
                                authViewModel.register(onRegisterSuccess)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("register_submit_button")
                        ) {
                            Text("CREATE SECURE ID", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already registered? Login", color = NDCGold, fontWeight = FontWeight.SemiBold)
            }
        }
}

// 5. HOME SCREEN
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    quizViewModel: QuizViewModel,
    navController: NavController,
    onNavigateToQuiz: (QuizEntity) -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val categories by quizViewModel.categories.collectAsStateWithLifecycle()
    val quizzes by quizViewModel.quizzes.collectAsStateWithLifecycle()
    val announcements by quizViewModel.announcements.collectAsStateWithLifecycle()
    val sponsors by quizViewModel.sponsors.collectAsStateWithLifecycle()
    val searchQuery by quizViewModel.searchQuery.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Quizzes") }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = NDCDarkBg) {
                NavigationBarItem(
                    selected = activeTab == "Quizzes",
                    onClick = { activeTab = "Quizzes" },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                    label = { Text("Quizzes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NDCGreen,
                        selectedTextColor = NDCGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "Leaderboard",
                    onClick = {
                        activeTab = "Leaderboard"
                        navController.navigate("leaderboard")
                    },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = null) },
                    label = { Text("Leaders") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NDCGreen,
                        selectedTextColor = NDCGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                if (currentUser?.role == "Admin" || currentUser?.role == "Super Admin") {
                    NavigationBarItem(
                        selected = activeTab == "Admin",
                        onClick = {
                            activeTab = "Admin"
                            navController.navigate("admin_dashboard")
                        },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                        label = { Text("Admin") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NDCGreen,
                            selectedTextColor = NDCGreen,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
                NavigationBarItem(
                    selected = activeTab == "Profile",
                    onClick = {
                        activeTab = "Profile"
                        navController.navigate("profile")
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NDCGreen,
                        selectedTextColor = NDCGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        containerColor = NDCDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NDCDarkBg)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NDCCharcoal)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NDCLogo(size = 46)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Akatamanso Portal",
                            fontSize = 13.sp,
                            color = NDCGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.fullName ?: "Guest Comrade",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row {
                    IconButton(onClick = {
                        Toast.makeText(navController.context, "Syncing quiz records with remote Supabase servers...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = NDCGreen)
                    }
                    IconButton(onClick = {
                        authViewModel.logout {
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = NDCRed)
                    }
                }
            }

            // Body
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Global Search Bar
                    TextField(
                        value = searchQuery,
                        onValueChange = { quizViewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search Quizzes, Categories...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NDCCharcoal,
                            unfocusedContainerColor = NDCCharcoal,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Announcements
                val activeAnnouncements = announcements.filter { it.active }
                if (activeAnnouncements.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        Text("PARTY ANNOUNCEMENTS", fontSize = 12.sp, color = NDCGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(activeAnnouncements) { announce ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NDCGreen.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, NDCGreen),
                                    modifier = Modifier.width(280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        if (!announce.imageUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = announce.imageUrl,
                                                contentDescription = "Announcement Image",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(115.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(NDCDarkBg),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        Text(announce.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(announce.content, color = Color.LightGray, fontSize = 12.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)

                                        if (!announce.linkUrl.isNullOrBlank()) {
                                            val currentUriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                            val currentContext = androidx.compose.ui.platform.LocalContext.current
                                            Button(
                                                onClick = {
                                                    val rawUrl = announce.linkUrl
                                                    val cleanUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                                                        "https://$rawUrl"
                                                    } else {
                                                        rawUrl
                                                    }
                                                    try {
                                                        currentUriHandler.openUri(cleanUrl)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(currentContext, "Could not open link", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp)
                                                    .height(34.dp)
                                            ) {
                                                Text(
                                                    text = if (!announce.linkLabel.isNullOrBlank()) announce.linkLabel.uppercase() else "LEARN MORE",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Categories Row
                if (searchQuery.isEmpty()) {
                    item {
                        Text("DISCOVER CATEGORIES", fontSize = 12.sp, color = NDCGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(categories) { cat ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                    modifier = Modifier.width(130.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = cat.categoryImage,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            cat.categoryName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Quizzes List filtered by Search Query
                item {
                    Text("ACTIVE LEAGUE QUIZZES", fontSize = 12.sp, color = NDCGold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                val filteredQuizzes = quizzes.filter {
                    it.active && (it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
                }

                if (filteredQuizzes.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No quizzes found matching search.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(filteredQuizzes) { quiz ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onNavigateToQuiz(quiz) }
                                .testTag("quiz_card_${quiz.id}")
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = quiz.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            quiz.title,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (quiz.accessCode.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = NDCGold, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        quiz.description,
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Timer, contentDescription = null, tint = NDCGold, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("${quiz.timeLimitMinutes} Mins", color = NDCGold, fontSize = 11.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.HelpCenter, contentDescription = null, tint = NDCGreen, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("${quiz.totalQuestions} Questions", color = NDCGreen, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sponsors Row
                if (sponsors.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("SUPPORTED BY OFFICIAL SPONSORS", fontSize = 11.sp, color = NDCGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            items(sponsors) { sp ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(NDCCharcoal, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    AsyncImage(
                                        model = sp.logoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp).clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(sp.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. QUIZ ACCESS SCREEN
@Composable
fun QuizAccessScreen(
    quiz: QuizEntity,
    quizViewModel: QuizViewModel,
    userId: String,
    onCodeValidated: () -> Unit,
    onBack: () -> Unit
) {
    var enteredCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NDCCharcoal)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = NDCGold, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Protected Battle Entry", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "This quiz requires a participant Access Code to compete. Provided by sponsors.",
            color = Color.LightGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = enteredCode,
            onValueChange = { enteredCode = it; codeError = null },
            placeholder = { Text("Enter Access Code") },
            modifier = Modifier.fillMaxWidth().testTag("access_code_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = NDCDarkBg,
                unfocusedContainerColor = NDCDarkBg,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        codeError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val attempts = quizViewModel.getAttemptsForQuizAndUser(quiz.id, userId)
                    if (attempts.size >= quiz.maxAttempts) {
                        codeError = "Maximum attempts reached (${attempts.size}/${quiz.maxAttempts})!"
                    } else if (enteredCode.trim().equals(quiz.accessCode, ignoreCase = true)) {
                        onCodeValidated()
                    } else {
                        codeError = "Incorrect Passcode. Try Again (e.g. NDC2026)"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("validate_code_button")
        ) {
            Text("UNLOCK & COMMENCE BATTLE", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onBack) {
            Text("Return to Camp", color = Color.LightGray)
        }
    }
}

// 7. ACTIVE QUIZ GAME COMPOSABLE
@Composable
fun QuizScreen(
    quizViewModel: QuizViewModel,
    authViewModel: AuthViewModel,
    onNavigateToResults: (score: Int, total: Int, timeUsed: Long) -> Unit,
    onBack: () -> Unit
) {
    val activeQuiz by quizViewModel.activeQuiz.collectAsStateWithLifecycle()
    val questions by quizViewModel.quizQuestions.collectAsStateWithLifecycle()
    val currentIndex by quizViewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val selectedAnswers by quizViewModel.selectedAnswers.collectAsStateWithLifecycle()
    val timeLeft by quizViewModel.timeLeftSeconds.collectAsStateWithLifecycle()
    val warnCount by quizViewModel.cheatAttemptCount.collectAsStateWithLifecycle()

    val currentSessionUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isQuizCompleted by quizViewModel.isQuizCompleted.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val sponsors by quizViewModel.sponsors.collectAsStateWithLifecycle()
    var showSponsorScreen by remember { mutableStateOf(true) }

    LaunchedEffect(activeQuiz, sponsors) {
        if (activeQuiz != null) {
            val sponsor = sponsors.find { it.id == activeQuiz?.sponsorId || (it.name.isNotEmpty() && it.name == activeQuiz?.sponsorName) }
            val hasSponsor = sponsor != null || !activeQuiz?.sponsorName.isNullOrBlank()
            if (!hasSponsor) {
                showSponsorScreen = false
                quizViewModel.startTimer()
            }
        }
    }

    // SCREENSHOT BLOCK: Secure Window Protection
    val activity = context as? Activity
    DisposableEffect(Unit) {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // APP SWITCHING LOBBY DETECTION: pause tracker
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // User switched apps
                quizViewModel.detectAppSwitching()
                Toast.makeText(context, "Anti-Cheat Warning: App switching is restricted!", Toast.LENGTH_LONG).show()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Auto navigate to outcome result
    LaunchedEffect(isQuizCompleted) {
        if (isQuizCompleted) {
            val correct = questions.count { selectedAnswers[it.id] == it.correctAnswer }
            onNavigateToResults(correct, questions.size, ((activeQuiz?.timeLimitMinutes ?: 0) * 60 - timeLeft).toLong())
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NDCCharcoal)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    Text(if (showSponsorScreen) "BATTLE INVITATION" else "ACTIVE BATTLE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (!showSponsorScreen) {
                    // Countdown Timer Design
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(if (timeLeft <= 30) NDCRed else NDCGreen, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        val mins = timeLeft / 60
                        val secs = timeLeft % 60
                        Text(
                            String.format("%02d:%02d", mins, secs),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        containerColor = NDCDarkBg
    ) { innerPadding ->
        if (showSponsorScreen) {
            val sponsor = sponsors.find { it.id == activeQuiz?.sponsorId || (it.name.isNotEmpty() && it.name == activeQuiz?.sponsorName) }
            val sponsorName = sponsor?.name ?: activeQuiz?.sponsorName ?: "NDC Patriot"
            val sponsorLogo = sponsor?.logoUrl ?: activeQuiz?.sponsorLogo ?: ""
            val sponsorDescription = sponsor?.description ?: "Official Sponsor of this Combat Battle Module."

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, NDCGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = NDCGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "OFFICIAL COMBAT SPONSOR",
                            color = NDCGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (sponsorLogo.isNotEmpty()) {
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(sponsorLogo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NDCDarkBg)
                                    .padding(4.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NDCDarkBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = sponsorName,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = sponsorDescription,
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        showSponsorScreen = false
                        quizViewModel.startTimer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "START QUIZ BATTLE",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        } else if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NDCGold)
            }
        } else {
            val currentQuestion = questions[currentIndex]
            val chosenOption = selectedAnswers[currentQuestion.id]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Anti cheating alert row
                if (warnCount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NDCRed.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, NDCRed),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = NDCRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SECURITY WARNING ($warnCount/3): App switching flagged. Zero tolerance for auxiliary aids.", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                // Progress Indicator
                val progress = (currentIndex + 1).toFloat() / questions.size
                LinearProgressIndicator(
                    progress = { progress },
                    color = NDCGreen,
                    trackColor = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Question ${currentIndex + 1} of ${questions.size}",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Question Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            currentQuestion.questionText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 26.sp
                        )

                        if (currentQuestion.imageUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(currentQuestion.imageUrl),
                                contentDescription = "Question Graphic",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NDCDarkBg),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Options list
                        val options = listOf(
                            "A" to currentQuestion.optionA,
                            "B" to currentQuestion.optionB,
                            "C" to currentQuestion.optionC,
                            "D" to currentQuestion.optionD
                        )

                        options.forEach { (code, text) ->
                            val isSelected = chosenOption == code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NDCGreen else NDCDarkBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) NDCGold else Color.Gray.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { quizViewModel.selectAnswer(currentQuestion.id, code) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(if (isSelected) NDCGold else Color.Gray, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(code, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Nav Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { quizViewModel.previousQuestion() },
                        enabled = currentIndex > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = NDCCharcoal)
                    ) {
                        Text("PREVIOUS", color = Color.White)
                    }

                    if (currentIndex == questions.size - 1) {
                        Button(
                            onClick = {
                                quizViewModel.submitQuiz(
                                    autoSubmit = false,
                                    userId = currentSessionUser?.id ?: ""
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NDCGold)
                        ) {
                            Text("SUBMIT QUIZ", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { quizViewModel.nextQuestion() },
                            colors = ButtonDefaults.buttonColors(containerColor = NDCGreen)
                        ) {
                            Text("NEXT", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// 8. RESULTS SCREEN WITH EXPLANATION
@Composable
fun ResultsScreen(
    score: Int,
    total: Int,
    timeUsedSeconds: Long,
    quizViewModel: QuizViewModel,
    onNavigateBackToHome: () -> Unit
) {
    val activeQuiz by quizViewModel.activeQuiz.collectAsStateWithLifecycle()
    val questions by quizViewModel.quizQuestions.collectAsStateWithLifecycle()
    val selectedAnswers by quizViewModel.selectedAnswers.collectAsStateWithLifecycle()

    val percentage = (score.toFloat() / total * 100).toInt()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NDCCharcoal)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("BATTLE OUTCOME ANALYSIS", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = NDCDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                NDCLogo(size = 70)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    activeQuiz?.title ?: "NDC Quiz",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Score Ring / Visual Circular Progress
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(130.dp)) {
                        drawCircle(color = Color.Gray.copy(alpha = 0.2f), style = Stroke(width = 12f))
                        drawArc(
                            color = if (percentage >= 50) NDCGreen else NDCRed,
                            startAngle = -90f,
                            sweepAngle = (360 * (percentage / 100f)),
                            useCenter = false,
                            style = Stroke(width = 12f, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$percentage%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("$score / $total Correct", fontSize = 13.sp, color = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Summary Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = NDCCharcoal)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TIME USED", color = NDCGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${timeUsedSeconds}s", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = NDCCharcoal)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RANK FLAG", color = NDCGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(if (percentage >= 80) "GOLDEN EAGLE" else "PATRIOT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Text("QUESTION DEBRIEF & EXPLANATIONS", fontSize = 13.sp, color = NDCGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Question Explanations List
            items(questions) { qn ->
                val userAns = selectedAnswers[qn.id]
                val correctAns = qn.correctAnswer
                val isCorrect = userAns == correctAns

                Card(
                    colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                    border = BorderStroke(1.dp, if (isCorrect) NDCGreen else NDCRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(qn.questionText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (qn.imageUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(qn.imageUrl),
                                contentDescription = "Question Graphic",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NDCDarkBg),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Your Answer: ${userAns ?: "None"} ${if (isCorrect) "✓" else "✗"}", color = if (isCorrect) NDCGreen else NDCRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        if (!isCorrect) {
                            Text("Correct Answer: $correctAns", color = NDCGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Debrief: ${qn.explanation}",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateBackToHome,
                    colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("RETURN TO BASE", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// 9. LEADERBOARD SCREEN
@Composable
fun LeaderboardScreen(
    quizViewModel: QuizViewModel,
    onBack: () -> Unit
) {
    var filterPeriod by remember { mutableStateOf("Global") } // Global, Weekly, Monthly
    val categories by quizViewModel.categories.collectAsStateWithLifecycle()
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    val leaderboardEntries by quizViewModel.fetchLeaderboard(null, selectedCategoryId, filterPeriod).collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NDCCharcoal)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    Text("NDC QUIZ LEADERBOARD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Icon(Icons.Default.Leaderboard, contentDescription = null, tint = NDCGold)
            }
        },
        containerColor = NDCDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Period selector tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NDCCharcoal)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val periods = listOf("Global", "Weekly", "Monthly")
                periods.forEach { p ->
                    val isSelected = filterPeriod == p
                    Button(
                        onClick = { filterPeriod = p },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) NDCGreen else Color.Transparent
                        )
                    ) {
                        Text(p, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Category filter chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NDCCharcoal)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "FILTER BY CATEGORY",
                    color = NDCGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All categories option
                    item {
                        val isSelected = selectedCategoryId == null
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) NDCGreen else NDCDarkBg,
                                    RoundedCornerShape(32.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NDCGold else Color.Gray.copy(alpha = 0.3f),
                                    RoundedCornerShape(32.dp)
                                )
                                .clickable { selectedCategoryId = null }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "ALL CATEGORIES",
                                style = androidx.compose.ui.text.TextStyle(
                                    color = if (isSelected) Color.White else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Specific categories dynamically
                    items(categories) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) NDCGreen else NDCDarkBg,
                                    RoundedCornerShape(32.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NDCGold else Color.Gray.copy(alpha = 0.3f),
                                    RoundedCornerShape(32.dp)
                                )
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat.categoryName.uppercase(),
                                style = androidx.compose.ui.text.TextStyle(
                                    color = if (isSelected) Color.White else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Entries List
            if (leaderboardEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Diversity3, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No participants recorded for $filterPeriod.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    itemsIndexed(leaderboardEntries) { index, entry ->
                        val isSelf = index == 0 // Styling first entry beautifully
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (index < 3) NDCGreen.copy(alpha = 0.15f) else NDCCharcoal
                            ),
                            border = if (index < 3) BorderStroke(1.dp, NDCGold) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Rank
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = when (index) {
                                                    0 -> NDCGold
                                                    1 -> Color.LightGray
                                                    2 -> Color(0xFFCD7F32) // Bronze
                                                    else -> Color.DarkGray
                                                },
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${index + 1}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(entry.userFullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("${entry.region} • ${entry.constituency}", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${entry.score} Pts", color = NDCGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("${entry.completionTimeSeconds}s", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 10. USER PROFILE & SETTINGS
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    quizViewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val attempts by quizViewModel.attemptsFlow.collectAsStateWithLifecycle(emptyList())

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var editEmail by remember { mutableStateOf(currentUser?.email ?: "") }
    var editRegion by remember { mutableStateOf(currentUser?.region ?: "") }
    var editConstituency by remember { mutableStateOf(currentUser?.constituency ?: "") }
    var editLang by remember { mutableStateOf(currentUser?.languagePreference ?: "English") }

    var isChangingPassword by remember { mutableStateOf(false) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NDCCharcoal)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    Text("COMRADE DOSSIER & PROFILE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Icon(Icons.Default.Badge, contentDescription = null, tint = NDCGold)
            }
        },
        containerColor = NDCDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Profile Avatar Photo URL
                NDCLogo(size = 90)
                Spacer(modifier = Modifier.height(12.dp))
                Text(currentUser?.fullName ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Role: ${currentUser?.role} • Status: ${currentUser?.status}", fontSize = 12.sp, color = NDCGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // User Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = NDCCharcoal)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BATTLES FOUGHT", color = NDCGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${attempts.size}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = NDCCharcoal)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AVG SCORE", color = NDCGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val avg = if (attempts.isNotEmpty()) attempts.map { it.score }.average().toInt() else 0
                            Text("$avg Pts", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (isEditing) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Edit Security Dossier", color = NDCGold, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            TextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            TextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            TextField(
                                value = editConstituency,
                                onValueChange = { editConstituency = it },
                                label = { Text("Constituency") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Dropdowns
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        authViewModel.updateProfile(
                                            fullName = editName,
                                            email = editEmail,
                                            region = editRegion,
                                            constituency = editConstituency,
                                            language = editLang,
                                            photo = ""
                                        )
                                        isEditing = false
                                        Toast.makeText(context, "Dossier Securely Updated", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NDCGreen)
                                ) {
                                    Text("SAVE")
                                }
                                Button(
                                    onClick = { isEditing = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = NDCRed)
                                ) {
                                    Text("CANCEL")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                item {
                    Button(
                        onClick = { isEditing = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("EDIT DOSSIER", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Change Password Dialogue Segment
            if (isChangingPassword) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Update Cryptographic Key", color = NDCGold, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            TextField(
                                value = oldPass,
                                onValueChange = { oldPass = it },
                                label = { Text("Current Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            TextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("New Secure Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row {
                                Button(
                                    onClick = {
                                        authViewModel.changePassword(oldPass, newPass) { res ->
                                            res.fold(
                                                onSuccess = {
                                                    oldPass = ""
                                                    newPass = ""
                                                    isChangingPassword = false
                                                    Toast.makeText(context, "Cryptographic Key Rotated Successfully", Toast.LENGTH_LONG).show()
                                                },
                                                onFailure = {
                                                    Toast.makeText(context, it.message ?: "Rotation Failed", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NDCGreen)
                                ) {
                                    Text("PIN ROTATE")
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = { isChangingPassword = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = NDCRed)
                                ) {
                                    Text("DISMISS")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                item {
                    Button(
                        onClick = { isChangingPassword = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NDCCharcoal),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("ROTATE PASS CODE", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Historical Battles logs
            item {
                Text("HISTORIC BATTLE RECORDS", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (attempts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().background(NDCCharcoal, RoundedCornerShape(12.dp)).padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No historic records found. Head to campsite to begin.", color = Color.Gray)
                    }
                }
            } else {
                items(attempts) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Quiz Code Flag ID", color = NDCGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Quiz ID: ${item.quizId}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Score: ${item.score} Correct", color = NDCGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("${item.completionTimeSeconds}s Used", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun exportUsersToPdf(context: android.content.Context, usersToExport: List<UserEntity>) {
    try {
        val pdfDocument = PdfDocument()
        
        val usersPerPage = 15
        val totalPages = if (usersToExport.isEmpty()) 1 else ((usersToExport.size - 1) / usersPerPage) + 1
        
        val paintText = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 10f
            isAntiAlias = true
        }
        
        val paintTitle = Paint().apply {
            color = AndroidColor.rgb(0, 122, 51) // NDCGreen
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val paintHeader = Paint().apply {
            color = AndroidColor.DKGRAY
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintSub = Paint().apply {
            color = AndroidColor.rgb(234, 179, 8) // NDCGold
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintLine = Paint().apply {
            color = AndroidColor.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        for (p in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, p + 1).create() // standard A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var yOffset = 40f
            if (p == 0) {
                canvas.drawText("NATIONAL DEMOCRATIC CONGRESS - USER CADRE REPORT", 40f, yOffset, paintTitle)
                yOffset += 18f
                canvas.drawText("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}", 40f, yOffset, paintText)
                yOffset += 12f
                canvas.drawText("Total Count: ${usersToExport.size} users listed", 40f, yOffset, paintText)
                yOffset += 24f
            } else {
                canvas.drawText("USER CADRE REPORT (Page ${p + 1} of $totalPages)", 40f, yOffset, paintSub)
                yOffset += 20f
            }
            
            // Draw table columns header
            canvas.drawText("NAME / CONTACT DETAILS", 40f, yOffset, paintHeader)
            canvas.drawText("ROLE", 240f, yOffset, paintHeader)
            canvas.drawText("STATUS", 310f, yOffset, paintHeader)
            canvas.drawText("REGION & CONSTITUENCY", 380f, yOffset, paintHeader)
            
            yOffset += 6f
            canvas.drawLine(40f, yOffset, 555f, yOffset, paintLine)
            yOffset += 16f
            
            val startIdx = p * usersPerPage
            val endIdx = minOf(startIdx + usersPerPage, usersToExport.size)
            
            if (usersToExport.isEmpty()) {
                canvas.drawText("No users available to display.", 40f, yOffset, paintText)
            } else {
                for (i in startIdx until endIdx) {
                    val u = usersToExport[i]
                    
                    // Name
                    paintText.isFakeBoldText = true
                    paintText.textSize = 10f
                    canvas.drawText(u.fullName, 40f, yOffset, paintText)
                    
                    // Email & Phone
                    paintText.isFakeBoldText = false
                    paintText.textSize = 8f
                    canvas.drawText("${u.phoneNumber} • ${u.email}", 40f, yOffset + 11f, paintText)
                    
                    // Role
                    paintText.textSize = 9f
                    canvas.drawText(u.role, 240f, yOffset + 4f, paintText)
                    
                    // Status
                    canvas.drawText(u.status, 310f, yOffset + 4f, paintText)
                    
                    // Region / Constituency
                    canvas.drawText("${u.region} / ${u.constituency}", 380f, yOffset + 4f, paintText)
                    
                    yOffset += 24f
                    canvas.drawLine(40f, yOffset, 555f, yOffset, paintLine)
                    yOffset += 16f
                }
            }
            
            // Draw page number footer
            paintText.isFakeBoldText = false
            paintText.textSize = 8f
            canvas.drawText("Page ${p + 1} of $totalPages", 40f, 810f, paintText)
            
            pdfDocument.finishPage(page)
        }
        
        val fileName = "User_Cadre_Export_${System.currentTimeMillis()}.pdf"
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, fileName)
        
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        
        Toast.makeText(context, "PDF saved to Downloads folder!", Toast.LENGTH_LONG).show()
        
        // Share via Intent
        try {
            val authority = "com.example.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share User Cadre PDF Report"))
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(context, "Location: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun exportLeaderboardToPdf(
    context: android.content.Context,
    leaderboardToExport: List<LeaderboardEntity>,
    quizzes: List<QuizEntity>,
    categories: List<CategoryEntity>,
    filterQuizId: String?,
    filterCategoryId: String?,
    filterRegion: String,
    filterPeriod: String
) {
    try {
        val pdfDocument = PdfDocument()
        
        val itemsPerPage = 15
        val totalPages = if (leaderboardToExport.isEmpty()) 1 else ((leaderboardToExport.size - 1) / itemsPerPage) + 1
        
        val paintText = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 9f
            isAntiAlias = true
        }
        
        val paintTitle = Paint().apply {
            color = AndroidColor.rgb(0, 122, 51) // NDCGreen
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val paintHeader = Paint().apply {
            color = AndroidColor.DKGRAY
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintSub = Paint().apply {
            color = AndroidColor.rgb(234, 179, 8) // NDCGold
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintLine = Paint().apply {
            color = AndroidColor.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val activeQuiz = quizzes.find { it.id == filterQuizId }?.title ?: "All Quizzes"
        val activeCategory = categories.find { it.id == filterCategoryId }?.categoryName ?: "All Categories"

        for (p in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, p + 1).create() // standard A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var yOffset = 40f
            if (p == 0) {
                // Top Header Banner
                canvas.drawText("NATIONAL DEMOCRATIC CONGRESS - E-LEARNING PORTAL", 40f, yOffset, paintTitle)
                yOffset += 18f
                canvas.drawText("OFFICIAL LEADERBOARD & INDIVIDUAL PERFORMANCE AUDIT", 40f, yOffset, paintSub)
                yOffset += 18f
                
                // Active configuration text
                paintText.textSize = 8.5f
                paintText.color = AndroidColor.GRAY
                canvas.drawText("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}", 40f, yOffset, paintText)
                yOffset += 12f
                canvas.drawText("Filters Applied: Quiz ($activeQuiz) | Category ($activeCategory) | Region ($filterRegion) | Timeframe ($filterPeriod)", 40f, yOffset, paintText)
                yOffset += 12f
                canvas.drawText("Total Qualified Records: ${leaderboardToExport.size} entries", 40f, yOffset, paintText)
                yOffset += 24f
            } else {
                canvas.drawText("OFFICIAL LEADERBOARD REPORT (Page ${p + 1} of $totalPages)", 40f, yOffset, paintSub)
                yOffset += 20f
            }
            
            // Draw table columns header
            paintHeader.textSize = 9f
            canvas.drawText("RK", 40f, yOffset, paintHeader)
            canvas.drawText("CANDIDATE / LOCATION", 80f, yOffset, paintHeader)
            canvas.drawText("SCORE", 350f, yOffset, paintHeader)
            canvas.drawText("TIME", 410f, yOffset, paintHeader)
            canvas.drawText("EXAMINATION MODULE", 460f, yOffset, paintHeader)
            
            yOffset += 6f
            canvas.drawLine(40f, yOffset, 555f, yOffset, paintLine)
            yOffset += 16f
            
            val startIdx = p * itemsPerPage
            val endIdx = minOf(startIdx + itemsPerPage, leaderboardToExport.size)
            
            if (leaderboardToExport.isEmpty()) {
                paintText.color = AndroidColor.BLACK
                paintText.textSize = 10f
                canvas.drawText("No leaderboard records available for this filter configuration.", 40f, yOffset, paintText)
            } else {
                for (i in startIdx until endIdx) {
                    val entry = leaderboardToExport[i]
                    val rankNum = i + 1
                    
                    // Rank Badge / Number
                    paintText.isFakeBoldText = true
                    paintText.textSize = 10f
                    paintText.color = when (rankNum) {
                        1 -> AndroidColor.rgb(204, 153, 0) // Gold
                        2 -> AndroidColor.rgb(120, 120, 120) // Silver
                        3 -> AndroidColor.rgb(180, 110, 50) // Bronze
                        else -> AndroidColor.BLACK
                    }
                    canvas.drawText("#$rankNum", 40f, yOffset + 4f, paintText)
                    
                    // Name
                    paintText.isFakeBoldText = true
                    paintText.textSize = 9.5f
                    paintText.color = AndroidColor.BLACK
                    canvas.drawText(entry.userFullName, 80f, yOffset, paintText)
                    
                    // Location: Region & Constituency
                    paintText.isFakeBoldText = false
                    paintText.textSize = 8f
                    paintText.color = AndroidColor.GRAY
                    canvas.drawText("${entry.region} • ${entry.constituency}", 80f, yOffset + 11f, paintText)
                    
                    // Score
                    paintText.isFakeBoldText = true
                    paintText.textSize = 10f
                    paintText.color = AndroidColor.rgb(0, 122, 51) // green for scores
                    canvas.drawText("${entry.score} pts", 350f, yOffset + 4f, paintText)
                    
                    // Time used
                    paintText.isFakeBoldText = false
                    paintText.textSize = 9f
                    paintText.color = AndroidColor.BLACK
                    canvas.drawText("${entry.completionTimeSeconds}s", 410f, yOffset + 4f, paintText)
                    
                    // Quiz title / category
                    paintText.textSize = 8f
                    paintText.color = AndroidColor.BLACK
                    val entryQuizTitle = quizzes.find { it.id == entry.quizId }?.title ?: "Module"
                    val itemDisplay = if (entryQuizTitle.length > 20) entryQuizTitle.take(17) + "..." else entryQuizTitle
                    canvas.drawText(itemDisplay, 460f, yOffset + 4f, paintText)
                    
                    yOffset += 24f
                    paintLine.color = AndroidColor.rgb(240, 240, 240) // lighter grey for row dividers
                    canvas.drawLine(40f, yOffset, 555f, yOffset, paintLine)
                    yOffset += 14f
                }
            }
            
            // Draw page number & disclaimer footer
            paintText.isFakeBoldText = false
            paintText.color = AndroidColor.GRAY
            paintText.textSize = 8f
            canvas.drawText("Page ${p + 1} of $totalPages | Confidential Administrative Record", 40f, 810f, paintText)
            
            pdfDocument.finishPage(page)
        }
        
        val fileName = "NDC_Leaderboard_Export_${System.currentTimeMillis()}.pdf"
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, fileName)
        
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        
        Toast.makeText(context, "PDF saved to Downloads folder!", Toast.LENGTH_LONG).show()
        
        // Share via Intent
        try {
            val authority = "com.example.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Leaderboard Report"))
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(context, "Location: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// 11. RESPONSIVE WEB-INSPIRED ADMIN PORTAL inside App

fun exportAuditLogsToPdf(
    context: android.content.Context,
    logsToExport: List<AuditLogEntity>
) {
    try {
        val pdfDocument = PdfDocument()
        
        val itemsPerPage = 20
        val totalPages = if (logsToExport.isEmpty()) 1 else ((logsToExport.size - 1) / itemsPerPage) + 1
        
        val paintText = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 9f
            isAntiAlias = true
        }
        
        val paintTitle = Paint().apply {
            color = AndroidColor.rgb(0, 122, 51) // NDCGreen
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val paintHeader = Paint().apply {
            color = AndroidColor.DKGRAY
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintLine = Paint().apply {
            color = AndroidColor.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        for (p in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, p + 1).create() // standard A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var yOffset = 40f
            
            if (p == 0) {
                canvas.drawText("AUDIT LOG REPORT", 40f, yOffset, paintTitle)
                yOffset += 20f
                canvas.drawText("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}", 40f, yOffset, paintText)
                yOffset += 20f
            }
            
            // Header
            canvas.drawText("ACTION", 40f, yOffset, paintHeader)
            canvas.drawText("TARGET", 150f, yOffset, paintHeader)
            canvas.drawText("ADMIN", 400f, yOffset, paintHeader)
            canvas.drawText("DATE", 480f, yOffset, paintHeader)
            yOffset += 5f
            canvas.drawLine(40f, yOffset, 555f, yOffset, paintLine)
            yOffset += 15f
            
            val startIdx = p * itemsPerPage
            val endIdx = minOf(startIdx + itemsPerPage, logsToExport.size)
            
            for (i in startIdx until endIdx) {
                val log = logsToExport[i]
                canvas.drawText(log.action, 40f, yOffset, paintText)
                canvas.drawText(log.target, 150f, yOffset, paintText)
                canvas.drawText(log.adminName, 400f, yOffset, paintText)
                canvas.drawText(java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(log.timestamp)), 480f, yOffset, paintText)
                yOffset += 20f
            }
            pdfDocument.finishPage(page)
        }
        
        val fileName = "Audit_Logs_${System.currentTimeMillis()}.pdf"
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, fileName)
        
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        
        Toast.makeText(context, "Audit logs saved to Downloads!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    quizViewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val users by quizViewModel.allUsers.collectAsStateWithLifecycle()
    val categories by quizViewModel.categories.collectAsStateWithLifecycle()
    val quizzes by quizViewModel.quizzes.collectAsStateWithLifecycle()
    val sponsors by quizViewModel.sponsors.collectAsStateWithLifecycle()
    val announcements by quizViewModel.announcements.collectAsStateWithLifecycle()
    val auditLogs by quizViewModel.auditLogs.collectAsStateWithLifecycle()

    var activeAdminTab by remember { mutableStateOf("Analytics") } // Analytics, Quizzes, Users, Sponsors & Categories, Audit

    // Admin Leaderboard states
    var adminLeaderboardSelectedQuizId by remember { mutableStateOf<String?>(null) }
    var adminLeaderboardSelectedCategoryId by remember { mutableStateOf<String?>(null) }
    var adminLeaderboardPeriod by remember { mutableStateOf("Global") } // Global, Weekly, Monthly
    var adminLeaderboardSearchQuery by remember { mutableStateOf("") }
    var adminLeaderboardSelectedRegion by remember { mutableStateOf("All Regions") }
    
    var showConfirmClearAllLeaderboard by remember { mutableStateOf(false) }
    var showConfirmClearQuizLeaderboard by remember { mutableStateOf<String?>(null) }
    var showConfirmClearCategoryLeaderboard by remember { mutableStateOf<String?>(null) }
    var showConfirmDeleteLeaderboardEntryId by remember { mutableStateOf<String?>(null) }
    
    val adminLeaderboardEntries by quizViewModel.fetchLeaderboard(
        adminLeaderboardSelectedQuizId,
        adminLeaderboardSelectedCategoryId,
        adminLeaderboardPeriod
    ).collectAsStateWithLifecycle(emptyList())

    // Dropdown open states
    var adminQuizDropdownExpanded by remember { mutableStateOf(false) }
    var adminCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var adminRegionDropdownExpanded by remember { mutableStateOf(false) }

    // User management filtering & bulk action variables
    var userSearchQuery by remember { mutableStateOf("") }
    var userFilterRole by remember { mutableStateOf("All") } // All, Admin, User
    var userFilterStatus by remember { mutableStateOf("All") } // All, Active, Suspended
    var selectedUserIds by remember { mutableStateOf(setOf<String>()) }
    var showConfirmBulkDelete by remember { mutableStateOf(false) }

    var auditLogSearchQuery by remember { mutableStateOf("") }
    var auditLogSelectedAction by remember { mutableStateOf("All") }
    var auditLogDropdownExpanded by remember { mutableStateOf(false) }

    var configProvider by remember { mutableStateOf("Supabase") }
    var configDbUrl by remember { mutableStateOf("") }
    var configProjectId by remember { mutableStateOf("") }
    
    var addQuizTitle by remember { mutableStateOf("") }
    var addQuizDesc by remember { mutableStateOf("") }
    var addQuizImg by remember { mutableStateOf("") }
    var addQuizSponsor by remember { mutableStateOf("") }
    var addQuizSponsorLogo by remember { mutableStateOf("") }
    var addQuizAccessCode by remember { mutableStateOf("") }
    var addQuizMinutes by remember { mutableStateOf("5") }
    var addQuizQnNum by remember { mutableStateOf("4") }
    var addQuizMaxAttempts by remember { mutableStateOf("1") }
    var addQuizSelectedCat by remember { mutableStateOf("") }
    var addQuizSelectedSponsorId by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var sponsorDropdownExpanded by remember { mutableStateOf(false) }

    // Sponsor creation states lifted for local pickers
    var spName by remember { mutableStateOf("") }
    var spDesc by remember { mutableStateOf("") }
    var spLogo by remember { mutableStateOf("") }

    val quizImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            addQuizImg = it.toString()
        }
    }

    val sponsorLogoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            spLogo = it.toString()
        }
    }

    // CSV imports variables
    var csvText by remember { mutableStateOf("") }
    val cvsPreview by quizViewModel.csvPreviewQuestions.collectAsStateWithLifecycle()
    val cvsErr by quizViewModel.csvImportError.collectAsStateWithLifecycle()

    // Manual question builder states
    var selectedQuizIdForQuestions by remember { mutableStateOf<String?>(null) }
    var manualQnText by remember { mutableStateOf("") }
    var manualQnOptA by remember { mutableStateOf("") }
    var manualQnOptB by remember { mutableStateOf("") }
    var manualQnOptC by remember { mutableStateOf("") }
    var manualQnOptD by remember { mutableStateOf("") }
    var manualQnCorrectAnswer by remember { mutableStateOf("A") }
    var manualQnCorrectDropdownExpanded by remember { mutableStateOf(false) }
    var manualQnExplanation by remember { mutableStateOf("") }
    var manualQnImageUrl by remember { mutableStateOf("") }

    // Broadcast system overlay variables
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastContent by remember { mutableStateOf("") }
    var broadcastImageUrl by remember { mutableStateOf("") }
    var broadcastLinkUrl by remember { mutableStateOf("") }
    var broadcastLinkLabel by remember { mutableStateOf("") }

    val broadcastImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            broadcastImageUrl = it.toString()
        }
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NDCCharcoal)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    Text("CENTRAL CONTROL CONSOLE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NDCRed)
            }
        },
        containerColor = NDCDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dashboard Drawer Selector Row styled beautifully
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NDCCharcoal)
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val tabs = listOf("Analytics", "Quizzes", "Leaderboards", "Broadcast Alerts", "Users", "Sponsors & Categories", "Audit Logs", "Database")
                tabs.forEach { t ->
                    val isSelected = activeAdminTab == t
                    Button(
                        onClick = { activeAdminTab = t },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) NDCRed else Color.Gray.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(t, color = if (isSelected) Color.White else Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Segment containers
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (activeAdminTab) {
                    "Analytics" -> {
                        item {
                            Text("EXECUTIVE METRIC SCORECARDS", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Grid style responsive summaries
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Card(colors = CardDefaults.cardColors(containerColor = NDCCharcoal)) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("TOTAL USERS", color = NDCGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("${users.size}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                item {
                                    Card(colors = CardDefaults.cardColors(containerColor = NDCCharcoal)) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("QUIZ MODULES", color = NDCGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("${quizzes.size}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                item {
                                    Card(colors = CardDefaults.cardColors(containerColor = NDCCharcoal)) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("PARTNERS", color = NDCGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("${sponsors.size}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                item {
                                    Card(colors = CardDefaults.cardColors(containerColor = NDCCharcoal)) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("CATEGORIES", color = NDCGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("${categories.size}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text("REGIONAL PARTICIPATION DENSITY GRAPH", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Custom canvas charts details
                            Card(colors = CardDefaults.cardColors(containerColor = NDCCharcoal)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                    ) {
                                        // Draw base line
                                        drawLine(Color.Gray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 3f)

                                        // Draw some bar entries indicating region user counts
                                        val bars = listOf(140f, 210f, 80f, 170f, 110f)
                                        val barNames = listOf("GAR", "ASH", "WR", "ER", "NR")
                                        val barColor = NDCGreen
                                        val redAccent = NDCRed

                                        val stepX = size.width / 5
                                        bars.forEachIndexed { i, value ->
                                            val rectHeight = value
                                            val x = stepX * i + stepX * 0.2f
                                            val rectColor = if (i % 2 == 0) barColor else redAccent

                                            drawRect(
                                                color = rectColor,
                                                topLeft = Offset(x, size.height - rectHeight),
                                                size = androidx.compose.ui.geometry.Size(stepX * 0.6f, rectHeight)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Text("G. Accra", color = Color.White, fontSize = 10.sp)
                                        Text("Ashanti", color = Color.White, fontSize = 10.sp)
                                        Text("Western", color = Color.White, fontSize = 10.sp)
                                        Text("Eastern", color = Color.White, fontSize = 10.sp)
                                        Text("Northern", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    "Quizzes" -> {
                        item {
                            Text("ESTABLISH NEW BATTLE MODULE", fontWeight = FontWeight.Bold, color = NDCGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Configure high-quality educational challenges for your users.", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Main wrapped container card for clean form structure
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal.copy(alpha = 0.45f)),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    
                                    // SECTION 1: IDENTITY
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = NDCGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("1. IDENTITY & DETAILS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    TextField(
                                        value = addQuizTitle,
                                        onValueChange = { addQuizTitle = it },
                                        label = { Text("Quiz Title") },
                                        placeholder = { Text("e.g. Modern Web Development") },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = NDCDarkBg,
                                            unfocusedContainerColor = NDCDarkBg,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedLabelColor = NDCGold,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedIndicatorColor = NDCGreen,
                                            unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    TextField(
                                        value = addQuizDesc,
                                        onValueChange = { addQuizDesc = it },
                                        label = { Text("Short Description") },
                                        placeholder = { Text("Provide clear guidance of what this quiz covers...") },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = NDCDarkBg,
                                            unfocusedContainerColor = NDCDarkBg,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedLabelColor = NDCGold,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedIndicatorColor = NDCGreen,
                                            unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        maxLines = 3
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // SECTION 2: PARAMETERS & SECURITY
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = NDCGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("2. PARAMETERS & ACCESS CONTROL", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        TextField(
                                            value = addQuizMinutes,
                                            onValueChange = { addQuizMinutes = it },
                                            label = { Text("Duration (Mins)") },
                                            placeholder = { Text("10") },
                                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = NDCDarkBg,
                                                unfocusedContainerColor = NDCDarkBg,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedLabelColor = NDCGold,
                                                unfocusedLabelColor = Color.Gray,
                                                focusedIndicatorColor = NDCGreen,
                                                unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        TextField(
                                            value = addQuizQnNum,
                                            onValueChange = { addQuizQnNum = it },
                                            label = { Text("Total Qns") },
                                            placeholder = { Text("5") },
                                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = NDCDarkBg,
                                                unfocusedContainerColor = NDCDarkBg,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedLabelColor = NDCGold,
                                                unfocusedLabelColor = Color.Gray,
                                                focusedIndicatorColor = NDCGreen,
                                                unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        TextField(
                                            value = addQuizMaxAttempts,
                                            onValueChange = { addQuizMaxAttempts = it },
                                            label = { Text("Max Attempts") },
                                            placeholder = { Text("1") },
                                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = NDCDarkBg,
                                                unfocusedContainerColor = NDCDarkBg,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedLabelColor = NDCGold,
                                                unfocusedLabelColor = Color.Gray,
                                                focusedIndicatorColor = NDCGreen,
                                                unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        value = addQuizAccessCode,
                                        onValueChange = { addQuizAccessCode = it },
                                        label = { Text("Access Passcode") },
                                        placeholder = { Text("Leave blank if open for public access") },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = NDCDarkBg,
                                            unfocusedContainerColor = NDCDarkBg,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedLabelColor = NDCGold,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedIndicatorColor = NDCGreen,
                                            unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // SECTION 3: CATEGORY & PARTNER BRANDING
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.List, contentDescription = null, tint = NDCGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("3. CATEGORIZATION & BRANDING", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Interactive Category Horizontal Scroller
                                    Text("SELECT THE CATEGORY *", fontWeight = FontWeight.Bold, color = NDCGold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (categories.isEmpty()) {
                                        Text("No categories available. Please create one.", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                                    } else {
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(categories) { cat ->
                                                val isSelected = addQuizSelectedCat == cat.id
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (isSelected) NDCGreen.copy(alpha = 0.15f) else NDCDarkBg, RoundedCornerShape(12.dp))
                                                        .border(
                                                            width = 1.5.dp,
                                                            color = if (isSelected) NDCGreen else Color.Gray.copy(alpha = 0.3f),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable { addQuizSelectedCat = cat.id }
                                                        .padding(horizontal = 14.dp, vertical = 9.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = NDCGreen, modifier = Modifier.size(13.dp))
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                        }
                                                        Text(
                                                            text = cat.categoryName,
                                                            color = if (isSelected) Color.White else Color.LightGray,
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Interactive Sponsor Selector Scroller
                                    Text("SELECT SPONSOR PARTNER (OPTIONAL)", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Option for None
                                        item {
                                            val isSelected = addQuizSelectedSponsorId.isEmpty()
                                            Box(
                                                modifier = Modifier
                                                    .background(if (isSelected) NDCGreen.copy(alpha = 0.15f) else NDCDarkBg, RoundedCornerShape(12.dp))
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (isSelected) NDCGreen else Color.Gray.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable {
                                                        addQuizSelectedSponsorId = ""
                                                        addQuizSponsor = ""
                                                        addQuizSponsorLogo = ""
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (isSelected) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = NDCGreen, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    Text(
                                                        text = "NO SPONSOR",
                                                        color = if (isSelected) Color.White else Color.LightGray,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }

                                        items(sponsors) { sp ->
                                            val isSelected = addQuizSelectedSponsorId == sp.id
                                            Box(
                                                modifier = Modifier
                                                    .background(if (isSelected) NDCGreen.copy(alpha = 0.15f) else NDCDarkBg, RoundedCornerShape(12.dp))
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (isSelected) NDCGreen else Color.Gray.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable {
                                                        addQuizSelectedSponsorId = sp.id
                                                        addQuizSponsor = sp.name
                                                        addQuizSponsorLogo = sp.logoUrl
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (!sp.logoUrl.isNullOrBlank()) {
                                                        AsyncImage(
                                                            model = sp.logoUrl,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp).clip(RoundedCornerShape(2.dp))
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    if (isSelected) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = NDCGreen, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    Text(
                                                        text = sp.name,
                                                        color = if (isSelected) Color.White else Color.LightGray,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Banner Image Upload/Viewer
                                    Text("BATTLE MODULE BANNER IMAGE (OPTIONAL)", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = NDCDarkBg),
                                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            if (addQuizImg.isNotEmpty()) {
                                                AsyncImage(
                                                    model = addQuizImg,
                                                    contentDescription = "Selected Banner",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(115.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(NDCCharcoal),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { quizImagePickerLauncher.launch("image/*") },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NDCGreen),
                                                        border = BorderStroke(1.dp, NDCGreen),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Change", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    OutlinedButton(
                                                        onClick = { addQuizImg = "" },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NDCRed),
                                                        border = BorderStroke(1.dp, NDCRed),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Remove", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("No image uploaded", color = Color.Gray, fontSize = 11.sp)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Button(
                                                    onClick = { quizImagePickerLauncher.launch("image/*") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Upload Banner", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Submit Button
                                    Button(
                                        onClick = {
                                            if (addQuizTitle.trim().isEmpty() || addQuizSelectedCat.isEmpty()) {
                                                Toast.makeText(context, "Fill in the quiz title and select a category", Toast.LENGTH_SHORT).show()
                                            } else {
                                                quizViewModel.createQuiz(
                                                    title = addQuizTitle.trim(),
                                                    desc = addQuizDesc.trim(),
                                                    imageUrl = addQuizImg,
                                                    sponsor = addQuizSponsor,
                                                    sponsorLogo = addQuizSponsorLogo,
                                                    categoryId = addQuizSelectedCat,
                                                    accessCode = addQuizAccessCode.trim(),
                                                    timeLimit = addQuizMinutes.toIntOrNull() ?: 5,
                                                    totalQuests = addQuizQnNum.toIntOrNull() ?: 5,
                                                    sponsorId = addQuizSelectedSponsorId,
                                                    maxAttempts = addQuizMaxAttempts.toIntOrNull() ?: 1
                                                )
                                                addQuizTitle = ""
                                                addQuizDesc = ""
                                                addQuizImg = ""
                                                addQuizSelectedCat = ""
                                                addQuizSelectedSponsorId = ""
                                                addQuizSponsor = ""
                                                addQuizSponsorLogo = ""
                                                addQuizMinutes = "5"
                                                addQuizQnNum = "5"
                                                addQuizMaxAttempts = "1"
                                                addQuizAccessCode = ""
                                                Toast.makeText(context, "League Quiz Registered Securely", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(44.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ESTABLISH BATTLE MODULE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    }

                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(24.dp))

                            // CSV & Manual Questions Manager
                            Text("MANAGE BATTLE QUESTIONS & CONTENT", fontWeight = FontWeight.Bold, color = NDCGold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            if (selectedQuizIdForQuestions == null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = NDCGold, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No Battle Module Selected For Questions", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Please click the \"Manage\" icon (the wrench/build) next to any quiz in the \"CURRENT REGISTERED BATTLES\" list below to manually add, edit, or import questions.", color = Color.LightGray, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            } else {
                                val targetQuizSelected = quizzes.find { it.id == selectedQuizIdForQuestions }
                                if (targetQuizSelected == null) {
                                    selectedQuizIdForQuestions = null
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = NDCCharcoal.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        border = BorderStroke(1.dp, NDCGold)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("ACTIVE MODULE TARGET:", color = NDCGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text(targetQuizSelected.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                }
                                                IconButton(onClick = { selectedQuizIdForQuestions = null }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Deselect", tint = NDCRed)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                            Divider(color = Color.Gray.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Sub-Section A: Manual Question Builder
                                            Text("METHOD A: MANUAL QUESTION CREATION", color = NDCGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            TextField(
                                                value = manualQnText,
                                                onValueChange = { manualQnText = it },
                                                label = { Text("Question Text") },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            )

                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                TextField(
                                                    value = manualQnOptA,
                                                    onValueChange = { manualQnOptA = it },
                                                    label = { Text("Option A") },
                                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp, vertical = 4.dp)
                                                )
                                                TextField(
                                                    value = manualQnOptB,
                                                    onValueChange = { manualQnOptB = it },
                                                    label = { Text("Option B") },
                                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp, vertical = 4.dp)
                                                )
                                            }

                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                TextField(
                                                    value = manualQnOptC,
                                                    onValueChange = { manualQnOptC = it },
                                                    label = { Text("Option C") },
                                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp, vertical = 4.dp)
                                                )
                                                TextField(
                                                    value = manualQnOptD,
                                                    onValueChange = { manualQnOptD = it },
                                                    label = { Text("Option D") },
                                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp, vertical = 4.dp)
                                                )
                                            }

                                            // Correct Answer Dropdown
                                            Text("CORRECT ANSWER CHOICE", color = NDCOffWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                    onClick = { manualQnCorrectDropdownExpanded = true },
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                                    border = BorderStroke(1.dp, Color.Gray),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("Option $manualQnCorrectAnswer", color = Color.White)
                                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NDCGold)
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = manualQnCorrectDropdownExpanded,
                                                    onDismissRequest = { manualQnCorrectDropdownExpanded = false },
                                                    modifier = Modifier.background(NDCCharcoal)
                                                ) {
                                                    listOf("A", "B", "C", "D").forEach { opt ->
                                                        DropdownMenuItem(
                                                            text = { Text("Option $opt", color = Color.White) },
                                                            onClick = {
                                                                manualQnCorrectAnswer = opt
                                                                manualQnCorrectDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            TextField(
                                                value = manualQnExplanation,
                                                onValueChange = { manualQnExplanation = it },
                                                label = { Text("Explanation (Optional but recommended)") },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            )

                                            TextField(
                                                value = manualQnImageUrl,
                                                onValueChange = { manualQnImageUrl = it },
                                                label = { Text("Question Image URL (Optional)") },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            )

                                            // Fast presets
                                            Text("CHOOSE AN IMAGE PRESET:", color = NDCGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val presets = listOf(
                                                    "Math Graph" to "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?auto=format&fit=crop&w=400&q=80",
                                                    "Chemistry" to "https://images.unsplash.com/photo-1603126857599-f6e157fa2fe6?auto=format&fit=crop&w=400&q=80",
                                                    "Geography Map" to "https://images.unsplash.com/photo-1524661135-423995f22d0b?auto=format&fit=crop&w=400&q=80",
                                                    "Technology Code" to "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?auto=format&fit=crop&w=400&q=80",
                                                    "Astro Nebula" to "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?auto=format&fit=crop&w=400&q=80"
                                                )
                                                presets.forEach { (name, url) ->
                                                    AssistChip(
                                                        onClick = { manualQnImageUrl = url },
                                                        label = { Text(name, fontSize = 10.sp) },
                                                        colors = AssistChipDefaults.assistChipColors(
                                                            labelColor = Color.White,
                                                            containerColor = if (manualQnImageUrl == url) NDCGreen.copy(alpha = 0.4f) else NDCCharcoal
                                                        )
                                                    )
                                                }
                                            }

                                            if (manualQnImageUrl.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                androidx.compose.foundation.Image(
                                                    painter = coil.compose.rememberAsyncImagePainter(manualQnImageUrl),
                                                    contentDescription = "Preview",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(120.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(NDCDarkBg),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Button(
                                                onClick = {
                                                    if (manualQnText.isEmpty() || manualQnOptA.isEmpty() || manualQnOptB.isEmpty()) {
                                                        Toast.makeText(context, "Filled questions and at least options A & B", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        quizViewModel.addManualQuestion(
                                                            quizId = selectedQuizIdForQuestions!!,
                                                            questionText = manualQnText,
                                                            optionA = manualQnOptA,
                                                            optionB = manualQnOptB,
                                                            optionC = manualQnOptC,
                                                            optionD = manualQnOptD,
                                                            correctAnswer = manualQnCorrectAnswer,
                                                            explanation = manualQnExplanation,
                                                            imageUrl = manualQnImageUrl
                                                        )
                                                        manualQnText = ""
                                                        manualQnOptA = ""
                                                        manualQnOptB = ""
                                                        manualQnOptC = ""
                                                        manualQnOptD = ""
                                                        manualQnCorrectAnswer = "A"
                                                        manualQnExplanation = ""
                                                        manualQnImageUrl = ""
                                                        Toast.makeText(context, "Question added securely", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("RECORD MANUAL QUESTION", fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                            Divider(color = Color.Gray.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Sub-Section B: CSV Batch Importer
                                            Text("METHOD B: CSV QUESTIONS BATCH IMPORT", color = NDCGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Format: Question,OptionA,OptionB,OptionC,OptionD,Answer,Explanation", color = Color.LightGray, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            TextField(
                                                value = csvText,
                                                onValueChange = { csvText = it },
                                                placeholder = { Text("Paste CSV formatted lines here...") },
                                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                                maxLines = 5,
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                                                )
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Button(
                                                    onClick = { quizViewModel.parseAndPreviewCSV(csvText) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = NDCGreen)
                                                ) {
                                                    Text("PREVIEW CSV")
                                                }
                                                Button(
                                                    onClick = {
                                                        quizViewModel.saveImportedQuestions(selectedQuizIdForQuestions!!)
                                                        csvText = ""
                                                        Toast.makeText(context, "Batch uploaded to secure datastore", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = NDCGold)
                                                ) {
                                                    Text("COMMIT CSV LOAD")
                                                }
                                            }

                                            cvsErr?.let {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Error: $it", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            if (cvsPreview.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("PREVIEW BATCH (${cvsPreview.size} Rows):", color = NDCGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                cvsPreview.forEach { q ->
                                                    Text("• ${q.questionText} (Ans: ${q.correctAnswer})", color = Color.White, fontSize = 10.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                            Divider(color = Color.Gray.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Sub-Section C: Live Active Question logs
                                            val liveQuestionsFlow = remember(selectedQuizIdForQuestions) { quizViewModel.getQuestionsForQuiz(selectedQuizIdForQuestions!!) }
                                            val liveQuestions by liveQuestionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

                                            Text("ACTIVE QUESTIONS IN THIS MODULE (${liveQuestions.size})", color = NDCGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (liveQuestions.isEmpty()) {
                                                Text("No questions recorded in this module yet. Add using Method A or B.", color = Color.LightGray, fontSize = 11.sp)
                                            } else {
                                                liveQuestions.forEachIndexed { index, q ->
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(8.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text("${index + 1}. ${q.questionText}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                                if (q.imageUrl.isNotEmpty()) {
                                                                    Spacer(modifier = Modifier.height(4.dp))
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        Icon(Icons.Default.Image, contentDescription = null, tint = NDCGold, modifier = Modifier.size(12.dp))
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text("Attachment Included", color = NDCGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                                    }
                                                                }
                                                                Text("Correct: Option ${q.correctAnswer} • A: ${q.optionA} • B: ${q.optionB}", color = Color.LightGray, fontSize = 10.sp)
                                                            }
                                                            IconButton(onClick = { quizViewModel.deleteManualQuestion(q.id, selectedQuizIdForQuestions!!) }) {
                                                                Icon(Icons.Default.Delete, contentDescription = "Delete Question", tint = NDCRed, modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = Color.LightGray)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("CURRENT REGISTERED BATTLES", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Curremt quizzes list
                        items(quizzes) { q ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(q.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Access Code: ${q.accessCode.ifEmpty { "None (Public)" }}", color = NDCGold, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(if (q.active) NDCGreen.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (q.active) "ACTIVE" else "DEACTIVATED",
                                                    color = if (q.active) NDCGreen else Color.Gray,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { quizViewModel.toggleQuizActive(q.id) }) {
                                            Icon(
                                                imageVector = if (q.active) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (q.active) "Deactivate" else "Activate",
                                                tint = if (q.active) NDCGreen else Color.Gray
                                            )
                                        }
                                        IconButton(onClick = { selectedQuizIdForQuestions = q.id }) {
                                            Icon(Icons.Default.Build, contentDescription = "Manage Questions", tint = NDCGold)
                                        }
                                        IconButton(onClick = { quizViewModel.deleteQuiz(q.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = NDCRed)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Leaderboards" -> {
                        item {
                            Text("ADMIN LEADERBOARD CONTROL CENTER", fontWeight = FontWeight.Bold, color = NDCGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Analyze participant performance, apply custom security filters, and manage score records dynamically.", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Action & Control Panel
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal.copy(alpha = 0.45f)),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FilterList, contentDescription = null, tint = NDCGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ADVANCED DATABASE FILTERS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Filter Row 1: Quiz & Category Dropdowns
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        // Quiz Filter
                                        Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                            OutlinedButton(
                                                onClick = { adminQuizDropdownExpanded = true },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                            ) {
                                                val selQuizName = quizzes.find { it.id == adminLeaderboardSelectedQuizId }?.title ?: "All Quizzes"
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (selQuizName.length > 15) selQuizName.take(12) + "..." else selQuizName,
                                                        color = Color.White,
                                                        fontSize = 11.sp
                                                    )
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NDCGold, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            DropdownMenu(
                                                expanded = adminQuizDropdownExpanded,
                                                onDismissRequest = { adminQuizDropdownExpanded = false },
                                                modifier = Modifier.background(NDCCharcoal)
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("All Quizzes", color = Color.White, fontSize = 12.sp) },
                                                    onClick = {
                                                        adminLeaderboardSelectedQuizId = null
                                                        adminQuizDropdownExpanded = false
                                                    }
                                                )
                                                quizzes.forEach { q ->
                                                    DropdownMenuItem(
                                                        text = { Text(q.title, color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            adminLeaderboardSelectedQuizId = q.id
                                                            // Clear category filter to prevent conflicts in default DAO flow
                                                            adminLeaderboardSelectedCategoryId = null
                                                            adminQuizDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Category Filter
                                        Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                            OutlinedButton(
                                                onClick = { adminCategoryDropdownExpanded = true },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                            ) {
                                                val selCatName = categories.find { it.id == adminLeaderboardSelectedCategoryId }?.categoryName ?: "All Categories"
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (selCatName.length > 15) selCatName.take(12) + "..." else selCatName,
                                                        color = Color.White,
                                                        fontSize = 11.sp
                                                    )
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NDCGold, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            DropdownMenu(
                                                expanded = adminCategoryDropdownExpanded,
                                                onDismissRequest = { adminCategoryDropdownExpanded = false },
                                                modifier = Modifier.background(NDCCharcoal)
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("All Categories", color = Color.White, fontSize = 12.sp) },
                                                    onClick = {
                                                        adminLeaderboardSelectedCategoryId = null
                                                        adminCategoryDropdownExpanded = false
                                                    }
                                                )
                                                categories.forEach { c ->
                                                    DropdownMenuItem(
                                                        text = { Text(c.categoryName, color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            adminLeaderboardSelectedCategoryId = c.id
                                                            // Clear quiz selection to prevent conflicts in default DAO flow
                                                            adminLeaderboardSelectedQuizId = null
                                                            adminCategoryDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Filter Row 2: Regions & Timeframe Filters
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        // Region Selector Dropdown
                                        Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                            OutlinedButton(
                                                onClick = { adminRegionDropdownExpanded = true },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = adminLeaderboardSelectedRegion,
                                                        color = Color.White,
                                                        fontSize = 11.sp
                                                    )
                                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = NDCGold, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            DropdownMenu(
                                                expanded = adminRegionDropdownExpanded,
                                                onDismissRequest = { adminRegionDropdownExpanded = false },
                                                modifier = Modifier.background(NDCCharcoal)
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("All Regions", color = Color.White, fontSize = 12.sp) },
                                                    onClick = {
                                                        adminLeaderboardSelectedRegion = "All Regions"
                                                        adminRegionDropdownExpanded = false
                                                    }
                                                )
                                                authViewModel.regionsList.forEach { r ->
                                                    DropdownMenuItem(
                                                        text = { Text(r, color = Color.White, fontSize = 12.sp) },
                                                        onClick = {
                                                            adminLeaderboardSelectedRegion = r
                                                            adminRegionDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Period selection Segment Row inside Outlined Box
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 4.dp)
                                                .background(NDCDarkBg, RoundedCornerShape(8.dp))
                                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(2.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            val prds = listOf("Global", "Weekly", "Monthly")
                                            prds.forEach { p ->
                                                val isSelected = adminLeaderboardPeriod == p
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(if (isSelected) NDCGreen else Color.Transparent, RoundedCornerShape(6.dp))
                                                        .clickable { adminLeaderboardPeriod = p }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(p, color = if (isSelected) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Participant / Location Text Filter
                                    TextField(
                                        value = adminLeaderboardSearchQuery,
                                        onValueChange = { adminLeaderboardSearchQuery = it },
                                        label = { Text("Search by Candidate Name or Constituency") },
                                        placeholder = { Text("e.g. Samuel, Tamale...") },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NDCGold, modifier = Modifier.size(16.dp)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = NDCDarkBg,
                                            unfocusedContainerColor = NDCDarkBg,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedLabelColor = NDCGold,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedIndicatorColor = NDCGreen,
                                            unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Divider(color = Color.Gray.copy(alpha = 0.15f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // EXPORT & REPORT SHARING SECTION
                                    Text("REPORTS & EXPORTS", fontWeight = FontWeight.Bold, color = NDCGold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Button(
                                        onClick = {
                                            val filteredEntities = adminLeaderboardEntries.filter { item ->
                                                val matchesSearch = adminLeaderboardSearchQuery.isBlank() || 
                                                    item.userFullName.contains(adminLeaderboardSearchQuery, ignoreCase = true) ||
                                                    item.constituency.contains(adminLeaderboardSearchQuery, ignoreCase = true)
                                                
                                                val matchesRegion = adminLeaderboardSelectedRegion == "All Regions" || 
                                                    item.region.equals(adminLeaderboardSelectedRegion, ignoreCase = true)

                                                matchesSearch && matchesRegion
                                            }
                                            exportLeaderboardToPdf(
                                                context = context,
                                                leaderboardToExport = filteredEntities,
                                                quizzes = quizzes,
                                                categories = categories,
                                                filterQuizId = adminLeaderboardSelectedQuizId,
                                                filterCategoryId = adminLeaderboardSelectedCategoryId,
                                                filterRegion = adminLeaderboardSelectedRegion,
                                                filterPeriod = adminLeaderboardPeriod
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(40.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("SHARE FILTERED LEADERBOARD (PDF)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Divider(color = Color.Gray.copy(alpha = 0.15f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // PURGING / CLEARING OPTIONS ROW
                                    Text("ADMINISTRATIVE PURGING ACTIONS", fontWeight = FontWeight.Bold, color = NDCRed, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Option 1: Purge Current Filter View
                                        Button(
                                            onClick = {
                                                when {
                                                    adminLeaderboardSelectedQuizId != null -> {
                                                        showConfirmClearQuizLeaderboard = adminLeaderboardSelectedQuizId
                                                    }
                                                    adminLeaderboardSelectedCategoryId != null -> {
                                                        showConfirmClearCategoryLeaderboard = adminLeaderboardSelectedCategoryId
                                                    }
                                                    else -> {
                                                        showConfirmClearAllLeaderboard = true
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NDCRed.copy(alpha = 0.15f), contentColor = NDCRed),
                                            border = BorderStroke(1.dp, NDCRed),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                val clearActionLabel = when {
                                                    adminLeaderboardSelectedQuizId != null -> "Purge Quiz Board"
                                                    adminLeaderboardSelectedCategoryId != null -> "Purge Category Board"
                                                    else -> "Purge All Records"
                                                }
                                                Text(clearActionLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Option 2: Total Reset / Factory Purge
                                        if (adminLeaderboardSelectedQuizId != null || adminLeaderboardSelectedCategoryId != null) {
                                            Button(
                                                onClick = { showConfirmClearAllLeaderboard = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Gray),
                                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Full DB Purge", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // List entries count header
                            val filteredEntities = adminLeaderboardEntries.filter { item ->
                                val matchesSearch = adminLeaderboardSearchQuery.isBlank() || 
                                    item.userFullName.contains(adminLeaderboardSearchQuery, ignoreCase = true) ||
                                    item.constituency.contains(adminLeaderboardSearchQuery, ignoreCase = true)
                                
                                val matchesRegion = adminLeaderboardSelectedRegion == "All Regions" || 
                                    item.region.equals(adminLeaderboardSelectedRegion, ignoreCase = true)

                                matchesSearch && matchesRegion
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LEADERBOARD REGISTRY (${filteredEntities.size} RECORDS)",
                                    fontWeight = FontWeight.Bold,
                                    color = NDCOffWhite,
                                    fontSize = 12.sp
                                )
                                if (filteredEntities.isNotEmpty()) {
                                    Text(
                                        text = "Top Rank 1st",
                                        color = NDCGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            if (filteredEntities.isEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NDCCharcoal.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Leaderboard, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No leaderboard records matches specified parameters.", color = Color.Gray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }

                            // ----------------------------------------
                            // Leaderboard Action Dialogs (Inside first item block of tab)
                            // ----------------------------------------
                            if (showConfirmClearAllLeaderboard) {
                                AlertDialog(
                                    onDismissRequest = { showConfirmClearAllLeaderboard = false },
                                    containerColor = NDCCharcoal,
                                    title = { Text("PURGE ALL LEADERBOARDS", color = NDCRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                    text = { Text("Are you absolutely sure you want to permanently clear the ENTIRE leaderboard registry? This will wipe out all user exam scores, ranks, and records globally.", color = Color.White, fontSize = 13.sp) },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                quizViewModel.clearLeaderboardRecords(null, null)
                                                Toast.makeText(context, "All leaderboard records cleared successfully", Toast.LENGTH_SHORT).show()
                                                showConfirmClearAllLeaderboard = false
                                            }
                                        ) {
                                            Text("CONFIRM PURGE", color = NDCRed, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showConfirmClearAllLeaderboard = false }) {
                                            Text("CANCEL", color = Color.White)
                                        }
                                    }
                                )
                            }

                            showConfirmClearQuizLeaderboard?.let { qId ->
                                val quizTitle = quizzes.find { it.id == qId }?.title ?: "Selected Quiz"
                                AlertDialog(
                                    onDismissRequest = { showConfirmClearQuizLeaderboard = null },
                                    containerColor = NDCCharcoal,
                                    title = { Text("CLEAR QUIZ LEADERBOARD", color = NDCRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                    text = { Text("Are you sure you want to clear leaderboard records specifically for '$quizTitle'? Other quizzes will not be impacted.", color = Color.White, fontSize = 13.sp) },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                quizViewModel.clearLeaderboardRecords(qId, null)
                                                Toast.makeText(context, "Quiz leaderboard cleared successfully", Toast.LENGTH_SHORT).show()
                                                showConfirmClearQuizLeaderboard = null
                                            }
                                        ) {
                                            Text("CLEAR RECORDS", color = NDCRed, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showConfirmClearQuizLeaderboard = null }) {
                                            Text("CANCEL", color = Color.White)
                                        }
                                    }
                                )
                            }

                            showConfirmClearCategoryLeaderboard?.let { cId ->
                                val catTitle = categories.find { it.id == cId }?.categoryName ?: "Selected Category"
                                AlertDialog(
                                    onDismissRequest = { showConfirmClearCategoryLeaderboard = null },
                                    containerColor = NDCCharcoal,
                                    title = { Text("CLEAR CATEGORY LEADERBOARD", color = NDCRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                    text = { Text("Are you sure you want to clear leaderboards for all examinations under the '$catTitle' category?", color = Color.White, fontSize = 13.sp) },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                quizViewModel.clearLeaderboardRecords(null, cId)
                                                Toast.makeText(context, "Category leaderboard cleared successfully", Toast.LENGTH_SHORT).show()
                                                showConfirmClearCategoryLeaderboard = null
                                            }
                                        ) {
                                            Text("CLEAR RECORDS", color = NDCRed, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showConfirmClearCategoryLeaderboard = null }) {
                                            Text("CANCEL", color = Color.White)
                                        }
                                    }
                                )
                            }

                            showConfirmDeleteLeaderboardEntryId?.let { entryId ->
                                AlertDialog(
                                    onDismissRequest = { showConfirmDeleteLeaderboardEntryId = null },
                                    containerColor = NDCCharcoal,
                                    title = { Text("DELETE SINGLE ENTRY", color = NDCRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                    text = { Text("Do you want to permanently remove this participant's specific score record? Their rank will recalculate automatically.", color = Color.White, fontSize = 13.sp) },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                quizViewModel.deleteLeaderboardEntry(entryId)
                                                Toast.makeText(context, "Record removed successfully", Toast.LENGTH_SHORT).show()
                                                showConfirmDeleteLeaderboardEntryId = null
                                            }
                                        ) {
                                            Text("DELETE ENTRY", color = NDCRed, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showConfirmDeleteLeaderboardEntryId = null }) {
                                            Text("CANCEL", color = Color.White)
                                        }
                                    }
                                )
                            }
                        }

                        // Rendered items of final filtered entities
                        val filteredEntities = adminLeaderboardEntries.filter { item ->
                            val matchesSearch = adminLeaderboardSearchQuery.isBlank() || 
                                item.userFullName.contains(adminLeaderboardSearchQuery, ignoreCase = true) ||
                                item.constituency.contains(adminLeaderboardSearchQuery, ignoreCase = true)
                            
                            val matchesRegion = adminLeaderboardSelectedRegion == "All Regions" || 
                                item.region.equals(adminLeaderboardSelectedRegion, ignoreCase = true)

                            matchesSearch && matchesRegion
                        }

                        itemsIndexed(filteredEntities) { idx, entry ->
                            val qTitle = quizzes.find { it.id == entry.quizId }?.title ?: "Individual Module"
                            val catTitle = categories.find { it.id == entry.categoryId }?.categoryName ?: "General"
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Medallist indicator badge
                                        val (badgeColor, badgeText) = when (idx) {
                                            0 -> NDCGold to "#1"
                                            1 -> Color.LightGray to "#2"
                                            2 -> Color(0xFFCD7F32) to "#3"
                                            else -> Color.Gray.copy(alpha = 0.3f) to "#${idx + 1}"
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(badgeColor, RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = badgeText,
                                                color = if (idx in 0..2) Color.Black else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(entry.userFullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$qTitle • $catTitle",
                                                color = NDCGold,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "${entry.region} • ${entry.constituency}",
                                                color = Color.LightGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${entry.score} pts",
                                                color = NDCGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "${entry.completionTimeSeconds}s",
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { showConfirmDeleteLeaderboardEntryId = entry.id },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete entry", tint = NDCRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Broadcast Alerts" -> {
                        item {
                            Text("BROADCAST ALERT MESSAGES ENGINE", fontWeight = FontWeight.Bold, color = NDCGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("As an administrator, issue global real-time notifications to all users on active screens. Warnings/Alerts won't pop up if they are in the active quiz session to protect user focus.", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("DISPATCH NEW BROADCAST ALERT", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    TextField(
                                        value = broadcastTitle,
                                        onValueChange = { broadcastTitle = it },
                                        placeholder = { Text("Display Label Header (e.g. SERVER MAINTENANCE)") },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                                        )
                                    )

                                    TextField(
                                        value = broadcastContent,
                                        onValueChange = { broadcastContent = it },
                                        placeholder = { Text("Full alert broadcast announcement content details...") },
                                        modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 4.dp),
                                        maxLines = 4,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Optional Broadcast Image Upload Button & Preview
                                    Text("OPTIONAL BANNER IMAGE (UPLOAD)", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = NDCDarkBg),
                                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            if (broadcastImageUrl.isNotEmpty()) {
                                                AsyncImage(
                                                    model = broadcastImageUrl,
                                                    contentDescription = "Selected Broadcast Banner",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(115.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(NDCCharcoal),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { broadcastImagePickerLauncher.launch("image/*") },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NDCGreen),
                                                        border = BorderStroke(1.dp, NDCGreen),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Change", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    OutlinedButton(
                                                        onClick = { broadcastImageUrl = "" },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NDCRed),
                                                        border = BorderStroke(1.dp, NDCRed),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Remove", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("No image uploaded", color = Color.Gray, fontSize = 11.sp)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Button(
                                                    onClick = { broadcastImagePickerLauncher.launch("image/*") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Upload Image", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action Link URL info
                                    Text("OPTIONAL ACTION BUTTON LINK", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                                    TextField(
                                        value = broadcastLinkUrl,
                                        onValueChange = { broadcastLinkUrl = it },
                                        placeholder = { Text("https://example.com/target-action-page") },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                                        )
                                    )

                                    TextField(
                                        value = broadcastLinkLabel,
                                        onValueChange = { broadcastLinkLabel = it },
                                        placeholder = { Text("Button Text (e.g. PARTICIPATE NOW - defaults to VIEW LINK)") },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = NDCCharcoal, unfocusedContainerColor = NDCCharcoal,
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (broadcastTitle.isEmpty() || broadcastContent.isEmpty()) {
                                                Toast.makeText(context, "All alerts fields are mandatory", Toast.LENGTH_SHORT).show()
                                            } else {
                                                quizViewModel.createAnnouncement(
                                                    title = broadcastTitle,
                                                    content = broadcastContent,
                                                    imageUrl = broadcastImageUrl.ifBlank { null },
                                                    linkUrl = broadcastLinkUrl.ifBlank { null },
                                                    linkLabel = broadcastLinkLabel.ifBlank { null }
                                                )
                                                broadcastImageUrl = ""
                                                broadcastLinkUrl = ""
                                                broadcastLinkLabel = ""
                                                broadcastTitle = ""
                                                broadcastContent = ""
                                                Toast.makeText(context, "System announcement broadcast dispatched successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("DISPATCH SYSTEM BROADCAST ALERT", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text("CURRENT LIVE SYSTEM BROADCASTS", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (announcements.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        Text("No live announcement alerts currently dispatched.", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            items(announcements) { a ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.Top) {
                                                if (!a.imageUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = a.imageUrl,
                                                        contentDescription = "Image Thumbnail",
                                                        modifier = Modifier
                                                            .size(50.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(NDCDarkBg),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(a.title.uppercase(), color = NDCGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .background(if (a.active) NDCGreen.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = if (a.active) "ACTIVE" else "DEACTIVATED",
                                                                color = if (a.active) NDCGreen else Color.Gray,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(a.content, color = Color.White, fontSize = 12.sp)

                                                    if (!a.linkUrl.isNullOrBlank()) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier
                                                                .background(NDCGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                                .border(1.dp, NDCGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Link,
                                                                contentDescription = null,
                                                                tint = NDCGreen,
                                                                modifier = Modifier.size(10.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "${if (!a.linkLabel.isNullOrBlank()) a.linkLabel.uppercase() else "LEARN MORE"}: ${a.linkUrl}",
                                                                color = NDCGreen,
                                                                fontSize = 10.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { quizViewModel.toggleAnnouncementActive(a.id) }) {
                                                Icon(
                                                    imageVector = if (a.active) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle Visibility",
                                                    tint = if (a.active) NDCGreen else Color.Gray
                                                )
                                            }
                                            IconButton(onClick = { quizViewModel.deleteAnnouncement(a.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Revoke Alert", tint = NDCRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Users" -> {
                        item {
                            Text("ACTIVE USER CADRE", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Filter, bulk-select, promote, suspend or permanently delete cadres of the NDC party network.", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            // SEARCH & FILTERS CONTAINER
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("FILTERING & UTILITIES", color = NDCGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Search Input
                                    TextField(
                                        value = userSearchQuery,
                                        onValueChange = { userSearchQuery = it },
                                        placeholder = { Text("Search by name, email, phone, location...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = NDCDarkBg, unfocusedContainerColor = NDCDarkBg,
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Filter category rows (Role & Status drop-down / rows of Chips)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Role filter
                                        Box(modifier = Modifier.weight(1f)) {
                                            var roleExpanded by remember { mutableStateOf(false) }
                                            Button(
                                                onClick = { roleExpanded = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = NDCDarkBg),
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                    Text("Role: $userFilterRole", fontSize = 11.sp, color = NDCOffWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NDCGold, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            DropdownMenu(
                                                expanded = roleExpanded,
                                                onDismissRequest = { roleExpanded = false },
                                                modifier = Modifier.background(NDCCharcoal)
                                            ) {
                                                listOf("All", "Admin", "User").forEach { role ->
                                                    DropdownMenuItem(
                                                        text = { Text(role, color = Color.White, fontSize = 11.sp) },
                                                        onClick = {
                                                            userFilterRole = role
                                                            roleExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Status filter
                                        Box(modifier = Modifier.weight(1f)) {
                                            var statusExpanded by remember { mutableStateOf(false) }
                                            Button(
                                                onClick = { statusExpanded = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = NDCDarkBg),
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                    Text("Status: $userFilterStatus", fontSize = 11.sp, color = NDCOffWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NDCGold, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            DropdownMenu(
                                                expanded = statusExpanded,
                                                onDismissRequest = { statusExpanded = false },
                                                modifier = Modifier.background(NDCCharcoal)
                                            ) {
                                                listOf("All", "Active", "Suspended").forEach { status ->
                                                    DropdownMenuItem(
                                                        text = { Text(status, color = Color.White, fontSize = 11.sp) },
                                                        onClick = {
                                                            userFilterStatus = status
                                                            statusExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Filtered active list
                            val filteredUsers = users.filter { u ->
                                val matchesSearch = u.fullName.contains(userSearchQuery, ignoreCase = true) ||
                                        u.email.contains(userSearchQuery, ignoreCase = true) ||
                                        u.phoneNumber.contains(userSearchQuery, ignoreCase = true) ||
                                        u.region.contains(userSearchQuery, ignoreCase = true) ||
                                        u.constituency.contains(userSearchQuery, ignoreCase = true)
                                
                                val matchesRole = userFilterRole == "All" || u.role.equals(userFilterRole, ignoreCase = true)
                                val matchesStatus = userFilterStatus == "All" || u.status.equals(userFilterStatus, ignoreCase = true)

                                matchesSearch && matchesRole && matchesStatus
                            }

                            // ACTIONS & EXPORT HEADER
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val allFilteredIds = filteredUsers.map { it.id }.toSet()
                                val isAllSelected = filteredUsers.isNotEmpty() && selectedUserIds.containsAll(allFilteredIds)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isAllSelected,
                                        onCheckedChange = { checked ->
                                            selectedUserIds = if (checked) {
                                                selectedUserIds + allFilteredIds
                                            } else {
                                                selectedUserIds - allFilteredIds
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = NDCGreen,
                                            uncheckedColor = Color.Gray,
                                            checkmarkColor = Color.White
                                        )
                                    )
                                    Text("Select All (${filteredUsers.size})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Export & Delete actions
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // PDF Export Button
                                    Button(
                                        onClick = {
                                            exportUsersToPdf(context, filteredUsers)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("EXPORT PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Bulk Delete Button
                                    if (selectedUserIds.isNotEmpty()) {
                                        Button(
                                            onClick = { showConfirmBulkDelete = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = NDCRed),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("DELETE (${selectedUserIds.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            if (showConfirmBulkDelete) {
                                AlertDialog(
                                    onDismissRequest = { showConfirmBulkDelete = false },
                                    containerColor = NDCCharcoal,
                                    title = { Text("CONFIRM BULK DELETION", color = NDCRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                    text = { Text("Are absolutely sure you want to permanently delete ${selectedUserIds.size} selected users? This action is irreversible.", color = Color.White, fontSize = 13.sp) },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                quizViewModel.deleteUsers(selectedUserIds.toList())
                                                Toast.makeText(context, "Successfully deleted ${selectedUserIds.size} users", Toast.LENGTH_SHORT).show()
                                                selectedUserIds = emptySet()
                                                showConfirmBulkDelete = false
                                            }
                                        ) {
                                            Text("DELETE CADRE", color = NDCRed, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showConfirmBulkDelete = false }) {
                                            Text("CANCEL", color = Color.White)
                                        }
                                    }
                                )
                            }
                        }

                        val filteredUsersList = users.filter { u ->
                            val matchesSearch = u.fullName.contains(userSearchQuery, ignoreCase = true) ||
                                    u.email.contains(userSearchQuery, ignoreCase = true) ||
                                    u.phoneNumber.contains(userSearchQuery, ignoreCase = true) ||
                                    u.region.contains(userSearchQuery, ignoreCase = true) ||
                                    u.constituency.contains(userSearchQuery, ignoreCase = true)
                            
                            val matchesRole = userFilterRole == "All" || u.role.equals(userFilterRole, ignoreCase = true)
                            val matchesStatus = userFilterStatus == "All" || u.status.equals(userFilterStatus, ignoreCase = true)

                            matchesSearch && matchesRole && matchesStatus
                        }

                        if (filteredUsersList.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No party cadres match the selected filter criteria.", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            items(filteredUsersList) { u ->
                                val isSelected = selectedUserIds.contains(u.id)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) NDCGreen.copy(alpha = 0.1f) else NDCCharcoal),
                                    border = BorderStroke(1.dp, if (isSelected) NDCGreen else Color.Gray.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                        selectedUserIds = if (isSelected) {
                                            selectedUserIds - u.id
                                        } else {
                                            selectedUserIds + u.id
                                        }
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = { checked ->
                                                        selectedUserIds = if (checked) {
                                                            selectedUserIds + u.id
                                                        } else {
                                                            selectedUserIds - u.id
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = NDCGreen,
                                                        uncheckedColor = Color.Gray,
                                                        checkmarkColor = Color.White
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Column {
                                                    Text(u.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("Role: ${u.role} • Status: ${u.status}", color = NDCGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text("Region: ${u.region} • Constituency: ${u.constituency}", color = Color.LightGray, fontSize = 11.sp)
                                                    Text("Contact: ${u.phoneNumber} • ${u.email}", color = Color.Gray, fontSize = 10.sp)
                                                }
                                            }

                                            Row {
                                                // Promotion button toggle
                                                IconButton(onClick = {
                                                    val nextRole = if (u.role == "Admin" || u.role == "Super Admin") "User" else "Admin"
                                                    quizViewModel.updateUserRole(u.id, nextRole)
                                                    Toast.makeText(context, "${u.fullName} changed role to $nextRole", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.ArrowCircleUp, contentDescription = "Promote cadre", tint = NDCGreen)
                                                }
                                                // Suspend toggle button
                                                IconButton(onClick = {
                                                    val nextStatus = if (u.status == "Suspended") "Active" else "Suspended"
                                                    quizViewModel.updateUserStatus(u.id, nextStatus)
                                                    Toast.makeText(context, "${u.fullName} set status to $nextStatus", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Block, contentDescription = "Suspend cadre", tint = NDCGold)
                                                }
                                                // General single delete button
                                                IconButton(onClick = {
                                                    quizViewModel.deleteUser(u.id)
                                                    Toast.makeText(context, "${u.fullName} permanently deleted", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete cadre", tint = NDCRed)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Sponsors & Categories" -> {
                        item {
                            Text("CATEGORY CONFIGURATIONS", fontWeight = FontWeight.Bold, color = NDCGold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            var catName by remember { mutableStateOf("") }
                            var catDesc by remember { mutableStateOf("") }

                            TextField(
                                value = catName,
                                onValueChange = { catName = it },
                                label = { Text("Category Name") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            TextField(
                                value = catDesc,
                                onValueChange = { catDesc = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (catName.isEmpty()) {
                                        Toast.makeText(context, "Fill category name", Toast.LENGTH_SHORT).show()
                                    } else {
                                        quizViewModel.addCategory(catName, catDesc, "")
                                        catName = ""
                                        catDesc = ""
                                        Toast.makeText(context, "New category added", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ESTABLISH CATEGORY")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        items(categories) { cat ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(cat.categoryName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(cat.description, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                                    }
                                    IconButton(onClick = { quizViewModel.deleteCategory(cat.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = NDCRed)
                                    }
                                }
                            }
                        }

                        // Sponsor creation section
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = Color.LightGray)
                            Spacer(modifier = Modifier.height(24.dp))

                            Text("SPONSOR CONFIGURATIONS", fontWeight = FontWeight.Bold, color = NDCGold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            TextField(
                                value = spName,
                                onValueChange = { spName = it },
                                label = { Text("Sponsor Name") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            TextField(
                                value = spDesc,
                                onValueChange = { spDesc = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )

                            // Sponsor Logo Image Upload Button & Preview
                            Text("SPONSOR LOGO IMAGE", fontWeight = FontWeight.Bold, color = NDCOffWhite, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (spLogo.isNotEmpty()) {
                                        AsyncImage(
                                            model = spLogo,
                                            contentDescription = "Sponsor Logo",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NDCDarkBg),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            OutlinedButton(
                                                onClick = { sponsorLogoPickerLauncher.launch("image/*") },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NDCGreen),
                                                border = BorderStroke(1.dp, NDCGreen)
                                            ) {
                                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Change Logo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedButton(
                                                onClick = { spLogo = "" },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NDCRed),
                                                border = BorderStroke(1.dp, NDCRed)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("No logo uploaded yet", color = Color.Gray, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { sponsorLogoPickerLauncher.launch("image/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = NDCGreen)
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Upload Logo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (spName.isEmpty()) {
                                        Toast.makeText(context, "Fill sponsor name", Toast.LENGTH_SHORT).show()
                                    } else {
                                        quizViewModel.addSponsor(spName, spDesc, spLogo)
                                        spName = ""
                                        spDesc = ""
                                        spLogo = ""
                                        Toast.makeText(context, "New sponsor added successfully", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ESTABLISH SPONSOR")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        items(sponsors) { sp ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        if (sp.logoUrl.isNotEmpty()) {
                                            androidx.compose.foundation.Image(
                                                painter = coil.compose.rememberAsyncImagePainter(sp.logoUrl),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .padding(2.dp),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                        }
                                        Column {
                                            Text(sp.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(sp.description, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                                        }
                                    }
                                    IconButton(onClick = { quizViewModel.deleteSponsor(sp.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = NDCRed)
                                    }
                                }
                            }
                        }
                    }
                    "Audit Logs" -> {
                        item {
                            Text("COMPUTATIONAL SECURITY LOGGING EVENTS", fontWeight = FontWeight.Bold, color = NDCRed, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Filters
                            TextField(
                                value = auditLogSearchQuery,
                                onValueChange = { auditLogSearchQuery = it },
                                placeholder = { Text("Search logs (action/target)...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(focusedContainerColor = NDCDarkBg, unfocusedContainerColor = NDCDarkBg, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    val filteredLogs = auditLogs.filter { log ->
                                        (auditLogSearchQuery.isBlank() || log.action.contains(auditLogSearchQuery, true) || log.target.contains(auditLogSearchQuery, true))
                                    }
                                    exportAuditLogsToPdf(context, filteredLogs)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NDCGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("EXPORT FILTERED LOGS TO PDF")
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        val filteredAuditLogs = auditLogs.filter { log ->
                            (auditLogSearchQuery.isBlank() || log.action.contains(auditLogSearchQuery, true) || log.target.contains(auditLogSearchQuery, true))
                        }

                        items(filteredAuditLogs) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NDCCharcoal),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(log.action, color = if (log.action in listOf("SECURITY_ALERT", "LEADERBOARD_CLEAR", "LEADERBOARD_PURGE")) NDCRed else NDCGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(log.timestamp)), color = Color.Gray, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(log.target, color = Color.White, fontSize = 12.sp)
                                    Text("By: ${log.adminName}", color = Color.LightGray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    "Database" -> {
                        item {
                            DatabaseSettingsScreen(
                                configProvider, { configProvider = it },
                                configDbUrl, { configDbUrl = it },
                                configProjectId, { configProjectId = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatabaseSettingsScreen(
    configProvider: String,
    onProviderChange: (String) -> Unit,
    dbUrl: String,
    onDbUrlChange: (String) -> Unit,
    projectId: String,
    onProjectIdChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Database Connection Management", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("IMPORTANT: To keep your app secure, please enter sensitive API keys in the Secrets panel in AI Studio.", color = NDCGold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Provider Selection
        Text("Database Provider", color = Color.Gray, fontSize = 12.sp)
        Row(modifier = Modifier.fillMaxWidth()) {
            val providers = listOf("Supabase", "Firebase", "Other")
            providers.forEach { p ->
                RadioButton(
                    selected = configProvider == p,
                    onClick = { onProviderChange(p) },
                    colors = RadioButtonDefaults.colors(selectedColor = NDCGreen, unselectedColor = Color.Gray)
                )
                Text(p, color = Color.White, modifier = Modifier.align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(
            value = dbUrl,
            onValueChange = onDbUrlChange,
            label = { Text("Database URL / Endpoint") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = NDCDarkBg, unfocusedContainerColor = NDCDarkBg, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = projectId,
            onValueChange = onProjectIdChange,
            label = { Text("Project ID") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = NDCDarkBg, unfocusedContainerColor = NDCDarkBg, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Setup Guide", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        
        val guide = when (configProvider) {
            "Supabase" -> "1. Go to Supabase dashboard.\n2. Create a new project.\n3. Find your API URL and Project ID in Project Settings > API.\n4. Add API Key (ANON_KEY) to AI Studio Secrets panel."
            "Firebase" -> "1. Go to Firebase Console.\n2. Create a new project.\n3. Create a Firestore database.\n4. Get your Project ID from General settings.\n5. Add Service Account JSON credentials to AI Studio Secrets panel."
            else -> "Consult your database provider's documentation to find the required API URL/Endpoint and Project ID. Keep sensitive keys in the AI Studio Secrets panel."
        }
        Text(guide, color = Color.LightGray, fontSize = 14.sp)
    }
}
