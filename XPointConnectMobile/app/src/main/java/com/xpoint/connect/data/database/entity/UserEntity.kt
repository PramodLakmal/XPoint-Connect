/**
 * UserEntity.kt
 *
 * Purpose: Room database entity for storing user authentication and profile data Author: XPoint
 * Connect Development Team Date: October 10, 2025
 *
 * Description: This Room entity replaces SharedPreferences for storing user data locally. It
 * includes authentication tokens, user identification, and basic profile information. The entity
 * supports all user types including EV Owners and provides structured data storage.
 *
 * Key Features:
 * - Primary key based on user ID for unique identification
 * - Authentication token storage for API access
 * - User type and email information
 * - Login preferences (remember me, auto login)
 * - National Identity Card (NIC) support for Sri Lankan users
 * - Vehicle information for EV owners
 * - Created and updated timestamp tracking
 */
package com.xpoint.connect.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
        @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
        @ColumnInfo(name = "auth_token") val authToken: String? = null,
        @ColumnInfo(name = "user_type") val userType: String? = null,
        @ColumnInfo(name = "user_email") val userEmail: String? = null,
        @ColumnInfo(name = "user_name") val userName: String? = null,
        @ColumnInfo(name = "user_nic") val userNIC: String? = null,
        @ColumnInfo(name = "remember_me") val rememberMe: Boolean = false,
        @ColumnInfo(name = "auto_login") val autoLogin: Boolean = false,
        @ColumnInfo(name = "created_at") val createdAt: Date = Date(),
        @ColumnInfo(name = "updated_at") val updatedAt: Date = Date()
)
