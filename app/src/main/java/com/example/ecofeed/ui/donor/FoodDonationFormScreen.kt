package com.example.ecofeed.ui.donor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ecofeed.data.model.CreateDonationRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private val EcoGreen = Color(0xFF008000)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun FoodDonationFormScreen(
    donorId: String,
    onBackClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {},
    viewModel: DonationViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var foodName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var mealsCount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("LUNCH") }
    var expiryDate by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var donorSourceType by remember { mutableStateOf("Restaurant / Hotel") }
    var establishmentName by remember { mutableStateOf("") }
    var dietaryCategory by remember { mutableStateOf("VEG") }
    var donorPhoneNumber by remember { mutableStateOf("") }
    var mealCompositionText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var storageCondition by remember { mutableStateOf("ROOM_TEMP") }
    var dietaryType by remember { mutableStateOf("VEG") }
    var packagingType by remember { mutableStateOf("PACKED_CONTAINERS") }
    var isEdible by remember { mutableStateOf(true) }

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Location & Permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        if (locationPermissionsState.allPermissionsGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    
                    // Reverse Geocoding
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                withContext(Dispatchers.Main) {
                                    addressText = addresses[0].getAddressLine(0)
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                addressText = "Lat: ${it.latitude}, Lng: ${it.longitude}"
                            }
                        }
                    }
                }
            }
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    // Auto-fetch location on start if permitted
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            fetchCurrentLocation()
        }
    }

    if (uiState.isSuccess) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Donation successful!", Toast.LENGTH_SHORT).show()
            onSubmitClick()
            viewModel.resetSuccess()
        }
    }

    // Multi-Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = (selectedImages + uris).take(3)
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        expiryDate = dateFormatter.format(Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = EcoGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donate Food", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EcoGreen)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Food Name
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food Name") },
                placeholder = { Text("e.g. Fresh Biryani") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Qty (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = mealsCount,
                    onValueChange = { mealsCount = it },
                    label = { Text("Meals") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Donor Source Type & Establishment
            Column {
                Text("Donor Source", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                val sources = listOf("Restaurant / Hotel", "Catering / Event", "Private Party / Birthday", "Household", "Other")
                ExposedDropdownMenuBox(expanded = false, onExpandedChange = { /* no-op placeholder */ }) {
                    // simple chips as before
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sources) { src ->
                            FilterChip(
                                selected = donorSourceType == src,
                                onClick = { donorSourceType = src },
                                label = { Text(src) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EcoGreen.copy(alpha = 0.1f),
                                    selectedLabelColor = EcoGreen
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = establishmentName,
                    onValueChange = { establishmentName = it },
                    label = { Text("Event / Establishment Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Dietary Category & Contact Details
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Dietary Category", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                val dietaryOptions = listOf("VEG", "NON_VEG", "BOTH")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dietaryOptions) { option ->
                        FilterChip(
                            selected = dietaryCategory == option,
                            onClick = {
                                dietaryCategory = option
                                dietaryType = option
                            },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EcoGreen.copy(alpha = 0.1f),
                                selectedLabelColor = EcoGreen
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = donorPhoneNumber,
                    onValueChange = { donorPhoneNumber = it.filter(Char::isDigit).take(10) },
                    label = { Text("Donor Contact Number") },
                    placeholder = { Text("9876543210") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = mealCompositionText,
                    onValueChange = { mealCompositionText = it },
                    label = { Text("Meal Items / Composition") },
                    placeholder = { Text("Rice, Sambar, Curd, Chapati") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Optional Description / Notes") },
                    placeholder = { Text("Handled with clean packaging, urgent pickup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Category Selection (Dropdown/Row)
            Column {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                val categories = listOf("LUNCH", "DINNER", "BREAKFAST", "SNACKS", "BAKERY", "EXPIRED")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EcoGreen.copy(alpha = 0.1f),
                                selectedLabelColor = EcoGreen
                            )
                        )
                    }
                }
            }

            // Expiry & Prep Time
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Expiry Date") },
                    modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    }
                )
                OutlinedTextField(
                    value = prepTime,
                    onValueChange = { prepTime = it },
                    label = { Text("Prep Time") },
                    placeholder = { Text("e.g. 12:00 PM") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Location with Auto-Geocoding
            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                label = { Text("Pickup Address") },
                placeholder = { Text("Fetching location...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcoGreen)
                },
                trailingIcon = {
                    IconButton(onClick = { fetchCurrentLocation() }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Get Location")
                    }
                }
            )

            // Storage / Dietary / Packaging / Edible
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Storage
                    val storageOptions = listOf("ROOM_TEMP", "REFRIGERATED", "FROZEN")
                    ExposedDropdownMenuBox(expanded = false, onExpandedChange = { }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(storageOptions) { opt ->
                                FilterChip(selected = storageCondition == opt, onClick = { storageCondition = opt }, label = { Text(opt) })
                            }
                        }
                    }

                    // Dietary
                    val dietOptions = listOf("VEG", "NON_VEG", "VEGAN")
                    ExposedDropdownMenuBox(expanded = false, onExpandedChange = { }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(dietOptions) { opt ->
                                FilterChip(selected = dietaryType == opt, onClick = { dietaryType = opt }, label = { Text(opt) })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val packOptions = listOf("PACKED_CONTAINERS", "BULK_VESSELS", "UNPACKED")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(packOptions) { opt ->
                            FilterChip(selected = packagingType == opt, onClick = { packagingType = opt }, label = { Text(opt) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEdible, onCheckedChange = { isEdible = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Is this food edible for immediate consumption?", color = Color.Gray)
                }
            }

            // Multi-Image Selection Preview
            Column {
                Text("Food Photos (Max 3)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Selection Button
                    if (selectedImages.size < 3) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    // Image Previews
                    selectedImages.forEachIndexed { index, uri ->
                        Box(modifier = Modifier.size(100.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImages = selectedImages.filterIndexed { i, _ -> i != index } },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.White, CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = Color.Red, fontSize = 14.sp)
            }

            // Submit Button
            Button(
                onClick = {
                    val request = viewModel.buildCompressedDonationRequest(
                        context = context,
                        donorId = donorId,
                        foodName = foodName,
                        donorSourceType = donorSourceType,
                        establishmentName = establishmentName,
                        category = category,
                        quantity = quantity,
                        mealsCount = mealsCount,
                        prepTime = prepTime,
                        expiryDate = expiryDate,
                        addressText = addressText,
                        latitude = latitude,
                        longitude = longitude,
                        dietaryCategory = dietaryCategory,
                        packagingType = packagingType,
                        donorPhoneNumber = donorPhoneNumber,
                        mealCompositionText = mealCompositionText,
                        description = description,
                        isEdible = isEdible,
                        selectedImages = selectedImages
                    )
                    viewModel.submitDonation(request, onSubmitClick)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                enabled = !uiState.isLoading && foodName.isNotBlank() && addressText.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SUBMIT DONATION", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
