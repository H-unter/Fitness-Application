package com.example.fitnessapp.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.HealthConnectAvailability
import com.example.fitnessapp.data.HealthConnectManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit

class AppSettingsViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {
    private val _revoking = MutableStateFlow(false)
    val revoking: StateFlow<Boolean> = _revoking

    // This function was never fully realised, and would require comprehensive testing on a logged in device, meaning an emulator is not suitable.
    fun revokeAllHealthConnectPermissions(
        onPermissionsRevoked: (() -> Unit)? = null,
        onManualRevokeRequired: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _revoking.value = true
            try {
                val availability = healthConnectManager.checkAvailability()
                if (availability == HealthConnectAvailability.INSTALLED) {
                    val hadPermissions = healthConnectManager.hasAllPermissions()
                    Log.d("AppSettingsViewModel", "Had permissions before revoke: $hadPermissions")

                    if (!hadPermissions) {
                        Log.d("AppSettingsViewModel", "No permissions to revoke")
                        return@launch
                    }
                    healthConnectManager.healthConnectClient.permissionController.revokeAllPermissions()
                    Log.d("AppSettingsViewModel", "Called revokeAllPermissions()")
                    kotlinx.coroutines.delay(1000)
                    val hasPermissionsAfter = healthConnectManager.hasAllPermissions()
                    Log.d("AppSettingsViewModel", "Has permissions after revoke (after delay): $hasPermissionsAfter")

                    if (!hasPermissionsAfter) {
                        Log.d("AppSettingsViewModel", "Successfully revoked all Health Connect permissions")
                        onPermissionsRevoked?.invoke()
                    } else {
                        Log.w("AppSettingsViewModel", "Permissions were not revoked - manual action required")
                        onManualRevokeRequired?.invoke()
                    }
                } else {
                    Log.e("AppSettingsViewModel", "Health Connect is not installed, cannot revoke permissions")
                }
            } catch (e: Exception) {
                Log.e("AppSettingsViewModel", "Failed to revoke Health Connect permissions", e)
            } finally {
                _revoking.value = false
            }
        }
    }
}

class ThemePreference(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = preferences.getBoolean("dark_mode", false)
        set(value) {
            preferences.edit { putBoolean("dark_mode", value) }
        }
}
