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

fun WorkoutWithSetGroupsAndEntries.toDomain(): Workout =
    Workout(
        id         = workout.workoutId,
        locationId = workout.gymId,
        startTime  = workout.startTime,
        endTime    = workout.endTime,
        setGroups  = setGroups.map { it.toDomain() },
        isInProgress = workout.isInProgress
    )
