package com.example.fitnessapp.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrentWorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val setGroupDao: SetGroupDao,
    private val dispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope
) : CurrentWorkoutRepository {


    private val _currentWorkout = MutableStateFlow<WorkoutEntity?>(null)
    override val currentWorkout: StateFlow<WorkoutEntity?> = _currentWorkout

    init {
        // collect the data access objects
        scope.launch {
            workoutDao.getCurrentWorkout()
                .collect { workoutEntity ->
                    _currentWorkout.value = workoutEntity
                }
        }
    }

    // start a new workout, returning the new rowId
    override suspend fun startNewWorkout(gymId: Int): Long = withContext(dispatcher) {
        val startTimestamp = System.currentTimeMillis()
        workoutDao.insertWorkout(
            WorkoutEntity(
                workoutId    = 0,
                gymId        = gymId,
                startTime    = startTimestamp,
                endTime      = startTimestamp,
                isInProgress = true
            )
        )
    }

    // finish the current workout
    override suspend fun finishCurrentWorkout() {
        withContext(dispatcher) {
            _currentWorkout.value
                ?.let { current -> workoutDao.markFinished(current.workoutId) }
        }
    }

    // get the full workout
    override fun getCurrentWorkout(): Flow<Workout> =
        workoutDao.getCurrentWorkout()
            .filterNotNull()
            .flatMapLatest { workoutEntity ->
                setGroupDao.getWorkoutSetGroups(workoutEntity.workoutId)
                    .map { setGroupEntities ->
                        Workout(
                            id         = workoutEntity.workoutId,
                            locationId = workoutEntity.gymId,
                            startTime  = workoutEntity.startTime,
                            endTime    = workoutEntity.endTime,
                            setGroups  = setGroupEntities.map { setGroupEntity ->
                                SetGroup(
                                    id         = setGroupEntity.setGroupId,
                                    workoutId  = setGroupEntity.workoutId,
                                    name       = "Exercise" /* TODO: implement some kind of enumeration, or perhaps something linked to its exercise id (future foreign key */,
                                    weightUnit = setGroupEntity.weightUnit,
                                    sets       = setGroupEntity.sets
                                )
                            }
                        )
                    }
            }

    // return list of SetGroups
    override fun getSetGroups(): Flow<List<SetGroup>> =
        workoutDao.getCurrentWorkout()
            .filterNotNull()
            .flatMapLatest { workoutEntity ->
                setGroupDao.getWorkoutSetGroups(workoutEntity.workoutId)
                    .map { setGroupEntities ->
                        setGroupEntities.map { setGroupEntity ->
                            SetGroup(
                                id         = setGroupEntity.setGroupId,
                                workoutId  = setGroupEntity.workoutId,
                                name       = "" /* TODO: implement some kind of enumeration, or perhaps something linked to its exercise id (future foreign key */,
                                weightUnit = setGroupEntity.weightUnit,
                                sets       = setGroupEntity.sets
                            )
                        }
                    }
            }

    // add or remove an exercise (i.e. a SetGroup)
    override suspend fun addExercise(setGroup: SetGroup) {
        withContext(dispatcher) {
            setGroupDao.insertSetGroup(
                SetGroupEntity(
                    setGroupId = setGroup.id,
                    workoutId  = setGroup.workoutId,
                    exerciseId = 0,  /* TODO: implement some kind of enumeration, or perhaps something linked to its exercise id (future foreign key */
                    weightUnit = setGroup.weightUnit,
                    sets       = setGroup.sets
                )
            )
        }
    }

    override suspend fun removeExercise(setGroup: SetGroup) {
        withContext(dispatcher) {
            setGroupDao.deleteSetGroupById(setGroup.id)
        }
    }

    override suspend fun addSetToExercise(exerciseIndex: Int) {
        withContext(dispatcher) {
            val currentWorkoutEntity = _currentWorkout.value ?: return@withContext
            val setGroupEntities = setGroupDao
                .getWorkoutSetGroups(currentWorkoutEntity.workoutId)
                .first()
            val targetGroup = setGroupEntities[exerciseIndex]

            // Append a default SetItem
            val updatedSets = targetGroup.sets.toMutableList().apply {
                add(SetItem(weight = "0", reps = "0"))
            }

            setGroupDao.updateSetGroup(
                SetGroupEntity(
                    setGroupId = targetGroup.setGroupId,
                    workoutId  = targetGroup.workoutId,
                    exerciseId = targetGroup.exerciseId,
                    weightUnit = targetGroup.weightUnit,
                    sets       = updatedSets
                )
            )
        }
    }

    override suspend fun removeSetFromExercise(exerciseIndex: Int, setIndex: Int) {
        withContext(dispatcher) {
            val currentWorkoutEntity = _currentWorkout.value ?: return@withContext
            val setGroupEntities = setGroupDao
                .getWorkoutSetGroups(currentWorkoutEntity.workoutId)
                .first()
            val targetGroup = setGroupEntities[exerciseIndex]

            val updatedSets = targetGroup.sets.toMutableList().apply {
                if (setIndex in indices) removeAt(setIndex)
            }

            setGroupDao.updateSetGroup(
                SetGroupEntity(
                    setGroupId = targetGroup.setGroupId,
                    workoutId  = targetGroup.workoutId,
                    exerciseId = targetGroup.exerciseId,
                    weightUnit = targetGroup.weightUnit,
                    sets       = updatedSets
                )
            )
        }
    }

    // update a single SetItem weight by rewriting its SetGroupEntity
    override suspend fun updateSetWeight(
        exerciseIndex: Int,
        setIndex: Int,
        weight: String
    ) {
        withContext(dispatcher) {
            val currentWorkoutEntity = _currentWorkout.value ?: return@withContext
            val setGroupEntities = setGroupDao
                .getWorkoutSetGroups(currentWorkoutEntity.workoutId)
                .first()
            val targetGroup = setGroupEntities[exerciseIndex]

            // create updated list of SetItems
            val updatedSets = targetGroup.sets.toMutableList().apply {
                this[setIndex] = this[setIndex].copy(weight = weight)
            }

            setGroupDao.updateSetGroup(
                SetGroupEntity(
                    setGroupId = targetGroup.setGroupId,
                    workoutId  = targetGroup.workoutId,
                    exerciseId = targetGroup.exerciseId,
                    weightUnit = targetGroup.weightUnit,
                    sets       = updatedSets
                )
            )
        }
    }

    // update a single SetItem reps by rewriting its SetGroupEntity
    override suspend fun updateSetReps(
        exerciseIndex: Int,
        setIndex: Int,
        reps: String
    ) {
        withContext(dispatcher) {
            val currentWorkoutEntity = _currentWorkout.value ?: return@withContext
            val setGroupEntities = setGroupDao
                .getWorkoutSetGroups(currentWorkoutEntity.workoutId)
                .first()
            val targetGroup = setGroupEntities[exerciseIndex]

            val updatedSets = targetGroup.sets.toMutableList().apply {
                this[setIndex] = this[setIndex].copy(reps = reps)
            }

            setGroupDao.updateSetGroup(
                SetGroupEntity(
                    setGroupId = targetGroup.setGroupId,
                    workoutId  = targetGroup.workoutId,
                    exerciseId = targetGroup.exerciseId,
                    weightUnit = targetGroup.weightUnit,
                    sets       = updatedSets
                )
            )
        }
    }
}
