package com.example.fitnessapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao,
    private val setGroupDao: SetGroupDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao.getExercises().map { exerciseEntities ->
            exerciseEntities.map { entity ->
                Exercise(
                    id = entity.exerciseId.toLong(),
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
                id = entity.exerciseId.toLong(),
                name = entity.name
            )
        }
    }

    override fun getExerciseActivityById(exerciseId: Long): Flow<List<SetGroup>> =
        setGroupDao.getSetGroupsWithEntriesByExerciseId(exerciseId)
            .map { groupWithEntriesList ->
                groupWithEntriesList.map { it.toDomain() }
            }
}
