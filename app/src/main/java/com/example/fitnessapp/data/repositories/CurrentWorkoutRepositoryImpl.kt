package com.example.fitnessapp.data.repositories

import com.example.fitnessapp.data.room.ExerciseDao
import com.example.fitnessapp.data.room.SetEntryDao
import com.example.fitnessapp.data.room.SetEntryEntity
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.data.room.SetGroupDao
import com.example.fitnessapp.data.room.SetGroupEntity
import com.example.fitnessapp.data.Workout
import com.example.fitnessapp.data.room.WorkoutDao
import com.example.fitnessapp.data.room.WorkoutEntity
import com.example.fitnessapp.data.room.toDomain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrentWorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val setGroupDao: SetGroupDao,
    private val setEntryDao: SetEntryDao,
    private val dispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope
) : CurrentWorkoutRepository {

    private val _currentWorkout = MutableStateFlow<WorkoutEntity?>(null)
    override val currentWorkout: StateFlow<WorkoutEntity?> = _currentWorkout

    init {
        scope.launch {
            workoutDao.getCurrentWorkout().collect { workoutEntity ->
                _currentWorkout.value = workoutEntity
            }
        }
    }

    override suspend fun startNewWorkout(gymId: Int): Long = withContext(dispatcher) {
        val now = System.currentTimeMillis()
        workoutDao.insertWorkout(
            WorkoutEntity(
                workoutId = 0,
                gymId = gymId,
                startTime = now,
                endTime = now,
                isInProgress = true
            )
        )
    }

    override suspend fun finishCurrentWorkout(endTime: Long) {
        withContext(dispatcher) {
            _currentWorkout.value?.let { workoutEntity ->
                workoutDao.markFinished(workoutEntity.workoutId, endTime)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCurrentWorkoutOrNull(): Flow<Workout?> =
        workoutDao
            .getCurrentWorkout()
            .flatMapLatest { entity ->
                if (entity == null) {
                    flowOf(null)
                } else {
                    workoutDao
                        .getWorkoutWithSetGroupsAndEntries(entity.workoutId)
                        .map { it.toDomain() }
                }
            }

    override suspend fun updateWorkoutGym(workoutId: Long, gymId: Int) {
        withContext(dispatcher) {
            workoutDao.updateWorkoutGym(workoutId.toInt(), gymId)
        }
    }
    override fun getSetGroups(): Flow<List<SetGroup>> =
        getCurrentWorkoutOrNull()
            .map { workout -> workout?.setGroups ?: emptyList() }

    override suspend fun addSetGroupToWorkout(setGroup: SetGroup) {
        withContext(dispatcher) {
            val currentWorkout = _currentWorkout.value ?: return@withContext

            val exercise = setGroup.exerciseName.trim()
            val matchingExercise = exerciseDao.getExerciseByName(exercise)
            val newSetGroup = SetGroupEntity(
                setGroupId = 0,
                workoutId = currentWorkout.workoutId,
                exerciseId = matchingExercise.exerciseId,
                weightUnit = setGroup.weightUnit
            )
            val setGroupId = setGroupDao.insertSetGroup(newSetGroup).toInt()

            setGroup.entries.forEachIndexed { index, setItem ->
                // Convert empty string to null to properly represent empty fields
                val weight = if (setItem.weight.isEmpty()) null else setItem.weight.toFloatOrNull()
                val reps = if (setItem.reps.isEmpty()) null else setItem.reps.toIntOrNull()

                setEntryDao.insertSetEntry(
                    SetEntryEntity(
                        setEntryId = 0, // auto generated
                        setGroupId = setGroupId,
                        setIndex = index,
                        weight = weight, // Use null for empty strings
                        reps = reps    // Use null for empty strings
                    )
                )
            }
        }
    }

    override suspend fun removeExercise(setGroup: SetGroup) {
        withContext(dispatcher) {
            setGroupDao.deleteSetGroupById(setGroup.setGroupId)
        }
    }

    override suspend fun addSetToExercise(exerciseIndex: Int) {
        withContext(dispatcher) {
            val currentWorkout = _currentWorkout.value ?: return@withContext
            val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(currentWorkout.workoutId).first()
            val targetSetGroup = workoutWithGroups.setGroups.getOrNull(exerciseIndex) ?: return@withContext
            val nextSetIndex = (targetSetGroup.entries.maxOfOrNull { it.setIndex } ?: -1) + 1
            setEntryDao.insertSetEntry(
                SetEntryEntity(
                    setEntryId = 0, // auto generated
                    setGroupId = targetSetGroup.group.setGroupId,
                    setIndex = nextSetIndex,
                    weight = null, // Use null instead of 0f to represent empty string
                    reps = null   // Use null instead of 0 to represent empty string
                )
            )
        }
    }

    override suspend fun removeSetFromExercise(exerciseIndex: Int, setIndex: Int) {
        withContext(dispatcher) {
            val currentWorkout = _currentWorkout.value ?: return@withContext
            val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(currentWorkout.workoutId).first()
            val targetSetGroup = workoutWithGroups.setGroups.getOrNull(exerciseIndex) ?: return@withContext
            val targetSetEntry = targetSetGroup.entries.find { it.setIndex == setIndex } ?: return@withContext
            setEntryDao.deleteSetEntryById(targetSetEntry.setEntryId)
        }
    }

    override suspend fun updateSetWeight(exerciseIndex: Int, setIndex: Int, weight: String) {
        withContext(dispatcher) {
            val currentWorkout = _currentWorkout.value ?: return@withContext
            val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(currentWorkout.workoutId).first()
            val targetSetGroup = workoutWithGroups.setGroups.getOrNull(exerciseIndex) ?: return@withContext
            val targetSetEntry = targetSetGroup.entries.find { it.setIndex == setIndex } ?: return@withContext

            // Store the raw input weight to preserve exact user input
            val weightValue = if (weight.isEmpty()) null
                             else try {
                                 weight.toFloat()
                             } catch (e: NumberFormatException) {
                                 weight.toFloatOrNull() ?: 0f
                             }
            setEntryDao.updateSetEntry(targetSetEntry.copy(weight = weightValue))
        }
    }

    override suspend fun updateSetReps(exerciseIndex: Int, setIndex: Int, reps: String) {
        withContext(dispatcher) {
            val currentWorkout = _currentWorkout.value ?: return@withContext
            val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(currentWorkout.workoutId).first()
            val targetSetGroup = workoutWithGroups.setGroups.getOrNull(exerciseIndex) ?: return@withContext
            val targetSetEntry = targetSetGroup.entries.find { it.setIndex == setIndex } ?: return@withContext

            // Convert empty string to null
            val repsValue = if (reps.isEmpty()) null else reps.toIntOrNull() ?: 0
            setEntryDao.updateSetEntry(targetSetEntry.copy(reps = repsValue))
        }
    }

    override suspend fun updateSetCompletion(exerciseIndex: Int, setIndex: Int, completed: Boolean) {
        withContext(dispatcher) {
            val currentWorkout = _currentWorkout.value ?: return@withContext
            val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(currentWorkout.workoutId).first()
            val targetSetGroup = workoutWithGroups.setGroups.getOrNull(exerciseIndex) ?: return@withContext
            val targetSetEntry = targetSetGroup.entries.find { it.setIndex == setIndex } ?: return@withContext
            
            setEntryDao.updateSetEntry(targetSetEntry.copy(completed = completed))
        }
    }

    override suspend fun cancelCurrentWorkout() {
        withContext(dispatcher) {
            val workout = _currentWorkout.value ?: return@withContext
            setGroupDao.deleteSetGroupsByWorkoutId(workout.workoutId)
            workoutDao.deleteWorkoutById(workout.workoutId)
            _currentWorkout.value = null
        }
    }

    override suspend fun updateSetGroupWeightUnit(exerciseIndex: Int, weightUnit: WeightUnit) {
        withContext(dispatcher) {
            val currentWorkout = _currentWorkout.value ?: return@withContext
            val workoutWithGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(currentWorkout.workoutId).first()
            val targetSetGroup = workoutWithGroups.setGroups.getOrNull(exerciseIndex)?.group ?: return@withContext

            setGroupDao.updateSetGroup(targetSetGroup.copy(weightUnit = weightUnit))
        }
    }
}
