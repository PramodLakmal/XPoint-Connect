/**
 * EVOwnerDao.kt
 *
 * Purpose: Data Access Object for EV Owner profile database operations Author: XPoint Connect
 * Development Team Date: October 10, 2025
 *
 * Description: This DAO interface defines database operations for Electric Vehicle owner profile
 * management. It provides comprehensive CRUD operations for EV owner data including personal
 * information, vehicle details, and account management.
 *
 * Key Features:
 * - Insert, update, and delete EV owner profiles
 * - Query operations for profile retrieval by ID and NIC
 * - Vehicle information management (model, year, battery capacity)
 * - License and registration data operations
 * - Suspend functions for coroutine support
 * - Flow-based reactive data access for real-time updates
 * - Account status and activity tracking
 */
package com.xpoint.connect.data.database.dao

import androidx.room.*
import androidx.room.Query
import com.xpoint.connect.data.database.entity.EVOwnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EVOwnerDao {

    @Query("SELECT * FROM ev_owners WHERE id = :id LIMIT 1")
    suspend fun getEVOwnerById(id: String): EVOwnerEntity?

    @Query("SELECT * FROM ev_owners WHERE user_id = :userId LIMIT 1")
    suspend fun getEVOwnerByUserId(userId: String): EVOwnerEntity?

    @Query("SELECT * FROM ev_owners WHERE nic = :nic LIMIT 1")
    suspend fun getEVOwnerByNIC(nic: String): EVOwnerEntity?

    @Query("SELECT * FROM ev_owners WHERE user_id = :userId")
    fun getEVOwnerFlow(userId: String): Flow<EVOwnerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEVOwner(evOwner: EVOwnerEntity)

    @Update suspend fun updateEVOwner(evOwner: EVOwnerEntity)

    @Delete suspend fun deleteEVOwner(evOwner: EVOwnerEntity)

    @Query("DELETE FROM ev_owners WHERE user_id = :userId")
    suspend fun deleteEVOwnerByUserId(userId: String)

    @Query("DELETE FROM ev_owners") suspend fun deleteAllEVOwners()

    @Query("UPDATE ev_owners SET vehicle_model = :vehicleModel WHERE user_id = :userId")
    suspend fun updateVehicleModel(userId: String, vehicleModel: String?)

    @Query("UPDATE ev_owners SET vehicle_year = :vehicleYear WHERE user_id = :userId")
    suspend fun updateVehicleYear(userId: String, vehicleYear: Int)

    @Query("UPDATE ev_owners SET battery_capacity = :batteryCapacity WHERE user_id = :userId")
    suspend fun updateBatteryCapacity(userId: String, batteryCapacity: Double)

    @Query("UPDATE ev_owners SET license_number = :licenseNumber WHERE user_id = :userId")
    suspend fun updateLicenseNumber(userId: String, licenseNumber: String?)

    @Query("UPDATE ev_owners SET is_active = :isActive WHERE user_id = :userId")
    suspend fun updateActiveStatus(userId: String, isActive: Boolean)

    @Query("SELECT vehicle_model FROM ev_owners WHERE user_id = :userId")
    suspend fun getVehicleModel(userId: String): String?

    @Query("SELECT battery_capacity FROM ev_owners WHERE user_id = :userId")
    suspend fun getBatteryCapacity(userId: String): Double?

    @Query("SELECT license_number FROM ev_owners WHERE user_id = :userId")
    suspend fun getLicenseNumber(userId: String): String?

    @Query("SELECT COUNT(*) > 0 FROM ev_owners WHERE user_id = :userId")
    suspend fun hasEVOwnerProfile(userId: String): Boolean
}
