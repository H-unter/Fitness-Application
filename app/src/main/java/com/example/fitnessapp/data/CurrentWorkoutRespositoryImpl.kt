package com.example.fitnessapp.data

class CurrentWorkoutRespositoryImpl : CurrentWorkoutRepository  {

    private var currentWorkout: Workout? = null

    override fun getCurrentWorkout(): Workout? {
        return currentWorkout
    }
}