package com.example.fitnessapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long  // return the rowId

    // get all historical workouts
    @Query("Select * From Workout")
    fun getWorkouts(): Flow<List<WorkoutEntity>>

    // get current workout
    @Query("SELECT * FROM Workout WHERE isInProgress = 1 LIMIT 1")
    fun getCurrentWorkout(): Flow<WorkoutEntity?>

    @Query("UPDATE Workout SET isInProgress = 0 WHERE workoutId = :id")
    suspend fun markFinished(id: Int)

    @Query("UPDATE Workout SET isInProgress = 1 WHERE workoutId = :id")
    suspend fun markInProgress(id: Int)
}
