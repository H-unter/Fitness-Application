package com.example.fitnessapp.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.navigation.Screens

@Composable
fun BottomNavigationBar(navController: NavHostController = rememberNavController()) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background,
        content = {
            NavigationBarItem(
                selected = navController.currentDestination?.route == Screens.WorkoutHistoryScreen.route,
                onClick = {
                    navController.navigate(Screens.WorkoutHistoryScreen.route) {
                        // Pop up to the start destination to avoid building up a stack
                        popUpTo(Screens.CurrentWorkoutScreen.route) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "History Screen"
                    )
                },
                label = {
                    Text(text = "History")
                }
            )

            NavigationBarItem(
                selected = navController.currentDestination?.route == Screens.CurrentWorkoutScreen.route,
                onClick = {
                    navController.navigate(Screens.CurrentWorkoutScreen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Current Workout"
                    )
                },
                label = {
                    Text(text = "Workout")
                }
            )


            // settings
            NavigationBarItem(
                selected = false, onClick = { /*TODO*/ },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Screen"
                    )
                },
                label = {
                    Text(text = "Settings")
                }
            )
        }
    )
}


@Composable
@Preview
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = rememberNavController())
}