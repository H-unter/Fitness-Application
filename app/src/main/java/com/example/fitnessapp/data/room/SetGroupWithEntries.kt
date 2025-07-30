package com.example.fitnessapp.data.room

import androidx.room.Embedded
import androidx.room.Relation
import com.example.fitnessapp.data.SetGroup

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

fun SetGroupWithEntries.toDomain(): SetGroup = SetGroup(
    setGroupId = group.setGroupId,
    workoutId = group.workoutId,
    exerciseId = group.exerciseId,
    name = exercise?.name ?: "[Unknown Exercise]",
    weightUnit = group.weightUnit,
    exerciseName = exercise?.name ?: "[Unknown Exercise]",
    entries = entries.map { it.toDomain() }
)