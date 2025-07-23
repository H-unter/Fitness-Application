package com.example.fitnessapp.views

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.collectAsState
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
import com.example.fitnessapp.BottomNavigationBar
import com.example.fitnessapp.data.SetEntry
import com.example.fitnessapp.data.SetGroup
import com.example.fitnessapp.data.WeightUnit
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.viewmodel.ExerciseHistoryViewModel
import com.example.fitnessapp.viewmodel.SetGroupDisplayData
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
//import kotlin.time.Duration
import java.time.Duration


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExerciseHistoryScreen(
    navController: NavHostController,
    viewModel: ExerciseHistoryViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExerciseHistoryScreenContent(
        uiState = uiState,
        onBack  = { navController.popBackStack() },
        modifier= modifier
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExerciseHistoryScreenContent(
    uiState: ExerciseHistoryUIState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar    = { ExerciseHistoryTopAppBar(uiState.exerciseName, onBack) },
        bottomBar = { BottomNavigationBar() }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            ExerciseHistoryPlot(
                timestamps      = uiState.xValues,
                volumeValues    = uiState.volumeSeries,
                oneRepMaxValues = uiState.oneRepMaxSeries,
                modifier        = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier           = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding      = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(uiState.historyItems) { _, item ->
                    HistoricalExerciseCard(
                        date = item.timestamp,
                        sets = item.sets
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
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

private val historyLegendKey = ExtraStore.Key<List<String>>()

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExerciseHistoryPlot(
    timestamps: List<Double>,
    volumeValues: List<Double>,
    oneRepMaxValues: List<Double>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    Log.d(
        "ExerciseHistoryPlot",
        "plot data → xValues: $timestamps\nvolumeSeries: $volumeValues\noneRepMaxSeries: $oneRepMaxValues"
    )

    val barColor   = MaterialTheme.colorScheme.primary.toArgb()
    val lineColor  = MaterialTheme.colorScheme.secondary.toArgb()
    val textComponent = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface)
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(timestamps, volumeValues, oneRepMaxValues) {
        modelProducer.runTransaction {
            if (timestamps.size == volumeValues.size && timestamps.isNotEmpty()) {
                columnSeries { series(x = timestamps, y = volumeValues) }
                lineSeries   { series(x = timestamps, y = oneRepMaxValues) }
                extras        { it[historyLegendKey] = listOf("Volume", "1 RM") }
            }
        }
    }

    val xAxisFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMM")
            .withZone(ZoneId.systemDefault())
    }
    val xValueFormatter = CartesianValueFormatter { _, x, _ ->
        xAxisFormatter.format(Instant.ofEpochMilli(x.toLong()))
    }
    val axisTitleComponent = rememberTextComponent(
        color    = MaterialTheme.colorScheme.onSurface,
        textSize = MaterialTheme.typography.bodySmall.fontSize
    )
    val chartZoomState = rememberVicoZoomState(initialZoom = Zoom.Content)
    val oneDayStepMs = Duration.ofDays(1).toMillis().toDouble()

    val chart = rememberCartesianChart(
        rememberColumnCartesianLayer(
            columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                rememberLineComponent(
                    fill      = fill(Color(barColor)),
                    thickness = 0.1.dp
                )
            ),
            rangeProvider          = CartesianLayerRangeProvider.auto(),
            verticalAxisPosition   = Axis.Position.Vertical.End,
            columnCollectionSpacing= 8.dp
        ),
        rememberLineCartesianLayer(
            lineProvider           = LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                    LineCartesianLayer.LineFill.single(fill(Color(lineColor)))
                )
            ),
            rangeProvider          = CartesianLayerRangeProvider.auto(),
            verticalAxisPosition   = Axis.Position.Vertical.Start
        ),
        startAxis  = VerticalAxis.rememberStart(
            titleComponent = axisTitleComponent,
            title          = "1 RM"
        ),
        endAxis    = VerticalAxis.rememberEnd(
            titleComponent = axisTitleComponent,
            title          = "Volume"
        ),
        bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xValueFormatter),
        legend     = rememberHorizontalLegend(
            items = { extraStore: ExtraStore ->
                extraStore[historyLegendKey]?.let { labels ->
                    labels.forEachIndexed { index, labelText ->
                        add(
                            LegendItem(
                                icon           = shapeComponent(
                                    fill  = fill(if (index == 0) Color(barColor) else Color(lineColor)),
                                    shape = CorneredShape.Pill
                                ),
                                labelComponent = textComponent,
                                label          = labelText
                            )
                        )
                    }
                }
            }
        ),
        getXStep = { oneDayStepMs }
    )

    Surface(
        modifier       = modifier,
        shape          = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color          = MaterialTheme.colorScheme.surfaceContainer
    ) {
        CartesianChartHost(
            chart         = chart,
            modelProducer = modelProducer,
            zoomState     = chartZoomState,
            modifier      = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
fun HistoricalExerciseCard(
    date: String,
    sets: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val parsedSets: List<Pair<Float, Int>> = sets.mapNotNull { (w, r) ->
        val weight = w.toFloatOrNull()
        val reps   = r.toIntOrNull()
        if (weight != null && reps != null) weight to reps else null
    }

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Preview_ExerciseHistoryScreenContent() {
    FitnessappTheme {
        ExerciseHistoryScreenContent(
            uiState = ExerciseHistoryUIState(
                exerciseName    = "Bench Press",
                historyItems    = listOf(
                    SetGroupDisplayData("Yesterday", listOf("50" to "8", "55" to "6")),
                    SetGroupDisplayData("2 Days Ago", listOf("60" to "5", "62.5" to "5", "65" to "4"))
                ),
                xValues         = listOf(1.0, 2.0),
                volumeSeries    = listOf(200.0, 800.0),
                oneRepMaxSeries = listOf(13.3, 33.3),
                isLoading       = false,
                errorMessage    = null
            ),
            onBack = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
    name             = "Light Mode",
    uiMode           = Configuration.UI_MODE_NIGHT_NO,
    showBackground   = true
)
@Composable
fun Preview_ExerciseHistoryScreenContent_Light() {
    Preview_ExerciseHistoryScreenContent()
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
    name             = "Dark Mode",
    uiMode           = Configuration.UI_MODE_NIGHT_YES,
    showBackground   = true
)
@Composable
fun Preview_ExerciseHistoryScreenContent_Dark() {
    Preview_ExerciseHistoryScreenContent()
}
