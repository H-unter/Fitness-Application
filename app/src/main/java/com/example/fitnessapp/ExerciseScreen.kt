package com.example.fitnessapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitnessapp.meterialcomponents.PacktBottomNavigationBar
import com.example.fitnessapp.meterialcomponents.PacktFloatingActionButton
import com.example.fitnessapp.meterialcomponents.PacktSmallTopAppBar
import com.example.fitnessapp.ui.theme.FitnessappTheme


import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart

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
                    text = "Bench Press History",
                    style = MaterialTheme.typography.headlineSmall
                )

                ExerciseHistory()
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
        ComposeMultiplatformBasicLineChart()
    }
}



@Composable
fun ComposeMultiplatformBasicLineChart(modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            // Learn more: https://patrykandpatrick.com/z5ah6v.
            lineSeries { series(13, 8, 7, 12, 0, 1, 15, 14, 0, 11, 6, 12, 0, 11, 12, 11) }
        }
    }
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

@Composable
fun ExerciseHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        HistoricalExerciseCard(
            date = "Yesterday",
            sets = listOf(
                "10" to "8",
                "12" to "6",
                "8" to "10"
            ),
            rpe = 8
        )

        HistoricalExerciseCard(
            date = "2 Days Ago",
            sets = listOf(
                "15" to "5",
                "13" to "6",
                "14" to "5"
            ),
            rpe = 7
        )

        Spacer(modifier = Modifier.height(80.dp)) // prevent FAB from overlapping last card
    }
}



@Composable
fun HistoricalExerciseCard(
    date: String,
    sets: List<Pair<String, String>>,
    rpe: Int,
    modifier: Modifier = Modifier
) {
    val parsedSets = sets.mapNotNull { (w, r) ->
        val weight = w.toFloatOrNull()
        val reps = r.toIntOrNull()
        if (weight != null && reps != null) Triple(weight, reps, rpe) else null
    }

    val highest1RM = parsedSets.maxOfOrNull { (w, r, _) -> w * (1 + 0.0333 * r) } ?: 0.0
    val totalVolume = parsedSets.sumOf { (w, r, _) -> (w * r).toInt() }

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
                text = "Workout - $date",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            parsedSets.forEachIndexed { index, (weight, reps, _) ->
                val volume = (weight * reps).toInt()
                val oneRM = weight * (1 + 0.0333 * reps)
                HistoricalSetRow(
                    setNumber = index + 1,
                    reps = reps,
                    weight = weight,
                    rpe = rpe,
                    volume = volume,
                    oneRepMax = oneRM,
                    isMax = oneRM == highest1RM
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
    rpe: Int,
    volume: Int,
    oneRepMax: Double,
    isMax: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$setNumber: $reps reps × ${weight.toInt()}kg",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "RPE $rpe; Vol $volume; 1RM: ${String.format("%.1f", oneRepMax)}",
            style = if (isMax) MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ) else MaterialTheme.typography.bodyMedium
        )
    }
}

