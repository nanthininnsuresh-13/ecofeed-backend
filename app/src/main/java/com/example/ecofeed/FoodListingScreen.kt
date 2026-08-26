package com.example.ecofeed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ecofeed.ui.theme.EcoFeedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListingScreen() {
    var foodName by remember { mutableStateOf("") }
    var isEdible by remember { mutableStateOf(true) }
    var expiryHours by remember { mutableStateOf("") }
    var locationInfo by remember { mutableStateOf("Location not captured") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List Food Item") },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Food Name Input
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food Name") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Fresh Apples") }
            )

            // Category Selection (Edible / Non-Edible)
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isEdible,
                        onClick = { isEdible = true }
                    )
                    Text("Edible", modifier = Modifier.padding(end = 16.dp))
                    
                    RadioButton(
                        selected = !isEdible,
                        onClick = { isEdible = false }
                    )
                    Text("Non-Edible")
                }
            }

            // Expiry Hours Input
            OutlinedTextField(
                value = expiryHours,
                onValueChange = { expiryHours = it },
                label = { Text("Expiry Hours") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("e.g. 24") }
            )

            // GPS Location Capture
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GPS Location",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = locationInfo,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IconButton(
                        onClick = {
                            // Placeholder for GPS location capture logic
                            locationInfo = "Lat: 40.7128, Long: -74.0060" 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Capture Location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = { /* Handle listing submission */ },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Post Listing")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodListingScreenPreview() {
    EcoFeedTheme {
        FoodListingScreen()
    }
}
