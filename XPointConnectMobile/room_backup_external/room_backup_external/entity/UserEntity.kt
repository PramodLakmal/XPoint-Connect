/**
 * Purpose: Represents a user entity in the local database for the XPoint Connect application.
 *
 * Description: This entity class maps the user information to the database table 'users'.
 * It holds persistent data about users, including their authentication details,
 * personal information, and role within the system.
 *
 * Key Features:
 * - Database entity mapping with Room annotations
 * - Storage of user authentication credentials
 * - Persistence of user profile information
 * - Support for user role management
 * - Integration with Android's Room persistence library
 */

package com.xpoint.connect.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserEntity(
        @PrimaryKey val id: Int = 1, // Single user app, so we use fixed ID
        val authToken: String? = null,
        val refreshToken: String? = null,
        val userType: String? = null,
        val userId: String? = null,
        val userNIC: String? = null,
        val userName: String? = null,
        val userEmail: String? = null,
        val phoneNumber: String? = null,
        val licenseNumber: String? = null,
        val vehicleModel: String? = null,
        val batteryCapacity: Double = 0.0,
        val isLoggedIn: Boolean = false,
        val isFirstTime: Boolean = true,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
)
