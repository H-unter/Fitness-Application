package com.example.fitnessapp.data.repositories

import com.example.fitnessapp.data.room.WorkoutEntity
import com.example.fitnessapp.data.room.WorkoutWithSetGroupsAndEntries
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>
    fun getWorkoutWithSetGroupsAndEntries(id: Int): Flow<WorkoutWithSetGroupsAndEntries>
    suspend fun updateHealthConnectSynced(workoutId: Int, synced: Boolean)
}
