# XPoint Connect API

## Overview
XPoint Connect is a comprehensive EV Charging Station Booking System built with ASP.NET Core 8 and MongoDB. This backend API serves as the central web service handling all business logic for web applications and mobile applications.

## Features

### User Management
- **BackOffice Users**: System administration functions
- **Station Operators**: Can access both web and mobile applications for EV operations
- **EV Owners**: Mobile application users for booking charging slots

### Core Functionalities
1. **User Authentication & Authorization** (JWT-based)
2. **EV Owner Management** (with NIC as primary key)
3. **Charging Station Management** (AC/DC stations with location, slots, schedules)
4. **Booking Management** (reservations, modifications, cancellations)
5. **QR Code Generation** for approved bookings
6. **Dashboard Statistics** for different user types
7. **Nearby Station Discovery** (geolocation-based)

## Technology Stack
- **Framework**: ASP.NET Core 8 Web API
- **Database**: MongoDB (NoSQL)
- **Authentication**: JWT Bearer Tokens
- **ORM**: MongoDB.Driver
- **Documentation**: Swagger/OpenAPI

## Prerequisites
- .NET 8 SDK
- MongoDB Server (local or cloud)
- Visual Studio 2022 or VS Code

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/PramodLakmal/XPoint-Connect.git
cd XPoint-Connect/XPoint-Connect-API
```

### 2. Install Dependencies
```bash
dotnet restore
```

### 3. Configure MongoDB
Update `appsettings.json` with your MongoDB connection string:
```json
{
  "MongoDbSettings": {
    "ConnectionString": "mongodb://localhost:27017",
    "DatabaseName": "XPointConnectDB"
  }
}
```

### 4. Configure JWT Settings
Update JWT settings in `appsettings.json`:
```json
{
  "JwtSettings": {
    "SecretKey": "YourSuperSecretKeyThatShouldBeAtLeast32CharactersLongForSecurity",
    "Issuer": "XPointConnectAPI",
    "Audience": "XPointConnectClients",
    "TokenExpirationHours": 24
  }
}
```

### 5. Run the Application
```bash
dotnet run
```

The API will be available at:
- HTTP: `http://localhost:5000`
- HTTPS: `https://localhost:5001`
- Swagger UI: `https://localhost:5001/swagger`

## API Endpoints

### Authentication
- `POST /api/auth/login` - Web user login (BackOffice/StationOperator)
- `POST /api/auth/evowner/login` - EV Owner login
- `POST /api/auth/evowner/register` - EV Owner registration
- `POST /api/auth/register` - Create web user (BackOffice only)

### User Management
- `GET /api/users` - Get all users (BackOffice only)
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### EV Owner Management
- `GET /api/evowners` - Get all EV owners (BackOffice only)
- `GET /api/evowners/{nic}` - Get EV owner by NIC
- `POST /api/evowners` - Create EV owner
- `PUT /api/evowners/{nic}` - Update EV owner
- `DELETE /api/evowners/{nic}` - Delete EV owner (BackOffice only)
- `POST /api/evowners/{nic}/activate` - Activate EV owner (BackOffice only)
- `POST /api/evowners/{nic}/deactivate` - Deactivate EV owner
- `GET /api/evowners/reactivation-requests` - Get reactivation requests (BackOffice only)

### Charging Station Management
- `GET /api/chargingstations` - Get all stations
- `GET /api/chargingstations/{id}` - Get station by ID
- `POST /api/chargingstations` - Create station (BackOffice only)
- `PUT /api/chargingstations/{id}` - Update station
- `DELETE /api/chargingstations/{id}` - Delete station (BackOffice only)
- `POST /api/chargingstations/{id}/deactivate` - Deactivate station (BackOffice only)
- `POST /api/chargingstations/nearby` - Find nearby stations
- `GET /api/chargingstations/operator/{operatorId}` - Get stations by operator
- `PUT /api/chargingstations/{id}/schedule` - Update station schedule

### Booking Management
- `GET /api/bookings` - Get all bookings (BackOffice only)
- `GET /api/bookings/{id}` - Get booking by ID
- `POST /api/bookings` - Create booking
- `PUT /api/bookings/{id}` - Update booking
- `POST /api/bookings/{id}/cancel` - Cancel booking
- `POST /api/bookings/{id}/approve` - Approve booking (BackOffice/StationOperator)
- `POST /api/bookings/{id}/checkin` - Check-in booking (StationOperator)
- `POST /api/bookings/{id}/checkout` - Check-out booking (StationOperator)
- `GET /api/bookings/evowner/{nic}` - Get bookings by EV owner
- `GET /api/bookings/station/{stationId}` - Get bookings by station
- `POST /api/bookings/scan-qr` - Scan QR code (StationOperator)
- `GET /api/bookings/dashboard/{nic}` - Get dashboard stats
- `GET /api/bookings/stats` - Get booking statistics (BackOffice only)
- `GET /api/bookings/upcoming/{nic}` - Get upcoming bookings
- `GET /api/bookings/history/{nic}` - Get booking history

## Business Rules

### Booking Constraints
1. **Reservation Date**: Must be within 7 days from booking date
2. **Modification Window**: Bookings can only be modified/cancelled at least 12 hours before reservation time
3. **Station Deactivation**: Cannot deactivate stations with active bookings

### User Roles & Permissions
1. **BackOffice**: Full system access
2. **StationOperator**: Can manage assigned stations and process bookings
3. **EVOwner**: Can manage own profile and bookings

### Authentication
- JWT tokens with 24-hour expiration
- Role-based authorization
- Secure password hashing

## Database Collections

### Users Collection
```json
{
  "_id": "ObjectId",
  "username": "string",
  "email": "string",
  "passwordHash": "string",
  "role": "BackOffice|StationOperator",
  "isActive": "boolean",
  "createdAt": "DateTime",
  "updatedAt": "DateTime"
}
```

### EVOwners Collection
```json
{
  "_id": "NIC_string",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phoneNumber": "string",
  "address": "string",
  "passwordHash": "string",
  "isActive": "boolean",
  "requiresReactivation": "boolean",
  "createdAt": "DateTime",
  "updatedAt": "DateTime"
}
```

### ChargingStations Collection
```json
{
  "_id": "ObjectId",
  "name": "string",
  "location": {
    "latitude": "double",
    "longitude": "double",
    "address": "string",
    "city": "string",
    "province": "string"
  },
  "type": "AC|DC",
  "totalSlots": "integer",
  "availableSlots": "integer",
  "schedule": [
    {
      "startTime": "DateTime",
      "endTime": "DateTime",
      "availableSlots": "integer"
    }
  ],
  "isActive": "boolean",
  "operatorId": "string",
  "chargingRate": "double",
  "description": "string",
  "amenities": ["string"],
  "createdAt": "DateTime",
  "updatedAt": "DateTime"
}
```

### Bookings Collection
```json
{
  "_id": "ObjectId",
  "evOwnerNIC": "string",
  "chargingStationId": "string",
  "reservationDateTime": "DateTime",
  "bookingDate": "DateTime",
  "durationMinutes": "integer",
  "status": "Pending|Approved|CheckedIn|Completed|Cancelled|NoShow",
  "totalAmount": "double",
  "qrCode": "string",
  "checkInTime": "DateTime?",
  "checkOutTime": "DateTime?",
  "cancellationReason": "string?",
  "cancelledAt": "DateTime?",
  "operatorNotes": "string?",
  "createdAt": "DateTime",
  "updatedAt": "DateTime"
}
```

## Deployment

### IIS Deployment
1. Publish the application:
   ```bash
   dotnet publish -c Release -o ./publish
   ```
2. Copy files to IIS web directory
3. Configure IIS to run .NET 8 applications
4. Set up MongoDB connection
5. Configure HTTPS certificates

### Docker Deployment (Optional)
```dockerfile
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app
COPY ./publish .
EXPOSE 80
EXPOSE 443
ENTRYPOINT ["dotnet", "XPoint-Connect-API.dll"]
```

## Security Considerations
1. Use strong JWT secret keys
2. Enable HTTPS in production
3. Implement rate limiting
4. Validate all input data
5. Use secure MongoDB connections
6. Implement proper error handling
7. Log security events

## Development Guidelines
1. Follow RESTful API design principles
2. Use async/await for database operations
3. Implement proper error handling
4. Use dependency injection
5. Follow SOLID principles
6. Write unit tests for critical functionality

## Support
For issues and questions, please contact the development team or create an issue in the GitHub repository.

## License
This project is developed as part of an academic assignment.