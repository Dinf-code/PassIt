package com.dinachi.passit.userinterface.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.dinachi.passit.datamodel.Listing
import com.dinachi.passit.ui.theme.GoldPrimary
import com.dinachi.passit.viewmodel.ProfileTab
import com.dinachi.passit.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    currentUserId: String,
    onListingClick: (String) -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onBackPress: (() -> Unit)? = null,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val bg = Color(0xFF1A1410)
    val card = Color(0xFF2C2416)
    val card2 = Color(0xFF2C2C2E)

    // Initialize profile on load
    LaunchedEffect(userId) {
        viewModel.loadProfile(userId, currentUserId)
    }

    // Show loading state
    if (uiState.isLoading && uiState.user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GoldPrimary)
        }
        return
    }

    // Show error state
    if (uiState.error != null && uiState.user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.error ?: "Failed to load profile",
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.loadProfile(userId, currentUserId) }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    val user = uiState.user!!

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isOwnProfile) "My Profile" else "Profile",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (onBackPress != null) {
                        IconButton(onClick = onBackPress) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* share */ }) {
                        Icon(Icons.Default.IosShare, contentDescription = "Share", tint = Color.White)
                    }
                    if (uiState.isOwnProfile) {
                        IconButton(onClick = { /* settings */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Avatar
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(84.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF3A3A3C))
                    ) {
                        if (user.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            )
                        }
                    }
                    // Online dot
                    if (user.isOnline) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34C759))
                                .border(2.dp, bg, CircleShape)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Name + verified
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (user.isVerified) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF2F80ED),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Location and member since
            Text(
                text = "${user.location} • Member since ${user.getMemberSinceYear()}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp
            )

            Spacer(Modifier.height(8.dp))

            // Rating pill
            RatingPill(rating = user.rating, reviewsCount = user.reviewsCount)

            Spacer(Modifier.height(14.dp))

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    cardColor = card,
                    value = uiState.itemsSold.toString(),
                    label = "Items Sold",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    cardColor = card,
                    value = user.followersCount.toString(),
                    label = "Followers",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    cardColor = card,
                    value = user.followingCount.toString(),
                    label = "Following",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Buttons
            if (uiState.isOwnProfile) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onEditProfile,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        enabled = !uiState.isUpdating
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Edit Profile", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { /* promote */ },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.18f))
                        )
                    ) {
                        Text("Promote", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            } else {
                // Follow/Message buttons for other users
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.toggleFollow() },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isFollowing) Color(0xFF2C2C2E) else GoldPrimary
                        )
                    ) {
                        Text(
                            text = if (uiState.isFollowing) "Following" else "Follow",
                            color = if (uiState.isFollowing) Color.White else Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { /* open chat */ },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.18f))
                        )
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Message", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Seller reputation card
            SellerReputationCard(
                cardColor = card,
                rating = user.rating,
                reviewsCount = user.reviewsCount,
                onSeeAll = { /* see all reviews */ }
            )

            Spacer(Modifier.height(14.dp))

            // Tabs
            ProfileTabs(
                selected = uiState.selectedTab,
                activeCount = uiState.activeListings.size,
                soldCount = uiState.soldListings.size,
                reviewsCount = user.reviewsCount,
                onSelect = { viewModel.onTabSelected(it) }
            )

            Spacer(Modifier.height(10.dp))

            // Get listings based on selected tab
            val displayListings = when (uiState.selectedTab) {
                ProfileTab.Active -> uiState.activeListings
                ProfileTab.Sold -> uiState.soldListings
                ProfileTab.Reviews -> emptyList() // TODO: Load reviews
            }

            // Grid
            if (uiState.selectedTab == ProfileTab.Reviews) {
                // TODO: Show reviews list
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Reviews coming soon", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayListings) { listing ->
                        ProfileListingCard(
                            listing = listing,
                            cardColor = card2,
                            onClick = { onListingClick(listing.id) }
                        )
                    }
                }
            }
        }

        // Error Snackbar
        if (uiState.error != null && uiState.user != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(uiState.error ?: "An error occurred")
            }
        }
    }
}

@Composable
private fun RatingPill(rating: Float, reviewsCount: Int) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFF2C2416)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${String.format("%.1f", rating)} ($reviewsCount reviews)",
                color = GoldPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatCard(cardColor: Color, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(14.dp),
        color = cardColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun SellerReputationCard(
    cardColor: Color,
    rating: Float,
    reviewsCount: Int,
    onSeeAll: () -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp), color = cardColor) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Seller Reputation", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(
                    "See All",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSeeAll() }
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.width(90.dp)) {
                    Text(
                        text = String.format("%.1f", rating),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row {
                        repeat(5) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("$reviewsCount reviews", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    RatingBarRow(stars = "5", fill = 0.80f)
                    RatingBarRow(stars = "4", fill = 0.55f)
                    RatingBarRow(stars = "3", fill = 0.25f)
                    RatingBarRow(stars = "2", fill = 0.10f)
                    RatingBarRow(stars = "1", fill = 0.05f)
                }
            }
        }
    }
}

@Composable
private fun RatingBarRow(stars: String, fill: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stars, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, modifier = Modifier.width(14.dp))
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .height(6.dp)
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.10f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fill.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary)
            )
        }
    }
}

@Composable
private fun ProfileTabs(
    selected: ProfileTab,
    activeCount: Int,
    soldCount: Int,
    reviewsCount: Int,
    onSelect: (ProfileTab) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        TabItem(
            text = "Active",
            badge = activeCount.toString(),
            selected = selected == ProfileTab.Active
        ) { onSelect(ProfileTab.Active) }

        TabItem(
            text = "Sold",
            badge = soldCount.toString(),
            selected = selected == ProfileTab.Sold
        ) { onSelect(ProfileTab.Sold) }

        TabItem(
            text = "Reviews",
            badge = reviewsCount.toString(),
            selected = selected == ProfileTab.Reviews
        ) { onSelect(ProfileTab.Reviews) }
    }
}

@Composable
private fun TabItem(text: String, badge: String?, selected: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = if (selected) GoldPrimary else Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (badge != null) {
                Spacer(Modifier.width(6.dp))
                Surface(color = Color(0xFF2C2416), shape = RoundedCornerShape(10.dp)) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(if (selected) 54.dp else 0.dp)
                .background(GoldPrimary)
        )
    }
}

@Composable
private fun ProfileListingCard(
    listing: Listing,
    cardColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable { onClick() }
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                AsyncImage(
                    model = listing.imageUrls.firstOrNull(),
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Heart
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Price badge
                Surface(
                    color = GoldPrimary,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                ) {
                    Text(
                        text = "$${listing.price}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    listing.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${listing.category.name} • ${listing.condition.name}",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Extension function for User
fun com.dinachi.passit.datamodel.User.getMemberSinceYear(): String {
    // TODO: Calculate from createdAt timestamp
    return "2021"
}