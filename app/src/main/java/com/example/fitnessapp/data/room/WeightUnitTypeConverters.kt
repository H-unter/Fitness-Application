package com.example.fitnessapp.data.room

import androidx.room.TypeConverter
import com.example.fitnessapp.data.WeightUnit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WeightUnitTypeConverters {
    @TypeConverter
    fun convertWeightUnitToString(weightUnit: WeightUnit): String {
        return Json.encodeToString(weightUnit)
    }

    @TypeConverter
    fun convertStringToWeightUnit(weightUnit: String): WeightUnit {
        return Json.decodeFromString(weightUnit)
    }
}