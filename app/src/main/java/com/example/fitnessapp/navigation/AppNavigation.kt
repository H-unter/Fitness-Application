package com.example.fitnessapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.CurrentWorkoutScreen
import com.example.fitnessapp.ExerciseStatsScreen
import com.example.fitnessapp.navigation.Screens
import android.util.Log


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screens.CurrentWorkoutScreen.route
    ) {
        composable(Screens.CurrentWorkoutScreen.route) {
            CurrentWorkoutScreen(
                onNavigateToStats = {
                    Log.i("MY_MESSAGE", "Navigating to ExerciseStatsScreen")
                    navController.navigate(Screens.ExerciseStatsScreen.route)
                }
            )
        }

        composable(Screens.ExerciseStatsScreen.route) {
            ExerciseStatsScreen()
        }
    }
}
