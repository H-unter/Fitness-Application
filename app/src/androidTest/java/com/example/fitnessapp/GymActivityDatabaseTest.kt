package com.example.fitnessapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnessapp.data.Gym
import com.example.fitnessapp.data.SetEntry
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.data.Workout
import com.example.fitnessapp.data.room.GymActivityDatabase
import com.example.fitnessapp.data.room.SetEntryEntity
import com.example.fitnessapp.data.room.SetGroupEntity
import com.example.fitnessapp.data.room.WorkoutDao
import com.example.fitnessapp.data.room.WorkoutEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class GymActivityDatabaseTest {

    private lateinit var workoutDao: WorkoutDao
    private lateinit var database: GymActivityDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GymActivityDatabase::class.java
        ).allowMainThreadQueries() // Only for testing
            .build()
        workoutDao = database.workoutDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertWorkout() = runBlocking {
        val setEntry = SetEntry(
            weight = "50",
            reps = "10",
            completed = true
        )

        val setGroup = SetGroup(
            setGroupId = 0,
            workoutId = 0,
            exerciseId = 1,
            name = "Bench Press Group",
            weightUnit = WeightUnit.KG,
            exerciseName = "Bench Press",
            entries = listOf(setEntry)
        )

        val workout = Workout(
            id = 0,
            gymId = 123,
            startTime = Instant.now().toEpochMilli(),
            endTime = Instant.now().plusSeconds(3600).toEpochMilli(), // 1 hour later
            isInProgress = true,
            setGroups = listOf(setGroup),
            gym = Gym(
                id = 0,
                name = "My Favorite Gym"
            )
        )

        // Insert workout entity first
        val workoutEntity = WorkoutEntity(
            workoutId = 0, // Auto-generated
            gymId = workout.gymId,
            startTime = workout.startTime,
            endTime = workout.endTime,
            isInProgress = workout.isInProgress
        )

        val workoutId = workoutDao.insertWorkout(workoutEntity).toInt()

        // Now insert the set group
        val setGroupEntity = SetGroupEntity(
            setGroupId = 0, // Auto-generated
            exerciseId = setGroup.exerciseId,
            workoutId = workoutId,
            weightUnit = setGroup.weightUnit
        )

        val setGroupId = database.setGroupDao().insertSetGroup(setGroupEntity).toInt()

        // Now insert the set entry
        val setEntryEntity = SetEntryEntity(
            setEntryId = 0, // Auto-generated
            setGroupId = setGroupId,
            setIndex = 0,
            weight = setEntry.weight.toFloatOrNull(),
            reps = setEntry.reps.toIntOrNull(),
            completed = setEntry.completed
        )

        database.setEntryDao().insertSetEntry(setEntryEntity)

        // Query the database to verify the workout was inserted correctly
        val retrievedWorkoutWithSetGroups = workoutDao.getWorkoutWithSetGroupsAndEntries(workoutId).first()

        // Verify workout details
        assertNotNull(retrievedWorkoutWithSetGroups)
        assertEquals(workout.gymId, retrievedWorkoutWithSetGroups.workout.gymId)
        assertEquals(workout.startTime, retrievedWorkoutWithSetGroups.workout.startTime)
        assertEquals(workout.endTime, retrievedWorkoutWithSetGroups.workout.endTime)
        assertEquals(workout.isInProgress, retrievedWorkoutWithSetGroups.workout.isInProgress)

        // Verify set groups
        assertEquals(1, retrievedWorkoutWithSetGroups.setGroups.size)
        val retrievedSetGroup = retrievedWorkoutWithSetGroups.setGroups[0]

        // Fix: use 'group' instead of 'setGroup'
        assertEquals(setGroup.exerciseId, retrievedSetGroup.group.exerciseId)
        assertEquals(setGroup.weightUnit, retrievedSetGroup.group.weightUnit)

        // Verify set entries
        assertEquals(1, retrievedSetGroup.entries.size)
        val retrievedSetEntry = retrievedSetGroup.entries[0]
        assertEquals(setEntry.weight.toFloatOrNull(), retrievedSetEntry.weight)
        assertEquals(setEntry.reps.toIntOrNull(), retrievedSetEntry.reps)
        assertEquals(setEntry.completed, retrievedSetEntry.completed)

        // Verify workout can be retrieved as current workout if it's in progress
        val currentWorkout = workoutDao.getCurrentWorkout().first()
        assertNotNull(currentWorkout)
        assertEquals(workoutId, currentWorkout?.workoutId)

        // Test marking workout as finished
        workoutDao.markFinished(workoutId, Instant.now().toEpochMilli())
        val updatedWorkout = workoutDao.getWorkoutWithSetGroupsAndEntries(workoutId).first().workout
        assertEquals(false, updatedWorkout.isInProgress)
    }
}