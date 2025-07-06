package com.example.fitnessapp.data

import androidx.room.TypeConverter
import com.example.fitnessapp.data.SetItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class SetTypeConverters {
    @TypeConverter
    fun convertExerciseSetsToString(sets: List<SetItem>): String {
        return Json.encodeToString(sets)
    }

    @TypeConverter
    fun convertStringToExerciseSets(sets: String): List<SetItem> {
        return Json.decodeFromString(sets)
    }
}