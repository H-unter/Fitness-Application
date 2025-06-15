package com.example.fitnessapp.data

class CurrentWorkoutRepositoryImpl : CurrentWorkoutRepository  {

    private var currentWorkout: Workout? = null

    override fun getCurrentWorkout(): Workout? {
        return currentWorkout
    }
}