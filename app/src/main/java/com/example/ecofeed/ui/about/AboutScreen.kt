package com.example.ecofeed.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrimaryGreen = Color(0xFF2E7D32)
private val LightGreenAccent = Color(0xFFE8F5E9)
private val DarkGreenText = Color(0xFF1B5E20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About EcoFeed", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightGreenAccent)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Header Section
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "EcoFeed Logo",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = "EcoFeed",
                        color = DarkGreenText,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "v1.2.0 - Sustainable Tech Edition",
                        color = PrimaryGreen,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bridge the gap between surplus food and community hunger while driving zero-waste circular energy.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Impact Pillars
            item {
                Text(
                    text = "Our Core Impact Pillars",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreenText,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val pillars = listOf(
                Pillar("Zero Hunger Direct Routing", "AI-matched real-time surplus food dispatch to verified local NGOs.", Icons.Default.Restaurant),
                Pillar("Circular Energy Recovery", "Automated redirection of non-edible organic waste directly to local Biogas partners.", Icons.Default.Bolt),
                Pillar("Smart Proximity Tracking", "Direct Google Maps integration for minimal carbon footprint pickups.", Icons.Default.MyLocation),
                Pillar("Verified Trust Framework", "Mutual rating system ensuring food safety and partner reliability.", Icons.Default.VerifiedUser)
            )

            items(pillars) { pillar ->
                ImpactPillarCard(pillar)
            }

            // Impact Counter Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Live Eco-Impact Tracker",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "10,000+ Meals Served\n4,500 kg Waste Converted\n12.5 Tons CO2 Saved",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
            }

            // Footer
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Developed with ❤️ by the EcoFeed Team",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        TextButton(onClick = onPrivacyPolicyClick) {
                            Text("Privacy Policy", color = PrimaryGreen, fontSize = 12.sp)
                        }
                        Text("|", color = Color.Gray, modifier = Modifier.padding(top = 10.dp))
                        TextButton(onClick = { 
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:ecofeedorganization@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "EcoFeed Support Query")
                            }
                            try {
                                context.startActivity(Intent.createChooser(emailIntent, "Send Email via..."))
                            } catch (e: Exception) {
                                // handle error
                            }
                        }) {
                            Text("Support Email", color = PrimaryGreen, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

data class Pillar(val title: String, val description: String, val icon: ImageVector)

@Composable
fun ImpactPillarCard(pillar: Pillar) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGreenAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(pillar.icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = pillar.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreenText
                )
                Text(
                    text = pillar.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}
