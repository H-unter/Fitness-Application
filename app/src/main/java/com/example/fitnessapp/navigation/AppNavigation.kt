package com.example.fitnessapp.navigation

import androidx.compose.runtime.*
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.fitnessapp.viewmodel.ThemePreference
import com.example.fitnessapp.viewmodel.WorkoutHistoryViewModel
import com.example.fitnessapp.views.*
import android.util.Log

@Composable
fun AppNavigation(
    themePreference: ThemePreference,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    var permissionsGranted by remember { mutableStateOf(false) }
    val workoutHistoryViewModel: WorkoutHistoryViewModel = koinViewModel()

    NavHost(navController = navController, startDestination = Screens.CurrentWorkoutScreen.route) {

        composable(Screens.CurrentWorkoutScreen.route) {
            CurrentWorkoutScreen(navController = navController)
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
                    if (permissionsGranted != granted) {
                        permissionsGranted = granted
                        Log.d("AppNavigation", "Permissions state updated to: $granted")
                    }
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
                    Log.d("AppNavigation", "Permissions revoked - state updated to false")
                }
            )
        }
    }
}
