package com.shopperapp.ui.navigation.destinations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.shopperapp.ui.screens.*

fun NavGraphBuilder.mainNavigation(navController: NavController) {
    composable(Screen.ShoppingList.route) {
        ShoppingListScreen(navController)
    }
    composable(Screen.Scanner.route) {
        ScannerScreen(navController)
    }
    composable(Screen.Finance.route) {
        FinanceScreen(navController)
    }
    composable(Screen.Expiry.route) {
        ExpiryScreen(navController)
    }
    composable(Screen.Profile.route) {
        ProfileScreen(navController)
    }
    composable(Screen.GroupSettings.route) {
        GroupSettingsScreen(navController)
    }
}