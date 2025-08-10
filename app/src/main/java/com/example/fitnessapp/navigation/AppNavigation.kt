package com.example.fitnessapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitnessapp.viewmodel.ThemePreference
import com.example.fitnessapp.viewmodel.WorkoutHistoryViewModel
import com.example.fitnessapp.views.AppSettingsScreen
import com.example.fitnessapp.views.CurrentWorkoutScreen
import com.example.fitnessapp.views.ExerciseHistoryScreen
import com.example.fitnessapp.views.ExerciseListSelectionScreen
import com.example.fitnessapp.views.WorkoutHistoryScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    themePreference: ThemePreference,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val workoutHistoryViewModel: WorkoutHistoryViewModel = koinViewModel()

    var permissionsGranted by remember {
        mutableStateOf(workoutHistoryViewModel.uiState.value.permissionsGranted)
    }

    LaunchedEffect(Unit) {
        workoutHistoryViewModel.checkPermissionsOnly()
        workoutHistoryViewModel.uiState.collect { state ->
            permissionsGranted = state.permissionsGranted
        }
    }

    NavHost(navController = navController, startDestination = Screens.CurrentWorkoutScreen.route) {

        composable(Screens.CurrentWorkoutScreen.route) {
            CurrentWorkoutScreen(
                navController = navController
            )
        }

        composable(Screens.ExerciseListSelectionScreen.route) {
            ExerciseListSelectionScreen(
                navController = navController,
                isDarkMode = themePreference.isDarkMode
            )
        }

        composable(Screens.WorkoutHistoryScreen.route) {
            WorkoutHistoryScreen(
                navController = navController,
                viewModel = workoutHistoryViewModel,
                onPermissionsChecked = { granted ->
                    permissionsGranted = granted
                },
                overridePermissionsGranted = permissionsGranted
            )
        }

        composable(
            route = Screens.ExerciseStatsScreen.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) {
            ExerciseHistoryScreen(
                navController = navController
            )
        }

        composable(Screens.SettingsScreen.route) {
            AppSettingsScreen(
                isDarkMode = themePreference.isDarkMode,
                onDarkModeToggle = onThemeChange,
                navController = navController,
                permissionsGranted = permissionsGranted,
                onPermissionsRevoked = {
                    permissionsGranted = false
                    workoutHistoryViewModel.onPermissionsRevoked()
                }
            )
        }
    }
}
