package com.tadamaps.mobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tadamaps.mobile.presentation.booking.BookingScreen
import com.tadamaps.mobile.presentation.detail.LocationDetailScreen
import com.tadamaps.mobile.presentation.history.HistoryScreen
import com.tadamaps.mobile.presentation.map.MapScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Map) {
        composable(Routes.Map) { entry ->
            MapScreen(navController = navController, backStackEntry = entry)
        }
        composable(
            route = "${Routes.LocationDetail}/{slot}",
            arguments = listOf(
                navArgument("slot") { type = NavType.StringType },
            ),
        ) { entry ->
            val slot = entry.arguments?.getString("slot").orEmpty()
            LocationDetailScreen(navController = navController, slotArg = slot)
        }
        composable(Routes.Booking) {
            BookingScreen(navController = navController)
        }
        composable(Routes.History) {
            HistoryScreen(navController = navController)
        }
    }
}
