package com.example.fitnessapp.data

import kotlinx.serialization.Serializable

@Serializable
data class SetEntry (
    val weight: String,
    val reps: String,
//    val setEntryId: Int,
//    val setGroupId: Int,
//    val setIndex: Int
)