package com.example.fitnessapp

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.ExerciseHistoryViewModel
import com.example.fitnessapp.viewmodel.SetGroupDisplayData
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import org.koin.androidx.compose.koinViewModel


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


private val HistoryLegendKey = ExtraStore.Key<List<String>>()

@Composable
fun ExerciseHistoryPlot(modifier: Modifier = Modifier) {

    val barColor  = MaterialTheme.colorScheme.primary.toArgb()
    val lineColor = MaterialTheme.colorScheme.secondary.toArgb()


    val textComponent = rememberTextComponent(
        color = MaterialTheme.colorScheme.onSurface
    )

    // TODO: let this live in the ViewModel
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            columnSeries  { series(5,6,5,2,11,8,5,2,15,11,8,13,12,10,2,7) }
            lineSeries    { series(8,9,7,5,13,10,7,5,18,12,10,15,14,12,5,9) }
            extras { it[HistoryLegendKey] = listOf("Volume", "1RM") }
        }
    }

    Surface(
        modifier       = modifier,
        shape          = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color          = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp)) {

            CartesianChartHost(
                chart = rememberCartesianChart(
                    //column layer
                    rememberColumnCartesianLayer(
                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(fill = fill(Color(barColor)), thickness = 14.dp)
                        )
                    ),
                    // line layer
                    rememberLineCartesianLayer(
                        LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.Line(
                                LineCartesianLayer.LineFill.single(fill(Color(lineColor)))
                            )
                        )
                    ),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                    legend = rememberHorizontalLegend(
                        items = { extraStore ->
                            extraStore[HistoryLegendKey].forEachIndexed { i, label ->
                                add(
                                    LegendItem(
                                        shapeComponent(
                                            fill(if (i == 0) Color(barColor) else Color(lineColor)),
                                            CorneredShape.Pill
                                        ),
                                        textComponent,
                                        label
                                    )
                                )
                            }
                        }
                    )
                ),
                modelProducer = modelProducer,
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
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
