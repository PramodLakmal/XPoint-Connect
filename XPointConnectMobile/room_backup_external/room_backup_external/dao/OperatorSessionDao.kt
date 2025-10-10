/**
 * OperatorSessionDao.kt
 *
 * Purpose: Data Access Object for operator session management in SQLite database
 * Author: XPoint Connect Development Team
 * Date: December 2024
 *
 * Description: This DAO provides database operations for operator session persistence,
 * enabling automatic login functionality for station operators. Handles session
 * creation, validation, and cleanup operations.
 */
package com.xpoint.connect.data.database.dao

import androidx.room.*
import com.xpoint.connect.data.database.entity.OperatorSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OperatorSessionDao {
    
    @Query("SELECT * FROM operator_sessions WHERE session_id = 'operator_session'")
    suspend fun getOperatorSession(): OperatorSessionEntity?
    
    @Query("SELECT * FROM operator_sessions WHERE session_id = 'operator_session'")
    fun getOperatorSessionFlow(): Flow<OperatorSessionEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: OperatorSessionEntity)
    
    @Update
    suspend fun updateSession(session: OperatorSessionEntity)
    
    @Query("UPDATE operator_sessions SET session_timestamp = :timestamp WHERE session_id = 'operator_session'")
    suspend fun updateSessionTimestamp(timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE operator_sessions SET is_active = :isActive WHERE session_id = 'operator_session'")
    suspend fun updateSessionStatus(isActive: Boolean)
    
    @Query("UPDATE operator_sessions SET auth_token = :token WHERE session_id = 'operator_session'")
    suspend fun updateAuthToken(token: String)
    
    @Query("DELETE FROM operator_sessions WHERE session_id = 'operator_session'")
    suspend fun clearOperatorSession()
    
    @Query("DELETE FROM operator_sessions")
    suspend fun clearAllSessions()
    
    @Query("SELECT COUNT(*) FROM operator_sessions WHERE session_id = 'operator_session'")
    suspend fun sessionExists(): Int
    
    @Query("SELECT * FROM operator_sessions WHERE session_id = 'operator_session' AND is_active = 1 AND session_expiry > :currentTime")
    suspend fun getValidSession(currentTime: Long = System.currentTimeMillis()): OperatorSessionEntity?
}