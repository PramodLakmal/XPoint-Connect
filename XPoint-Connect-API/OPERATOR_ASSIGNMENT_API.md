# Operator Assignment System - API Documentation

## Overview
The Operator Assignment System provides comprehensive functionality for BackOffice users to create Station Operator accounts and assign them to specific charging stations. This system ensures secure, station-specific access control and efficient management of the charging network.

## Key Features

### 1. Station Operator Creation
- Create new Station Operator accounts
- Assign stations during operator creation
- Automatic password hashing and security
- User validation and duplicate checking

### 2. Station Assignment Management
- Assign operators to specific stations
- Bulk assignment to multiple stations
- Reassign stations between operators
- Unassign operators from stations

### 3. Assignment Monitoring
- View all operators with their assigned stations
- Track unassigned stations
- Assignment statistics and analytics
- Comprehensive assignment summary

## API Endpoints

### Create Station Operator
```http
POST /api/operatorassignments/operators
Authorization: Bearer {backoffice_token}
Content-Type: application/json

{
  "username": "operator_downtown",
  "email": "operator@example.com",
  "password": "SecurePassword123!",
  "assignedStationIds": ["station1", "station2", "station3"]
}
```

**Response:**
```json
{
  "id": "64a7b8c9d1e2f3a4b5c6d7e8",
  "username": "operator_downtown",
  "email": "operator@example.com",
  "role": "StationOperator",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### Assign Operator to Station
```http
POST /api/operatorassignments/assign
Authorization: Bearer {backoffice_token}
Content-Type: application/json

{
  "operatorId": "64a7b8c9d1e2f3a4b5c6d7e8",
  "stationId": "64a7b8c9d1e2f3a4b5c6d7e9"
}
```

### Bulk Assign Operator to Stations
```http
POST /api/operatorassignments/bulk-assign
Authorization: Bearer {backoffice_token}
Content-Type: application/json

{
  "operatorId": "64a7b8c9d1e2f3a4b5c6d7e8",
  "stationIds": ["station1", "station2", "station3", "station4"]
}
```

### Get Operators with Stations
```http
GET /api/operatorassignments/operators
Authorization: Bearer {backoffice_token}
```

**Response:**
```json
[
  {
    "id": "64a7b8c9d1e2f3a4b5c6d7e8",
    "username": "operator_downtown",
    "email": "operator@example.com",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00Z",
    "assignedStations": [
      {
        "id": "64a7b8c9d1e2f3a4b5c6d7e9",
        "name": "Downtown Station 1",
        "address": "123 Main St",
        "city": "Colombo",
        "type": "DC",
        "totalSlots": 4,
        "availableSlots": 2,
        "isActive": true
      }
    ]
  }
]
```

### Get Unassigned Stations
```http
GET /api/operatorassignments/unassigned-stations
Authorization: Bearer {backoffice_token}
```

**Response:**
```json
[
  {
    "id": "64a7b8c9d1e2f3a4b5c6d7f0",
    "name": "Airport Station",
    "address": "Airport Road",
    "city": "Katunayake",
    "type": "AC",
    "totalSlots": 6,
    "isActive": true
  }
]
```

### Get Assignment Summary
```http
GET /api/operatorassignments/summary
Authorization: Bearer {backoffice_token}
```

**Response:**
```json
{
  "totalStations": 25,
  "assignedStations": 20,
  "unassignedStations": 5,
  "totalOperators": 8,
  "activeOperators": 7,
  "unassignedStationsList": [...],
  "operatorsWithStations": [...]
}
```

### Get Assignment Statistics
```http
GET /api/operatorassignments/statistics
Authorization: Bearer {backoffice_token}
```

**Response:**
```json
{
  "totalStations": 25,
  "assignedStations": 20,
  "unassignedStations": 5,
  "totalOperators": 8,
  "activeOperators": 7,
  "assignmentPercentage": 80.0,
  "operatorsWithoutStations": 1,
  "averageStationsPerOperator": 2.86
}
```

### Reassign Station
```http
PUT /api/operatorassignments/stations/{stationId}/reassign
Authorization: Bearer {backoffice_token}
Content-Type: application/json

{
  "newOperatorId": "64a7b8c9d1e2f3a4b5c6d7e8"
}
```

### Unassign Operator from Station
```http
DELETE /api/operatorassignments/stations/{stationId}/operator
Authorization: Bearer {backoffice_token}
```

### Unassign Operator from All Stations
```http
DELETE /api/operatorassignments/operators/{operatorId}/stations
Authorization: Bearer {backoffice_token}
```

## Business Logic

### Station Assignment Rules
1. **Unique Assignment**: Each station can only be assigned to one operator at a time
2. **Automatic Reassignment**: Assigning a station to a new operator automatically unassigns the previous operator
3. **Bulk Operations**: Multiple stations can be assigned to an operator simultaneously
4. **Validation**: All assignments verify that both operator and station exist

### Authorization Rules
1. **BackOffice Only**: Only BackOffice users can manage operator assignments
2. **Operator Restrictions**: Station Operators cannot modify their own assignments
3. **Station Access**: Operators can only access stations assigned to them
4. **Automatic Enforcement**: Assignment validation is automatic across all endpoints

### Data Integrity
1. **Referential Integrity**: All assignments maintain valid operator and station references
2. **Cleanup**: Deleting an operator automatically unassigns all their stations
3. **Status Management**: Inactive operators cannot be assigned new stations
4. **Audit Trail**: All assignment changes are tracked with timestamps

## Use Cases

### 1. Setting Up New Operator
```http
# Step 1: Create operator with initial stations
POST /api/operatorassignments/operators
{
  "username": "operator_west",
  "email": "west@company.com",
  "password": "SecurePass123!",
  "assignedStationIds": ["station1", "station2"]
}

# Step 2: Assign additional stations later
POST /api/operatorassignments/bulk-assign
{
  "operatorId": "new_operator_id",
  "stationIds": ["station3", "station4", "station5"]
}
```

### 2. Network Reorganization
```http
# Step 1: Get current assignments
GET /api/operatorassignments/summary

# Step 2: Reassign stations as needed
PUT /api/operatorassignments/stations/station1/reassign
{
  "newOperatorId": "different_operator_id"
}

# Step 3: Verify new assignments
GET /api/operatorassignments/operators
```

### 3. Managing Unassigned Stations
```http
# Step 1: Check unassigned stations
GET /api/operatorassignments/unassigned-stations

# Step 2: Assign to available operators
POST /api/operatorassignments/assign
{
  "operatorId": "available_operator_id",
  "stationId": "unassigned_station_id"
}
```

## Error Handling

### Common Error Responses

#### 400 Bad Request - Invalid Data
```json
{
  "error": "Bad Request",
  "message": "Failed to assign operator to station. Check if operator and station exist.",
  "statusCode": 400
}
```

#### 401 Unauthorized - Missing Authentication
```json
{
  "error": "Unauthorized",
  "message": "Authorization header is required",
  "statusCode": 401
}
```

#### 403 Forbidden - Insufficient Permissions
```json
{
  "error": "Forbidden",
  "message": "Only BackOffice users can manage operator assignments",
  "statusCode": 403
}
```

#### 409 Conflict - Duplicate Username
```json
{
  "error": "Conflict",
  "message": "Username already exists",
  "statusCode": 409
}
```

## Integration Examples

### Frontend Integration
```javascript
// Create operator with stations
const createOperator = async (operatorData) => {
  try {
    const response = await api.post('/api/operatorassignments/operators', operatorData);
    toast.success('Operator created successfully');
    return response.data;
  } catch (error) {
    toast.error(error.response?.data?.message || 'Failed to create operator');
    throw error;
  }
};

// Get assignment statistics for dashboard
const getAssignmentStats = async () => {
  try {
    const response = await api.get('/api/operatorassignments/statistics');
    return response.data;
  } catch (error) {
    console.error('Failed to fetch assignment statistics:', error);
    return null;
  }
};
```

### Mobile App Integration
```javascript
// Check operator's assigned stations (for mobile operator app)
const getMyStations = async (operatorId) => {
  try {
    const response = await api.get(`/api/operatorassignments/operators/${operatorId}`);
    return response.data; // Returns only stations assigned to this operator
  } catch (error) {
    console.error('Failed to fetch assigned stations:', error);
    return [];
  }
};
```

## Security Considerations

### 1. Access Control
- All endpoints require BackOffice role authentication
- JWT tokens must be valid and non-expired
- Rate limiting recommended for production

### 2. Data Protection
- Passwords are automatically hashed using secure algorithms
- Sensitive data is not exposed in responses
- All database operations use parameterized queries

### 3. Validation
- Input validation on all endpoints
- Business rule enforcement at API level
- Referential integrity maintained

### 4. Audit Trail
- All assignment changes are logged
- User actions are tracked
- Timestamps recorded for all operations

This operator assignment system provides comprehensive functionality for managing the charging station network while maintaining security and data integrity.