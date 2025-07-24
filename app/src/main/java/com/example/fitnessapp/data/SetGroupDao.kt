package com.example.fitnessapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SetGroupDao {

    // insert or replace an entire set‐group.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetGroup(group: SetGroupEntity): Long

    // delete one set‐group by its id.
    @Query("DELETE FROM SetGroup WHERE SetGroupid = :groupId")
    suspend fun deleteSetGroupById(groupId: Int)

    // update an existing set‐group by modifying its SetItems list
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSetGroup(group: SetGroupEntity)

    // return all historical set groups that match a particular exercise id
    @Transaction
    @Query("SELECT * FROM SetGroup WHERE exerciseId = :exerciseId")
    fun getSetGroupsWithEntriesByExerciseId(exerciseId: Long): Flow<List<SetGroupWithEntries>>

    @Transaction
    @Query("""
      SELECT * FROM Workout
       WHERE workoutId = (SELECT workoutId FROM SetGroup WHERE setGroupId = :setGroupId)
    """)
    fun getWorkoutForSetGroup(setGroupId: Int): Flow<WorkoutEntity>

    @Query("""
    SELECT workout.startTime
      FROM Workout AS workout
      JOIN SetGroup AS setGroup
        ON workout.workoutId = setGroup.workoutId
     WHERE setGroup.setGroupId = :setGroupId
     LIMIT 1
  """)
    fun getWorkoutStartTimeForSetGroup(setGroupId: Int): Flow<Long>

}