/**
 * EVOwner.kt
 *
 * Purpose: Data model representing Electric Vehicle owners with comprehensive profile information
 * Author: XPoint Connect Development Team Date: September 27, 2025
 *
 * Description: This data class defines the complete profile structure for Electric Vehicle owners
 * within the XPoint Connect platform. It includes personal information, contact details, vehicle
 * specifications, and account management data. The model supports the full lifecycle of EV owner
 * registration, profile management, and charging station interactions.
 *
 * Key Features:
 * - Comprehensive EV owner profile with personal and vehicle data
 * - National Identity Card (NIC) based identification system
 * - Vehicle specifications including model, year, and battery capacity
 * - License and registration information management
 * - Account status tracking and registration date auditing
 * - JSON serialization for seamless API communication
 */
package com.xpoint.connect.data.model

import com.google.gson.annotations.SerializedName

data class EVOwner(
        @SerializedName("id") val id: String = "",
        @SerializedName("nic") val nic: String = "",
        @SerializedName("firstName") val firstName: String = "",
        @SerializedName("lastName") val lastName: String = "",
        @SerializedName("email") val email: String = "",
        @SerializedName("phoneNumber") val phoneNumber: String = "",
        @SerializedName("addresss") val address: String = "",
        @SerializedName("licenseNumber") val licenseNumber: String = "",
        @SerializedName("vehicleModel") val vehicleModel: String = "",
        @SerializedName("vehicleYear") val vehicleYear: Int = 0,
        @SerializedName("batteryCapacity") val batteryCapacity: Double = 0.0,
        @SerializedName("isActive") val isActive: Boolean = true,
        @SerializedName("registrationDate") val registrationDate: String = ""
)
