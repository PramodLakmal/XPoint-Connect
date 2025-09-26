package com.xpoint.connect.data.model

import com.google.gson.annotations.SerializedName

data class EVOwner(
        @SerializedName("id") val id: String = "",
        @SerializedName("nic") val nic: String = "",
        @SerializedName("firstName") val firstName: String = "",
        @SerializedName("lastName") val lastName: String = "",
        @SerializedName("email") val email: String = "",
        @SerializedName("phoneNumber") val phoneNumber: String = "",
        @SerializedName("licenseNumber") val licenseNumber: String = "",
        @SerializedName("vehicleModel") val vehicleModel: String = "",
        @SerializedName("vehicleYear") val vehicleYear: Int = 0,
        @SerializedName("batteryCapacity") val batteryCapacity: Double = 0.0,
        @SerializedName("isActive") val isActive: Boolean = true,
        @SerializedName("registrationDate") val registrationDate: String = ""
)
