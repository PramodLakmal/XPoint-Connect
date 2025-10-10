/**
 * UserDao.kt
 *
 * Purpose: Data Access Object for user-related database operations Author: XPoint Connect
 * Development Team Date: October 10, 2025
 *
 * Description: This DAO interface defines database operations for user authentication and profile
 * management. It provides suspend functions for asynchronous database operations and supports all
 * user data management requirements for the app.
 *
 * Key Features:
 * - Insert, update, and delete user records
 * - Query operations for authentication and profile retrieval
 * - Suspend functions for coroutine support
 * - Flow-based reactive data access
 * - Authentication token management
 * - User preference storage and retrieval
 */
package com.xpoint.connect.data.database.dao

import androidx.room.*
import androidx.room.Query
import com.xpoint.connect.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE auth_token IS NOT NULL LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    @Query("SELECT auth_token FROM users WHERE user_id = :userId")
    suspend fun getAuthToken(userId: String): String?

    @Query("SELECT * FROM users WHERE user_id = :userId")
    fun getUserFlow(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertUser(user: UserEntity)

    @Update suspend fun updateUser(user: UserEntity)

    @Delete suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users") suspend fun deleteAllUsers()

    @Query("UPDATE users SET auth_token = :token WHERE user_id = :userId")
    suspend fun updateAuthToken(userId: String, token: String?)

    @Query("UPDATE users SET remember_me = :rememberMe WHERE user_id = :userId")
    suspend fun updateRememberMe(userId: String, rememberMe: Boolean)

    @Query("UPDATE users SET auto_login = :autoLogin WHERE user_id = :userId")
    suspend fun updateAutoLogin(userId: String, autoLogin: Boolean)

    @Query("SELECT remember_me FROM users WHERE user_id = :userId")
    suspend fun getRememberMe(userId: String): Boolean?

    @Query("SELECT auto_login FROM users WHERE user_id = :userId")
    suspend fun getAutoLogin(userId: String): Boolean?

    @Query("SELECT user_nic FROM users WHERE user_id = :userId")
    suspend fun getUserNIC(userId: String): String?

    @Query("UPDATE users SET user_nic = :nic WHERE user_id = :userId")
    suspend fun updateUserNIC(userId: String, nic: String?)

    @Query("SELECT COUNT(*) > 0 FROM users WHERE auth_token IS NOT NULL")
    suspend fun hasLoggedInUser(): Boolean
}
