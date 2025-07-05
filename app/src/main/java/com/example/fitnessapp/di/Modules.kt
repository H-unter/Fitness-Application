package com.example.fitnessapp.di

import androidx.room.Room
import com.example.fitnessapp.data.CurrentWorkoutRepository
import com.example.fitnessapp.data.CurrentWorkoutRepositoryImpl
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import com.example.fitnessapp.data.GymActivityDatabase



val appModule = module {
    single<CurrentWorkoutRepository> { CurrentWorkoutRepositoryImpl() }
    viewModel { CurrentWorkoutViewModel(get()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            GymActivityDatabase::class.java,
            "gym-activity-database"
        ).build()
    }
}

