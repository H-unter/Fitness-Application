package com.example.fitnessapp.views

import com.example.fitnessapp.viewmodel.SetGroupDisplayData

data class ExerciseHistoryUIState(
    val exerciseName: String = "",                        // the title bar
    val historyItems: List<SetGroupDisplayData> = emptyList(), // for the lazy‐column
    val xValues: List<Double> = emptyList(),               // for the plot’s X axis
    val volumeSeries: List<Double> = emptyList(),          // for the plot’s volume line/columns
    val oneRepMaxSeries: List<Double> = emptyList(),       // for the plot’s 1RM line
    val isLoading: Boolean = false,                        // if you show a spinner
    val errorMessage: String? = null                       // if something went wrong
)