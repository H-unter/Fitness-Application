package com.example.fitnessapp.data

import kotlinx.serialization.Serializable

@Serializable
data class SetItem (
    val weight: String,
    val reps: String
)