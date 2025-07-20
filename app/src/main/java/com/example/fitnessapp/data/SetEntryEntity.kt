package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SetEntry")
data class SetEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val setEntryId: Int = 0,

    val setGroupId: Int,  // links to SetGroupEntity.setGroupId
    val setIndex: Int,    // index/order within the group
    val weight: Float,
    val reps: Int
)
