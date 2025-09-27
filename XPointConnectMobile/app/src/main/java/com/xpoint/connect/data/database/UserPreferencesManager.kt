package com.xpoint.connect.data.database

import android.content.Context
import com.xpoint.connect.data.database.entity.UserEntity
import com.xpoint.connect.data.model.EVOwner
import kotlinx.coroutines.flow.Flow

class UserPreferencesManager(context: Context) {

    private val database = XPointDatabase.getDatabase(context)
    private val userDao = database.userDao()

    // Initialize with default user if not exists
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

    // Save login data from new API response
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
