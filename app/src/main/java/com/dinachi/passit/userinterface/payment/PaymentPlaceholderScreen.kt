package com.dinachi.passit.userinterface.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinachi.passit.ui.theme.GoldPrimary
import com.dinachi.passit.ui.theme.PassItTheme
import com.dinachi.passit.userinterface.profile.UserProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentPlaceholderScreen(
    listingId: String,
    onBackPress: () -> Unit,
    onReturnToChat: (() -> Unit)? = null
) {
    val bg = Color(0xFF1A1410)
    val card = Color(0xFF2C2416)

    // Screen state
    var safetyExpanded by remember { mutableStateOf(false) }
    var notifyMe by remember { mutableStateOf(false) }

    // Sample data (replace later)
    val itemTitle = "Mid-Century Desk Lamp"
    val itemSubtitle = "Vintage Home Decor"
    val amount = "$45.00"

    // Optional: swap with your drawable resources
    val illustrationRes: Int? = null // e.g. R.drawable.payment_illustration
    val itemImageRes: Int? = null    // e.g. R.drawable.lamp

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transaction Status",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onReturnToChat?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(
                        text = "Return to Chat",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))

            // Big circular illustration area
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1C6A70).copy(alpha = 0.90f),
                                Color(0xFF1C6A70).copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner glow ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GoldPrimary.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                if (illustrationRes != null) {
                    Image(
                        painter = painterResource(illustrationRes),
                        contentDescription = null,
                        modifier = Modifier.size(140.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Placeholder icon (swap later)
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "PassIt Payments Coming Soon",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "In-app payments are currently under\ndevelopment. For now, please arrange payment\ndirectly with the seller.",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(14.dp))

            // Pending transaction card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = card
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = GoldPrimary.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "PENDING TRANSACTION",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = itemTitle,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = itemSubtitle,
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = amount,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF3A3123)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (itemImageRes != null) {
                            Image(
                                painter = painterResource(itemImageRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Safety Tips header
            Text(
                text = "Safety Tips",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Safety accordion row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = card
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { safetyExpanded = !safetyExpanded }
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF3A3123)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Safe Trading Guidelines",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Icon(
                            imageVector = if (safetyExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    if (safetyExpanded) {
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SafetyLine("Meet in a public place.")
                            SafetyLine("Inspect item before paying.")
                            SafetyLine("Keep chats inside PassIt.")
                            SafetyLine("Never share banking info.")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Notify me row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = card
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3A3123)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Notify me",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "When PassIt Payments launches",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = notifyMe,
                        onCheckedChange = { notifyMe = it }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SafetyLine(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .padding(top = 6.dp)
                .clip(CircleShape)
                .background(GoldPrimary)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentPlaceholderPreview() {
    PassItTheme {
        PaymentPlaceholderScreen(
            listingId = "listing123",
            onBackPress = {},
            onReturnToChat = {}
        )
    }
}