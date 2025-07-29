package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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