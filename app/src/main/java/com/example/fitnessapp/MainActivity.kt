package com.example.fitnessapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.meterialcomponents.PacktBottomNavigationBar
import com.example.fitnessapp.meterialcomponents.PacktFloatingActionButton
import com.example.fitnessapp.meterialcomponents.PacktSmallTopAppBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val sets = remember {
        mutableStateListOf(
            Pair("40", "10"),
            Pair("70", "6"),
            Pair("70", "5")
        )
    }

    Scaffold(
        topBar = {
            PacktSmallTopAppBar()
        },
        bottomBar = {
            PacktBottomNavigationBar()
        },
        floatingActionButton = {
            PacktFloatingActionButton()
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    vertical = 16.dp,
                    horizontal = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Bench Press · Barbell",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                itemsIndexed(sets) { index, (kg, reps) ->
                    SetRow(
                        setIndex = index + 1,
                        weight = kg,
                        reps = reps,
                        onWeightChange = { newKg -> sets[index] = sets[index].copy(first = newKg) },
                        onRepsChange = { newReps -> sets[index] = sets[index].copy(second = newReps) }
                    )
                }

            }
        }
    )
}


@Preview
@Composable
fun GreetingPreview() {
    FitnessappTheme {
        MainScreen()
    }
}


@Composable
fun SetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$label")},
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun SetRow(
    setIndex: Int,
    weight: String,
    reps: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 1.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "$setIndex",
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.width(24.dp)
            )

            SetTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = "Weight",
                modifier = modifier.weight(10f)
            )

            SetTextField(
                value = reps,
                onValueChange = onRepsChange,
                label = "Reps",
                modifier = modifier.weight(10f)
            )
        }
    }
}



@Preview
@Composable

fun ExerciseListPreview() {
    FitnessappTheme(darkTheme = true) {
    val sets = remember {
        mutableStateListOf(
            Pair("40", "10"),
            Pair("70", "6"),
            Pair("70", "5")
        )
    }

    LazyColumn {
        itemsIndexed(sets) { index, (kg, reps) ->
            SetRow(
                setIndex = index + 1,
                weight = kg,
                reps = reps,
                onWeightChange = { newKg -> sets[index] = sets[index].copy(first = newKg) },
                onRepsChange = { newReps -> sets[index] = sets[index].copy(second = newReps) }
            )
        }
    }
    }
}
