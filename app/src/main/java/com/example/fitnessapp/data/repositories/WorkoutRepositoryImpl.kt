package com.example.fitnessapp.data.repositories

import com.example.fitnessapp.data.room.WorkoutDao
import com.example.fitnessapp.data.room.WorkoutEntity
import com.example.fitnessapp.data.room.WorkoutWithSetGroupsAndEntries
import kotlinx.coroutines.flow.Flow

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {
    override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.getWorkouts()
    override fun getWorkoutWithSetGroupsAndEntries(id: Int): Flow<WorkoutWithSetGroupsAndEntries> =
        workoutDao.getWorkoutWithSetGroupsAndEntries(id)
    override suspend fun updateHealthConnectSynced(workoutId: Int, synced: Boolean) =
        workoutDao.updateHealthConnectSynced(workoutId, synced)
}

