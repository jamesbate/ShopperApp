package com.shopperapp.ui.navigation.destinations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.shopperapp.ui.screens.auth.LoginScreen
import com.shopperapp.ui.screens.auth.RegisterScreen

fun NavGraphBuilder.authNavigation(navController: NavController) {
    composable(Screen.Login.route) {
        LoginScreen(navController)
    }
    composable(Screen.Register.route) {
        RegisterScreen(navController)
    }
}