package com.example.fitnessapp.data

interface CurrentWorkoutRepository {
    fun getCurrentWorkout(): Workout?
}