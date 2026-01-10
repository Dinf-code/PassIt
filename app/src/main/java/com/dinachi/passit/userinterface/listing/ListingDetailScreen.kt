package com.dinachi.passit.userinterface.listing


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.dinachi.passit.datamodel.Listing
import com.dinachi.passit.datamodel.User
import com.dinachi.passit.viewmodel.ListingViewModel
import com.dinachi.passit.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    onBackPress: () -> Unit,
    onChatWithSeller: (String) -> Unit,
    onSellerProfileClick: (String) -> Unit,
    onBuyNow: () -> Unit,
    listingViewModel: ListingViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val listingState by listingViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()

    var currentImageIndex by remember { mutableStateOf(0) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isSafetyTipsExpanded by remember { mutableStateOf(false) }
    var isFavorited by remember { mutableStateOf(false) }

    // Load listing once
    LaunchedEffect(listingId) {
        listingViewModel.loadListing(listingId)
    }

    // When listing loads, observe seller
    LaunchedEffect(listingState.listing?.sellerId) {
        val sellerId = listingState.listing?.sellerId
        if (!sellerId.isNullOrBlank()) {
            profileViewModel.observeUser(sellerId)
        }
    }

    Scaffold(
        containerColor = Color(0xFF1C1C1E),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { /* More */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2C2C2E))
            )
        }
    ) { paddingValues ->

        // Loading / error states (NO sample content)
        when {
            listingState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Scaffold
            }

            listingState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = listingState.error ?: "Failed to load",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                return@Scaffold
            }

            listingState.listing == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Listing not found.",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                return@Scaffold
            }
        }

        val listing: Listing = listingState.listing!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ImageGallerySection(
                images = listing.imageUrls,
                currentIndex = currentImageIndex.coerceIn(0, (listing.imageUrls.size - 1).coerceAtLeast(0)),
                onImageChange = { currentImageIndex = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TitlePriceSection(
                title = listing.title,
                price = listing.price,
                currency = listing.currency,
                timeAgo = listing.createdTimestamp.toTimeAgo(),
                location = listing.location
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConditionBrandSection(
                condition = listing.condition.name.replace("LikeNew", "Like New"),
                brand = listing.brand.ifBlank { "—" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionButtonsSection(
                isFavorited = isFavorited,
                onFavoriteClick = { isFavorited = !isFavorited },
                onChatWithSeller = { onChatWithSeller(listing.sellerId) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            DescriptionSection(
                description = listing.description,
                isExpanded = isDescriptionExpanded,
                onToggleExpand = { isDescriptionExpanded = !isDescriptionExpanded }
            )

            Spacer(modifier = Modifier.height(24.dp))

            SellerSectionUser(
                seller = profileState.user,
                onViewProfile = { onSellerProfileClick(listing.sellerId) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            LocationMapSection(location = listing.location)

            Spacer(modifier = Modifier.height(24.dp))

            SafetyTipsSection(
                isExpanded = isSafetyTipsExpanded,
                onToggleExpand = { isSafetyTipsExpanded = !isSafetyTipsExpanded }
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { /* Report */ },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REPORT THIS LISTING",
                    color = Color(0xFFFF3B30),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SellerSectionUser(
    seller: User?,
    onViewProfile: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Seller",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C2C2E))
                .clickable { onViewProfile() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF3A3A3C)),
                        contentAlignment = Alignment.Center
                    ) {
                        val photo = seller?.photoUrl.orEmpty()
                        if (photo.isNotEmpty()) {
                            AsyncImage(
                                model = photo,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = seller?.name ?: "Loading...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = (seller?.rating ?: 0f).toString(),
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

/** Simple time-ago without extra libs */
private fun Long.toTimeAgo(): String {
    if (this <= 0L) return ""
    val now = System.currentTimeMillis()
    val diff = (now - this).coerceAtLeast(0L)

    val min = diff / 60_000L
    val hr = diff / 3_600_000L
    val day = diff / 86_400_000L

    return when {
        min < 1 -> "Just now"
        min < 60 -> "${min}m ago"
        hr < 24 -> "${hr}h ago"
        else -> "${day}d ago"
    }
}

// ==================== HELPER FUNCTIONS ====================


@Composable
private fun ImageGallerySection(
    images: List<String>,
    currentIndex: Int,
    onImageChange: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color(0xFF2C2C2E))
    ) {
        if (images.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
            }
        } else {
            AsyncImage(
                model = images.getOrNull(currentIndex) ?: images[0],
                contentDescription = "Listing image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    images.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentIndex) Color.White
                                    else Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = { /* Handle favorite */ },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun TitlePriceSection(
    title: String,
    price: Double,
    currency: String,
    timeAgo: String,
    location: String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$${"%.2f".format(price)} $currency",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = location,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "• $timeAgo",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ConditionBrandSection(
    condition: String,
    brand: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoChip(label = "Condition", value = condition, modifier = Modifier.weight(1f))
        InfoChip(label = "Brand", value = brand, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun InfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2C2C2E))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    onChatWithSeller: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onFavoriteClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorited) Color.Red else Color.White
            )
        }

        Button(
            onClick = onChatWithSeller,
            modifier = Modifier.weight(3f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Chat, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chat with Seller", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Description",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = if (isExpanded) Int.MAX_VALUE else 3
        )

        if (description.length > 100) {
            TextButton(onClick = onToggleExpand) {
                Text(
                    text = if (isExpanded) "Show less" else "Show more",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LocationMapSection(location: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Location",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C2C2E)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = location,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SafetyTipsSection(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Safety Tips",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Icon(
                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                "Meet in a public place",
                "Inspect items before payment",
                "Never share financial information",
                "Report suspicious behavior"
            )

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = tip,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}