package com.example.fitnessapp.meterialcomponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitnessapp.ui.theme.FitnessappTheme

@Composable
fun PacktBottomNavigationBar() {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background,
        content = {
            NavigationBarItem(
                selected = false, onClick = { /*TODO*/ },
                icon = {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Home Screen"
                    )
                },
                label = {
                    Text(text = "History")
                }
            )

            NavigationBarItem(
                selected = false, onClick = { /*TODO*/ },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Account Screen"
                    )
                },
                label = {
                    Text(text = "Account")
                }
            )
        }
    )
}


@Preview
@Composable
fun PacktBottomNavigationBarPreview() {
    FitnessappTheme {
        PacktBottomNavigationBar()
    }
}