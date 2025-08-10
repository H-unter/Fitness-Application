package com.example.fitnessapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.repositories.CurrentWorkoutRepository
import com.example.fitnessapp.data.repositories.ExerciseRepository
import com.example.fitnessapp.data.repositories.GymRepository
import com.example.fitnessapp.data.SetEntry
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.views.CurrentWorkoutUIState
import com.example.fitnessapp.views.WorkoutValidationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CurrentWorkoutViewModel(
    private val workoutRepository: CurrentWorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val gymRepository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CurrentWorkoutUIState(
            currentWorkout = null,
            setGroups = emptyList(),
            exerciseUiList = emptyList(),
            gyms = emptyList(),
            selectedGym = null,
            validationState = WorkoutValidationState.Valid
        )
    )
    val uiState: StateFlow<CurrentWorkoutUIState> = _uiState.asStateFlow()

    init {
        workoutRepository.getCurrentWorkoutOrNull()
            .onEach { workout ->
                _uiState.value = _uiState.value.copy(currentWorkout = workout)
                updateSelectedGymFromWorkout()
                validateWorkout()
            }
            .launchIn(viewModelScope)

        workoutRepository.getSetGroups()
            .onEach { groups ->
                _uiState.value = _uiState.value.copy(
                    setGroups = groups,
                    exerciseUiList = groups.map { group ->
                        group.exerciseName to group.entries.map { entry -> entry.weight to entry.reps }
                    }
                )
                validateWorkout()
            }
            .launchIn(viewModelScope)

        gymRepository.getAllGyms()
            .onEach { gyms ->
                _uiState.value = _uiState.value.copy(gyms = gyms)
                updateSelectedGymFromWorkout()
                validateWorkout()
            }
            .launchIn(viewModelScope)
    }

    /**
     * Updates the selected gym based on the current workout's locationId
     */
    private fun updateSelectedGymFromWorkout() {
        val workout = _uiState.value.currentWorkout
        val gyms = _uiState.value.gyms
        if (workout != null && workout.gymId > 0 && gyms.isNotEmpty()) {
            gyms.find { it.id == workout.gymId }?.let { gym ->
                if (_uiState.value.selectedGym?.id != gym.id) {
                    _uiState.value = _uiState.value.copy(selectedGym = gym)
                }
            }
        }
    }

    /**
     * Validates the current workout state and updates the UI state validation
     */
    private fun validateWorkout() {
        val state = _uiState.value
        val newValidationState = when {
            state.selectedGym == null -> WorkoutValidationState.NoGymSelected
            state.setGroups.isEmpty() -> WorkoutValidationState.NoExercises
            state.setGroups.any { group ->
                group.entries.isEmpty() || group.entries.all { entry ->
                    entry.weight.isBlank() || entry.reps.isBlank()
                }
            } -> WorkoutValidationState.NoExercises
            !state.areAllSetsCompleted() -> WorkoutValidationState.UncompletedSets
            else -> WorkoutValidationState.Valid
        }
        if (state.validationState != newValidationState) {
            _uiState.value = state.copy(validationState = newValidationState)
        }
    }

    // Select a gym by its ID
    fun selectGym(gymId: Int) = viewModelScope.launch {
        gymRepository.getGymById(gymId)?.let { gym ->
            _uiState.value = _uiState.value.copy(selectedGym = gym)

            // Update workout's gym if a workout exists
            uiState.value.currentWorkout?.let { workout ->
                workoutRepository.updateWorkoutGym(workout.id.toLong(), gym.id)
            }
            validateWorkout()
        }
    }

    // start a brand new workout
    fun startNewWorkout(gymId: Int = 0) = viewModelScope.launch {
        val actualGymId = gymId.takeIf { it > 0 }
            ?: uiState.value.selectedGym?.id
            ?: 0
        workoutRepository.startNewWorkout(actualGymId)
    }

    fun createNewGym(name: String) = viewModelScope.launch {
        val gymId = gymRepository.insertGym(name)
        selectGym(gymId.toInt())
    }

    // mark the current workout as finished
    fun finishCurrentWorkout() = viewModelScope.launch {
        workoutRepository.finishCurrentWorkout(System.currentTimeMillis())
    }

    // Cancel the current workout and remove all progress
    fun cancelCurrentWorkout() = viewModelScope.launch {
        workoutRepository.cancelCurrentWorkout()
    }

    // add an exercise (setGroup) by its id
    fun addExerciseById(exerciseId: Long) = viewModelScope.launch {
        val selectedExercise = exerciseRepository.getExerciseById(exerciseId) ?: return@launch
        val workout = _uiState.value.currentWorkout ?: return@launch
        val newSetGroup = SetGroup(
            setGroupId = 0,
            workoutId = workout.id,
            exerciseId = exerciseId.toInt(),
            name = selectedExercise.name,
            weightUnit = WeightUnit.KG,
            exerciseName = selectedExercise.name,
            entries = listOf(SetEntry(weight = "", reps = ""))
        )
        workoutRepository.addSetGroupToWorkout(newSetGroup)
    }

    // add a setGroup by its name
    fun removeExercise(exerciseIndex: Int) = viewModelScope.launch {
        _uiState.value.setGroups.getOrNull(exerciseIndex)?.let {
            workoutRepository.removeExercise(it)
        }
    }

    // add a new set to a specific exercise
    fun addSetToExercise(exerciseIndex: Int) = viewModelScope.launch {
        workoutRepository.addSetToExercise(exerciseIndex)
    }

    // remove a set from a specific exercise
    fun removeSetFromExercise(exerciseIndex: Int, setIndex: Int) = viewModelScope.launch {
        workoutRepository.removeSetFromExercise(exerciseIndex, setIndex)
    }

    // update one set's weight in a given exercise
    fun updateSetWeight(exerciseIndex: Int, setIndex: Int, newWeight: String) = viewModelScope.launch {
        workoutRepository.updateSetWeight(exerciseIndex, setIndex, newWeight)
    }

    // Update one sets reps in a given exercise
    fun updateSetReps(exerciseIndex: Int, setIndex: Int, newReps: String) = viewModelScope.launch {
        workoutRepository.updateSetReps(exerciseIndex, setIndex, newReps)
    }

    // Update one set's completion status and persist to database
    fun toggleSetCompletion(exerciseIndex: Int, setIndex: Int, completed: Boolean) = viewModelScope.launch {
        val updatedSetGroups = _uiState.value.setGroups.mapIndexed { sgIndex, group ->
            if (sgIndex == exerciseIndex) {
                group.copy(entries = group.entries.mapIndexed { entryIndex, entry ->
                    if (entryIndex == setIndex) entry.copy(completed = completed) else entry
                })
            } else group
        }
        _uiState.value = _uiState.value.copy(setGroups = updatedSetGroups)
        workoutRepository.updateSetCompletion(exerciseIndex, setIndex, completed)
        validateWorkout()
    }

    fun updateSetGroupWeightUnit(exerciseIndex: Int, weightUnit: WeightUnit) = viewModelScope.launch {
        workoutRepository.updateSetGroupWeightUnit(exerciseIndex, weightUnit)
    }

}