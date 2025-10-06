# Login API Update Documentation

## Overview

Updated the login functionality to match the new backend API specification.

## API Changes

### Login Endpoint

**API Endpoint**: `POST /api/Auth/evowner/login`

### Request Format

```json
{
  "nic": "string",
  "password": "string"
}
```

### Response Format

```json
{
  "token": "string",
  "nic": "string",
  "fullName": "string",
  "expiresAt": "2025-09-27T17:15:22.071Z"
}
```

## Code Changes Made

### 1. DTOs (`DTOs.kt`)

- **EVOwnerLoginResponse**: Updated to match new API response structure
  - Removed: `refreshToken`, `userType`, `evOwner`
  - Added: `nic`, `fullName`, `expiresAt`

### 2. UserPreferencesManager (`UserPreferencesManager.kt`)

- **Added new method**: `saveLoginData(nic, fullName, token)` to handle simplified login data
- **Maintains backward compatibility**: Keeps existing methods for other parts of the app

### 3. LoginActivity (`LoginActivity.kt`)

- **Updated login success handler**: Now uses `saveLoginData()` instead of multiple separate saves
- **Simplified data storage**: Only saves essential data from login response

## Key Differences from Previous Implementation

### Before (Old API Response)

```kotlin
// Old response had:
loginResponse.token          // JWT token
loginResponse.refreshToken   // Refresh token
loginResponse.userType       // User type (e.g., "EVOwner")
loginResponse.evOwner        // Full EVOwner object with all profile data
```

### After (New API Response)

```kotlin
// New response has:
loginResponse.token          // JWT token
loginResponse.nic            // User's NIC
loginResponse.fullName       // Full name
loginResponse.expiresAt      // Token expiration time
```

## Impact on User Data

### Login Process

1. User provides NIC and password
2. API returns basic authentication data (token, nic, fullName)
3. App stores token and basic user info locally
4. Full profile data can be fetched separately if needed using the `/api/evowners/{nic}` endpoint

### Data Storage Strategy

- **Immediate storage**: Token, NIC, and full name from login response
- **Deferred storage**: Email, phone, address, etc. can be fetched from profile endpoint when needed
- **Backward compatibility**: Existing user preference methods still work for other features

## Testing Notes

- Login flow now works with simplified API response
- Token storage and retrieval unchanged
- User profile features may need separate profile API calls for complete data
