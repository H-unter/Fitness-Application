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
        // observe current workout
        workoutRepository.getCurrentWorkoutOrNull()
            .onEach { workout ->
                _uiState.value = _uiState.value.copy(currentWorkout = workout)
                validateWorkout()
            }
            .launchIn(viewModelScope)

        // observe set groups
        workoutRepository.getSetGroups()
            .onEach { groups ->
                _uiState.value = _uiState.value.copy(
                    setGroups = groups,
                    exerciseUiList = groups.map { group ->
                        group.exerciseName to group.entries.map { entry ->
                            entry.weight to entry.reps
                        }
                    }
                )
                validateWorkout()
            }
            .launchIn(viewModelScope)

        // load gyms
        gymRepository.getAllGyms()
            .onEach { gyms ->
                _uiState.value = _uiState.value.copy(gyms = gyms)

                // If we have a current workout, load its gym
                uiState.value.currentWorkout?.let { workout ->
                    if (workout.locationId > 0) {
                        val gym = gyms.find { it.id == workout.locationId }
                        _uiState.value = _uiState.value.copy(selectedGym = gym)
                        validateWorkout()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Validates the current workout state and updates the UI state validation
     */
    private fun validateWorkout() {
        val currentState = _uiState.value
        val newValidationState = when {
            currentState.selectedGym == null ->
                WorkoutValidationState.NoGymSelected
            currentState.setGroups.isEmpty() ->
                WorkoutValidationState.NoExercises

            currentState.setGroups.any { group ->
                group.entries.isEmpty() || group.entries.all { entry ->
                    entry.weight.isBlank() || entry.reps.isBlank()
                }
            } ->
                WorkoutValidationState.NoExercises

            else ->
                WorkoutValidationState.Valid
        }

        if (currentState.validationState != newValidationState) {
            _uiState.value = currentState.copy(validationState = newValidationState)
        }
    }

    fun selectGym(gymId: Int) = viewModelScope.launch {
        val gym = gymRepository.getGymById(gymId)
        if (gym != null) {
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

    // add an exercise (setGroup) by its id
    fun addExerciseById(exerciseId: Long) = viewModelScope.launch {
        val selectedExercise = exerciseRepository.getExerciseById(exerciseId) ?: return@launch
        val workout = _uiState.value.currentWorkout ?: return@launch

        val newSetGroup = SetGroup(
            setGroupId    = 0,
            workoutId     = workout.id,
            exerciseId    = exerciseId.toInt(),
            name          = selectedExercise.name,
            weightUnit    = WeightUnit.KG,
            exerciseName  = selectedExercise.name,
            entries       = listOf(SetEntry(weight = "", reps = ""))
        )

        workoutRepository.addSetGroupToWorkout(newSetGroup)
    }

    // remove an exercise SetGroup
    fun removeExercise(exerciseIndex: Int) = viewModelScope.launch {
        val setGroup = _uiState.value.setGroups.getOrNull(exerciseIndex) ?: return@launch
        workoutRepository.removeExercise(setGroup)
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
}
