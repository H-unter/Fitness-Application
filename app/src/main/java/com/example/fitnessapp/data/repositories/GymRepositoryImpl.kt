package com.example.fitnessapp.data.repositories

import com.example.fitnessapp.data.Gym
import com.example.fitnessapp.data.room.GymDao
import com.example.fitnessapp.data.room.GymEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GymRepositoryImpl(
    private val gymDao: GymDao
) : GymRepository {

    override fun getAllGyms(): Flow<List<Gym>> =
        gymDao.getAllGyms().map { entities ->
            entities.map { entity ->
                Gym(id = entity.gymId, name = entity.name)
            }
        }

    override suspend fun getGymById(gymId: Int): Gym? =
        gymDao.getGymById(gymId)?.let { entity ->
            Gym(id = entity.gymId, name = entity.name)
        }

    override suspend fun insertGym(name: String): Long {
        val entity = GymEntity(name = name)
        return gymDao.insertGym(entity)
    }

    override suspend fun updateGym(gym: Gym) {
        val entity = GymEntity(
            gymId = gym.id,
            name = gym.name
        )
        gymDao.updateGym(entity)
    }

    override suspend fun deleteGym(gym: Gym) {
        val entity = GymEntity(
            gymId = gym.id,
            name = gym.name
        )
        gymDao.deleteGym(entity)
    }

    override suspend fun getDefaultGym(): Gym? =
        gymDao.getDefaultGym()?.let { entity ->
            Gym(id = entity.gymId, name = entity.name)
        }
}