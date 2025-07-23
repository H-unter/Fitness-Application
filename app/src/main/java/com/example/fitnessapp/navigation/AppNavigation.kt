package com.example.fitnessapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitnessapp.views.CurrentWorkoutScreen
import com.example.fitnessapp.views.ExerciseListSelectionScreen
import com.example.fitnessapp.views.ExerciseHistoryScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.CurrentWorkoutScreen.route) {

        composable(Screens.CurrentWorkoutScreen.route) {
            CurrentWorkoutScreen(navController = navController)
        }

        composable(Screens.ExerciseListSelectionScreen.route) {
            ExerciseListSelectionScreen(navController = navController)
        }

        composable(
            route = Screens.ExerciseStatsScreen.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) {
            ExerciseHistoryScreen(
                navController = navController
            )
        }

    }
}
