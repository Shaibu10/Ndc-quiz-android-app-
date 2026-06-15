package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.repository.SupabaseOfflineFirstQuizRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.QuizViewModel
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationImportant

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Local Persistence Database Initialization
        val database = AppDatabase.getDatabase(applicationContext)
        val quizAppDao = database.quizAppDao()
        val repository = SupabaseOfflineFirstQuizRepository(quizAppDao)

        // ViewModels
        val authViewModel = AuthViewModel(repository)
        val quizViewModel = QuizViewModel(repository)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val isBusyTakingQuiz = currentRoute?.startsWith("quiz/") == true || currentRoute == "quiz/{quizId}"

                // Active alerts
                val announcements by quizViewModel.announcements.collectAsState(initial = emptyList())
                var dismissedAlertIds by remember { mutableStateOf(setOf<String>()) }
                val activeAlert = announcements.firstOrNull { it.id !in dismissedAlertIds }

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // 1. SPLASH SCREEN
                        composable("splash") {
                            SplashScreen(authViewModel = authViewModel) {
                                val user = authViewModel.currentUser.value
                                val onboarded = authViewModel.onboardingCompleted.value
                                if (user != null) {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                } else if (!onboarded) {
                                    navController.navigate("onboarding") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                        }

                        // 2. ONBOARDING
                        composable("onboarding") {
                            OnboardingScreen(authViewModel = authViewModel) {
                                navController.navigate("login") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }

                        // 3. LOGIN
                        composable("login") {
                            LoginScreen(
                                authViewModel = authViewModel,
                                onLoginSuccess = { user ->
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }

                        // 4. REGISTER
                        composable("register") {
                            RegisterScreen(
                                authViewModel = authViewModel,
                                onRegisterSuccess = { user ->
                                    navController.navigate("home") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                }
                            )
                        }

                        // 5. HOME SCREEN
                        composable("home") {
                            HomeScreen(
                                authViewModel = authViewModel,
                                quizViewModel = quizViewModel,
                                navController = navController,
                                onNavigateToQuiz = { quiz ->
                                    if (quiz.accessCode.isNotEmpty()) {
                                        navController.navigate("quiz_access/${quiz.id}")
                                    } else {
                                        quizViewModel.startQuiz(quiz)
                                        navController.navigate("quiz/${quiz.id}")
                                    }
                                }
                            )
                        }

                        // 6. QUIZ ENTRY PROTECTION (ACCESS CODE)
                        composable(
                            route = "quiz_access/{quizId}",
                            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
                            val quizzes = quizViewModel.quizzes.value
                            val targetQuiz = quizzes.find { it.id == quizId }

                            if (targetQuiz != null) {
                                val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
                                if (currentUser != null) {
                                    QuizAccessScreen(
                                        quiz = targetQuiz,
                                        quizViewModel = quizViewModel,
                                        userId = currentUser!!.id,
                                        onCodeValidated = {
                                            quizViewModel.startQuiz(targetQuiz)
                                            navController.navigate("quiz/${targetQuiz.id}") {
                                                popUpTo("quiz_access/${targetQuiz.id}") { inclusive = true }
                                            }
                                        },
                                        onBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            } else {
                                navController.popBackStack()
                            }
                        }

                        // 7. ACTIVE QUIZ BATTLE
                        composable(
                            route = "quiz/{quizId}",
                            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
                        ) {
                            QuizScreen(
                                quizViewModel = quizViewModel,
                                authViewModel = authViewModel,
                                onNavigateToResults = { score, total, timeUsed ->
                                    navController.navigate("results/$score/$total/$timeUsed") {
                                        popUpTo("quiz/{quizId}") { inclusive = true }
                                    }
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 8. RESULTS OUTCOME SCREEN
                        composable(
                            route = "results/{score}/{total}/{timeUsed}",
                            arguments = listOf(
                                navArgument("score") { type = NavType.IntType },
                                navArgument("total") { type = NavType.IntType },
                                navArgument("timeUsed") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val score = backStackEntry.arguments?.getInt("score") ?: 0
                            val total = backStackEntry.arguments?.getInt("total") ?: 0
                            val timeUsed = backStackEntry.arguments?.getLong("timeUsed") ?: 0L

                            ResultsScreen(
                                score = score,
                                total = total,
                                timeUsedSeconds = timeUsed,
                                quizViewModel = quizViewModel,
                                onNavigateBackToHome = {
                                    navController.navigate("home") {
                                        popUpTo("results/{score}/{total}/{timeUsed}") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 9. LEADERBOARD SCREEN
                        composable("leaderboard") {
                            LeaderboardScreen(
                                quizViewModel = quizViewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 10. PROFILE SCREEN
                        composable("profile") {
                            ProfileScreen(
                                authViewModel = authViewModel,
                                quizViewModel = quizViewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 11. CENTRAL CONTROL ADMIN PORTAL
                        composable("admin_dashboard") {
                            AdminDashboardScreen(
                                authViewModel = authViewModel,
                                quizViewModel = quizViewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    // Floating Broadcast Alert
                    if (activeAlert != null && !isBusyTakingQuiz) {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // NDCCharcoal
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                border = BorderStroke(2.dp, Color(0xFFEAB308)), // NDCGold
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 500.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFFEAB308).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = Color(0xFFEAB308),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeAlert.title.uppercase(),
                                            color = Color(0xFFEAB308), // NDCGold
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = activeAlert.content,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { dismissedAlertIds = dismissedAlertIds + activeAlert.id },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss Active Alert",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
