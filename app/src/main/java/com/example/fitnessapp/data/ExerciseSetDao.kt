package com.example.fitnessapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseSetDao{
    @Insert
    suspend fun insertExerciseSet(exerciseSet: ExerciseSetEntity)

    @Query("Select * From `set`")
    fun getExerciseSets(): Flow<List<ExerciseSetEntity>>
}
