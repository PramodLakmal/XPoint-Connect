/**
 * Converters.kt
 *
 * Purpose: Room database type converters for handling complex data types Author: XPoint Connect
 * Development Team Date: October 10, 2025
 *
 * Description: This class provides type converters for Room database to handle complex data types
 * that are not natively supported by SQLite. It includes converters for Date objects and other
 * custom types used in the application.
 *
 * Key Features:
 * - Date to Long timestamp conversion for SQLite storage
 * - Long timestamp to Date object conversion for app usage
 * - Null safety for optional date fields
 * - Thread-safe conversion operations
 */
package com.xpoint.connect.data.database.converter

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
