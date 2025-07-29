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
    val setGroups: List<SetGroupWithEntries>,

    @Relation(
        entity = GymEntity::class,
        parentColumn = "gymId",
        entityColumn = "gymId"
    )
    val gym: GymEntity?
)

fun WorkoutWithSetGroupsAndEntries.toDomain(): Workout =
    Workout(
        id = workout.workoutId,
        locationId = workout.gymId ?: 0,
        startTime = workout.startTime,
        endTime = workout.endTime,
        setGroups = setGroups.map { it.toDomain() },
        isInProgress = workout.isInProgress,
        gym = gym?.toDomain()
    )