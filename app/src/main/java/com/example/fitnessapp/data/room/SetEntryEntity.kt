package com.example.fitnessapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitnessapp.data.SetEntry

@Entity(tableName = "SetEntry")
data class SetEntryEntity(
    @PrimaryKey(autoGenerate = true) val setEntryId: Int,
    val setGroupId: Int,  // links to SetGroupEntity.setGroupId
    val setIndex: Int,    // index/order within the group
    val weight: Float?,
    val reps: Int?,
    val completed: Boolean = false  // Added to persist completion status
)

fun SetEntryEntity.toDomain(): SetEntry = SetEntry(
    weight = weight?.let {
        if (it % 1 == 0f) it.toInt().toString() else it.toString()
    } ?: "",
    reps = reps?.toString() ?: "",
    completed = completed
)