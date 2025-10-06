/**
 * ChargingStation.kt
 *
 * Purpose: Data models for charging station information and related infrastructure data Author:
 * XPoint Connect Development Team Date: September 27, 2025
 *
 * Description: This file contains data models for charging station management including location
 * data, operating hours, time slots, and charging specifications. It supports the complete charging
 * station ecosystem with detailed information about power capabilities, connector types, pricing,
 * and availability scheduling for EV owners.
 *
 * Key Features:
 * - Comprehensive charging station profile with technical specifications
 * - Location data with geographic coordinates for mapping integration
 * - Operating hours and time slot management for reservations
 * - Charging type support (AC/DC) with power and connector specifications
 * - Pricing information and cost calculation support
 * - Operator information and station management data
 * - Rating and review system integration
 * - Amenities and facility information
 */
package com.xpoint.connect.data.model

import com.google.gson.annotations.SerializedName

data class ChargingStation(
        @SerializedName("id") val id: String = "",
        @SerializedName("name") val name: String = "",
        @SerializedName("location") val location: Location = Location(),
        @SerializedName("type") val type: Int = 0,
        @SerializedName("totalSlots") val totalSlots: Int = 0,
        @SerializedName("availableSlots") val availableSlots: Int = 0,
        @SerializedName("schedule") val schedule: List<TimeSlot> = emptyList(),
        @SerializedName("isActive") val isActive: Boolean = true,
        @SerializedName("operatorId") val operatorId: String = "",
        @SerializedName("createdAt") val createdAt: String = "",
        @SerializedName("updatedAt") val updatedAt: String = "",
        @SerializedName("chargingRate") val chargingRate: Double = 0.0,
        @SerializedName("description") val description: String = "",
        @SerializedName("amenities") val amenities: List<String> = emptyList(),
        @SerializedName("distance") val distance: Double = 0.0
) {
        // Helper properties for backward compatibility
        val costPerKWh: Double
                get() = chargingRate
        val maxPowerKW: Double
                get() = 50.0 // Default value for UI
}

data class Location(
        @SerializedName("latitude") val latitude: Double = 0.0,
        @SerializedName("longitude") val longitude: Double = 0.0,
        @SerializedName("address") val address: String = "",
        @SerializedName("city") val city: String = "",
        @SerializedName("province") val province: String = ""
)

data class OperatingHours(
        @SerializedName("openTime") val openTime: String = "00:00",
        @SerializedName("closeTime") val closeTime: String = "23:59",
        @SerializedName("is24Hours") val is24Hours: Boolean = true
)

data class TimeSlot(
        @SerializedName("startTime") val startTime: String = "",
        @SerializedName("endTime") val endTime: String = "",
        @SerializedName("availableSlots") val availableSlots: Int = 0
)

enum class ChargingStationType {
        @SerializedName("AC") AC,
        @SerializedName("DC") DC,
        @SerializedName("Rapid") Rapid,
        @SerializedName("Ultra") Ultra
}
