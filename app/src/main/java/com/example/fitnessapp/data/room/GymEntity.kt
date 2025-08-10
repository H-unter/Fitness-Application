package com.example.fitnessapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitnessapp.data.Gym

@Entity(tableName = "Gym")
data class GymEntity(
    @PrimaryKey(autoGenerate = true)
    val gymId: Int = 0,
    val name: String
)

fun GymEntity.toDomain(): Gym = Gym(
    id = gymId,
    name = name
)