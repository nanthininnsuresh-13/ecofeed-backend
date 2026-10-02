package com.example.ecofeed.ui.biogas

import android.content.ActivityNotFoundException
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofeed.data.model.FoodListingDto
import com.example.ecofeed.ui.auth.AuthViewModel
import com.example.ecofeed.ui.common.EcoFeedDrawerContent
import com.example.ecofeed.ui.common.EcoFeedTopAppBar
import kotlinx.coroutines.launch

private val EcoGreen = Color(0xFF008000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiogasDashboardScreen(
    biogasPartnerId: String,
    onNavigateToDetail: (String) -> Unit,
    onNotificationsClicked: () -> Unit,
    onFeedbackClick: () -> Unit = {},
    onProfileClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: BiogasViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EcoFeedDrawerContent(
                userName = "${authState.firstName} ${authState.lastName}",
                userEmail = authState.email,
                userRole = "Biogas Partner",
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
                        title = "EcoFeed - Biogas Partner",
                        profileImageUrl = authState.profileImageUrl,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onNotificationsClick = onNotificationsClicked,
                        onLogoutClick = onLogout
                    )
                },
                bottomBar = {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()
                    ) {
                        Button(
                            onClick = {
                                val acceptedDonations = uiState.availableDonations.filter { it.status == "ACCEPTED" }
                                if (acceptedDonations.isEmpty()) {
                                    Toast.makeText(context, "No accepted pickups to map.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                // For simplicity, navigating to the first accepted donation's address
                                val firstDonation = acceptedDonations.first()
                                val destination = if (firstDonation.coordinates.size == 2) {
                                    "${firstDonation.coordinates[1]},${firstDonation.coordinates[0]}"
                                } else {
                                    firstDonation.address ?: "Anna Nagar, Tiruchirappalli"
                                }
                                
                                val mapUri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {
                                    val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(destination)}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EcoGreen),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Route, contentDescription = null, tint = EcoGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🗺️ VIEW ROUTE MAP", color = EcoGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    Text(
                        text = "Waste Collection Requests",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.availableDonations) { item ->
                            BiogasWasteCard(
                                item = item,
                                onAccept = {
                                    val donationId = item._id.orEmpty()
                                    if (donationId.isNotBlank()) {
                                        viewModel.acceptDonation(donationId, biogasPartnerId) { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onClick = { onNavigateToDetail(item._id.orEmpty()) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun BiogasWasteCard(item: FoodListingDto, onAccept: () -> Unit, onClick: () -> Unit) {
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
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "${item.quantity} • ${item.establishmentName ?: item.donorName ?: "Unknown source"}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(text = "Expiry: ${item.expiryTime ?: item.expiryDate ?: "Not set"}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Text(text = "Location: ${item.address ?: "Anna Nagar"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "1.3 km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                val isAvailable = item.status.equals("AVAILABLE", ignoreCase = true)
                Button(
                    onClick = onAccept,
                    enabled = isAvailable,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(if (isAvailable) "ACCEPT PICKUP" else "ACCEPTED", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
