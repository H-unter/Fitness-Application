package com.example.fitnessapp.meterialcomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitnessapp.ui.theme.FitnessappTheme





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacktSmallTopAppBar() {
    var expanded by remember { mutableStateOf(false) }
    var selectedGym by remember { mutableStateOf("Main Gym") }
    val gymOptions = listOf("Main Gym", "Downtown Gym", "Campus Gym", "Home")

    TopAppBar(
        title = {
            Text("Monday Workout 01/01/2025")
        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable { expanded = true }
            ) {
                Text(text = selectedGym)
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select gym")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                gymOptions.forEach { gym ->
                    DropdownMenuItem(
                        text = { Text(gym) },
                        onClick = {
                            selectedGym = gym
                            expanded = false
                        }
                    )
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackMediumTopAooBar() {
    MediumTopAppBar(
        title = {
            Text(text = "Packt Publishing")
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackLargeTopBar() {
    LargeTopAppBar(
        title = {
            Text(text = "Packt Publishing")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacktCenterAlignedTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(text = "Packt Publishing")
        }
    )
}

@Preview
@Composable
fun PacktSmallTopAppBarPreview() {
    FitnessappTheme {
        PacktSmallTopAppBar()
    }
}

@Preview
@Composable
fun PackMediumTopAppBarPreview() {
    FitnessappTheme {
        PackMediumTopAooBar()
    }
}

@Preview
@Composable
fun PackLargeTopBarPreview() {
    FitnessappTheme {
        PackLargeTopBar()
    }
}

@Preview
@Composable
fun PacktCenterAlignedTopBarPreview() {
    FitnessappTheme {
        PacktCenterAlignedTopBar()
    }
}