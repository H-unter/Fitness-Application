package com.example.fitnessapp.views

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fitnessapp.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.example.fitnessapp.viewmodel.AppSettingsViewModel
import com.example.fitnessapp.viewmodel.WorkoutHistoryViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnessapp.ui.theme.FitnessappTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    navController: NavHostController,
    permissionsGranted: Boolean,
    onPermissionsRevoked: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: AppSettingsViewModel = koinViewModel()
    val workoutHistoryViewModel: WorkoutHistoryViewModel = koinViewModel()
    val coroutineScope = rememberCoroutineScope()
    val unlinking by viewModel.revoking.collectAsState()
    val workoutHistoryUiState by workoutHistoryViewModel.uiState.collectAsStateWithLifecycle()
    var showManualRevokeDialog by remember { mutableStateOf(false) }

    // Main content
    AppSettingsScreenContent(
        isDarkMode = isDarkMode,
        onDarkModeToggle = onDarkModeToggle,
        permissionsGranted = permissionsGranted,
        unlinking = unlinking,
        showManualRevokeDialog = showManualRevokeDialog,
        setShowManualRevokeDialog = { showManualRevokeDialog = it },
        onUnlinkClick = {
            if (!unlinking) {
                coroutineScope.launch {
                    viewModel.revokeAllHealthConnectPermissions(
                        onPermissionsRevoked = { onPermissionsRevoked?.invoke() },
                        onManualRevokeRequired = { showManualRevokeDialog = true }
                    )
                }
            }
        },
        onAppInfoClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        onViewHealthConnectDataClick = { workoutHistoryViewModel.readAllHealthConnectData() },
        workoutHistoryUiState = workoutHistoryUiState,
        workoutHistoryViewModel = workoutHistoryViewModel,
        context = context,
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreenContent(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    permissionsGranted: Boolean,
    unlinking: Boolean,
    showManualRevokeDialog: Boolean,
    setShowManualRevokeDialog: (Boolean) -> Unit,
    onUnlinkClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    onViewHealthConnectDataClick: () -> Unit,
    workoutHistoryUiState: WorkoutHistoryUIState,
    workoutHistoryViewModel: WorkoutHistoryViewModel?,
    context: android.content.Context,
    navController: NavHostController? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (navController != null) BottomNavigationBar(navController = navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {
            // App Preferences Section
            SettingsSectionDivider(
                text = stringResource(R.string.settings_section_app_preferences),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.dark_mode), modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onDarkModeToggle(it) }
                )
            }

            // App Info Section
            SettingsSectionDivider(
                text = stringResource(R.string.settings_section_app_info),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .clickable { onAppInfoClick() }
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_info),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.open_app_settings),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Health Connect Section
            SettingsSectionDivider(
                text = stringResource(R.string.settings_section_app_health_connect),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = if (permissionsGranted)
                    stringResource(R.string.health_connect_permissions_granted)
                else
                    stringResource(R.string.health_connect_permissions_not_granted),
                color = if (permissionsGranted)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = onUnlinkClick,
                enabled = permissionsGranted && !unlinking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (unlinking)
                        stringResource(R.string.revoking_permissions)
                    else
                        stringResource(R.string.unlink_health_connect)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onViewHealthConnectDataClick,
                enabled = permissionsGranted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.view_health_connect_data))
            }
        }

        // Health Connect Data Dialog
        if (workoutHistoryUiState.showHealthConnectDialog && workoutHistoryViewModel != null) {
            HealthConnectDataDialog(
                sessions = workoutHistoryUiState.healthConnectSessions ?: emptyList(),
                onDismiss = { workoutHistoryViewModel.dismissHealthConnectDialog() }
            )
        }

        // Manual Revoke Dialog
        if (showManualRevokeDialog) {
            AlertDialog(
                onDismissRequest = { setShowManualRevokeDialog(false) },
                title = { Text(stringResource(R.string.health_connect_status)) },
                text = { Text("Permissions could not be revoked automatically. Please open Health Connect and revoke permissions manually.") },
                confirmButton = {
                    Button(
                        onClick = {
                            setShowManualRevokeDialog(false)
                            com.example.fitnessapp.data.HealthConnectManager(context).launchHealthConnectPermissionsScreen(context)
                        }
                    ) {
                        Text("Open Health Connect")
                    }
                },
                dismissButton = {
                    Button(onClick = { setShowManualRevokeDialog(false) }) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSectionDivider(
    text: String,
    modifier: Modifier = Modifier
) {
    HorizontalDivider(modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.then(Modifier.padding(bottom = 8.dp))
    )
}

// Previews
@Composable
fun SettingsScreenPreview() {
    AppSettingsScreenContent(
        isDarkMode = true,
        onDarkModeToggle = {},
        permissionsGranted = true,
        unlinking = false,
        showManualRevokeDialog = false,
        setShowManualRevokeDialog = {},
        onUnlinkClick = {},
        onAppInfoClick = {},
        onViewHealthConnectDataClick = {},
        workoutHistoryUiState = WorkoutHistoryUIState(),
        workoutHistoryViewModel = null,
        context = android.app.Application(),
        navController = null
    )
}

@Preview(name = "Settings Screen - Light Mode", showBackground = true)
@Composable
fun SettingsScreenPreview_Light() {
    FitnessappTheme(darkTheme = false) {
        SettingsScreenPreview()
    }
}

@Preview(name = "Settings Screen - Dark Mode", showBackground = true)
@Composable
fun SettingsScreenPreview_Dark() {
    FitnessappTheme(darkTheme = true) {
        SettingsScreenPreview()
    }
}
