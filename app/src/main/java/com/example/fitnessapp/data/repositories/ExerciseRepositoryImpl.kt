package com.example.fitnessapp.data.repositories

import com.example.fitnessapp.data.Exercise
import com.example.fitnessapp.data.room.ExerciseDao
import com.example.fitnessapp.data.room.ExerciseEntity
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.room.SetGroupDao
import com.example.fitnessapp.data.room.WorkoutDao
import com.example.fitnessapp.data.room.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao,
    private val setGroupDao: SetGroupDao,
    private val workoutDao: WorkoutDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao.getExercises().map { exerciseEntities ->
            exerciseEntities.map { entity ->
                Exercise(
                    exerciseId = entity.exerciseId.toLong(),
                    name = entity.name
                )
            }
        }

    override suspend fun insertExercise(name: String): Int {
        val trimmedName = name.trim()
        val entity = ExerciseEntity(
            exerciseId = 0,
            name = trimmedName
        )
        return exerciseDao.insertExercise(entity).toInt()
    }

    override suspend fun getExerciseById(id: Long): Exercise? {
        return exerciseDao.getExerciseById(id.toInt())?.let { entity ->
            Exercise(
                exerciseId = entity.exerciseId.toLong(),
                name = entity.name
            )
        }
    }

    override fun getExerciseActivityById(exerciseId: Long, excludeCurrentWorkout: Boolean): Flow<List<SetGroup>> =
        setGroupDao.getSetGroupsWithEntriesByExerciseId(exerciseId=exerciseId)
            .map { groupWithEntriesList ->
                groupWithEntriesList.map { it.toDomain() }
            }

    override fun getWorkoutStartTimeForSetGroup(setGroupId: Long): Flow<Long> =
        setGroupDao
            .getWorkoutStartTimeForSetGroup(setGroupId.toInt())

    override suspend fun getGymNameForSetGroup(setGroupId: Long): String? =
        setGroupDao.getGymNameForSetGroup(setGroupId.toInt())

    override suspend fun getExerciseNameById(exerciseId: Long): String {
        return exerciseDao.getExerciseById(exerciseId.toInt())?.name ?: ""
    }

    override suspend fun updateExerciseName(exerciseId: Long, newName: String) {
        exerciseDao.updateExerciseName(exerciseId, newName.trim())
    }
}
