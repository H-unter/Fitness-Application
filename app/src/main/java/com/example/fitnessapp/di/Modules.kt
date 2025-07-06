package com.example.fitnessapp.di

import androidx.room.Room
import com.example.fitnessapp.data.CurrentWorkoutRepository
import com.example.fitnessapp.data.CurrentWorkoutRepositoryImpl
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import com.example.fitnessapp.data.GymActivityDatabase
import com.example.fitnessapp.data.SetGroupDao
import com.example.fitnessapp.data.WorkoutDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            GymActivityDatabase::class.java,
            "gym-activity-database"
        ).build()
    }
    single<WorkoutDao> {
        get<GymActivityDatabase>().workoutDao()
    }
    single<SetGroupDao> {
        get<GymActivityDatabase>().setGroupDao()
    }
    single<CoroutineDispatcher> { Dispatchers.IO }
    single<CoroutineScope> { CoroutineScope(get<CoroutineDispatcher>()) }


    single<CurrentWorkoutRepository> {
        CurrentWorkoutRepositoryImpl(
            workoutDao   = get(),    // WorkoutDao
            setGroupDao  = get(),    // SetGroupDao
            dispatcher   = get(),
            scope        = get()
        )
    }

    viewModel { CurrentWorkoutViewModel(get()) }
}


