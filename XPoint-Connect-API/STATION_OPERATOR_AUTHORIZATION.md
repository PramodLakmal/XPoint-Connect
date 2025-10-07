# XPoint Connect API - Station-Operator Authorization Documentation

## Overview
This document details the station-operator assignment and authorization system implemented in the XPoint Connect API.

## Station-Operator Assignment Architecture

### Core Concept
Each charging station is assigned to a specific Station Operator through the `OperatorId` field. Station Operators can ONLY access and manage:
- Bookings for their assigned stations
- Station details for their assigned stations
- QR code operations for their assigned stations

### Database Schema

#### ChargingStation Model
```csharp
public class ChargingStation
{
    public string Id { get; set; }
    public string Name { get; set; }
    public string OperatorId { get; set; } // Links station to operator
    // ... other properties
}
```

#### Authorization Flow
```
1. Station Operator logs in ? Receives JWT with userId
2. Operator attempts to access booking/station ? System checks station.OperatorId
3. If station.OperatorId == currentUserId ? Access granted
4. If station.OperatorId != currentUserId ? Access denied (403 Forbid)
```

## Authorization Implementation Details

### BookingsController Authorization

#### GetAllBookings
```csharp
[HttpGet]
public async Task<ActionResult<List<BookingResponseDto>>> GetAllBookings()
{
    // BackOffice: See all bookings
    if (User.IsInRole("BackOffice"))
        return Ok(await _bookingService.GetAllBookingsAsync());
    
    // StationOperator: Only see bookings for assigned stations
    if (User.IsInRole("StationOperator"))
    {
        var currentUserId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        var operatorBookings = await _bookingService.GetBookingsByOperatorAsync(currentUserId);
        return Ok(operatorBookings);
    }
    
    return Forbid();
}
```

#### Station-Specific Operations
```csharp
[HttpPost("{id}/approve")]
[Authorize(Roles = "BackOffice,StationOperator")]
public async Task<IActionResult> ApproveBooking(string id)
{
    // For station operators, verify station assignment
    if (User.IsInRole("StationOperator"))
    {
        var currentUserId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        var booking = await _bookingService.GetBookingByIdAsync(id);
        var station = await _bookingService.GetStationAsync(booking.ChargingStationId);
        
        if (station?.OperatorId != currentUserId)
            return Forbid("You can only approve bookings for stations assigned to you");
    }
    
    // Proceed with approval
    var success = await _bookingService.ApproveBookingAsync(id);
    return success ? NoContent() : BadRequest("Cannot approve booking");
}
```

### ChargingStationsController Authorization

#### Station Updates
```csharp
[HttpPut("{id}")]
[Authorize(Roles = "BackOffice,StationOperator")]
public async Task<ActionResult<ChargingStationResponseDto>> UpdateStation(string id, [FromBody] UpdateChargingStationDto updateStationDto)
{
    // Station operators can only update their assigned stations
    if (User.IsInRole("StationOperator"))
    {
        var currentUserId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        var station = await _chargingStationService.GetStationByIdAsync(id);
        
        if (station?.OperatorId != currentUserId)
            return Forbid("You can only update stations assigned to you");
        
        // Restrict what operators can update
        updateStationDto = new UpdateChargingStationDto
        {
            AvailableSlots = updateStationDto.AvailableSlots,
            Schedule = updateStationDto.Schedule,
            Description = updateStationDto.Description
        };
    }
    
    var result = await _chargingStationService.UpdateStationAsync(id, updateStationDto);
    return result != null ? Ok(result) : NotFound();
}
```

## Service Layer Implementation

### BookingService.GetBookingsByOperatorAsync
```csharp
public async Task<List<BookingResponseDto>> GetBookingsByOperatorAsync(string operatorId)
{
    // Get all stations assigned to this operator
    var operatorStations = await _chargingStationService.GetStationsByOperatorAsync(operatorId);
    var stationIds = operatorStations.Select(s => s.Id).ToList();

    // Get bookings for all operator's stations
    var bookings = await _context.Bookings
        .Find(b => stationIds.Contains(b.ChargingStationId))
        .SortByDescending(b => b.CreatedAt)
        .ToListAsync();

    // Map to DTOs and return
    var result = new List<BookingResponseDto>();
    foreach (var booking in bookings)
    {
        var dto = await MapToResponseDto(booking);
        if (dto != null) result.Add(dto);
    }

    return result;
}
```

## Authorization Matrix

### Complete Access Control Grid

| Endpoint | BackOffice | StationOperator | EV Owner | Authorization Logic |
|----------|------------|-----------------|----------|-------------------|
| `GET /api/bookings` | ? All Bookings | ? Assigned Stations Only | ? | Role + Station Assignment |
| `GET /api/bookings/{id}` | ? Any Booking | ? If Station Assigned | ? Own Bookings | Role + Ownership/Assignment |
| `POST /api/bookings/{id}/approve` | ? Any Booking | ? If Station Assigned | ? | Role + Station Assignment |
| `POST /api/bookings/{id}/checkin` | ? Any Booking | ? If Station Assigned | ? | Role + Station Assignment |
| `POST /api/bookings/{id}/checkout` | ? Any Booking | ? If Station Assigned | ? | Role + Station Assignment |
| `POST /api/bookings/scan-qr` | ? Any QR Code | ? If Station Assigned | ? | Role + Station Assignment |
| `GET /api/bookings/station/{id}` | ? Any Station | ? If Station Assigned | ? | Role + Station Assignment |
| `PUT /api/chargingstations/{id}` | ? Any Station | ? If Station Assigned | ? | Role + Station Assignment |
| `GET /api/chargingstations/operator/{id}` | ? Any Operator | ? Own Stations Only | ? | Role + Operator ID Match |

## Error Responses

### 403 Forbidden - Station Not Assigned
```json
{
  "error": "Forbidden",
  "message": "You can only access bookings for stations assigned to you",
  "statusCode": 403
}
```

### 403 Forbidden - QR Code Access Denied
```json
{
  "error": "Forbidden",
  "message": "This QR code is for a station not assigned to you",
  "statusCode": 403
}
```

## Security Benefits

### 1. Data Isolation
- Operators cannot access data from other operators' stations
- Clear separation of responsibilities
- Prevents unauthorized data access

### 2. Accountability
- Clear ownership of each station
- Audit trail for all operator actions
- Responsibility tracking

### 3. Scalability
- Easy to add new operators and assign stations
- Flexible station management
- Supports franchise/partner models

### 4. Business Logic Compliance
- Matches real-world station management
- Supports different operator types
- Enables regional management

## Implementation Examples

### Creating Station Assignment
```csharp
// When creating a station, assign an operator
var newStation = new ChargingStation
{
    Name = "Station Downtown",
    OperatorId = "operator123", // Assign to specific operator
    // ... other properties
};
```

### Checking Authorization in Business Logic
```csharp
public async Task<bool> CanOperatorAccessStation(string operatorId, string stationId)
{
    var station = await GetStationByIdAsync(stationId);
    return station?.OperatorId == operatorId;
}
```

### Frontend Integration
```javascript
// Web app checks user role to show appropriate data
if (user.role === 'StationOperator') {
    // Fetch only operator's bookings
    const bookings = await api.get('/api/bookings'); // Returns filtered data
} else if (user.role === 'BackOffice') {
    // Fetch all bookings
    const bookings = await api.get('/api/bookings'); // Returns all data
}
```

## Testing the Authorization

### Test Scenarios

1. **Station Operator Access Own Station**
   ```bash
   # Should succeed
   curl -H "Authorization: Bearer {operator_token}" \
        GET /api/bookings/station/{assigned_station_id}
   ```

2. **Station Operator Access Other Station**
   ```bash
   # Should return 403 Forbidden
   curl -H "Authorization: Bearer {operator_token}" \
        GET /api/bookings/station/{other_station_id}
   ```

3. **BackOffice Access Any Station**
   ```bash
   # Should succeed
   curl -H "Authorization: Bearer {backoffice_token}" \
        GET /api/bookings/station/{any_station_id}
   ```

## Conclusion

The station-operator assignment system provides:
- **Secure authorization** based on station assignments
- **Clear separation of responsibilities**
- **Scalable architecture** for growing networks
- **Business logic compliance** with real-world operations
- **Comprehensive access control** across all endpoints

This architecture ensures that the system can scale efficiently while maintaining security and proper access control for all users.