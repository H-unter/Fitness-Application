package com.example.fitnessapp.data.repositories

import com.example.fitnessapp.data.Gym
import kotlinx.coroutines.flow.Flow

interface GymRepository {
    fun getAllGyms(): Flow<List<Gym>>
    suspend fun getGymById(gymId: Int): Gym?
    suspend fun insertGym(name: String): Long
    suspend fun updateGym(gym: Gym)
    suspend fun deleteGym(gym: Gym)
    suspend fun getDefaultGym(): Gym?
}