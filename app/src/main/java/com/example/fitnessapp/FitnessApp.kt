package com.example.fitnessapp

import android.app.Application
import com.example.fitnessapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class FitnessApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FitnessApp)
            modules(appModule)
        }
    }
}
