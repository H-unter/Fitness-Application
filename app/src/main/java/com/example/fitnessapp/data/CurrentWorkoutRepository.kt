package com.example.fitnessapp.data
import kotlinx.coroutines.flow.Flow

// Current workout is a list of exercises

interface CurrentWorkoutRepository {
    fun getCurrentWorkout(): Workout?
}