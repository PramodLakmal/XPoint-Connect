package com.xpoint.connect.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.xpoint.connect.data.database.dao.UserDao
import com.xpoint.connect.data.database.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class XPointDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: XPointDatabase? = null

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
                                        // removes this in
                                        // production
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }
    }
}
