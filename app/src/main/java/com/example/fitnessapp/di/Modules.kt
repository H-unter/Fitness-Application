package com.example.fitnessapp.di

import androidx.room.Room
import com.example.fitnessapp.data.repositories.CurrentWorkoutRepository
import com.example.fitnessapp.data.repositories.CurrentWorkoutRepositoryImpl
import com.example.fitnessapp.data.room.ExerciseDao
import com.example.fitnessapp.data.repositories.ExerciseRepository
import com.example.fitnessapp.data.repositories.ExerciseRepositoryImpl
import com.example.fitnessapp.data.room.GymActivityDatabase
import com.example.fitnessapp.data.room.GymDao
import com.example.fitnessapp.data.repositories.GymRepository
import com.example.fitnessapp.data.repositories.GymRepositoryImpl
import com.example.fitnessapp.data.HealthConnectManager
import com.example.fitnessapp.data.room.SetEntryDao
import com.example.fitnessapp.data.room.SetGroupDao
import com.example.fitnessapp.data.room.WorkoutDao
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import com.example.fitnessapp.viewmodel.ExerciseHistoryViewModel
import com.example.fitnessapp.viewmodel.ExerciseListSelectionViewModel
import com.example.fitnessapp.viewmodel.WorkoutHistoryViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


// https://developer.android.com/training/data-storage/room/prepopulate

val appModule = module {
    // room database
    single {
        Room.databaseBuilder(
            androidContext(),
            GymActivityDatabase::class.java,
            "gym-activity-database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // data access objects
    single<WorkoutDao>  { get<GymActivityDatabase>().workoutDao() }
    single<GymDao>      { get<GymActivityDatabase>().gymDao() }
    single<SetGroupDao> { get<GymActivityDatabase>().setGroupDao() }
    single<SetEntryDao> { get<GymActivityDatabase>().setEntryDao() }
    single<ExerciseDao> { get<GymActivityDatabase>().exerciseDao() }

    // health connect manager

    single { HealthConnectManager(androidContext()) }

    // coroutine
    single<CoroutineDispatcher> { Dispatchers.IO }
    single<CoroutineScope>      { CoroutineScope(get<CoroutineDispatcher>()) }

    // repository classes
    single<CurrentWorkoutRepository> {
        CurrentWorkoutRepositoryImpl(
            workoutDao   = get(),
            setGroupDao  = get(),
            setEntryDao  = get(),
            exerciseDao  = get(),
            dispatcher   = get(),
            scope        = get()
        )
    }

    single<GymRepository> {
        GymRepositoryImpl(
            gymDao = get()
        )
    }

    single<ExerciseRepository> {
        ExerciseRepositoryImpl(
            exerciseDao = get(),
            setGroupDao = get(),
            workoutDao  = get()
        )
    }

    // view models
    viewModel { ExerciseListSelectionViewModel(exerciseRepository = get()) }
    viewModel { CurrentWorkoutViewModel(workoutRepository = get(), gymRepository = get(), exerciseRepository = get()) }
    viewModel { WorkoutHistoryViewModel(workoutDao = get(), healthConnectManager = get()) }
    viewModel { ExerciseHistoryViewModel(exerciseRepository = get(), savedStateHandle = get()) }
}


