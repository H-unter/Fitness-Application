package com.example.fitnessapp.di

import com.example.fitnessapp.data.CurrentWorkoutRepository
import com.example.fitnessapp.data.CurrentWorkoutRepositoryImpl
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<CurrentWorkoutRepository> { CurrentWorkoutRepositoryImpl() }
    viewModel { CurrentWorkoutViewModel(get()) }
}