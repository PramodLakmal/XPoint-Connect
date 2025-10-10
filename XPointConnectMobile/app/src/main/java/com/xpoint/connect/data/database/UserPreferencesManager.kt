/**
 * UserPreferencesManager.kt
 *
 * Purpose: SQLite-based user data management using Room database Author: XPoint Connect Development
 * Team Date: October 10, 2025
 *
 * Description: This class provides user data management functionality using Room database with
 * SQLite backend. It replaces the previous SharedPreferences implementation with structured
 * database storage for better performance, data integrity, and offline support.
 *
 * Key Features:
 * - SQLite database storage via Room framework
 * - Authentication token and user session management
 * - EV Owner profile storage and retrieval
 * - Vehicle information and preferences management
 * - Async operations with Coroutines support
 * - Reactive data access with Flow streams
 * - Data migration from SharedPreferences to SQLite
 */
package com.xpoint.connect.data.database

import android.content.Context
import android.content.SharedPreferences
import com.xpoint.connect.data.database.entity.EVOwnerEntity
import com.xpoint.connect.data.database.entity.UserEntity
import com.xpoint.connect.data.model.EVOwner
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class UserPreferencesManager(private val context: Context) {

    private val database: XPointDatabase = XPointDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val evOwnerDao = database.evOwnerDao()

    // Keep SharedPreferences for migration purposes
    private val prefs: SharedPreferences =
            context.getSharedPreferences("xpoint_prefs", Context.MODE_PRIVATE)

    suspend fun initialize() {
        // Migrate data from SharedPreferences to SQLite if needed
        migrateFromSharedPreferences()
    }

    /**
     * Migrates existing data from SharedPreferences to SQLite database This ensures a smooth
     * transition for existing users
     */
    private suspend fun migrateFromSharedPreferences() {
        try {
            // Check if migration is needed (if user has auth token in SharedPreferences but not in
            // database)
            val sharedPrefToken = prefs.getString("auth_token", null)
            val hasLoggedInUser = userDao.hasLoggedInUser()

            if (sharedPrefToken != null && !hasLoggedInUser) {
                // Migrate user data
                val userId = prefs.getString("user_id", null) ?: UUID.randomUUID().toString()
                val userEntity =
                        UserEntity(
                                userId = userId,
                                authToken = sharedPrefToken,
                                userType = prefs.getString("user_type", null),
                                userEmail = prefs.getString("user_email", null),
                                userName = prefs.getString("user_name", null),
                                userNIC = prefs.getString("user_nic", null),
                                rememberMe = prefs.getBoolean("remember_me", false),
                                autoLogin = prefs.getBoolean("auto_login", false)
                        )
                userDao.insertUser(userEntity)

                // Migrate EV Owner profile if exists
                val evOwnerId = prefs.getString("ev_owner_id", null)
                if (evOwnerId != null) {
                    val evOwnerEntity =
                            EVOwnerEntity(
                                    id = evOwnerId,
                                    userId = userId,
                                    nic = prefs.getString("user_nic", "") ?: "",
                                    firstName = prefs.getString("ev_owner_name", "") ?: "",
                                    lastName = "", // Not stored in SharedPreferences
                                    email = prefs.getString("ev_owner_email", "") ?: "",
                                    phoneNumber = prefs.getString("ev_owner_phone", "") ?: "",
                                    address = prefs.getString("ev_owner_address", "") ?: "",
                                    licenseNumber = prefs.getString("license_number", null),
                                    vehicleModel = prefs.getString("vehicle_model", null),
                                    vehicleYear = prefs.getInt("vehicle_year", 0),
                                    batteryCapacity =
                                            prefs.getString("battery_capacity", "0")
                                                    ?.toDoubleOrNull()
                                                    ?: 0.0
                            )
                    evOwnerDao.insertEVOwner(evOwnerEntity)
                }

                // Clear SharedPreferences after successful migration
                prefs.edit().clear().apply()
            }
        } catch (e: Exception) {
            // Log migration error but don't crash the app
            e.printStackTrace()
        }
    }

    suspend fun saveAuthToken(token: String) {
        val user = getLoggedInUser()
        if (user != null) {
            userDao.updateAuthToken(user.userId, token)
        } else {
            // Create new user with token
            val userId = UUID.randomUUID().toString()
            val userEntity = UserEntity(userId = userId, authToken = token)
            userDao.insertUser(userEntity)
        }
    }

    suspend fun getAuthToken(): String? {
        return userDao.getLoggedInUser()?.authToken
    }

    suspend fun saveUserId(userId: String) {
        val user = getLoggedInUser()
        if (user != null) {
            val updatedUser = user.copy(userId = userId, updatedAt = Date())
            userDao.updateUser(updatedUser)
        } else {
            val userEntity = UserEntity(userId = userId)
            userDao.insertUser(userEntity)
        }
    }

    suspend fun getUserId(): String? {
        return userDao.getLoggedInUser()?.userId
    }

    suspend fun saveUserType(userType: String) {
        val user = getLoggedInUser()
        if (user != null) {
            val updatedUser = user.copy(userType = userType, updatedAt = Date())
            userDao.updateUser(updatedUser)
        }
    }

    suspend fun getUserType(): String? {
        return userDao.getLoggedInUser()?.userType
    }

    suspend fun saveUserEmail(email: String) {
        val user = getLoggedInUser()
        if (user != null) {
            val updatedUser = user.copy(userEmail = email, updatedAt = Date())
            userDao.updateUser(updatedUser)
        }
    }

    suspend fun getUserEmail(): String? {
        return userDao.getLoggedInUser()?.userEmail
    }

    suspend fun saveUserName(userName: String) {
        val user = getLoggedInUser()
        if (user != null) {
            val updatedUser = user.copy(userName = userName, updatedAt = Date())
            userDao.updateUser(updatedUser)
        }
    }

    suspend fun getUserName(): String? {
        return userDao.getLoggedInUser()?.userName
    }

    private suspend fun getLoggedInUser(): UserEntity? {
        return userDao.getLoggedInUser()
    }

    suspend fun saveEVOwnerProfile(profile: EVOwner) {
        val user = getLoggedInUser()
        if (user != null) {
            val evOwnerEntity =
                    EVOwnerEntity(
                            id = profile.id,
                            userId = user.userId,
                            nic = profile.nic,
                            firstName = profile.firstName,
                            lastName = profile.lastName,
                            email = profile.email,
                            phoneNumber = profile.phoneNumber,
                            address = profile.address,
                            licenseNumber = profile.licenseNumber,
                            vehicleModel = profile.vehicleModel,
                            vehicleYear = profile.vehicleYear,
                            batteryCapacity = profile.batteryCapacity,
                            isActive = profile.isActive,
                            registrationDate = profile.registrationDate,
                            updatedAt = Date()
                    )
            evOwnerDao.insertEVOwner(evOwnerEntity)
        }
    }

    suspend fun getEVOwnerProfile(): EVOwner? {
        val user = getLoggedInUser() ?: return null
        val evOwnerEntity = evOwnerDao.getEVOwnerByUserId(user.userId) ?: return null

        return EVOwner(
                id = evOwnerEntity.id,
                nic = evOwnerEntity.nic,
                firstName = evOwnerEntity.firstName,
                lastName = evOwnerEntity.lastName,
                email = evOwnerEntity.email,
                phoneNumber = evOwnerEntity.phoneNumber,
                address = evOwnerEntity.address,
                licenseNumber = evOwnerEntity.licenseNumber ?: "",
                vehicleModel = evOwnerEntity.vehicleModel ?: "",
                vehicleYear = evOwnerEntity.vehicleYear,
                batteryCapacity = evOwnerEntity.batteryCapacity,
                isActive = evOwnerEntity.isActive,
                registrationDate = evOwnerEntity.registrationDate ?: ""
        )
    }

    suspend fun isLoggedIn(): Boolean {
        return userDao.hasLoggedInUser()
    }

    suspend fun clearUserData() {
        userDao.deleteAllUsers()
        evOwnerDao.deleteAllEVOwners()
    }

    fun getUserFlow(): Flow<EVOwner?> {
        return flow {
            try {
                val user = getLoggedInUser()
                if (user != null) {
                    evOwnerDao.getEVOwnerFlow(user.userId).collect { evOwnerEntity ->
                        val evOwner =
                                evOwnerEntity?.let {
                                    EVOwner(
                                            id = it.id,
                                            nic = it.nic,
                                            firstName = it.firstName,
                                            lastName = it.lastName,
                                            email = it.email,
                                            phoneNumber = it.phoneNumber,
                                            address = it.address,
                                            licenseNumber = it.licenseNumber ?: "",
                                            vehicleModel = it.vehicleModel ?: "",
                                            vehicleYear = it.vehicleYear,
                                            batteryCapacity = it.batteryCapacity,
                                            isActive = it.isActive,
                                            registrationDate = it.registrationDate ?: ""
                                    )
                                }
                        emit(evOwner)
                    }
                } else {
                    emit(null)
                }
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

    suspend fun saveRememberMe(remember: Boolean) {
        val user = getLoggedInUser()
        if (user != null) {
            userDao.updateRememberMe(user.userId, remember)
        }
    }

    suspend fun getRememberMe(): Boolean {
        val user = getLoggedInUser()
        return if (user != null) {
            userDao.getRememberMe(user.userId) ?: false
        } else false
    }

    suspend fun saveAutoLogin(autoLogin: Boolean) {
        val user = getLoggedInUser()
        if (user != null) {
            userDao.updateAutoLogin(user.userId, autoLogin)
        }
    }

    suspend fun getAutoLogin(): Boolean {
        val user = getLoggedInUser()
        return if (user != null) {
            userDao.getAutoLogin(user.userId) ?: false
        } else false
    }

    // Additional methods needed by the app
    suspend fun saveLoginData(token: String, userId: String, userType: String) {
        val userEntity =
                UserEntity(
                        userId = userId,
                        authToken = token,
                        userType = userType,
                        updatedAt = Date()
                )
        userDao.insertUser(userEntity)
    }

    suspend fun getUserNIC(): String? {
        val user = getLoggedInUser()
        return if (user != null) {
            userDao.getUserNIC(user.userId)
        } else null
    }

    suspend fun saveUserNIC(nic: String) {
        val user = getLoggedInUser()
        if (user != null) {
            userDao.updateUserNIC(user.userId, nic)
        }
    }

    suspend fun getVehicleModel(): String? {
        val user = getLoggedInUser()
        return if (user != null) {
            evOwnerDao.getVehicleModel(user.userId)
        } else null
    }

    suspend fun getBatteryCapacity(): Double {
        val user = getLoggedInUser()
        return if (user != null) {
            evOwnerDao.getBatteryCapacity(user.userId) ?: 0.0
        } else 0.0
    }

    suspend fun saveUserData(profile: EVOwner) {
        saveEVOwnerProfile(profile)
        saveUserNIC(profile.nic)
    }

    suspend fun logout() {
        clearUserData()
    }

    // Operator session management methods
    suspend fun saveOperatorSession(
        username: String,
        password: String,
        rememberMe: Boolean,
        userId: String? = null,
        authToken: String? = null
    ) {
        if (rememberMe) {
            prefs.edit().apply {
                putString("operator_username", username)
                putString("operator_password", password) // Note: In production, this should be encrypted
                putString("operator_user_id", userId)
                putString("operator_auth_token", authToken)
                putBoolean("operator_remember_me", true)
                putLong("operator_session_created", System.currentTimeMillis())
                apply()
            }
        } else {
            clearOperatorSession()
        }
    }

    suspend fun getOperatorSession(): Pair<String, String>? {
        val rememberMe = prefs.getBoolean("operator_remember_me", false)
        if (!rememberMe) return null

        val username = prefs.getString("operator_username", null) ?: return null
        val password = prefs.getString("operator_password", null) ?: return null

        // Check if session is still valid (30 days)
        val sessionCreated = prefs.getLong("operator_session_created", 0)
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        if (System.currentTimeMillis() - sessionCreated > thirtyDaysInMillis) {
            clearOperatorSession()
            return null
        }

        return Pair(username, password)
    }

    suspend fun clearOperatorSession() {
        prefs.edit().apply {
            remove("operator_username")
            remove("operator_password")
            remove("operator_user_id")
            remove("operator_auth_token")
            remove("operator_remember_me")
            remove("operator_session_created")
            apply()
        }
    }

    suspend fun isAutoLoginEnabled(): Boolean {
        return prefs.getBoolean("operator_remember_me", false)
    }

    suspend fun hasValidOperatorSession(): Boolean {
        val session = getOperatorSession()
        return session != null
    }

    suspend fun getStoredOperatorCredentials(): Pair<String, String>? {
        return getOperatorSession()
    }
}