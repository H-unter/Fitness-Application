package com.example.fitnessapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fitnessapp.data.CurrentWorkoutRepository

class CurrentWorkoutViewModel(
    private val currentWorkoutRepository: CurrentWorkoutRepository
): ViewModel() {
    fun getWorkout() = currentWorkoutRepository.getCurrentWorkout()
}