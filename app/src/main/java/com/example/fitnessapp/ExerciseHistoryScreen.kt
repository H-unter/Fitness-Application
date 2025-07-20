package com.example.fitnessapp

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.ExerciseHistoryViewModel
import com.example.fitnessapp.viewmodel.SetGroupDisplayData
import org.koin.androidx.compose.koinViewModel
import kotlin.Double


@Composable
fun ExerciseHistoryScreen(
    navController: NavHostController,
    viewModel: ExerciseHistoryViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    ExerciseHistoryScreenContent(
        exerciseName = state.exerciseName,
        history = state.history,
        onBack = {navController.popBackStack()},
        modifier = modifier
    )
}

@Composable
fun ExerciseHistoryScreenContent(
    exerciseName: String,
    history: List<SetGroupDisplayData>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar    = { ExerciseHistoryTopAppBar(exerciseName, onBack) },
        bottomBar = { BottomNavigationBar() }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ExerciseHistoryPlot(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(history, key = { index, _ -> index }) { _, group ->
                    HistoricalExerciseCard(
                        date = group.label,
                        sets = group.sets
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseHistoryTopAppBar(
    exerciseName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "History: $exerciseName",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor             = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor          = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
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
        // TODO: Replace with real plot
    }
}


@Composable
fun HistoricalExerciseCard(
    date: String,
    sets: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    // Parse into Float/Int pairs
    val parsedSets: List<Pair<Float, Int>> = sets.mapNotNull { (w, r) ->
        val weight = w.toFloatOrNull()
        val reps   = r.toIntOrNull()
        if (weight != null && reps != null) weight to reps else null
    }

    // Compute highest 1RM (as Float) and total volume
    val highest1RM = parsedSets
        .maxOfOrNull { (w, reps) -> w * (1 + 0.0333f * reps) }
        ?: 0f

    val totalVolume = parsedSets.sumOf { (w, reps) -> (w * reps).toInt() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Workout – $date",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            parsedSets.forEachIndexed { index, (weight, reps) ->
                val volume = (weight * reps).toInt()
                val oneRM  = weight * (1 + 0.0333f * reps)
                HistoricalSetRow(
                    setNumber = index + 1,
                    reps      = reps,
                    weight    = weight,
                    volume    = volume,
                    oneRepMax = oneRM,
                    isMax     = oneRM == highest1RM
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total Volume = $totalVolume",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun HistoricalSetRow(
    setNumber: Int,
    reps: Int,
    weight: Float,
    volume: Int,
    oneRepMax: Float,
    isMax: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = "$setNumber: $reps reps × ${weight.toInt()}kg",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text  = "Vol $volume · 1RM ${"%.1f".format(oneRepMax)}",
            style = if (isMax)
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else
                MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun Preview_ExerciseHistoryScreenContent() {
    FitnessappTheme {
        ExerciseHistoryScreenContent(
            onBack = {},
            exerciseName = "Bench Press",
            history = listOf(
                SetGroupDisplayData("Yesterday", listOf("50" to "8", "55" to "6")),
                SetGroupDisplayData("2 Days Ago", listOf("60" to "5", "62.5" to "5", "65" to "4"))
            )
        )
    }
}

@Preview(name = "Light Mode", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
fun Preview_ExerciseHistoryScreenContent_Light() {
    Preview_ExerciseHistoryScreenContent()
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun Preview_ExerciseHistoryScreenContent_Dark() {
    Preview_ExerciseHistoryScreenContent()
}
