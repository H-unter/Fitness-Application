package com.example.fitnessapp.data
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao,
    private val setGroupDao: SetGroupDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao
            .getExercises()
            .map { entityList ->
                entityList.map { entity ->
                    Exercise(
                        id   = entity.exerciseId.toLong(),
                        name = entity.name
                    )
                }
            }

    override suspend fun insertExercise(name: String) {
        val trimmedName = name.trim()
        val newExerciseEntity = ExerciseEntity(
            exerciseId          = 0,               // auto-assign
//            TODO: add exercise variation, perhaps through a secondary table
            name                = trimmedName
        )
        exerciseDao.insertExercise(newExerciseEntity)
    }

    override suspend fun getExerciseById(exerciseId: Long): Exercise? {
        val entity = exerciseDao.getExerciseById(exerciseId.toInt()) ?: return null
        return Exercise(
            id   = entity.exerciseId.toLong(),
            name = entity.name
        )
    }

    override suspend fun getExerciseActivityById(exerciseId: Long): Flow<List<SetGroup>> =
        setGroupDao
            .getExerciseActivityById(exerciseId)
            .map { entityList ->
                entityList.map { entity ->
                    SetGroup(
                        id         = entity.setGroupId,
                        workoutId  = entity.workoutId,
                        name       = entity.name,
                        weightUnit = entity.weightUnit,
                        sets       = entity.sets
                    )
                }
            }
}
