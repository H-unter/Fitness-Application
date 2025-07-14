package com.example.fitnessapp.di

import androidx.room.Room
import com.example.fitnessapp.data.CurrentWorkoutRepository
import com.example.fitnessapp.data.CurrentWorkoutRepositoryImpl
import com.example.fitnessapp.data.ExerciseDao
import com.example.fitnessapp.data.ExerciseRepository
import com.example.fitnessapp.data.ExerciseRepositoryImpl
import com.example.fitnessapp.data.GymActivityDatabase
import com.example.fitnessapp.data.SetGroupDao
import com.example.fitnessapp.data.WorkoutDao
import com.example.fitnessapp.viewmodel.CurrentWorkoutViewModel
import com.example.fitnessapp.viewmodel.ExerciseHistoryViewModel
import com.example.fitnessapp.viewmodel.ExerciseListSelectionViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            GymActivityDatabase::class.java,
            "gym-activity-database"
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    val now = System.currentTimeMillis()

                    // 1. Insert 3 Exercises
                    db.execSQL("""
                        INSERT INTO Exercise (exerciseId, name) 
                        VALUES (1, 'Squat'), (2, 'Bench'), (3, 'Deadlift')
                    """.trimIndent())

                    // 2. Insert 3 Workouts
                    for (i in 1..3) {
                        val workoutId = i
                        val startTime = now - ((4 - i) * 2 * 24 * 60 * 60 * 1000L)
                        val endTime = startTime + (45 * 60 * 1000L)

                        db.execSQL("""
                            INSERT INTO Workout (workoutId, gymId, startTime, endTime, isInProgress)
                            VALUES ($workoutId, 1, $startTime, $endTime, 0)
                        """.trimIndent())

                        // 3. Insert 3 SetGroups per workout
                        val exercises = listOf(
                            Triple("Squat", 1, "Kgs"),
                            Triple("Bench", 2, "Kgs"),
                            Triple("Deadlift", 3, "Kgs")
                        )

                        exercises.forEachIndexed { index, (name, exerciseId, weightUnit) ->
                            val setGroupId = workoutId * 10 + index
                            db.execSQL("""
                                INSERT INTO SetGroup (setGroupId, workoutId, exerciseId, name, weightUnit)
                                VALUES ($setGroupId, $workoutId, $exerciseId, '$name', '$weightUnit')
                            """.trimIndent())

                            // 4. Insert 3 SetItems per SetGroup
                            for (j in 0 until 3) {
                                val weight = 60 + j * 5 + i + index
                                val reps = 8 - j
                                db.execSQL("""
                                    INSERT INTO SetItem (setGroupId, weight, reps)
                                    VALUES ($setGroupId, $weight, $reps)
                                """.trimIndent())
                            }
                        }
                    }
                }
            })
            .build()
    }

    // data access objects
    single<WorkoutDao>  { get<GymActivityDatabase>().workoutDao() }
    single<SetGroupDao> { get<GymActivityDatabase>().setGroupDao() }
    single<ExerciseDao> { get<GymActivityDatabase>().exerciseDao() }

    // coroutine
    single<CoroutineDispatcher> { Dispatchers.IO }
    single<CoroutineScope>      { CoroutineScope(get<CoroutineDispatcher>()) }

    // repository classes
    single<CurrentWorkoutRepository> {
        CurrentWorkoutRepositoryImpl(
            workoutDao   = get(),
            setGroupDao  = get(),
            dispatcher   = get(),
            scope        = get()
        )
    }

    single<ExerciseRepository> {
        ExerciseRepositoryImpl(
            exerciseDao = get(),
            setGroupDao = get()
        )
    }

    // viewmodels
    viewModel { ExerciseListSelectionViewModel(exerciseRepository = get()) }
    viewModel { CurrentWorkoutViewModel(workoutRepository = get(), exerciseRepository = get()) }
    viewModel { ExerciseHistoryViewModel(exerciseRepository = get(), savedStateHandle = get()) }
}

