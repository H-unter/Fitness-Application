package com.example.fitnessapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    //update the gym of the current workout
    @Query("UPDATE Workout SET gymId = :gymId WHERE workoutId = :workoutId")
    suspend fun updateWorkoutGym(workoutId: Int, gymId: Int)

    @Query("UPDATE Workout SET isInProgress = 0, endTime = :endTime WHERE workoutId = :id")
    suspend fun markFinished(id: Int, endTime: Long)

    @Query("UPDATE Workout SET isInProgress = 1 WHERE workoutId = :id")
    suspend fun markInProgress(id: Int)

    // return all set groups given a workoutId
    @Transaction
    @Query("SELECT * FROM Workout WHERE workoutId = :id")
    fun getWorkoutWithSetGroupsAndEntries(id: Int): Flow<WorkoutWithSetGroupsAndEntries>

    @Query("""
    SELECT * FROM Workout
     WHERE workoutId IN (
       SELECT workoutId
         FROM SetGroup
        WHERE setGroupId = :setGroupId
     )
    LIMIT 1
  """)
    fun getWorkoutEntityBySetGroupId(
        setGroupId: Long
    ): Flow<WorkoutEntity>

    // Delete a workout by its ID
    @Query("DELETE FROM Workout WHERE workoutId = :workoutId")
    suspend fun deleteWorkoutById(workoutId: Int)

    @Query("UPDATE Workout SET isAndroidHealthConnectSynced = :synced WHERE workoutId = :workoutId")
    suspend fun updateHealthConnectSynced(workoutId: Int, synced: Boolean)
}
