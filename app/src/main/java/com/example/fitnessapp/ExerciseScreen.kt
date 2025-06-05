package com.example.fitnessapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitnessapp.meterialcomponents.PacktBottomNavigationBar
import com.example.fitnessapp.meterialcomponents.PacktFloatingActionButton
import com.example.fitnessapp.meterialcomponents.PacktSmallTopAppBar
import com.example.fitnessapp.ui.theme.FitnessappTheme


class SecondaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExerciseStatsScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun ExerciseStatsScreenPreview() {
    FitnessappTheme {
        ExerciseStatsScreen(Modifier)
    }
}



@Composable
fun ExerciseStatsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { PacktSmallTopAppBar() },
        bottomBar = { PacktBottomNavigationBar() },
        floatingActionButton = { PacktFloatingActionButton() },
        content = { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ExerciseHistoryPlot(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Temporary height for the graph placeholder
                )

                Text(
                    text = "Exercise History",
                    style = MaterialTheme.typography.headlineSmall
                )

                ExerciseHistoryList(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}


@Composable
fun ExerciseHistoryPlot(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Graph Placeholder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExerciseHistoryList(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        repeat(4) {
            Text("Workout - Date", style = MaterialTheme.typography.titleSmall)
            Text("- set 1 - 10 reps 5kg     RPE X; Volume Y")
            Text("- set 2 - 9 reps 5kg      RPE X; Volume Y")
            Text("- set 3 - 8 reps 5kg      RPE X; Volume Y")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


