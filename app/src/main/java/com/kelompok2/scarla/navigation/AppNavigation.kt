package com.kelompok2.scarla.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok2.scarla.firebase.FirestoreInitializer
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.kelompok2.scarla.ui.screens.AchievementPage
import com.kelompok2.scarla.ui.screens.AuthChoiceScreen
import com.kelompok2.scarla.ui.screens.BelajarScreen
import com.kelompok2.scarla.ui.screens.EducationLevelScreen
import com.kelompok2.scarla.ui.screens.EditAvatarScreen
import com.kelompok2.scarla.ui.screens.EditInterestsScreen
import com.kelompok2.scarla.ui.screens.EditMbtiScreen
import com.kelompok2.scarla.ui.screens.EditProfileScreen
import com.kelompok2.scarla.ui.screens.FriendRequestsScreen
import com.kelompok2.scarla.ui.screens.InformatikaScreen
import com.kelompok2.scarla.ui.screens.InterestsScreen
import com.kelompok2.scarla.ui.screens.LoginScreen
import com.kelompok2.scarla.ui.screens.MainScreen
import com.kelompok2.scarla.ui.screens.MbtiScreen
import com.kelompok2.scarla.ui.screens.OnboardingScreen
import com.kelompok2.scarla.ui.screens.ProfileSetupScreen
import com.kelompok2.scarla.ui.screens.ProfilScreen
import com.kelompok2.scarla.ui.screens.ScreenStreak
import com.kelompok2.scarla.ui.screens.SettingsScreen
import com.kelompok2.scarla.ui.screens.SignupScreen
import com.kelompok2.scarla.ui.screens.SplashScreen
import com.kelompok2.scarla.ui.screens.HtmlScreen
import com.kelompok2.scarla.ui.screens.QuizHtmlScreen
import com.kelompok2.scarla.ui.screens.ChatPage
import com.kelompok2.scarla.ui.screens.ChatRoomPage

private val firestore by lazy { FirebaseFirestore.getInstance() }

// Definisi Route
sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")
    object AuthChoice : Screen("auth_choice_screen")
    object Signup : Screen("signup_screen")
    object Login : Screen("login_screen")
    object ProfileSetup : Screen("profile_setup_screen")
    object Main : Screen("main_screen")
    object Profile : Screen("profile_screen")
    object Settings : Screen("settings_screen")
    object EditProfile : Screen("edit_profile_screen")
    object EditAvatar : Screen("edit_avatar_screen")
    object EditInterests : Screen("edit_interests_screen")
    object EditMbti : Screen("edit_mbti_screen")
    object EducationLevel : Screen("education_level_screen")
    object Mbti : Screen("mbti_screen")
    object Interests : Screen("interests_screen")
    object Streak : Screen("streak_screen")
    object HtmlScreen : Screen("html_screen")
    object Achievement : Screen("achievement_screen")
    object FriendRequests : Screen("friend_requests_screen")
    object Chat : Screen("chat_list")
    object ChatRoom : Screen("chat_room/{peerUid}/{peerName}") {
        fun createRoute(peerUid: String, peerName: String): String {
            val safeName = peerName.ifBlank { "Pengguna" }
            val encodedName = java.net.URLEncoder.encode(safeName, "UTF-8")
            return "chat_room/$peerUid/$encodedName"
        }
    }
    object PeerProfile : Screen("peer_profile/{peerUid}") {
        fun createRoute(peerUid: String) = "peer_profile/$peerUid"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(320)
            ) + fadeIn(animationSpec = tween(220))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(320)
            ) + fadeOut(animationSpec = tween(220))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(320)
            ) + fadeIn(animationSpec = tween(220))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(320)
            ) + fadeOut(animationSpec = tween(220))
        }
    ) {
        // 1. Rute Splash
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        // 2. Rute Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.AuthChoice.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        // 3. Rute AuthChoice (Pilihan daftar / masuk)
        composable(Screen.AuthChoice.route) {
            AuthChoiceScreen(
                onBack = { navController.popBackStack() },
                onDaftarSekarang = {
                    navController.navigate(Screen.Signup.route)
                },
                onMasuk = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // 4. Rute Signup
        composable(Screen.Signup.route) {
            SignupScreen(
                onBack = { navController.popBackStack() },
                onSignupSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. Rute Login
        composable(Screen.Login.route) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = { uid ->
                    scope.launch {
                        // Pastikan sub-koleksi (streaks, achievements) sudah ada
                        // untuk user baru maupun user lama yang belum ter-inisialisasi
                        FirestoreInitializer.ensureUserInitialized(uid)

                        val shouldCompleteProfile = shouldRouteToProfile(uid)
                        navController.navigate(
                            if (shouldCompleteProfile) Screen.ProfileSetup.route else Screen.Streak.route
                        ) {
                            popUpTo(Screen.AuthChoice.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Screen.EducationLevel.route)
                }
            )
        }

        composable(Screen.EducationLevel.route) {
            EducationLevelScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Screen.Mbti.route)
                }
            )
        }

        composable(Screen.Mbti.route) {
            MbtiScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Screen.Interests.route)
                }
            )
        }

        composable(Screen.Interests.route) {
            InterestsScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Screen.Streak.route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Streak.route) {
            ScreenStreak(
                onContinue = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Streak.route) { inclusive = true }
                    }
                }
            )
        }

        // 8. Rute Main (Halaman utama setelah login)
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfilScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.PeerProfile.route,
            arguments = listOf(
                androidx.navigation.navArgument("peerUid") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val peerUid = backStackEntry.arguments?.getString("peerUid")
            ProfilScreen(
                navController = navController,
                peerUid = peerUid
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onAvatarSelect = { navController.navigate(Screen.EditAvatar.route) },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.EditAvatar.route) {
            EditAvatarScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditInterests.route) {
            EditInterestsScreen(
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.EditMbti.route) {
            EditMbtiScreen(
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable("belajar") {
            BelajarScreen(navController = navController)
        }

        composable("informatika_screen") {
            InformatikaScreen(navController = navController)
        }

        composable("html_screen") {
            HtmlScreen(navController = navController)
        }

        composable("quiz_html") {
            QuizHtmlScreen(navController = navController)
        }

        composable(Screen.Achievement.route) {
            AchievementPage(navController = navController)
        }

        composable(Screen.FriendRequests.route) {
            FriendRequestsScreen(navController = navController)
        }

        // ── Chat ─────────────────────────────────────────────────────────
        composable(Screen.Chat.route) {
            ChatPage(navController = navController)
        }

        composable(
            route = Screen.ChatRoom.route,
            arguments = listOf(
                androidx.navigation.navArgument("peerUid") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("peerName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val peerUid = backStackEntry.arguments?.getString("peerUid") ?: ""
            val peerName = backStackEntry.arguments?.getString("peerName") ?: ""
            ChatRoomPage(
                peerUid = peerUid,
                peerName = peerName,
                navController = navController
            )
        }

    }
}

private suspend fun shouldRouteToProfile(uid: String): Boolean {
    return try {
        val snapshot = firestore
            .collection("users")
            .document(uid)
            .get()
            .await()

        val isProfileComplete = snapshot.getBoolean("profileComplete") == true
        !snapshot.exists() || !isProfileComplete
    } catch (e: Exception) {
        Log.w("AppNavigation", "Failed to check profile status", e)
        true
    }
}
