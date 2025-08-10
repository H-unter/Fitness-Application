package com.example.fitnessapp.views

//import kotlin.time.Duration
import android.content.res.Configuration
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.fitnessapp.R
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
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


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

@Composable
fun ExerciseHistoryScreenContent(
    uiState: ExerciseHistoryUIState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.historyItems) {
        if (uiState.historyItems.isNotEmpty()) {
            listState.scrollToItem(
                index = uiState.historyItems.size - 1,
                scrollOffset = 0
            )
        }
    }

    Scaffold(
        topBar = { ExerciseHistoryTopAppBar(uiState.exerciseName, onBack) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            ExerciseHistoryPlot(
                timestamps = uiState.xValues,
                volumeValues = uiState.volumeSeries,
                oneRepMaxValues = uiState.oneRepMaxSeries,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                itemsIndexed(uiState.historyItems) { _, item ->
                    HistoricalExerciseCard(
                        date = item.timestamp,
                        gymName = item.gymName,
                        sets = item.sets,
                        isInProgress = item.isInProgress
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
                text = stringResource(R.string.history_title, exerciseName),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
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

@Composable
fun ExerciseHistoryPlot(
    timestamps: List<Double>,
    volumeValues: List<Double>,
    oneRepMaxValues: List<Double>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val vicoTheme = rememberM3VicoTheme()
    val volumeLineColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val oneRmLineColor = MaterialTheme.colorScheme.primary.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface

    val oneDayStepMs = remember { Duration.ofDays(1).toMillis().toDouble() }
    val chartZoomState = rememberVicoZoomState(initialZoom = Zoom.Content)

    LaunchedEffect(timestamps, volumeValues, oneRepMaxValues) {
        modelProducer.runTransaction {
            if (timestamps.size == volumeValues.size && timestamps.isNotEmpty()) {
                lineSeries {
                    series(x = timestamps, y = volumeValues)
                }
                lineSeries {
                    series(x = timestamps, y = oneRepMaxValues)
                }
                extras { it[historyLegendKey] = listOf("Volume", "1 RM") }
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
        color = textColor,
        textSize = MaterialTheme.typography.bodyMedium.fontSize
    )
    val legendComponent = rememberTextComponent(
        color = textColor,
        textSize = MaterialTheme.typography.bodyMedium.fontSize
    )

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                    LineCartesianLayer.LineFill.single(fill(Color(volumeLineColor)))
                )
            ),
            rangeProvider = CartesianLayerRangeProvider.auto(),
            verticalAxisPosition = Axis.Position.Vertical.End
        ),
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                    LineCartesianLayer.LineFill.single(fill(Color(oneRmLineColor)))
                )
            ),
            rangeProvider = CartesianLayerRangeProvider.auto(),
            verticalAxisPosition = Axis.Position.Vertical.Start
        ),
        startAxis = VerticalAxis.rememberStart(
            title = "1 RM (kg)",
            titleComponent = axisTitleComponent,
            label = rememberTextComponent(color = textColor)
        ),
        endAxis = VerticalAxis.rememberEnd(
            title = "Volume (kg)",
            titleComponent = axisTitleComponent,
            label = rememberTextComponent(color = textColor)
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = xValueFormatter,
            label = rememberTextComponent(color = textColor)
        ),
        legend = rememberHorizontalLegend(
            items = { extraStore: ExtraStore ->
                extraStore[historyLegendKey].let { labels ->
                    labels.forEachIndexed { index, labelText ->
                        add(
                            LegendItem(
                                icon = shapeComponent(
                                    fill = fill(if (index == 0) Color(volumeLineColor) else Color(oneRmLineColor)),
                                    shape = CorneredShape.Pill
                                ),
                                labelComponent = legendComponent,
                                label = labelText
                            )
                        )
                    }
                }
            }
        ),
        getXStep = { oneDayStepMs }
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        ProvideVicoTheme(theme = vicoTheme) {
            CartesianChartHost(
                chart = chart,
                modelProducer = modelProducer,
                zoomState = chartZoomState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun HistoricalExerciseCard(
    date: String,
    gymName: String,
    sets: List<Pair<String, String>>,
    isInProgress: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Parse weights like "225 lbs (102.1 kg)" or "60 kg"
    val parsedSets: List<Triple<Float, Int, String>> = sets.mapNotNull { (w, r) ->
        val regex = Regex("""([\d.]+)\s*(kg|lbs|units)(?:\s*\(([\d.]+)\s*kg\))?""")
        val match = regex.find(w)
        val originalWeight = match?.groups?.get(1)?.value
        val originalUnit = match?.groups?.get(2)?.value
        val kgValue = match?.groups?.get(3)?.value
        val reps = r.toIntOrNull()
        if (originalWeight != null && originalUnit != null && reps != null) {
            val weightKg = kgValue?.toFloatOrNull() ?: originalWeight.toFloatOrNull() ?: 0f
            val displayUnit = if (kgValue != null && originalUnit != "kg") {
                "$originalWeight $originalUnit (${kgValue} kg)"
            } else {
                "$originalWeight $originalUnit"
            }
            Triple(weightKg, reps, displayUnit)
        } else null
    }

    val highest1RM = parsedSets
        .maxOfOrNull { (w, reps, _) -> w * (1 + 0.0333f * reps) }
        ?: 0f

    val totalVolume = parsedSets.sumOf { (w, reps, _) -> (w * reps).toInt() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        color = if (isInProgress)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(
                    if (isInProgress) R.string.gym_on_date_in_progress
                    else R.string.gym_on_date,
                    gymName,
                    date
                ),
                style = MaterialTheme.typography.titleMedium,
                color = if (isInProgress)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            parsedSets.forEachIndexed { index, (weightKg, reps, displayUnit) ->
                val volume = (weightKg * reps).toInt()
                val oneRM  = weightKg * (1 + 0.0333f * reps)
                HistoricalSetRow(
                    setNumber = index + 1,
                    reps      = reps,
                    weightDisplay = displayUnit,
                    volume    = volume,
                    oneRepMax = oneRM,
                    isMax     = oneRM == highest1RM
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.total_volume, totalVolume),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun HistoricalSetRow(
    setNumber: Int,
    reps: Int,
    weightDisplay: String,
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
            text  = stringResource(R.string.set_row_label, setNumber, reps, weightDisplay),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text  = stringResource(R.string.set_row_stats, volume, oneRepMax),
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
            uiState = ExerciseHistoryUIState(
                exerciseName = "Bench Press",
                historyItems = listOf(
                    SetGroupDisplayData(
                        timestamp = "2 Days Ago",
                        gymName = "Planet Fitness",
                        sets = listOf("60" to "5", "62.5" to "5", "65" to "4"),
                        isInProgress = false
                    ),
                    SetGroupDisplayData(
                        timestamp = "Today, 10:30",
                        gymName = "Gold's Gym",
                        sets = listOf("50" to "8", "55" to "6"),
                        isInProgress = true
                    )
                ),
                xValues = listOf(1.0, 2.0),
                volumeSeries = listOf(200.0, 800.0),
                oneRepMaxSeries = listOf(13.3, 33.3),
                isLoading = false,
                errorMessage = null
            ),
            onBack = {}
        )
    }
}

@Preview(
    name             = "Light Mode",
    uiMode           = Configuration.UI_MODE_NIGHT_NO,
    showBackground   = true
)
@Composable
fun Preview_ExerciseHistoryScreenContent_Light() {
    Preview_ExerciseHistoryScreenContent()
}

@Preview(
    name             = "Dark Mode",
    uiMode           = Configuration.UI_MODE_NIGHT_YES,
    showBackground   = true
)
@Composable
fun Preview_ExerciseHistoryScreenContent_Dark() {
    Preview_ExerciseHistoryScreenContent()
}