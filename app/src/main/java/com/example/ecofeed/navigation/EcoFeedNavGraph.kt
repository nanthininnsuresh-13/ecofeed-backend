package com.example.ecofeed.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ecofeed.ui.auth.AuthViewModel
import com.example.ecofeed.ui.auth.LoginScreen
import com.example.ecofeed.ui.auth.RoleSelectionScreen
import com.example.ecofeed.ui.auth.SignUpScreen
import com.example.ecofeed.ui.donor.DonorDashboardScreen
import androidx.compose.ui.platform.LocalContext
import com.example.ecofeed.ui.donor.FoodDonationFormScreen
import com.example.ecofeed.ui.donor.DonationHistoryScreen
import com.example.ecofeed.ui.ProfileScreen
import com.example.ecofeed.ui.ngo.NgoDashboardScreen
import com.example.ecofeed.ui.biogas.BiogasDashboardScreen
import com.example.ecofeed.ui.biogas.BiogasWasteDetailScreen
import com.example.ecofeed.ui.listing.FoodListingDetailScreen
import com.example.ecofeed.ui.about.AboutScreen
import com.example.ecofeed.ui.about.PrivacyPolicyScreen
import com.example.ecofeed.ui.settings.SettingsScreen
import com.example.ecofeed.ui.notifications.NotificationsScreen
import com.example.ecofeed.ui.history.ActivityHistoryScreen
import com.example.ecofeed.ui.settings.ChangePasswordScreen
import com.example.ecofeed.ui.profile.EditProfileScreen
import com.example.ecofeed.ui.feedback.FeedbackReceivedScreen
import com.example.ecofeed.ui.feedback.GiveFeedbackScreen

object EcoFeedRoutes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val ROLE_SELECTION = "role_selection"
    const val DONOR_DASHBOARD = "donor_dashboard"
    const val NGO_DASHBOARD = "ngo_dashboard"
    const val BIOGAS_DASHBOARD = "biogas_dashboard"
    const val DONATE_FOOD_FORM = "donate_food_form"
    const val FOOD_DETAIL = "food_detail/{id}"
    const val WASTE_DETAIL = "biogas_waste_detail_screen/{id}"
    const val NGO_MAP = "ngo_map"
    const val BIOGAS_ROUTE_MAP = "biogas_route_map"
    const val NOTIFICATIONS = "notifications"
    const val DONATION_HISTORY = "donation_history"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val CHANGE_PASSWORD = "change_password"
    const val ACTIVITY_HISTORY = "activity_history"
    const val PRIVACY_POLICY = "privacy_policy"
    const val FEEDBACK_RECEIVED = "feedback_received"
    const val GIVE_FEEDBACK = "give_feedback"
}

@Composable
fun EcoFeedNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsState()

    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = EcoFeedRoutes.LOGIN,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(200)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(200)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(200)) }
    ) {
        composable(EcoFeedRoutes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(EcoFeedRoutes.SIGNUP) },
                onLoginSuccess = {
                    when (uiState.role) {
                        "NGO" -> navController.navigate(EcoFeedRoutes.NGO_DASHBOARD)
                        "BIOGAS" -> navController.navigate(EcoFeedRoutes.BIOGAS_DASHBOARD)
                        else -> navController.navigate(EcoFeedRoutes.DONOR_DASHBOARD)
                    }
                }
            )
        }

        composable(EcoFeedRoutes.SIGNUP) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToRoleSelection = { navController.navigate(EcoFeedRoutes.ROLE_SELECTION) },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(EcoFeedRoutes.ROLE_SELECTION) {
            RoleSelectionScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onRegistrationComplete = { role ->
                    authViewModel.updateRole(role)
                    authViewModel.completeRegistration {
                        when (role) {
                            "DONOR" -> navController.navigate(EcoFeedRoutes.DONOR_DASHBOARD)
                            "NGO" -> navController.navigate(EcoFeedRoutes.NGO_DASHBOARD)
                            "BIOGAS" -> navController.navigate(EcoFeedRoutes.BIOGAS_DASHBOARD)
                        }
                    }
                }
            )
        }

        composable(EcoFeedRoutes.DONOR_DASHBOARD) {
            DonorDashboardScreen(
                onDonateClick = { navController.navigate(EcoFeedRoutes.DONATE_FOOD_FORM) },
                onHistoryClick = { navController.navigate(EcoFeedRoutes.ACTIVITY_HISTORY) },
                onProfileClick = { navController.navigate(EcoFeedRoutes.PROFILE) },
                onNotificationsClick = { navController.navigate(EcoFeedRoutes.NOTIFICATIONS) },
                onFeedbackClick = { navController.navigate(EcoFeedRoutes.FEEDBACK_RECEIVED) },
                onSettingsClick = { navController.navigate(EcoFeedRoutes.SETTINGS) },
                onAboutClick = { navController.navigate(EcoFeedRoutes.ABOUT) },
                onLogout = {
                    authViewModel.logout(context) {
                        navController.navigate(EcoFeedRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(EcoFeedRoutes.NGO_DASHBOARD) {
            NgoDashboardScreen(
                ngoId = uiState.userId ?: "",
                onNavigateToDetail = { id -> navController.navigate("food_detail/$id") },
                onViewMapClicked = { navController.navigate(EcoFeedRoutes.NGO_MAP) },
                onNotificationsClicked = { navController.navigate(EcoFeedRoutes.NOTIFICATIONS) },
                onFeedbackClick = { navController.navigate(EcoFeedRoutes.GIVE_FEEDBACK) },
                onProfileClick = { navController.navigate(EcoFeedRoutes.PROFILE) },
                onHistoryClick = { navController.navigate(EcoFeedRoutes.ACTIVITY_HISTORY) },
                onSettingsClick = { navController.navigate(EcoFeedRoutes.SETTINGS) },
                onAboutClick = { navController.navigate(EcoFeedRoutes.ABOUT) },
                onLogout = {
                    authViewModel.logout(context) {
                        navController.navigate(EcoFeedRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(EcoFeedRoutes.BIOGAS_DASHBOARD) {
            BiogasDashboardScreen(
                biogasPartnerId = uiState.userId ?: "",
                onNavigateToDetail = { id -> navController.navigate("biogas_waste_detail_screen/$id") },
                onNotificationsClicked = { navController.navigate(EcoFeedRoutes.NOTIFICATIONS) },
                onFeedbackClick = { navController.navigate(EcoFeedRoutes.GIVE_FEEDBACK) },
                onProfileClick = { navController.navigate(EcoFeedRoutes.PROFILE) },
                onHistoryClick = { navController.navigate(EcoFeedRoutes.ACTIVITY_HISTORY) },
                onSettingsClick = { navController.navigate(EcoFeedRoutes.SETTINGS) },
                onAboutClick = { navController.navigate(EcoFeedRoutes.ABOUT) },
                onLogout = {
                    authViewModel.logout(context) {
                        navController.navigate(EcoFeedRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(EcoFeedRoutes.DONATE_FOOD_FORM) {
            FoodDonationFormScreen(
                donorId = uiState.userId ?: "",
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { navController.popBackStack() }
            )
        }

        composable(
            route = EcoFeedRoutes.FOOD_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val donationId = backStackEntry.arguments?.getString("id") ?: ""
            FoodListingDetailScreen(
                donationId = donationId,
                isEdible = true,
                acceptedBy = uiState.userId ?: "",
                onBack = { navController.popBackStack() },
                onAcceptClick = { navController.popBackStack() },
                onDirectionsClick = { /* Map directions */ }
            )
        }

        composable(EcoFeedRoutes.WASTE_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val donationId = backStackEntry.arguments?.getString("id") ?: ""
            BiogasWasteDetailScreen(
                donationId = donationId,
                onBack = { navController.popBackStack() },
                onAcceptPickup = { navController.popBackStack() }
            )
        }

        composable(EcoFeedRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onNotificationClick = { _ -> },
                authViewModel = authViewModel
            )
        }

        composable(EcoFeedRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogoutSuccess = {
                    navController.navigate(EcoFeedRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfileClick = { navController.navigate(EcoFeedRoutes.PROFILE) },
                onChangePasswordClick = { navController.navigate(EcoFeedRoutes.CHANGE_PASSWORD) },
                viewModel = authViewModel
            )
        }

        composable(EcoFeedRoutes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                userId = uiState.userId ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable(EcoFeedRoutes.ACTIVITY_HISTORY) {
            ActivityHistoryScreen(
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }

        composable(EcoFeedRoutes.ABOUT) {
            AboutScreen(
                onBack = { navController.popBackStack() },
                onPrivacyPolicyClick = { navController.navigate(EcoFeedRoutes.PRIVACY_POLICY) }
            )
        }

        composable(EcoFeedRoutes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Placeholder routes for maps
        composable(EcoFeedRoutes.NGO_MAP) { /* Map View Implementation */ }
        composable(EcoFeedRoutes.BIOGAS_ROUTE_MAP) { /* Route Map View Implementation */ }

        composable(EcoFeedRoutes.DONATION_HISTORY) {
            DonationHistoryScreen(
                donorId = uiState.userId ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable(EcoFeedRoutes.PROFILE) {
            ProfileScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onEditClick = { navController.navigate(EcoFeedRoutes.EDIT_PROFILE) },
                onChangePasswordClick = { navController.navigate(EcoFeedRoutes.CHANGE_PASSWORD) }
            )
        }

        composable(EcoFeedRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(EcoFeedRoutes.FEEDBACK_RECEIVED) {
            FeedbackReceivedScreen(
                donorId = uiState.userId ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable(EcoFeedRoutes.GIVE_FEEDBACK) {
            GiveFeedbackScreen(
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
    }
}
