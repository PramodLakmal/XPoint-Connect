package com.xpoint.connect.data.database.dao

import androidx.room.*
import com.xpoint.connect.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getUser(): UserEntity?
    
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserFlow(): Flow<UserEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE user_preferences SET authToken = :token, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateAuthToken(token: String?, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET refreshToken = :token, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateRefreshToken(token: String?, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET userType = :userType, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateUserType(userType: String?, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET isLoggedIn = :isLoggedIn, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateLoginStatus(isLoggedIn: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET isFirstTime = :isFirstTime, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateFirstTimeStatus(isFirstTime: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET userNIC = :nic, userName = :name, userEmail = :email, phoneNumber = :phone, licenseNumber = :license, vehicleModel = :vehicle, batteryCapacity = :battery, isLoggedIn = :isLoggedIn, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateUserData(
        nic: String?,
        name: String?,
        email: String?,
        phone: String?,
        license: String?,
        vehicle: String?,
        battery: Double,
        isLoggedIn: Boolean = true,
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE user_preferences SET authToken = NULL, refreshToken = NULL, isLoggedIn = 0, updatedAt = :timestamp WHERE id = 1")
    suspend fun logout(timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM user_preferences")
    suspend fun clearAllData()
    
    @Query("SELECT COUNT(*) FROM user_preferences WHERE id = 1")
    suspend fun userExists(): Int
}