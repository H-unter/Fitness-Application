package com.example.fitnessapp.views

import com.example.fitnessapp.data.Gym
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.Workout

/**
 * Aggregates all state for the CurrentWorkout screen.
 */
data class CurrentWorkoutUIState(
    val currentWorkout: Workout?,                                       // null if no workout started
    val setGroups: List<SetGroup>,                                      // raw domain groups
    val exerciseUiList: List<Pair<String, List<Pair<String, String>>>>,  // (exerciseName, list of (weight,reps))
    val gyms: List<Gym>,
    val selectedGym: Gym?,
    val validationState: WorkoutValidationState
)

/**
 * Represents the validation state of a workout
 */
enum class WorkoutValidationState {
    Valid,
    NoGymSelected,
    NoExercises;

    val message: String
        get() = when(this) {
            Valid -> "Are you sure you want to finish this workout?"
            NoGymSelected -> "Please select a gym before finishing your workout."
            NoExercises -> "Your workout has no exercises. Please add at least one exercise."
        }

    val canFinish: Boolean
        get() = this != NoGymSelected && this != NoExercises
}
