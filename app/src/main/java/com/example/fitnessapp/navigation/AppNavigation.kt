package com.example.fitnessapp.navigation

import android.R.attr.type
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitnessapp.CurrentWorkoutScreen
import com.example.fitnessapp.ExerciseListSelectionScreen
import com.example.fitnessapp.ExerciseHistoryScreen
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import com.example.fitnessapp.viewmodel.ExerciseHistoryViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
            ExerciseHistoryScreen()
        }

    }
}
