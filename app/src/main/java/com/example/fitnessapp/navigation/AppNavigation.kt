package com.example.fitnessapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.CurrentWorkoutScreen
import com.example.fitnessapp.ExerciseListSelectionScreen
import com.example.fitnessapp.ExerciseHistoryScreen
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // one shared ViewModel to handle adding picked exercises
    val workoutViewModel: CurrentWorkoutViewModel = koinViewModel()

    NavHost(
        navController    = navController,
        startDestination = Screens.CurrentWorkoutScreen.route
    ) {
        // 1) Main workout screen
        composable(Screens.CurrentWorkoutScreen.route) { backStackEntry ->


            val workoutViewModel: CurrentWorkoutViewModel = koinViewModel()

            backStackEntry.savedStateHandle
                .getLiveData<Long>("selected_exercise_id")
                .observe(backStackEntry) { exerciseId ->
                    workoutViewModel.addExerciseById(exerciseId)
                    backStackEntry.savedStateHandle.remove<Long>("selected_exercise_id")
                }

            CurrentWorkoutScreen(
                onAddExercise = {
                    navController.navigate(Screens.ExerciseListSelectionScreen.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screens.ExerciseStatsScreen.route)
                }
            )
        }

        // 2) Stats screen
        composable(Screens.ExerciseStatsScreen.route) {
            ExerciseHistoryScreen()
        }

        // 3) Exercise picker screen
        composable(Screens.ExerciseListSelectionScreen.route) {
            ExerciseListSelectionScreen(
                onExerciseSelected = { pickedExercise ->
                    // store only the ID, not the whole object
                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_exercise_id", pickedExercise.id)
                    navController.popBackStack()
                }
            )
        }
    }


}
