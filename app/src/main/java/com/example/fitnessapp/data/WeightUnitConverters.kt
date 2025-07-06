package com.example.fitnessapp.data

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WeightUnitConverters {
    @TypeConverter
    fun convertWeightUnitToString(weightUnit: WeightUnit): String {
        return Json.encodeToString(weightUnit)
    }

    @TypeConverter
    fun convertStringToWeightUnit(weightUnit: String): WeightUnit {
        return Json.decodeFromString(weightUnit)
    }
}