package com.example.fitnessapp.navigation

sealed class Screens(val route: String) {
    object CurrentWorkoutScreen : Screens("currentWorkout")
    object ExerciseStatsScreen : Screens("exercise")
    object ExerciseListSelectionScreen: Screens("exerciseListSelection")
}