package com.shopperapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.shopperapp.ui.navigation.destinations.authNavigation
import com.shopperapp.ui.navigation.destinations.mainNavigation
import com.shopperapp.ui.navigation.destinations.splashNavigation

@Composable
fun ShopperNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        splashNavigation(navController)
        authNavigation(navController)
        mainNavigation(navController)
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ShoppingList : Screen("shopping_list")
    object Scanner : Screen("scanner")
    object Finance : Screen("finance")
    object Expiry : Screen("expiry")
    object Profile : Screen("profile")
    object GroupSettings : Screen("group_settings")
}