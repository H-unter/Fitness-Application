package com.example.fitnessapp.data
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao
            .getExercises()
            .map { exerciseEntityList ->
                val exerciseDomainList = mutableListOf<Exercise>()
                for (exerciseEntity in exerciseEntityList) {
                    exerciseDomainList.add(
                        Exercise(
                            id   = exerciseEntity.exerciseId.toLong(),
                            name = exerciseEntity.name
                        )
                    )
                }
                exerciseDomainList
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

    override suspend fun getExerciseById(id: Long): Exercise? {
        val entity = exerciseDao.getExerciseById(id.toInt()) ?: return null
        return Exercise(
            id   = entity.exerciseId.toLong(),
            name = entity.name
        )
    }
}
