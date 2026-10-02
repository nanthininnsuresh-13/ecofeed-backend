package com.example.ecofeed.ui.ngo

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ecofeed.data.model.FoodListingDto
import com.example.ecofeed.ui.auth.AuthViewModel
import com.example.ecofeed.ui.common.EcoFeedDrawerContent
import com.example.ecofeed.ui.common.EcoFeedTopAppBar
import com.example.ecofeed.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

private val EcoGreen = Color(0xFF008000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NgoDashboardScreen(
    ngoId: String,
    onNavigateToDetail: (String) -> Unit,
    onViewMapClicked: () -> Unit,
    onNotificationsClicked: () -> Unit,
    onFeedbackClick: () -> Unit = {},
    onProfileClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: NgoViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var searchQuery by remember { mutableStateOf("") }
    var feedbackDonationId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                authState.userId?.let { authViewModel.fetchUserProfile(it) }
                viewModel.fetchAvailableDonations()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        authState.userId?.let { authViewModel.fetchUserProfile(it) }
        viewModel.fetchAvailableDonations()
    }

    feedbackDonationId?.let { donationId ->
        val donation = uiState.availableDonations.find { it._id == donationId }
        if (donation != null) {
            val donorId = donation.donorId.orEmpty()
            if (donationId.isNotBlank() && donorId.isNotBlank()) {
                NgoFeedbackDialog(
                    donationId = donationId,
                    donorId = donorId,
                    ngoId = ngoId,
                    foodTitle = donation.title,
                    onDismiss = { feedbackDonationId = null },
                    onSubmit = { review ->
                        viewModel.submitReview(review) {
                            feedbackDonationId = null
                            Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                feedbackDonationId = null
            }
        } else {
            feedbackDonationId = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EcoFeedDrawerContent(
                userName = "${authState.firstName} ${authState.lastName}",
                userEmail = authState.email,
                userRole = "NGO Partner",
                profileImageUrl = authState.profileImageUrl,
                onProfileClick = { 
                    onProfileClick()
                    coroutineScope.launch { drawerState.close() }
                },
                onHistoryClick = { 
                    onHistoryClick()
                    coroutineScope.launch { drawerState.close() }
                },
                onNotificationsClick = { 
                    onNotificationsClicked()
                    coroutineScope.launch { drawerState.close() }
                },
                onFeedbackClick = {
                    onFeedbackClick()
                    coroutineScope.launch { drawerState.close() }
                },
                onSettingsClick = { 
                    onSettingsClick()
                    coroutineScope.launch { drawerState.close() }
                },
                onAboutClick = { 
                    onAboutClick()
                    coroutineScope.launch { drawerState.close() }
                },
                onLogoutClick = onLogout,
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    EcoFeedTopAppBar(
                        title = "EcoFeed - NGO",
                        profileImageUrl = authState.profileImageUrl,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onNotificationsClick = onNotificationsClicked,
                        onLogoutClick = onLogout
                    )
                },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = {
                                val firstDonation = uiState.availableDonations.firstOrNull()
                                if (firstDonation != null) {
                                    val destination = if (firstDonation.coordinates.size == 2) {
                                        "${firstDonation.coordinates[1]},${firstDonation.coordinates[0]}"
                                    } else {
                                        firstDonation.address ?: "Trichy, Tamil Nadu, India"
                                    }
                                    
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (_: Exception) {
                                        // Fallback to browser
                                        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(destination)}")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                                    }
                                } else {
                                    Toast.makeText(context, "No active donations to show on map", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EcoGreen),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = EcoGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🗺️ VIEW MAP", color = EcoGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Header Title
                    Text(
                        text = "Available Donations Near You",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search food / location") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    if (uiState.isLoading && uiState.availableDonations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = EcoGreen)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.availableDonations.filter {
                                (it.title ?: "").contains(searchQuery, ignoreCase = true) ||
                                (it.address ?: "").contains(searchQuery, ignoreCase = true)
                            }) { item ->
                                NgoDonationCard(
                                    item = item,
                                    onAccept = {
                                        viewModel.acceptDonation(item._id, ngoId) { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            feedbackDonationId = item._id
                                        }
                                    },
                                    onClick = { onNavigateToDetail(item._id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NgoDonationCard(item: FoodListingDto, onAccept: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column {
            // CRITICAL FIX: Full-width image at the top of the card
            val imageUrl = item.imageUrl ?: item.imageUrls.firstOrNull()
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Donated Food Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.res.painterResource(id = com.example.ecofeed.R.drawable.ic_placeholder_food),
                    placeholder = androidx.compose.ui.res.painterResource(id = com.example.ecofeed.R.drawable.ic_placeholder_food)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                }
            }

            // Card content below the image
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (item.isAiRecommended) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                            Text(text = "🤖 AI Recommended", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "${item.quantity} • Expires: ${item.expiryDate}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(text = "Donor: ${item.establishmentName ?: item.donorName ?: item.hotelName ?: item.address ?: "Nearby Donor"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = "📞 ${item.donorPhoneNumber ?: "No contact"} • ⭐ ${String.format("%.1f", item.averageRating)} (${item.reviewCount} reviews)", color = Color(0xFFFFA500), fontSize = 11.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        val dietaryBadge = item.dietaryCategory ?: item.dietaryType ?: "VEG"
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
                            Text(text = dietaryBadge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                        item.packagingType?.let { pt ->
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)) {
                                Text(text = pt, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "📍 1.2 km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    val isAvailable = item.status.equals("AVAILABLE", ignoreCase = true)
                    Button(
                        onClick = onAccept,
                        enabled = isAvailable,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (isAvailable) "ACCEPT" else "ACCEPTED", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
