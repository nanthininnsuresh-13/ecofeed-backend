package com.example.ecofeed.ui.donor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofeed.ui.auth.AuthViewModel
import com.example.ecofeed.ui.common.EcoFeedDrawerContent
import com.example.ecofeed.ui.common.EcoFeedTopAppBar
import com.example.ecofeed.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

private val EcoGreen = Color(0xFF008000)

data class DonationItem(
    val id: Int,
    val title: String,
    val weight: String,
    val date: String,
    val status: String,
    val statusColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorDashboardScreen(
    onDonateClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    authViewModel: AuthViewModel,
    donationViewModel: DonationViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val donationState by donationViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState.userId) {
        authState.userId?.let { donationViewModel.fetchHistory(it) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EcoFeedDrawerContent(
                userName = "${authState.firstName} ${authState.lastName}",
                userEmail = authState.email,
                userRole = "Food Donor",
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
                    onNotificationsClick()
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
                        title = "EcoFeed - Donor",
                        profileImageUrl = authState.profileImageUrl,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onNotificationsClick = onNotificationsClick,
                        onLogoutClick = onLogout
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Welcome Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Welcome, ${authState.firstName}!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Thank you for helping reduce food waste.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Quick Action Cards Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Inventory,
                                label = "Donate Food",
                                onClick = onDonateClick
                            )
                            ActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Assignment,
                                label = "Donation History",
                                onClick = onHistoryClick
                            )
                            ActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.AccountCircle,
                                label = "Profile",
                                onClick = onProfileClick
                            )
                        }
                    }

                    // Recent Donations Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Donations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onHistoryClick) {
                                Text("View All", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Recent Donations List
                    if (donationState.isLoading && donationState.history.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryGreen)
                            }
                        }
                    } else if (donationState.history.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No active donations yet", color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = onDonateClick, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                                    Text("Create Donation")
                                }
                            }
                        }
                    } else {
                        items(donationState.history.take(5)) { item ->
                            val displayItem = DonationItem(
                                id = item.id.hashCode(),
                                title = item.title ?: "Unknown Food",
                                weight = item.quantity ?: "0 kg",
                                date = item.createdAt ?: "N/A",
                                status = item.status ?: "PENDING",
                                statusColor = when(item.status?.uppercase()) {
                                    "ACCEPTED" -> PrimaryGreen
                                    "PENDING", "AVAILABLE" -> Color(0xFFFFA500)
                                    else -> Color.Gray
                                }
                            )
                            DonationListItem(displayItem)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EcoGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun DonationListItem(donation: DonationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE9ECEF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (donation.title == "Rice") Icons.Default.Fastfood else Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = donation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${donation.weight} • ${donation.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            // Status Badge
            Surface(
                color = donation.statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = donation.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = donation.statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
