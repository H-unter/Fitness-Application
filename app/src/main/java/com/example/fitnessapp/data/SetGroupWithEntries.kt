package com.example.fitnessapp.data

import androidx.room.Embedded
import androidx.room.Relation

// https://developer.android.com/training/data-storage/room/relationships
// https://developer.android.com/training/data-storage/room/relationships/nested

data class SetGroupWithEntries(
    @Embedded val group: SetGroupEntity,

    @Relation(
        parentColumn = "setGroupId",
        entityColumn = "setGroupId"
    )
    val entries: List<SetEntryEntity>,

    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val exercise: ExerciseEntity?
)