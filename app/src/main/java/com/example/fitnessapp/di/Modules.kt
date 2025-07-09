package com.example.fitnessapp.di

import androidx.room.Room
import com.example.fitnessapp.data.CurrentWorkoutRepository
import com.example.fitnessapp.data.CurrentWorkoutRepositoryImpl
import com.example.fitnessapp.data.ExerciseDao
import com.example.fitnessapp.data.ExerciseRepository
import com.example.fitnessapp.data.ExerciseRepositoryImpl
import com.example.fitnessapp.data.GymActivityDatabase
import com.example.fitnessapp.data.SetGroupDao
import com.example.fitnessapp.data.WorkoutDao
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import com.example.fitnessapp.viewmodel.ExerciseListSelectionViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // room database
    single {
        Room.databaseBuilder(
            androidContext(),
            GymActivityDatabase::class.java,
            "gym-activity-database"
        )
        .fallbackToDestructiveMigration() // TODO: remove this when the database stops being in flux
        .build()
    }

    // data access objects
    single<WorkoutDao>   { get<GymActivityDatabase>().workoutDao() }
    single<SetGroupDao>  { get<GymActivityDatabase>().setGroupDao() }
    single<ExerciseDao>  { get<GymActivityDatabase>().exerciseDao() }      // ← added

    // coroutine
    single<CoroutineDispatcher> { Dispatchers.IO }
    single<CoroutineScope>      { CoroutineScope(get<CoroutineDispatcher>()) }

    // repository classes
    single<CurrentWorkoutRepository> {
        CurrentWorkoutRepositoryImpl(
            workoutDao   = get(),
            setGroupDao  = get(),
            dispatcher   = get(),
            scope        = get()
        )
    }
    single<ExerciseRepository> {
        ExerciseRepositoryImpl(
            exerciseDao = get(),
            setGroupDao = get()
        )
    }

    viewModel { ExerciseListSelectionViewModel(exerciseRepository = get()) }
    viewModel { CurrentWorkoutViewModel(workoutRepository = get(), exerciseRepository  = get()) }
}
