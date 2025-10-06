/**
 * UserPreferencesManager.kt
 *
 * Purpose: Manages user preferences and session data using SQLite database storage Author: XPoint
 * Connect Development Team Date: September 27, 2025
 *
 * Description: This manager class provides a high-level interface for user data persistence using
 * Room database. It replaces SharedPreferences with more robust SQLite storage for better
 * performance and data integrity. Handles authentication tokens, user profile data, and application
 * preferences with coroutine support.
 *
 * Key Features:
 * - SQLite-based user data storage with Room ORM
 * - Authentication token management for API requests
 * - User profile data persistence and retrieval
 * - Session state management (login/logout)
 * - Reactive data observation through Flow
 * - Backward compatibility with existing preference methods
 */
package com.xpoint.connect.data.database

import android.content.Context
import com.xpoint.connect.data.database.entity.UserEntity
import com.xpoint.connect.data.model.EVOwner
import kotlinx.coroutines.flow.Flow

class UserPreferencesManager(context: Context) {

    private val database = XPointDatabase.getDatabase(context)
    private val userDao = database.userDao()

    /**
     * Initializes the user preferences database with default values Creates a default user entity
     * if no user data exists in the database
     */
    suspend fun initialize() {
        if (userDao.userExists() == 0) {
            userDao.insertOrUpdateUser(UserEntity())
        }
    }

    // Auth Token Methods
    suspend fun saveAuthToken(token: String) {
        userDao.updateAuthToken(token)
    }

    suspend fun getAuthToken(): String? {
        return userDao.getUser()?.authToken
    }

    // Refresh Token Methods
    suspend fun saveRefreshToken(token: String) {
        userDao.updateRefreshToken(token)
    }

    suspend fun getRefreshToken(): String? {
        return userDao.getUser()?.refreshToken
    }

    // User Type Methods
    suspend fun saveUserType(userType: String) {
        userDao.updateUserType(userType)
    }

    suspend fun getUserType(): String? {
        return userDao.getUser()?.userType
    }

    // User Data Methods
    suspend fun saveUserData(evOwner: EVOwner) {
        userDao.updateUserData(
                nic = evOwner.nic,
                name = "${evOwner.firstName} ${evOwner.lastName}",
                email = evOwner.email,
                phone = evOwner.phoneNumber,
                license = evOwner.licenseNumber,
                vehicle = evOwner.vehicleModel,
                battery = evOwner.batteryCapacity,
                isLoggedIn = true
        )
    }

    /**
     * Saves essential login data from the new simplified API response Stores authentication token
     * and basic user information from login endpoint
     * @param nic User's National Identity Card number
     * @param fullName User's complete name from API response
     * @param token JWT authentication token for API requests
     */
    suspend fun saveLoginData(nic: String, fullName: String, token: String) {
        userDao.updateAuthToken(token)
        userDao.updateUserData(
                nic = nic,
                name = fullName,
                email = "", // Will be filled later when we get full profile
                phone = "",
                license = "",
                vehicle = "",
                battery = 0.0,
                isLoggedIn = true
        )
    }

    suspend fun getUserNIC(): String? {
        return userDao.getUser()?.userNIC
    }

    suspend fun getUserName(): String? {
        return userDao.getUser()?.userName
    }

    suspend fun getUserEmail(): String? {
        return userDao.getUser()?.userEmail
    }

    suspend fun getPhoneNumber(): String? {
        return userDao.getUser()?.phoneNumber
    }

    suspend fun getLicenseNumber(): String? {
        return userDao.getUser()?.licenseNumber
    }

    suspend fun getVehicleModel(): String? {
        return userDao.getUser()?.vehicleModel
    }

    suspend fun getBatteryCapacity(): Double {
        return userDao.getUser()?.batteryCapacity ?: 0.0
    }

    // Login Status Methods
    suspend fun isLoggedIn(): Boolean {
        return userDao.getUser()?.isLoggedIn ?: false
    }

    // First Time Methods
    suspend fun setFirstTime(isFirstTime: Boolean) {
        userDao.updateFirstTimeStatus(isFirstTime)
    }

    suspend fun isFirstTime(): Boolean {
        return userDao.getUser()?.isFirstTime ?: true
    }

    // Clear Data Methods
    suspend fun clearUserData() {
        userDao.clearAllData()
        initialize() // Re-initialize with default user
    }

    suspend fun logout() {
        userDao.logout()
    }

    // Flow Methods for Reactive Programming
    fun getUserFlow(): Flow<UserEntity?> {
        return userDao.getUserFlow()
    }

    // Get complete user entity
    suspend fun getUser(): UserEntity? {
        return userDao.getUser()
    }
}
