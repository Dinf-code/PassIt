package com.dinachi.passit.userinterface.listing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dinachi.passit.viewmodel.ListingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    onListingCreated: () -> Unit,
    onBackPress: () -> Unit,
    viewModel: ListingViewModel = viewModel()
) {
    val uiState by viewModel.createUiState.collectAsState()
    var showCurrencyMenu by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.onPhotosSelected(uris)
    }

    // Show error snackbar if there's an error
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            // Error will be shown in UI, can add Snackbar here if needed
        }
    }

    Scaffold(
        containerColor = Color(0xFF1C1C1E),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Listing",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBackPress) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                },
                actions = {
                    Text(
                        text = "${uiState.selectedPhotos.size}/10",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Error message display
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Photos Section
            PhotosSection(
                selectedPhotos = uiState.selectedPhotos,
                onAddPhotoClick = { photoPickerLauncher.launch("image/*") },
                onRemovePhoto = { index -> viewModel.onRemovePhoto(index) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Details Section
            DetailsSection(
                title = uiState.title,
                onTitleChange = { viewModel.onTitleChange(it) },
                titleError = uiState.titleError,
                price = uiState.price,
                onPriceChange = { viewModel.onPriceChange(it) },
                priceError = uiState.priceError,
                selectedCurrency = uiState.selectedCurrency,
                onCurrencyClick = { showCurrencyMenu = true },
                selectedCondition = uiState.selectedCondition,
                onConditionSelected = { viewModel.onConditionChange(it) },
                selectedCategory = uiState.selectedCategory,
                onCategoryClick = { showCategorySheet = true },
                description = uiState.description,
                onDescriptionChange = { viewModel.onDescriptionChange(it) },
                descriptionError = uiState.descriptionError
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Post Listing Button
            Button(
                onClick = { viewModel.createListing(onSuccess = onListingCreated) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !uiState.isCreating
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1C1C1E),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Post Listing",
                        color = Color(0xFF1C1C1E),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Location Section
            LocationSection(
                location = uiState.location,
                onLocationChange = { viewModel.onLocationChange(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Currency Selection Menu
        if (showCurrencyMenu) {
            CurrencySelectionDialog(
                currentCurrency = uiState.selectedCurrency,
                onDismiss = { showCurrencyMenu = false },
                onCurrencySelected = { newCurrency ->
                    viewModel.onCurrencyChange(newCurrency)
                    showCurrencyMenu = false
                }
            )
        }

        // Category Selection Bottom Sheet
        if (showCategorySheet) {
            CategorySelectionSheet(
                currentCategory = uiState.selectedCategory,
                onDismiss = { showCategorySheet = false },
                onCategorySelected = { newCategory ->
                    viewModel.onCategoryChange(newCategory)
                    showCategorySheet = false
                }
            )
        }
    }
}

@Composable
fun PhotosSection(
    selectedPhotos: List<Uri>,
    onAddPhotoClick: () -> Unit,
    onRemovePhoto: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Photos",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Add up to 10 photos. First photo is the cover.",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add Photo Button
            item {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = selectedPhotos.size < 10) { onAddPhotoClick() }
                        .background(Color(0xFF2C2C2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add Photo",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Selected Photos
            itemsIndexed(selectedPhotos) { index, uri ->
                Box(
                    modifier = Modifier.size(100.dp)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Photo ${index + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Cover Badge (only on first photo)
                    if (index == 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Cover",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Remove Button
                    IconButton(
                        onClick = { onRemovePhoto(index) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsSection(
    title: String,
    onTitleChange: (String) -> Unit,
    titleError: String?,
    price: String,
    onPriceChange: (String) -> Unit,
    priceError: String?,
    selectedCurrency: String,
    onCurrencyClick: () -> Unit,
    selectedCondition: String,
    onConditionSelected: (String) -> Unit,
    selectedCategory: String,
    onCategoryClick: () -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionError: String?
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Details",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title Field
        Column {
            Text(
                text = "Title",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = {
                    Text(
                        text = "What are you selling?",
                        color = Color.White.copy(alpha = 0.3f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = titleError?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2C2C2E),
                    unfocusedContainerColor = Color(0xFF2C2C2E),
                    errorContainerColor = Color(0xFF2C2C2E),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Price Field
        Column {
            Text(
                text = "Price",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (price.isEmpty()) "" else "$ $price",
                    onValueChange = { newValue ->
                        onPriceChange(newValue.replace("$", "").trim())
                    },
                    placeholder = {
                        Text(
                            text = "$ 0.00",
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    isError = priceError != null,
                    supportingText = priceError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF2C2C2E),
                        unfocusedContainerColor = Color(0xFF2C2C2E),
                        errorContainerColor = Color(0xFF2C2C2E),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2C2E))
                        .clickable { onCurrencyClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = selectedCurrency,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$0.00 USD is based on current rates.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Condition
        Column {
            Text(
                text = "Condition",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("New", "Like New", "Good", "Fair").forEach { condition ->
                    ConditionChip(
                        text = condition,
                        selected = selectedCondition == condition,
                        onClick = { onConditionSelected(condition) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category
        Column {
            Text(
                text = "Category",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E))
                    .clickable { onCategoryClick() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCategory,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Column {
            Text(
                text = "Description",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = {
                    Text(
                        text = "Describe your item...",
                        color = Color.White.copy(alpha = 0.3f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                isError = descriptionError != null,
                supportingText = descriptionError?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2C2C2E),
                    unfocusedContainerColor = Color(0xFF2C2C2E),
                    errorContainerColor = Color(0xFF2C2C2E),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 6
            )
        }
    }
}

@Composable
fun ConditionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else Color(0xFF2C2C2E)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF1C1C1E) else Color.White,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun LocationSection(
    location: String,
    onLocationChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Location",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C2C2E))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = location,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$location M5V 3A8",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    TextButton(onClick = { /* Edit location */ }) {
                        Text(
                            text = "Edit",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFF3A3A3C)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Map Preview",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencySelectionDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    val currencies = listOf("CAD", "USD", "EUR", "GBP", "JPY", "AUD")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Currency",
                color = Color.White
            )
        },
        text = {
            LazyColumn {
                items(currencies) { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencySelected(currency) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currency,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        if (currency == currentCurrency) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF2C2C2E)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionSheet(
    currentCategory: String,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "Electronics & Gadgets" to Icons.Default.PhoneAndroid,
        "Clothing & Fashion" to Icons.Default.ShoppingBag,
        "Home & Garden" to Icons.Default.Home,
        "Sports & Outdoors" to Icons.Default.SportsSoccer,
        "Books & Media" to Icons.Default.Book,
        "Toys & Games" to Icons.Default.SportsEsports,
        "Vehicles" to Icons.Default.DirectionsCar,
        "Furniture" to Icons.Default.Chair,
        "Art & Collectibles" to Icons.Default.Palette,
        "Other" to Icons.Default.MoreHoriz
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C2C2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Category",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(categories) { (category, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = category,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        if (category == currentCategory) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (category != categories.last().first) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}