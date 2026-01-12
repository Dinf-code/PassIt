package com.dinachi.passit.storage.remote

import android.util.Log
import com.dinachi.passit.datamodel.Category
import com.dinachi.passit.datamodel.Condition
import com.dinachi.passit.datamodel.Listing
import com.dinachi.passit.storage.RepositoryProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * One-time script to seed Firebase with sample data
 * Call seedFirebaseData() once to populate Firestore
 */
object FirebaseSeeder {

    fun seedFirebaseData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId == null) {
                    Log.e("FirebaseSeeder", "No user logged in!")
                    return@launch
                }

                Log.d("FirebaseSeeder", "Starting seed...")

                // Get repos
                val listingRepo = RepositoryProvider.provideListingRepo()

                // Create 25 listings
                val listings = createSampleListings(currentUserId)

                // Upload to Firestore
                listings.forEach { listing ->
                    try {
                        listingRepo.createListing(listing)
                        Log.d("FirebaseSeeder", "Created: ${listing.title}")
                    } catch (e: Exception) {
                        Log.e("FirebaseSeeder", "Failed to create ${listing.title}: ${e.message}")
                    }
                }

                Log.d("FirebaseSeeder", "✅ Seed complete! Created ${listings.size} listings")

            } catch (e: Exception) {
                Log.e("FirebaseSeeder", "Seed failed: ${e.message}", e)
            }
        }
    }

    private fun createSampleListings(sellerId: String): List<Listing> {
        val locations = listOf(
            "Downtown Toronto, ON",
            "North York, ON",
            "Scarborough, ON",
            "Etobicoke, ON",
            "Mississauga, ON"
        )

        return listOf(
            // Electronics
            Listing(
                id = "",
                title = "iPhone 13 Pro Max - 256GB",
                description = "Excellent condition iPhone 13 Pro Max in Sierra Blue. Comes with original box, charger, and case. Battery health at 95%. No scratches or dents.",
                price = 850.0,
                currency = "CAD",
                category = Category.Electronics,
                condition = Condition.LikeNew,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1556656793-08538906a9f8?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Apple"
            ),

            Listing(
                id = "",
                title = "MacBook Air M2 - Space Gray",
                description = "2023 MacBook Air with M2 chip, 8GB RAM, 256GB SSD. Perfect for students and professionals. Only 3 months old, barely used.",
                price = 1100.0,
                currency = "CAD",
                category = Category.Electronics,
                condition = Condition.LikeNew,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Apple"
            ),

            Listing(
                id = "",
                title = "Sony WH-1000XM5 Headphones",
                description = "Premium noise-cancelling headphones. Crystal clear sound, 30-hour battery life. Comes with carrying case and all accessories.",
                price = 320.0,
                currency = "CAD",
                category = Category.Electronics,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Sony"
            ),

            // Furniture
            Listing(
                id = "",
                title = "Mid-Century Modern Sofa",
                description = "Beautiful teal velvet sofa in excellent condition. 3-seater, very comfortable. Selling due to moving. Must pick up.",
                price = 450.0,
                currency = "CAD",
                category = Category.Furniture,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = ""
            ),

            Listing(
                id = "",
                title = "Industrial Dining Table Set",
                description = "Solid wood dining table with 6 chairs. Industrial style with metal legs. Seats 8 people comfortably. Great for hosting!",
                price = 600.0,
                currency = "CAD",
                category = Category.Furniture,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = ""
            ),

            Listing(
                id = "",
                title = "Queen Size Bed Frame - White",
                description = "Modern platform bed frame with storage drawers. White finish, minimal assembly required. Mattress not included.",
                price = 280.0,
                currency = "CAD",
                category = Category.Furniture,
                condition = Condition.LikeNew,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "IKEA"
            ),

            Listing(
                id = "",
                title = "Ergonomic Office Chair",
                description = "Herman Miller Aeron chair, Size B. Perfect condition, all adjustments work smoothly. Selling after WFH setup change.",
                price = 550.0,
                currency = "CAD",
                category = Category.Furniture,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1592078615290-033ee584e267?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Herman Miller"
            ),

            Listing(
                id = "",
                title = "Wooden Bookshelf - 5 Tier",
                description = "Tall bookshelf with 5 shelves. Solid wood construction, holds plenty of books. Some minor wear, very sturdy.",
                price = 120.0,
                currency = "CAD",
                category = Category.Furniture,
                condition = Condition.Fair,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1603380353725-f8a4d39cc41e?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = ""
            ),

            // Clothing & Fashion
            Listing(
                id = "",
                title = "Canada Goose Expedition Parka",
                description = "Authentic Canada Goose parka, Men's Large. Black color, barely worn. Warmest parka in their collection. Paid $1,800 new.",
                price = 850.0,
                currency = "CAD",
                category = Category.Clothing,
                condition = Condition.LikeNew,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1578932750294-f5075e85f44a?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Canada Goose"
            ),

            Listing(
                id = "",
                title = "Nike Air Jordan 1 Retro High",
                description = "Size 10.5, Chicago colorway. Excellent condition, worn 3 times. Comes with original box and extra laces.",
                price = 380.0,
                currency = "CAD",
                category = Category.Clothing,
                condition = Condition.LikeNew,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Nike"
            ),

            Listing(
                id = "",
                title = "Leather Jacket - Brown",
                description = "Genuine leather motorcycle jacket. Size Medium, fits slim. Perfect for fall weather. Minor wear on elbows.",
                price = 220.0,
                currency = "CAD",
                category = Category.Clothing,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1520975954732-35dd22299614?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = ""
            ),

            // Sports
            Listing(
                id = "",
                title = "Peloton Bike+ with Accessories",
                description = "Peloton Bike+ in perfect working condition. Includes cycling shoes (Size 9), weights, and yoga mat. Subscription not included.",
                price = 1800.0,
                currency = "CAD",
                category = Category.Sports,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1517649763962-0c623066013b?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Peloton"
            ),

            Listing(
                id = "",
                title = "Trek Mountain Bike - 27.5\"",
                description = "Trek Marlin 7 mountain bike. Hydraulic disc brakes, 21-speed. Great condition, regularly serviced. Perfect for trails.",
                price = 650.0,
                currency = "CAD",
                category = Category.Sports,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1511994298241-608e28f14fde?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Trek"
            ),

            Listing(
                id = "",
                title = "Adjustable Dumbbell Set - 50lbs",
                description = "Bowflex SelectTech adjustable dumbbells. Range from 5-50lbs each. Space-saving design, perfect for home gym.",
                price = 420.0,
                currency = "CAD",
                category = Category.Sports,
                condition = Condition.LikeNew,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Bowflex"
            ),

            // Home & Garden
            Listing(
                id = "",
                title = "KitchenAid Stand Mixer",
                description = "Classic red KitchenAid stand mixer, 5-quart. Includes whisk, dough hook, and flat beater. Works perfectly!",
                price = 280.0,
                currency = "CAD",
                category = Category.Home,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1570222094114-d054a817e56b?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "KitchenAid"
            ),

            Listing(
                id = "",
                title = "Nespresso Vertuo Coffee Maker",
                description = "Makes perfect espresso and coffee at the push of a button. Chrome finish, includes milk frother. Like new!",
                price = 140.0,
                currency = "CAD",
                category = Category.Home,
                condition = Condition.LikeNew,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1545665225-b23b99e4d45e?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Nespresso"
            ),

            Listing(
                id = "",
                title = "Indoor Plant Bundle - 5 Plants",
                description = "Collection of 5 healthy indoor plants: Monstera, Snake Plant, Pothos, ZZ Plant, and Spider Plant. Includes pots!",
                price = 80.0,
                currency = "CAD",
                category = Category.Home,
                condition = Condition.New,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = ""
            ),

            // Books
            Listing(
                id = "",
                title = "Harry Potter Complete Book Set",
                description = "All 7 Harry Potter books in hardcover. Excellent condition, minimal wear. Perfect for collectors or first-time readers!",
                price = 120.0,
                currency = "CAD",
                category = Category.Books,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = ""
            ),

            // More Electronics
            Listing(
                id = "",
                title = "Samsung 55\" 4K Smart TV",
                description = "Samsung QLED 4K TV, 55 inches. Crystal clear picture, smart features, voice control. Wall mount included.",
                price = 550.0,
                currency = "CAD",
                category = Category.Electronics,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1571506165871-ee72a35bc9d1?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Samsung"
            ),

            Listing(
                id = "",
                title = "Nintendo Switch OLED - White",
                description = "Nintendo Switch OLED model with vibrant screen. Includes dock, joy-cons, and 3 games: Mario Kart, Zelda, Animal Crossing.",
                price = 380.0,
                currency = "CAD",
                category = Category.Electronics,
                condition = Condition.LikeNew,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1622297845775-5ff3fef71d13?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Nintendo"
            ),

            Listing(
                id = "",
                title = "Dyson V11 Cordless Vacuum",
                description = "Powerful cordless vacuum with LCD screen showing battery life. Multiple attachments included. Perfect for apartments!",
                price = 420.0,
                currency = "CAD",
                category = Category.Home,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1558317374-067fb5f30001?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Dyson"
            ),

            Listing(
                id = "",
                title = "Vintage Polaroid Camera",
                description = "Working Polaroid OneStep+ instant camera. Retro style, perfect for parties. Includes 2 packs of film!",
                price = 180.0,
                currency = "CAD",
                category = Category.Electronics,
                condition = Condition.Good,
                location = locations.random(),
                imageUrls = listOf("https://images.unsplash.com/photo-1500051638674-ff996a0ec29e?w=800&h=600&fit=crop"),
                sellerId = sellerId,
                createdTimestamp = System.currentTimeMillis() - (1..48).random() * 3600000L,
                updatedTimestamp = System.currentTimeMillis(),
                isSold = false,
                brand = "Polaroid"
            ),

            Listing(
                id = "",
                title = "Electric Guitar & Amp Bundle",
                description = "Fender Stratocaster electric guitar with 20W practice amp. Great starter set for beginners. Includes cables and strap.",
                price = 450.0,
                currency = "CAD",
                category = Category.Other,
                condition = Condition.Good,
                location = locations.random(),
                   imageUrls = listOf("https://images.unsplash.com/photo-1510915228340-29c85a43dcfe?w=800&h=600&fit=crop"),
            )

        )
    }

}