package com.example.fitnessapp.views

import com.example.fitnessapp.viewmodel.SetGroupDisplayData

data class ExerciseHistoryUIState(
    val exerciseName: String = "",
    val historyItems: List<SetGroupDisplayData> = emptyList(),
    val xValues: List<Double> = emptyList(),
    val volumeSeries: List<Double> = emptyList(),
    val oneRepMaxSeries: List<Double> = emptyList(),
    val isInProgress: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)