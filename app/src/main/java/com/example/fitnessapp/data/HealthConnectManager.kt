package com.example.fitnessapp.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.fitnessapp.data.room.SetGroupWithEntries
import com.example.fitnessapp.data.room.WorkoutWithSetGroupsAndEntries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.time.Instant
import java.time.ZonedDateTime


const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1


/**
 * Manager class for Health Connect operations, as implemented in the codelabs
 * https://developer.android.com/reference/android/health/connect/HealthConnectManager
 *
 * https://github.com/android/android-health-connect-codelab/blob/main/finished/src/main/java/com/example/healthconnect/codelab/data/HealthConnectManager.kt
 */
class HealthConnectManager(
    private val context: Context,
    val healthConnectClient: HealthConnectClient = HealthConnectClient.getOrCreate(context)
) {

    companion object {
        private const val TAG = "HealthConnectManager"

        // Create a set of permissions for required data types (exactly like docs)
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class)
        )
    }

    fun getPermissionLauncher(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    // Permissions needed for workout data
    suspend fun hasAllPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            Log.d(TAG, "Granted permissions: $granted")
            Log.d(TAG, "Required permissions: $PERMISSIONS")
            granted.containsAll(PERMISSIONS)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            false
        }
    }

    suspend fun getGrantedPermissions(): Set<String> {
        return healthConnectClient.permissionController.getGrantedPermissions()
    }

    // Check Health Connect availability
    fun checkAvailability(): HealthConnectAvailability {
        val sdkStatus = HealthConnectClient.getSdkStatus(context)
        Log.d(TAG, "SDK Status: $sdkStatus")
        Log.d(TAG, "Is supported (API level): ${isSupported()}")

        return when {
            sdkStatus == SDK_AVAILABLE -> {
                Log.d(TAG, "Health Connect is INSTALLED")
                HealthConnectAvailability.INSTALLED
            }
            isSupported() -> {
                Log.d(TAG, "Health Connect is NOT_INSTALLED but supported")
                HealthConnectAvailability.NOT_INSTALLED
            }
            else -> {
                Log.d(TAG, "Health Connect is NOT_SUPPORTED")
                HealthConnectAvailability.NOT_SUPPORTED
            }
        }
    }

    private suspend fun hasRequiredPermissions(): Boolean {
        val permissions = setOf(
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class)
        )
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    @SuppressLint("RestrictedApi")
    suspend fun writeWorkoutToHealthConnect(workout: WorkoutWithSetGroupsAndEntries): Result<Unit> {
        return try {
            Log.d(TAG, "Attempting to write workout: ${workout.workout.workoutId}")

            val currentAvailability = checkAvailability()
            if (currentAvailability != HealthConnectAvailability.INSTALLED) {
                Log.e(TAG, "Health Connect not available: $currentAvailability")
                return Result.failure(Exception("Health Connect not available"))
            }

            if (!hasRequiredPermissions()) {
                Log.e(TAG, "Missing required permissions")
                return Result.failure(Exception("Missing Health Connect permissions"))
            }

            val start = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(workout.workout.startTime),
                ZonedDateTime.now().zone
            )

            val end = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(workout.workout.endTime),
                ZonedDateTime.now().zone
            )

            // Check if this workout already exists in Health Connect
            val existingSessions = readWorkoutSessions(start.toInstant(), end.toInstant())
            val workoutExists = existingSessions.any { session ->
                session.startTime == start.toInstant() &&
                session.endTime == end.toInstant() &&
                session.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING
            }

            if (workoutExists) {
                Log.d(TAG, "Workout already exists in Health Connect, skipping")
                return Result.success(Unit)
            }

            // Create exercise segments from set groups
            val segments = createExerciseSegments(workout.setGroups, start, end)

            val sessionRecord = ExerciseSessionRecord(
                metadata = Metadata.manualEntry(
                    clientRecordId = workout.workout.workoutId.toString()
                ),
                startTime = start.toInstant(),
                startZoneOffset = start.offset,
                endTime = end.toInstant(),
                endZoneOffset = end.offset,
                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
                title = "${workout.gym?.name ?: "Workout"} - Strength Training",
                segments = segments
            )

            Log.d(TAG, "Writing session: ${sessionRecord.title}, duration: ${sessionRecord.endTime.epochSecond - sessionRecord.startTime.epochSecond}s, segments: ${segments.size}")

            healthConnectClient.insertRecords(listOf(sessionRecord))
            Log.d(TAG, "Successfully wrote workout to Health Connect")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write workout to Health Connect", e)
            Result.failure(e)
        }
    }

    private fun createExerciseSegments(
        setGroups: List<SetGroupWithEntries>,
        workoutStart: ZonedDateTime,
        workoutEnd: ZonedDateTime
    ): List<ExerciseSegment> {
        if (setGroups.isEmpty()) return emptyList()

        val totalDurationMs = workoutEnd.toInstant().toEpochMilli() - workoutStart.toInstant().toEpochMilli()
        val segmentDurationMs = totalDurationMs / setGroups.size

        return setGroups.mapIndexed { index, setGroup ->
            val segmentStart = workoutStart.toInstant().plusMillis(index * segmentDurationMs)
            val segmentEnd = workoutStart.toInstant().plusMillis((index + 1) * segmentDurationMs)

            val totalReps = setGroup.entries.sumOf { entry ->
                entry.reps?.toInt() ?: 0
            }

            ExerciseSegment(
                startTime = segmentStart,
                endTime = segmentEnd,
                segmentType = ExerciseSegment.EXERCISE_SEGMENT_TYPE_WEIGHTLIFTING,
                repetitions = totalReps
            )
        }
    }

    // Read workout sessions from Health Connect
    suspend fun readWorkoutSessions(
        startTime: Instant,
        endTime: Instant
    ): List<ExerciseSessionRecord> {
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )

        return try {
            val response = healthConnectClient.readRecords(request)
            response.records
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Read all workout sessions from Health Connect with no time constraints
    suspend fun readAllExerciseSessions(): Result<List<ExerciseSessionRecord>> {
        return try {
            Log.d(TAG, "Reading all exercise sessions from Health Connect")

            if (!hasRequiredPermissions()) {
                Log.e(TAG, "Missing required permissions")
                return Result.failure(Exception("Missing Health Connect permissions"))
            }

            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.EPOCH, // Beginning of time
                    Instant.now().plusMillis(1000 * 60 * 60 * 24) // Future (tomorrow)
                )
            )

            val response = healthConnectClient.readRecords(request)
            Log.d(TAG, "Found ${response.records.size} exercise sessions")

            // Log details about each record
            response.records.forEachIndexed { index, record ->
                Log.d(TAG, "Record $index: ${record.title}, " +
                        "start=${record.startTime}, " +
                        "end=${record.endTime}, " +
                        "type=${record.exerciseType}, " +
                        "segments=${record.segments.size}, " +
                        "metadata.clientRecordId=${record.metadata.clientRecordId}")

                // Log details about each segment if any
                record.segments.forEachIndexed { segIndex, segment ->
                    Log.d(TAG, "  Segment $segIndex: " +
                            "type=${segment.segmentType}, " +
                            "reps=${segment.repetitions}, " +
                            "start=${segment.startTime}, " +
                            "end=${segment.endTime}")
                }
            }

            Result.success(response.records)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading all exercise sessions", e)
            Result.failure(e)
        }
    }

    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    suspend fun getChangesToken(): String {
        return healthConnectClient.getChangesToken(
            ChangesTokenRequest(
                setOf(
                    ExerciseSessionRecord::class
                )
            )
        )
    }

    sealed class ChangesMessage {
        data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
        data class ChangeList(val changes: List<Change>) : ChangesMessage()
    }

    suspend fun getChanges(token: String): Flow<ChangesMessage> = flow {
        var nextChangesToken = token
        do {
            val response = healthConnectClient.getChanges(nextChangesToken)
            if (response.changesTokenExpired) {
                throw IOException("Changes token has expired")
            }
            emit(ChangesMessage.ChangeList(response.changes))
            nextChangesToken = response.nextChangesToken
        } while (response.hasMore)
        emit(ChangesMessage.NoMoreChanges(nextChangesToken))
    }

}

/**
 * Health Connect requires that the underlying Health Connect APK is installed on the device.
 * [HealthConnectAvailability] represents whether this APK is indeed installed, whether it is not
 * installed but supported on the device, or whether the device is not supported (based on Android
 * version).
 */
enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}