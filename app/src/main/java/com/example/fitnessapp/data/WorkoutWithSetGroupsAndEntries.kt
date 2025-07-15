package com.example.fitnessapp.data

import androidx.room.Embedded
import androidx.room.Relation

// https://developer.android.com/training/data-storage/room/relationships/nested

data class WorkoutWithSetGroupsAndEntries(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = SetGroupEntity::class,
        parentColumn = "workoutId",
        entityColumn = "workoutId"
    )
    val setGroups: List<SetGroupWithEntries>
)
