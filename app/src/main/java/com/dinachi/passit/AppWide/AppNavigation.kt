package com.dinachi.passit.AppWide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.dinachi.passit.userinterface.auth.OnboardingScreen
import com.dinachi.passit.userinterface.auth.LoginSignUpScreen
import com.dinachi.passit.userinterface.chat.ChatScreen
import com.dinachi.passit.userinterface.chat.ChatListScreen  // NEW
import com.dinachi.passit.userinterface.home.HomeFeedScreen
import com.dinachi.passit.userinterface.home.SearchFilterScreen
import com.dinachi.passit.userinterface.listing.CreateListingScreen
import com.dinachi.passit.userinterface.listing.ListingDetailScreen
import com.dinachi.passit.userinterface.listing.MyListingsScreen  // NEW
import com.dinachi.passit.userinterface.payment.PaymentPlaceholderScreen
import com.dinachi.passit.userinterface.profile.UserProfileScreen
import androidx.compose.foundation.layout.fillMaxSize

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Welcome : Screen("welcome")

    // Main Bottom Nav Screens
    object Home : Screen("home")
    object MyListings : Screen("my_listings")  // CHANGED: was Search
    object CreateListing : Screen("create_listing")
    object Messages : Screen("messages")
    object Profile : Screen("profile")

    // Detail Screens
    object ListingDetail : Screen("listing_detail/{listingId}") {
        fun createRoute(listingId: String) = "listing_detail/$listingId"
    }

    object Chat : Screen("chat/{chatRoomId}/{listingId}/{otherUserId}") {
        fun createRoute(chatRoomId: String, listingId: String, otherUserId: String) =
            "chat/$chatRoomId/$listingId/$otherUserId"
    }

    object SellerProfile : Screen("seller_profile/{sellerId}") {
        fun createRoute(sellerId: String) = "seller_profile/$sellerId"
    }

    object Payment : Screen("payment/{listingId}") {
        fun createRoute(listingId: String) = "payment/$listingId"
    }

    // Keep Search as separate screen (accessible from Home search bar)
    object Search : Screen("search")
}

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home")
    object MyListings : BottomNavItem(Screen.MyListings.route, Icons.Default.ShoppingBag, "My Listings")  // CHANGED
    object Messages : BottomNavItem(Screen.Messages.route, Icons.Default.Email, "Messages")
    object Profile : BottomNavItem(Screen.Profile.route, Icons.Default.Person, "Profile")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    isUserLoggedIn: Boolean = false,
    currentUserId: String = ""
) {
    val startDestination = if (isUserLoggedIn) {
        Screen.Home.route
    } else {
        Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ============ AUTH FLOW ============
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            LoginSignUpScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        // ============ MAIN APP (with Bottom Nav) ============
        composable(Screen.Home.route) {
            HomeFeedScreen(
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        // NEW: My Listings replaces Search in bottom nav
        composable(Screen.MyListings.route) {
            MyListingsScreen(
                currentUserId = currentUserId,
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onCreateListing = {
                    navController.navigate(Screen.CreateListing.route)
                }
            )
        }

        // Search still accessible from Home screen search bar
        composable(Screen.Search.route) {
            SearchFilterScreen(
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateListing.route) {
            CreateListingScreen(
                onListingCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }

        // UPDATED: Messages now shows ChatListScreen
        composable(Screen.Messages.route) {
            ChatListScreen(
                currentUserId = currentUserId,
                onChatClick = { chatRoomId, listingId, otherUserId ->
                    navController.navigate(
                        Screen.Chat.createRoute(chatRoomId, listingId, otherUserId)
                    )
                }
            )
        }

        composable(Screen.Profile.route) {
            UserProfileScreen(
                userId = currentUserId,
                currentUserId = currentUserId,
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onEditProfile = {
                    // TODO: Navigate to edit profile screen
                },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackPress = null
            )
        }

        // ============ DETAIL SCREENS ============
        composable(
            route = Screen.ListingDetail.route,
            arguments = listOf(
                navArgument("listingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            ListingDetailScreen(
                listingId = listingId,
                onBackPress = {
                    navController.popBackStack()
                },
                onChatWithSeller = { sellerId ->
                    val chatRoomId = "chat_${listingId}_${currentUserId}_${sellerId}"
                    navController.navigate(
                        Screen.Chat.createRoute(chatRoomId, listingId, sellerId)
                    )
                },
                onSellerProfileClick = { sellerId ->
                    navController.navigate(Screen.SellerProfile.createRoute(sellerId))
                },
                onBuyNow = {
                    navController.navigate(Screen.Payment.createRoute(listingId))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("chatRoomId") { type = NavType.StringType },
                navArgument("listingId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""

            ChatScreen(
                chatRoomId = chatRoomId,
                listingId = listingId,
                otherUserId = otherUserId,
                currentUserId = currentUserId,
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.SellerProfile.route,
            arguments = listOf(
                navArgument("sellerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            UserProfileScreen(
                userId = sellerId,
                currentUserId = currentUserId,
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument("listingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            PaymentPlaceholderScreen(
                listingId = listingId,
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    currentUserId: String = "",
    content: @Composable (NavHostController) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.MyListings.route,  // CHANGED
        Screen.Messages.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                NavigationBar {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.MyListings,  // CHANGED
                        BottomNavItem.Messages,
                        BottomNavItem.Profile
                    )

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute in bottomNavRoutes) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.CreateListing.route)
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Listing")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content(navController)
        }
    }
}