package com.dinachi.passit.storage.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dinachi.passit.storage.local.dao.*
import com.dinachi.passit.storage.local.*

@Database(
    entities = [
        ListingEntity::class,
        UserEntity::class,
        ChatMessageEntity::class,
        ExchangeRateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PassItDatabase : RoomDatabase() {

    abstract fun listingDao(): ListingDao
    abstract fun userDao(): UserDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {
        @Volatile
        private var INSTANCE: PassItDatabase? = null

        fun getDatabase(context: Context): PassItDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PassItDatabase::class.java,
                    "passit_database"
                )
                    .fallbackToDestructiveMigration()  // For development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}