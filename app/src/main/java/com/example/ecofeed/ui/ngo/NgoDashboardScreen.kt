package com.example.ecofeed.ui.ngo

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ecofeed.data.model.FoodListingDto
import com.example.ecofeed.ui.auth.AuthViewModel
import com.example.ecofeed.ui.common.EcoFeedDrawerContent
import com.example.ecofeed.ui.common.EcoFeedTopAppBar
import kotlinx.coroutines.launch

private val EcoGreen = Color(0xFF008000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NgoDashboardScreen(
    ngoId: String,
    onNavigateToDetail: (String) -> Unit,
    onViewMapClicked: () -> Unit,
    onNotificationsClicked: () -> Unit,
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
    
    var searchQuery by remember { mutableStateOf("") }
    var feedbackDonationId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchAvailableDonations()
    }

    feedbackDonationId?.let { donationId ->
        val donorId = uiState.availableDonations.firstOrNull { it._id == donationId }?.donorId ?: ""
        NgoFeedbackDialog(
            donationId = donationId,
            donorId = donorId,
            ngoId = ngoId,
            onDismiss = { feedbackDonationId = null },
            onSubmit = { review ->
                viewModel.submitReview(review) {
                    feedbackDonationId = null
                    Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                }
            }
        )
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
                            onClick = onViewMapClicked,
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
                        fontWeight = FontWeight.Bold
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
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail (safe access)
            val image = item.imageUrls?.firstOrNull().orEmpty()
            if (image.isNotBlank()) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE9ECEF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (item.isAiRecommended) {
                    Surface(shape = RoundedCornerShape(8.dp), color = EcoGreen.copy(alpha = 0.12f)) {
                        Text(text = "🤖 AI Recommended", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = EcoGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "${item.quantity} • Expires: ${item.expiryDate}", color = Color.Gray, fontSize = 12.sp)
                Text(text = "Donor: ${item.establishmentName ?: item.donorName ?: item.hotelName ?: item.address ?: "Nearby Donor"}", style = MaterialTheme.typography.bodySmall, color = EcoGreen)
                Text(text = "📞 ${item.donorPhoneNumber ?: "No contact"} • ⭐ ${String.format("%.1f", item.averageRating)} (${item.reviewCount} reviews)", color = Color(0xFFFFA500), fontSize = 11.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    val dietaryBadge = item.dietaryCategory ?: item.dietaryType ?: "VEG"
                    Surface(shape = RoundedCornerShape(8.dp), color = EcoGreen.copy(alpha = 0.08f)) {
                        Text(text = dietaryBadge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = EcoGreen, style = MaterialTheme.typography.labelSmall)
                    }
                    item.packagingType?.let { pt ->
                        Surface(shape = RoundedCornerShape(8.dp), color = Color.LightGray.copy(alpha = 0.08f)) {
                            Text(text = pt, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.DarkGray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "📍 1.2 km", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                val isAvailable = item.status.equals("AVAILABLE", ignoreCase = true)
                Button(
                    onClick = onAccept,
                    enabled = isAvailable,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAvailable) EcoGreen else Color.Gray),
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
