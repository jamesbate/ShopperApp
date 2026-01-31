package com.shopperapp.ui.navigation.destinations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.shopperapp.ui.screens.SplashScreen

fun NavGraphBuilder.splashNavigation(navController: NavController) {
    composable(Screen.Splash.route) {
        SplashScreen(navController)
    }
}