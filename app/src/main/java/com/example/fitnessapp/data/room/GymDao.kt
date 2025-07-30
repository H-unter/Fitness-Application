package com.example.fitnessapp.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {
    @Query("SELECT * FROM Gym ORDER BY name")
    fun getAllGyms(): Flow<List<GymEntity>>

    @Query("SELECT * FROM Gym WHERE gymId = :gymId")
    suspend fun getGymById(gymId: Int): GymEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGym(gym: GymEntity): Long

    @Update
    suspend fun updateGym(gym: GymEntity)

    @Delete
    suspend fun deleteGym(gym: GymEntity)

    @Query("SELECT * FROM Gym LIMIT 1")
    suspend fun getDefaultGym(): GymEntity?
}