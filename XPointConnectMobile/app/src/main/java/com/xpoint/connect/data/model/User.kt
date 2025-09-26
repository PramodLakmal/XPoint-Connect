package com.xpoint.connect.data.model

import com.google.gson.annotations.SerializedName

data class User(
        @SerializedName("id") val id: String = "",
        @SerializedName("firstName") val firstName: String = "",
        @SerializedName("lastName") val lastName: String = "",
        @SerializedName("email") val email: String = "",
        @SerializedName("phoneNumber") val phoneNumber: String = "",
        @SerializedName("userType") val userType: UserType = UserType.EVOwner,
        @SerializedName("isActive") val isActive: Boolean = true,
        @SerializedName("createdDate") val createdDate: String = ""
)

enum class UserType {
    @SerializedName("BackOffice") BackOffice,
    @SerializedName("StationOperator") StationOperator,
    @SerializedName("EVOwner") EVOwner
}
