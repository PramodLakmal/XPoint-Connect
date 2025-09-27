/**
 * Resource.kt
 *
 * Purpose: Sealed class for handling API response states and data flow management Author: XPoint
 * Connect Development Team Date: September 27, 2025
 *
 * Description: This sealed class provides a standardized way to handle different states of API
 * responses and data operations throughout the application. It encapsulates success, error, and
 * loading states with optional data and message parameters, enabling consistent error handling and
 * UI state management across all features.
 *
 * Key Features:
 * - Success state with data payload
 * - Error state with error message and optional data
 * - Loading state for UI feedback during operations
 * - Type-safe state management with generics
 * - Consistent API response handling pattern
 */
package com.xpoint.connect.utils

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
