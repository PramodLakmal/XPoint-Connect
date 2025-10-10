/**
 * XPointDatabase.kt
 *
 * Purpose: Central Room database configuration for the XPoint Connect application Author: XPoint
 * Connect Development Team Date: October 10, 2025
 *
 * Description: This class defines the Room database configuration for the application. It provides
 * access to the application's SQLite database and manages the creation and access to the database
 * instance using the Singleton pattern. Replaces SharedPreferences with structured SQLite storage
 * for better data management and performance.
 *
 * Key Features:
 * - Provides a single database instance throughout the application
 * - Manages UserEntity and EVOwnerEntity data via their respective DAOs
 * - Implements thread-safe lazy initialization with Singleton pattern
 * - Includes type converters for complex data types (Date objects)
 * - Supports database versioning and migration strategies
 * - Optimized for offline-first architecture with local data caching
 */
package com.xpoint.connect.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xpoint.connect.data.database.converter.Converters
import com.xpoint.connect.data.database.dao.EVOwnerDao
import com.xpoint.connect.data.database.dao.UserDao
import com.xpoint.connect.data.database.entity.EVOwnerEntity
import com.xpoint.connect.data.database.entity.UserEntity

@Database(entities = [UserEntity::class, EVOwnerEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class XPointDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun evOwnerDao(): EVOwnerDao

    companion object {
        @Volatile private var INSTANCE: XPointDatabase? = null

        /**
         * Gets the database instance using thread-safe Singleton pattern Creates the database if it
         * doesn't exist, otherwise returns existing instance
         *
         * @param context Application context for database creation
         * @return XPointDatabase instance for database operations
         */
        fun getDatabase(context: Context): XPointDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                XPointDatabase::class.java,
                                                "xpoint_database"
                                        )
                                        .fallbackToDestructiveMigration() // For development -
                                        // recreates database on
                                        // schema changes
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }

        /**
         * Clears the database instance (useful for testing) Should be used carefully in production
         * environments
         */
        fun clearInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
