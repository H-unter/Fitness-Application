package com.example.fitnessapp.data

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class SetTypeConverters {
    @TypeConverter
    fun convertExerciseSetsToString(sets: List<SetEntry>): String {
        return Json.encodeToString(sets)
    }

    @TypeConverter
    fun convertStringToExerciseSets(sets: String): List<SetEntry> {
        return Json.decodeFromString(sets)
    }
}