# Test Charging Station Data

## POST /api/ChargingStations

To create test charging stations, you can POST this data to your API:

```json
{
  "name": "Green Valley Charging Hub",
  "location": {
    "latitude": 6.9271,
    "longitude": 79.8612,
    "address": "123 Main Street",
    "city": "Colombo",
    "province": "Western"
  },
  "type": 1,
  "totalSlots": 4,
  "schedule": [
    {
      "startTime": "2025-10-06T06:00:00.000Z",
      "endTime": "2025-10-06T22:00:00.000Z",
      "availableSlots": 4
    }
  ],
  "operatorId": "operator-001",
  "chargingRate": 25.5,
  "description": "Fast charging station with multiple connectors. 24/7 access available with covered parking.",
  "amenities": ["WiFi", "Parking", "Restroom", "Cafe"]
}
```

```json
{
  "name": "City Center Quick Charge",
  "location": {
    "latitude": 6.9344,
    "longitude": 79.8428,
    "address": "456 Business District",
    "city": "Colombo",
    "province": "Western"
  },
  "type": 2,
  "totalSlots": 2,
  "schedule": [
    {
      "startTime": "2025-10-06T00:00:00.000Z",
      "endTime": "2025-10-06T23:59:00.000Z",
      "availableSlots": 2
    }
  ],
  "operatorId": "operator-002",
  "chargingRate": 30.0,
  "description": "Premium rapid charging in the heart of the city. Convenient for business travelers.",
  "amenities": ["Covered Parking", "Security", "Shopping"]
}
```

## Testing Steps:

1. **Verify Backend is Running:**

   ```
   GET http://localhost:5034/api/ChargingStations
   ```

2. **Create Test Data:**

   ```
   POST http://localhost:5034/api/ChargingStations
   Content-Type: application/json
   [Use the JSON data above]
   ```

3. **Test Mobile App:**
   - Login → Dashboard → Create Booking → Select Station
   - Should now show the test stations

## Expected Mobile App Behavior:

✅ **Successful Case:** Shows list of charging stations with:

- Station names and descriptions
- Locations (address, city)
- Charging rates
- Available/Unavailable status
- Search functionality

❌ **Error Cases:** Shows appropriate error messages:

- "No charging stations available" (if empty response)
- "Failed to load stations: [error]" (if API error)
- "Error loading stations: [exception]" (if network/parsing error)
