/**
 * EVOwnerEntity.kt
 *
 * Purpose: Room database entity for storing Electric Vehicle owner profile information Author:
 * XPoint Connect Development Team Date: October 10, 2025
 *
 * Description: This Room entity stores comprehensive EV owner profile data locally, replacing
 * SharedPreferences storage. It includes personal information, vehicle details, and contact
 * information for offline access and caching.
 *
 * Key Features:
 * - Primary key based on EV owner ID (NIC)
 * - Complete personal information (name, email, phone, address)
 * - Vehicle specifications (model, year, battery capacity)
 * - License and registration information
 * - Account status and activity tracking
 * - Foreign key relationship with UserEntity
 * - Created and updated timestamp tracking
 */
package com.xpoint.connect.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
        tableName = "ev_owners",
        foreignKeys =
                [
                        ForeignKey(
                                entity = UserEntity::class,
                                parentColumns = ["user_id"],
                                childColumns = ["user_id"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index(value = ["user_id"])]
)
data class EVOwnerEntity(
        @PrimaryKey @ColumnInfo(name = "id") val id: String,
        @ColumnInfo(name = "user_id") val userId: String,
        @ColumnInfo(name = "nic") val nic: String,
        @ColumnInfo(name = "first_name") val firstName: String,
        @ColumnInfo(name = "last_name") val lastName: String,
        @ColumnInfo(name = "email") val email: String,
        @ColumnInfo(name = "phone_number") val phoneNumber: String,
        @ColumnInfo(name = "address") val address: String,
        @ColumnInfo(name = "license_number") val licenseNumber: String? = null,
        @ColumnInfo(name = "vehicle_model") val vehicleModel: String? = null,
        @ColumnInfo(name = "vehicle_year") val vehicleYear: Int = 0,
        @ColumnInfo(name = "battery_capacity") val batteryCapacity: Double = 0.0,
        @ColumnInfo(name = "is_active") val isActive: Boolean = true,
        @ColumnInfo(name = "registration_date") val registrationDate: String? = null,
        @ColumnInfo(name = "created_at") val createdAt: Date = Date(),
        @ColumnInfo(name = "updated_at") val updatedAt: Date = Date()
)
