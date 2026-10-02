package com.example.ecofeed.ui.listing

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

private val EcoGreen = Color(0xFF008000)
private val EcoOrange = Color(0xFFFFA500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListingDetailScreen(
    donationId: String,
    isEdible: Boolean = true,
    acceptedBy: String = "",
    onBack: () -> Unit,
    onAcceptClick: () -> Unit = {},
    onDirectionsClick: () -> Unit = {},
    viewModel: DonationDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showDetailSheet by remember { mutableStateOf(false) }

    LaunchedEffect(donationId) {
        viewModel.loadDonation(donationId)
    }

    val donation = uiState.donation
    val title = donation?.title ?: "Donation"
    val donorName = donation?.establishmentName ?: donation?.donorName ?: "Donor"
    val categoryText = "${donation?.category ?: "General"} / ${donation?.dietaryCategory ?: donation?.dietaryType ?: "VEG"}"
    val quantityText = "${donation?.mealsCount ?: 0} Meals (${donation?.quantityKg ?: 0.0} kg)"
    val timingText = "Prep: ${donation?.prepTime ?: "N/A"} | Exp: ${donation?.expiryTime ?: donation?.expiryDate ?: "N/A"}"

    val callAction = {
        val phone = donation?.donorPhoneNumber?.trim().orEmpty()
        if (phone.isBlank()) {
            Toast.makeText(context, "Phone number not provided by donor", Toast.LENGTH_SHORT).show()
        } else {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(callIntent)
        }
    }

    val chatAction = {
        val phone = donation?.donorPhoneNumber?.trim().orEmpty()
        if (phone.isBlank()) {
            Toast.makeText(context, "Phone number not provided by donor", Toast.LENGTH_SHORT).show()
        } else {
            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                putExtra("sms_body", "Hi, I am contacting you regarding your food donation: $title on EcoFeed.")
            }
            context.startActivity(smsIntent)
        }
    }

    val directionsAction = {
        val address = donation?.address?.takeIf { it.isNotBlank() } ?: "Donation location"
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(mapIntent)
        onDirectionsClick()
    }

    val acceptAction = {
        val id = donationId.ifBlank { donation?._id.orEmpty() }
        if (id.isNotBlank()) {
            viewModel.acceptDonation(id, acceptedBy.ifBlank { "" }) {
                Toast.makeText(context, "Donation successfully reserved!", Toast.LENGTH_SHORT).show()
                onAcceptClick()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donation Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EcoGreen)
            )
        },
        bottomBar = {
            BottomActionBar(
                isEdible = isEdible,
                onAcceptClick = acceptAction,
                onDirectionsClick = directionsAction,
                onViewDetailsClick = { showDetailSheet = true }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && donation == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EcoGreen)
            }
            return@Scaffold
        }

        if (donation == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Unable to load donation details")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { PriorityBadgeBar(donation.priorityLevel ?: "MEDIUM", donation.address ?: "Location") }
            item { HeroSection(title, donation.imageUrls.orEmpty()) }
            item { DonorInfoCard(donorName, donation.donorPhoneNumber ?: "", callAction, chatAction) }
            item { DetailsGrid(categoryText, quantityText, timingText, donation.packagingType ?: "Not specified") }
            item { AiMatchingCard(isEdible, donation.address ?: "Location not provided") }
            if (!donation.description.isNullOrBlank()) {
                item { DescriptionCard(donation.description ?: "") }
            }
            item { ImpactMetricsBanner(donation.mealsCount ?: 0, donation.quantityKg ?: 0.0) }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showDetailSheet && donation != null) {
        val detailMealComposition = donation.mealComposition?.joinToString(", ") ?: "Not provided"
        val detailStorage = donation.storageCondition ?: "Not specified"
        val detailSafety = donation.description?.takeIf { it.isNotBlank() } ?: "No additional donor notes"

        AlertDialog(
            onDismissRequest = { showDetailSheet = false },
            confirmButton = {
                TextButton(onClick = { showDetailSheet = false }) { Text("Close") }
            },
            title = { Text("Donation Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Meal Composition: $detailMealComposition")
                    Text("Storage: $detailStorage")
                    Text("Safety Notes: $detailSafety")
                }
            }
        )
    }
}

@Composable
fun PriorityBadgeBar(priority: String, address: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BadgeChip("🟢 Priority: $priority", EcoGreen)
        BadgeChip("📍 ${address.take(18)}", MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BadgeChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HeroSection(title: String, imageUrls: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        if (imageUrls.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(imageUrls.take(3)) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("Available", EcoGreen)
        }
    }
}

@Composable
fun StatusChip(text: String, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(4.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun DonorInfoCard(donorName: String, donorPhone: String, onCall: () -> Unit, onChat: () -> Unit) {
    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = donorName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.Default.CheckCircle, "", tint = EcoGreen, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                    }
                    Text(text = if (donorPhone.isBlank()) "No phone provided" else donorPhone, color = EcoOrange, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Call, "", tint = EcoGreen, modifier = Modifier.size(18.dp))
                    Text("Call Donor", color = EcoGreen, modifier = Modifier.padding(start = 8.dp))
                }
                Button(
                    onClick = onChat,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, "", tint = EcoGreen, modifier = Modifier.size(18.dp))
                    Text("Chat", color = EcoGreen, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
fun DetailsGrid(categoryText: String, quantityText: String, timingText: String, packagingType: String) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DetailItem("Category", categoryText, Icons.Default.Category)
            DetailItem("Quantity", quantityText, Icons.Default.Scale)
            DetailItem("Timing", timingText, Icons.Default.Timer)
            DetailItem("Packaging", packagingType, Icons.Default.Inventory2)
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = EcoGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AiMatchingCard(isEdible: Boolean, address: String) {
    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGreen.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, EcoGreen.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isEdible) "🤖 AI Recommended Receiver" else "♻️ Non-Edible Fallback",
                fontWeight = FontWeight.Bold,
                color = EcoGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(address, fontWeight = FontWeight.SemiBold)
            Text("Pickup Address • Available for immediate collection", fontSize = 14.sp)
        }
    }
}

@Composable
fun DescriptionCard(description: String) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Donor Notes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ImpactMetricsBanner(meals: Int, weightKg: Double) {
    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGreen),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ImpactMetric("🍽️ $meals", "Meals")
            ImpactMetric("♻️ ${"%.1f".format(weightKg)}kg", "Weight")
            ImpactMetric("🌱 1", "Impact")
        }
    }
}

@Composable
fun ImpactMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

@Composable
fun BottomActionBar(
    isEdible: Boolean,
    onAcceptClick: () -> Unit,
    onDirectionsClick: () -> Unit,
    onViewDetailsClick: () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Button(
                onClick = onAcceptClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
            ) {
                Text(
                    text = if (isEdible) "ACCEPT DONATION" else "REQUEST WASTE PICKUP",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onViewDetailsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Details")
                }
                OutlinedButton(
                    onClick = onDirectionsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Get Directions")
                }
            }
        }
    }
}
