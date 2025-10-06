# XPoint Connect API

## Overview
XPoint Connect is a comprehensive EV Charging Station Booking System built with ASP.NET Core 8 and MongoDB. This backend API serves as the central web service handling all business logic for web applications and mobile applications.

## Features

### User Management
- **BackOffice Users**: System administration functions with full access to all features
- **Station Operators**: Assigned to specific charging stations, can access both web and mobile applications for EV operations at their assigned stations only
- **EV Owners**: Mobile application users for booking charging slots

### Core Functionalities
1. **User Authentication & Authorization** (JWT-based with role-based access control)
2. **EV Owner Management** (with Sri Lankan NIC as primary key)
3. **Charging Station Management** (AC/DC stations with location, slots, schedules, and operator assignments)
4. **Booking Management** (reservations, modifications, cancellations with business rule enforcement)
5. **QR Code Generation** for approved bookings
6. **Dashboard Statistics** for different user types
7. **Nearby Station Discovery** (geolocation-based)
8. **Station-Operator Assignment System** (operators can only manage their assigned stations)

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
- `POST /api/users` - Create user (BackOffice only)
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (BackOffice only)
- `POST /api/users/{id}/activate` - Activate user (BackOffice only)
- `POST /api/users/{id}/deactivate` - Deactivate user (BackOffice only)

### EV Owner Management
- `GET /api/evowners` - Get all EV owners (BackOffice/StationOperator)
- `GET /api/evowners/{nic}` - Get EV owner by NIC
- `POST /api/evowners` - Create EV owner (BackOffice/StationOperator)
- `PUT /api/evowners/{nic}` - Update EV owner
- `DELETE /api/evowners/{nic}` - Delete EV owner (BackOffice only)
- `POST /api/evowners/{nic}/activate` - Activate EV owner (BackOffice only)
- `POST /api/evowners/{nic}/deactivate` - Deactivate EV owner
- `GET /api/evowners/reactivation-requests` - Get reactivation requests (BackOffice only)

### Charging Station Management
- `GET /api/chargingstations` - Get all stations
- `GET /api/chargingstations/{id}` - Get station by ID
- `POST /api/chargingstations` - Create station (BackOffice only)
- `PUT /api/chargingstations/{id}` - Update station (BackOffice: full access, StationOperator: assigned stations only)
- `DELETE /api/chargingstations/{id}` - Delete station (BackOffice only)
- `POST /api/chargingstations/{id}/deactivate` - Deactivate station (BackOffice only)
- `POST /api/chargingstations/nearby` - Find nearby stations
- `GET /api/chargingstations/operator/{operatorId}` - Get stations by operator (StationOperator: own stations only)
- `PUT /api/chargingstations/{id}/schedule` - Update station schedule (operators: assigned stations only)

### Booking Management
- `GET /api/bookings` - Get bookings (BackOffice: all bookings, StationOperator: assigned stations only)
- `GET /api/bookings/{id}` - Get booking by ID (with authorization checks)
- `POST /api/bookings` - Create booking
- `PUT /api/bookings/{id}` - Update booking (with 12-hour advance notice rule)
- `POST /api/bookings/{id}/cancel` - Cancel booking (with 12-hour advance notice rule)
- `POST /api/bookings/{id}/approve` - Approve booking (BackOffice: all, StationOperator: assigned stations only)
- `POST /api/bookings/{id}/checkin` - Check-in booking (StationOperator: assigned stations only)
- `POST /api/bookings/{id}/checkout` - Check-out booking (StationOperator: assigned stations only)
- `GET /api/bookings/evowner/{nic}` - Get bookings by EV owner
- `GET /api/bookings/station/{stationId}` - Get bookings by station (StationOperator: assigned stations only)
- `POST /api/bookings/scan-qr` - Scan QR code (StationOperator: assigned stations only)
- `GET /api/bookings/dashboard/{nic}` - Get dashboard stats
- `GET /api/bookings/stats` - Get booking statistics (BackOffice only)
- `GET /api/bookings/upcoming/{nic}` - Get upcoming bookings
- `GET /api/bookings/history/{nic}` - Get booking history

### Operator Assignment Management (BackOffice Only)
- `POST /api/operatorassignments/operators` - Create new Station Operator with optional station assignments
- `POST /api/operatorassignments/assign` - Assign operator to specific station
- `POST /api/operatorassignments/bulk-assign` - Assign operator to multiple stations
- `DELETE /api/operatorassignments/stations/{stationId}/operator` - Unassign operator from station
- `DELETE /api/operatorassignments/operators/{operatorId}/stations` - Unassign operator from all stations
- `GET /api/operatorassignments/operators` - Get all operators with their assigned stations
- `GET /api/operatorassignments/operators/{operatorId}` - Get specific operator's assigned stations
- `GET /api/operatorassignments/unassigned-stations` - Get all unassigned stations
- `GET /api/operatorassignments/summary` - Get assignment summary with statistics
- `PUT /api/operatorassignments/stations/{stationId}/reassign` - Reassign station to different operator
- `GET /api/operatorassignments/statistics` - Get assignment statistics for dashboard

## Station-Operator Assignment System

### How It Works
1. **Station Assignment**: Each charging station has an `OperatorId` field that links it to a specific Station Operator
2. **Authorization Logic**: Station Operators can only access bookings and manage stations they are assigned to
3. **Security Implementation**: All endpoints validate operator assignments before allowing access
4. **Assignment Management**: BackOffice users can create operators and assign/reassign stations through dedicated endpoints

### Operator Assignment Features
- **Create Station Operators**: BackOffice can create new operator accounts with initial station assignments
- **Dynamic Assignment**: Assign/unassign operators to/from stations at any time
- **Bulk Operations**: Assign operators to multiple stations simultaneously
- **Assignment Tracking**: Monitor which stations are assigned and which operators have no assignments
- **Reassignment**: Transfer station ownership from one operator to another
- **Statistics**: Comprehensive assignment statistics for management oversight

### Authorization Matrix
| Action | BackOffice | StationOperator | EV Owner |
|--------|------------|-----------------|----------|
| View All Bookings | ? All | ? Assigned Stations Only | ? |
| Approve Bookings | ? All | ? Assigned Stations Only | ? |
| Check-in/Check-out | ? All | ? Assigned Stations Only | ? |
| QR Code Scanning | ? All | ? Assigned Stations Only | ? |
| Manage Stations | ? All | ? Assigned Only | ? |
| Create Users | ? Yes | ? | ? |
| Create EV Owners | ? Yes | ? Yes | ? |

## Business Rules

### Booking Constraints
1. **Reservation Date**: Must be within 7 days from booking date
2. **Modification Window**: Bookings can only be modified/cancelled at least 12 hours before reservation time
3. **Station Deactivation**: Cannot deactivate stations with active bookings
4. **Operator Authorization**: Station operators can only process bookings for their assigned stations

### User Roles & Permissions
1. **BackOffice**: Full system access, user management, all stations and bookings
2. **StationOperator**: Can manage assigned stations and process bookings for assigned stations only
3. **EVOwner**: Can manage own profile and bookings

### Authentication & Authorization
- JWT tokens with 24-hour expiration
- Role-based authorization with station-specific access control
- Secure password hashing
- NIC-based EV owner identification

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

## Web Application Features

The system includes a React.js web application with the following features:
- **User Management**: Create and manage BackOffice and Station Operator accounts
- **EV Owner Management**: Complete CRUD operations with NIC validation
- **Charging Station Management**: Manage stations with GPS coordinates and amenities
- **Booking Management**: Process reservations with business rule enforcement
- **Dashboard**: Role-based statistics and monitoring
- **Responsive Design**: Works on desktop, tablet, and mobile devices

### Default Login Credentials
- **BackOffice Admin**: username: `admin`, password: `Admin123!`
- **Station Operator**: username: `operator1`, password: `Operator123!`

## Mobile Application Support

The API provides full support for mobile applications with:
- **EV Owner Registration**: Self-registration with NIC validation
- **Booking Management**: Create, modify, cancel reservations
- **QR Code Generation**: For approved bookings
- **Dashboard Statistics**: Personalized stats for EV owners
- **Nearby Station Discovery**: Geolocation-based station finding
- **Station Operator Functions**: QR scanning, check-in/check-out for assigned stations

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

### Configuration Requirements
- MongoDB connection string
- JWT secret key (minimum 32 characters)
- CORS policy for web application
- HTTPS configuration for production

## Security Features

### Authentication & Authorization
1. **JWT Bearer Authentication** with role-based claims
2. **Station-Operator Authorization** - operators can only access assigned stations
3. **Input Validation** - comprehensive validation for all endpoints
4. **Password Security** - secure hashing with salt
5. **NIC Validation** - Sri Lankan NIC format validation

### Security Best Practices
1. Use strong JWT secret keys (32+ characters)
2. Enable HTTPS in production
3. Implement rate limiting
4. Validate all input data
5. Use secure MongoDB connections
6. Implement proper error handling
7. Log security events
8. Protect sensitive endpoints with proper authorization

## Development Guidelines

### Code Standards
1. Follow RESTful API design principles
2. Use async/await for database operations
3. Implement proper error handling
4. Use dependency injection
5. Follow SOLID principles
6. Write unit tests for critical functionality
7. Implement proper logging

### Business Logic Implementation
1. **Fat Service Architecture** - All business logic in the API
2. **Validation at Multiple Levels** - Model validation, business rule validation
3. **Proper Status Management** - Clear booking status transitions
4. **Operator Assignment Validation** - Enforce station-operator relationships
5. **Time-based Constraints** - 7-day booking window, 12-hour modification rule

## Assignment Compliance

This API fully implements the assignment requirements:
- ? **Web Application Backend** - Complete support for BackOffice and Station Operator functions
- ? **Mobile Application Backend** - Full EV Owner and Station Operator mobile support
- ? **User Management** - Two distinct roles with proper access control
- ? **EV Owner Management** - NIC-based with activation/deactivation
- ? **Station Management** - AC/DC types with location and operator assignment
- ? **Booking Management** - Complete lifecycle with business rules
- ? **FAT Service** - All business logic in centralized web service
- ? **NoSQL Database** - MongoDB implementation
- ? **IIS Deployment Ready** - Production deployment support

## Support
For issues and questions, please contact the development team or create an issue in the GitHub repository.

## License
This project is developed as part of an academic assignment