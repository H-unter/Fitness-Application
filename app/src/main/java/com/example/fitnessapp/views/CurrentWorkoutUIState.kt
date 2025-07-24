package com.example.fitnessapp.views

import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.Workout

/**
 * Aggregates all state for the CurrentWorkout screen.
 */
data class CurrentWorkoutUIState(
    val currentWorkout: Workout?,                                       // null if no workout started
    val setGroups: List<SetGroup>,                                      // raw domain groups
    val exerciseUiList: List<Pair<String, List<Pair<String, String>>>>  // (exerciseName, list of (weight,reps))
)
