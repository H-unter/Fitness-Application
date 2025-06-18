package com.example.fitnessapp.data

// Current workout is a list of exercises

interface CurrentWorkoutRepository {
    fun getCurrentWorkout(): Workout?
}