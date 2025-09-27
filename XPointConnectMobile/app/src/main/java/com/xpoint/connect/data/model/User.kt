/**
 * User.kt
 *
 * Purpose: Data model representing system users with role-based access control Author: XPoint
 * Connect Development Team Date: September 27, 2025
 *
 * Description: This data class defines the structure for user entities within the XPoint Connect
 * system. It supports multiple user types including BackOffice administrators, Station Operators,
 * and EV Owners, each with different access permissions and functionality. The model includes user
 * identification, contact information, and account status management.
 *
 * Key Features:
 * - Multi-role user system with UserType enumeration
 * - JSON serialization for API communication
 * - User account status tracking (active/inactive)
 * - Creation date tracking for audit purposes
 * - Contact information management
 */
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
