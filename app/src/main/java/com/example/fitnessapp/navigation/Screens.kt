package com.example.fitnessapp.navigation

sealed class Screens(val route: String) {
    object CurrentWorkoutScreen : Screens("currentWorkout")
    object ExerciseListSelectionScreen : Screens("exerciseListSelection")
    object WorkoutHistoryScreen : Screens("workoutHistory")
    object ExerciseStatsScreen : Screens("exercise/{exerciseId}") {
        fun createRoute(exerciseId: Long): String = "exercise/$exerciseId"
    }
}