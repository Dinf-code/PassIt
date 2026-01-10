package com.dinachi.passit.userinterface.home


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dinachi.passit.ui.theme.GoldPrimary
import com.dinachi.passit.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * Search & Filter Screen
 * Shows search results with filter bottom sheet
 * Uses HomeViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterScreen(
    onListingClick: (String) -> Unit = {},
    onBackPress: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(true) } // Show filter by default
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1410))
    ) {
        // Main content - search results
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top search bar with back button
            SearchTopBar(
                searchQuery = state.searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onBackPress = onBackPress,
                onFilterClick = { showFilterSheet = true }
            )

            // Results count
            if (state.searchResults.isNotEmpty()) {
                Text(
                    text = "🔍 ${state.resultsCount} results",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                // Search results grid
                SearchResultsGrid(
                    results = state.searchResults,
                    onListingClick = onListingClick
                )
            }
        }

        // Filter Bottom Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF2C2416),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
            ) {
                FilterBottomSheetContent(
                    selectedSort = state.selectedSort,
                    onSortSelected = { viewModel.updateSortOption(it) },
                    priceRange = state.priceRange,
                    onPriceRangeChange = { viewModel.updatePriceRange(it) },
                    selectedCondition = state.selectedCondition,
                    onConditionSelected = { viewModel.updateCondition(it) },
                    resultsCount = state.resultsCount,
                    onShowResults = {
                        viewModel.applyFilters()
                        scope.launch {
                            sheetState.hide()
                            showFilterSheet = false
                        }
                    },
                    onReset = { viewModel.resetFilters() }
                )
            }
        }
    }
}

/**
 * Search top bar with back button and filter
 */
@Composable
fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackPress: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Back button
        IconButton(onClick = onBackPress) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Search input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    "Search furniture, electronics...",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF2C2416),
                unfocusedContainerColor = Color(0xFF2C2416),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Filter button
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF2C2416), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = Color.White
            )
        }
    }
}

/**
 * Filter Bottom Sheet Content
 */
@Composable
fun FilterBottomSheetContent(
    selectedSort: String,
    onSortSelected: (String) -> Unit,
    priceRange: ClosedFloatingPointRange<Float>,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    selectedCondition: String,
    onConditionSelected: (String) -> Unit,
    resultsCount: Int,
    onShowResults: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter & Sort",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onReset) {
                Text(
                    text = "Reset",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SORT BY section
        Text(
            text = "SORT BY",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sort options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "Recommended",
                isSelected = selectedSort == "Recommended",
                onClick = { onSortSelected("Recommended") }
            )
            FilterChip(
                label = "Lowest Price",
                isSelected = selectedSort == "Lowest Price",
                onClick = { onSortSelected("Lowest Price") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FilterChip(
            label = "Newest",
            isSelected = selectedSort == "Newest",
            onClick = { onSortSelected("Newest") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // PRICE RANGE section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRICE RANGE",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = "$${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}+",
                color = GoldPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Range slider
        RangeSlider(
            value = priceRange,
            onValueChange = onPriceRangeChange,
            valueRange = 0f..5000f,
            colors = SliderDefaults.colors(
                thumbColor = GoldPrimary,
                activeTrackColor = GoldPrimary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )

        // Min and Max labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$0",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Text(
                text = "$5,000+",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // CONDITION section
        Text(
            text = "CONDITION",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Condition chips (scrollable row)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ConditionChip(
                    label = "Any",
                    isSelected = selectedCondition == "Any",
                    onClick = { onConditionSelected("Any") }
                )
            }
            item {
                ConditionChip(
                    label = "New",
                    isSelected = selectedCondition == "New",
                    onClick = { onConditionSelected("New") }
                )
            }
            item {
                ConditionChip(
                    label = "Used - Good",
                    isSelected = selectedCondition == "Used - Good",
                    onClick = { onConditionSelected("Used - Good") }
                )
            }
            item {
                ConditionChip(
                    label = "Fair",
                    isSelected = selectedCondition == "Fair",
                    onClick = { onConditionSelected("Fair") }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // LOCATION section
        Text(
            text = "LOCATION",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Current location row
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF3D3426),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Open location selector */ }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Current Location",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Toronto, ON (Within 10 miles)",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Show Results button
        Button(
            onClick = onShowResults,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary
            )
        ) {
            Text(
                text = "Show $resultsCount Results",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

/**
 * Filter chip (for Sort By)
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) GoldPrimary else Color(0xFF3D3426),
        modifier = Modifier
            .height(44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Condition chip (for Condition section)
 */
@Composable
fun ConditionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) GoldPrimary else Color(0xFF3D3426),
        modifier = Modifier
            .height(40.dp)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, GoldPrimary, RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Search results grid - displays actual search results from ViewModel
 */
@Composable
fun SearchResultsGrid(
    results: List<com.dinachi.passit.viewmodel.HomeListingUi>,
    onListingClick: (String) -> Unit
) {
    if (results.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No results found",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(results.size) { index ->
            val item = results[index]
            SearchResultCard(
                item = item,
                onClick = { onListingClick(item.id) }
            )
        }
    }
}

/**
 * Individual search result card
 */
@Composable
fun SearchResultCard(
    item: com.dinachi.passit.viewmodel.HomeListingUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2416))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )

            // Favorite heart icon
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Info section at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = item.priceText,
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.locationText,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}