package com.example.fitnessapp.data

import android.annotation.SuppressLint
import android.content.Context
import android.health.connect.datatypes.ExerciseSegmentType
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
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
class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    // Permissions needed for workout data
    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    // Check Health Connect availability
    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    fun checkAvailability() {
        availability.value = when {
            HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
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
    suspend fun writeWorkoutToHealthConnect(workout: WorkoutWithSetGroupsAndEntries): Result<Unit> =
        try {
            if (availability.value != HealthConnectAvailability.INSTALLED || !hasRequiredPermissions()) {
                Result.failure(Exception("Health Connect not available or missing permissions"))
            } else {
                val start = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(workout.workout.startTime),
                    ZonedDateTime.now().zone
                )
                val end = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(workout.workout.endTime),
                    ZonedDateTime.now().zone
                )
                val sessionRecord = ExerciseSessionRecord(
                    metadata = Metadata.manualEntry(),
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
                    title = workout.gym?.name
                )
                healthConnectClient.insertRecords(listOf(sessionRecord))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun createSegmentsForSetGroup(setGroupWithEntries: SetGroupWithEntries): List<ExerciseSegment> {
        val segmentType = ExerciseSegmentType.EXERCISE_SEGMENT_TYPE_WEIGHTLIFTING
        return setGroupWithEntries.entries.mapIndexed { index, setEntry ->
            val setDurationMs = 60_000L
            val setStartTime = Instant.ofEpochMilli(System.currentTimeMillis() + (index * setDurationMs))
            val setEndTime = Instant.ofEpochMilli(setStartTime.toEpochMilli() + setDurationMs)
            val reps = setEntry.reps // Assuming reps is Int
            ExerciseSegment(
                startTime = setStartTime,
                endTime = setEndTime,
                segmentType = segmentType,
                repetitions = reps
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

    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

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