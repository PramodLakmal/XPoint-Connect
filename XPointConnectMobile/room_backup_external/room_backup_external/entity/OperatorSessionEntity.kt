/**
 * OperatorSessionEntity.kt
 *
 * Purpose: Entity for storing operator session data in SQLite database for persistent login
 * Author: XPoint Connect Development Team
 * Date: December 2024
 *
 * Description: This entity stores encrypted operator login credentials and session information
 * to enable automatic login functionality. Supports secure credential storage using encryption
 * and session validation through timestamps.
 *
 * Key Features:
 * - Encrypted username and password storage
 * - Session timestamp tracking for validity
 * - Remember me preference storage
 * - Auto-login status management
 */
package com.xpoint.connect.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operator_sessions")
data class OperatorSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "session_id")
    val sessionId: String = "operator_session",

    @ColumnInfo(name = "encrypted_username")
    val encryptedUsername: String = "",

    @ColumnInfo(name = "encrypted_password")
    val encryptedPassword: String = "",

    @ColumnInfo(name = "session_timestamp")
    val sessionTimestamp: Long = 0L,

    @ColumnInfo(name = "remember_me")
    val rememberMe: Boolean = false,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false,

    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "username")
    val username: String = "",

    @ColumnInfo(name = "auth_token")
    val authToken: String = "",

    @ColumnInfo(name = "session_expiry")
    val sessionExpiry: Long = 0L // 30 days from login
)