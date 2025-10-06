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
        @SerializedName("description") val description: String = "",
        @SerializedName("location") val location: Location = Location(),
        @SerializedName("chargingType")
        val chargingType: ChargingStationType = ChargingStationType.AC,
        @SerializedName("connectorTypes") val connectorTypes: List<String> = emptyList(),
        @SerializedName("maxPowerKW") val maxPowerKW: Double = 0.0,
        @SerializedName("costPerKWh") val costPerKWh: Double = 0.0,
        @SerializedName("operatingHours") val operatingHours: OperatingHours = OperatingHours(),
        @SerializedName("availableTimeSlots") val availableTimeSlots: List<TimeSlot> = emptyList(),
        @SerializedName("amenities") val amenities: List<String> = emptyList(),
        @SerializedName("operatorId") val operatorId: String = "",
        @SerializedName("operatorName") val operatorName: String = "",
        @SerializedName("isActive") val isActive: Boolean = true,
        @SerializedName("createdDate") val createdDate: String = "",
        @SerializedName("imageUrl") val imageUrl: String? = null,
        @SerializedName("rating") val rating: Double = 0.0,
        @SerializedName("totalReviews") val totalReviews: Int = 0
)

data class Location(
        @SerializedName("address") val address: String = "",
        @SerializedName("city") val city: String = "",
        @SerializedName("latitude") val latitude: Double = 0.0,
        @SerializedName("longitude") val longitude: Double = 0.0
)

data class OperatingHours(
        @SerializedName("openTime") val openTime: String = "00:00",
        @SerializedName("closeTime") val closeTime: String = "23:59",
        @SerializedName("is24Hours") val is24Hours: Boolean = true
)

data class TimeSlot(
        @SerializedName("startTime") val startTime: String = "",
        @SerializedName("endTime") val endTime: String = "",
        @SerializedName("isAvailable") val isAvailable: Boolean = true,
        @SerializedName("date") val date: String = ""
)

enum class ChargingStationType {
        @SerializedName("AC") AC,
        @SerializedName("DC") DC,
        @SerializedName("Rapid") Rapid,
        @SerializedName("Ultra") Ultra
}
