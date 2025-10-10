/*
 * Program.cs
 * Main entry point for the XPoint-Connect API application
 */

using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Text;
using XPoint_Connect_API.Configuration;
using XPoint_Connect_API.Services;

var builder = WebApplication.CreateBuilder(args);

// Configure application settings and dependency injection
builder.Services.Configure<MongoDbSettings>(
    builder.Configuration.GetSection("MongoDbSettings"));

builder.Services.Configure<JwtSettings>(
    builder.Configuration.GetSection("JwtSettings"));

// Add MongoDB context
var mongoDbSettings = builder.Configuration.GetSection("MongoDbSettings").Get<MongoDbSettings>();
if (mongoDbSettings != null)
{
    builder.Services.AddSingleton(mongoDbSettings);
    builder.Services.AddSingleton<IMongoDbContext, MongoDbContext>();
}

// Add JWT settings
var jwtSettings = builder.Configuration.GetSection("JwtSettings").Get<JwtSettings>();
if (jwtSettings != null)
{
    builder.Services.AddSingleton(jwtSettings);
    builder.Services.AddSingleton<IJwtService, JwtService>();
}

// Add services
builder.Services.AddScoped<IPasswordHashingService, PasswordHashingService>();
builder.Services.AddScoped<IQRCodeService, QRCodeService>();
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<IEVOwnerService, EVOwnerService>();
builder.Services.AddScoped<IChargingStationService, ChargingStationService>();
builder.Services.AddScoped<IBookingService, BookingService>();
builder.Services.AddScoped<IOperatorAssignmentService, OperatorAssignmentService>();
builder.Services.AddScoped<IDataSeedService, DataSeedService>();

// Add JWT Authentication
if (jwtSettings != null)
{
    builder.Services.AddAuthentication(options =>
    {
        options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
        options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
    })
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.ASCII.GetBytes(jwtSettings.SecretKey)),
            ValidateIssuer = true,
            ValidIssuer = jwtSettings.Issuer,
            ValidateAudience = true,
            ValidAudience = jwtSettings.Audience,
            ValidateLifetime = true,
            ClockSkew = TimeSpan.Zero
        };
    });
}

// Add CORS for development
builder.Services.AddCors(options =>
{
    options.AddPolicy("DevelopmentCors", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.Converters.Add(new System.Text.Json.Serialization.JsonStringEnumConverter());
    });
builder.Services.AddEndpointsApiExplorer();

// Configure Swagger with JWT Authentication that automatically adds "Bearer " prefix
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo 
    { 
        Title = "XPoint Connect API", 
        Version = "v1.0.0",
        Description = @"
# XPoint Connect API - EV Charging Station Booking System

A comprehensive RESTful API for managing electric vehicle charging stations, user accounts, and booking reservations.

## Features
- **User Management**: BackOffice and Station Operator accounts with role-based access
- **EV Owner Management**: Profile management with Sri Lankan NIC validation
- **Charging Station Management**: AC/DC stations with location and slot management
- **Booking Management**: Reservation system with business rule enforcement
- **Operator Assignment**: Station-operator assignment system for secure access control
- **QR Code Integration**: Booking identification and check-in/check-out
- **Real-time Analytics**: Dashboard statistics and monitoring

## Authentication
All endpoints (except registration) require JWT Bearer authentication. Use the 'Authorize' button above to authenticate.

## Roles
- **BackOffice**: Full system access including user management and operator assignments
- **StationOperator**: Limited access to assigned stations and related bookings
- **EVOwner**: Access to own profile and bookings (mobile app users)

## Business Rules
- Reservations must be within 7 days from booking date
- Bookings can only be modified/cancelled at least 12 hours before reservation
- Stations cannot be deactivated if they have active bookings
- Operators can only access stations assigned to them

## Support
API Documentation: https://github.com/PramodLakmal/XPoint-Connect
",
        Contact = new OpenApiContact
        {
            Name = "XPoint Connect Development Team",
            Email = "support@xpointconnect.com"
        }
    });

    // Include XML comments for better documentation
    var xmlFile = $"{System.Reflection.Assembly.GetExecutingAssembly().GetName().Name}.xml";
    var xmlPath = Path.Combine(AppContext.BaseDirectory, xmlFile);
    if (File.Exists(xmlPath))
    {
        c.IncludeXmlComments(xmlPath);
    }

    // Configure JWT Authentication for Swagger with automatic "Bearer " prefix
    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Description = @"
JWT Authorization header using the Bearer scheme. 

Enter your token in the text input below.
Example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'

The 'Bearer ' prefix will be added automatically.",
        Name = "Authorization",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT"
    });

    c.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });

    // Group endpoints by tags for better organization
    c.TagActionsBy(api => 
    {
        var controllerName = api.ActionDescriptor.RouteValues["controller"];
        return new[] { controllerName switch
        {
            "Auth" => "?? Authentication",
            "Users" => "?? User Management",
            "EVOwners" => "?? EV Owner Management", 
            "ChargingStations" => "? Charging Station Management",
            "Bookings" => "?? Booking Management",
            "OperatorAssignments" => "?? Operator Assignment Management",
            "Dev" => "??? Development Tools",
            _ => controllerName ?? "Other"
        }};
    });

    // Enable annotations for better endpoint descriptions
    c.EnableAnnotations();

    // Configure operation ordering
    c.OrderActionsBy(apiDesc => 
    {
        var routeValues = apiDesc.ActionDescriptor.RouteValues;
        var controllerName = routeValues.TryGetValue("controller", out var controller) ? controller : "Unknown";
        var actionName = routeValues.TryGetValue("action", out var action) ? action : "Unknown";
        
        var order = controllerName switch
        {
            "Auth" => "1",
            "Users" => "2", 
            "EVOwners" => "3",
            "ChargingStations" => "4",
            "Bookings" => "5",
            "OperatorAssignments" => "6",
            "Dev" => "9",
            _ => "8"
        };
        return $"{order}-{actionName}";
    });
});

var app = builder.Build();

// Configure the HTTP request pipeline for development
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "XPoint Connect API V1");
        c.RoutePrefix = "swagger";
        c.DocumentTitle = "XPoint Connect API Documentation";
    });
    app.UseDeveloperExceptionPage();
}
else
{
    app.UseExceptionHandler("/Error");
    app.UseHsts();
    app.UseHttpsRedirection();
}

app.UseCors("DevelopmentCors");

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

// Add data seeding endpoint
app.MapPost("/api/seed-data", async (IDataSeedService dataSeedService) =>
{
    await dataSeedService.SeedDefaultDataAsync();
    return Results.Ok(new { message = "Data seeding completed successfully" });
})
.WithName("SeedData");

// Add a comprehensive status endpoint
app.MapGet("/", () => new {
    message = "?? XPoint Connect API is running!",
    version = "1.0.0",
    timestamp = DateTime.UtcNow,
    status = "OK",
    environment = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") ?? "Development",
    server = "Kestrel (Development Server)",
    documentation = "/swagger",
    endpoints = new[] {
        "GET /swagger - API Documentation",
        "POST /api/auth/login - User Login (BackOffice/StationOperator)",
        "POST /api/auth/evowner/login - EV Owner Login",
        "POST /api/auth/evowner/register - EV Owner Registration",
        "GET /api/chargingstations - Get Charging Stations",
        "POST /api/chargingstations/nearby - Find Nearby Stations",
        "POST /api/bookings - Create Booking",
        "POST /api/operatorassignments/operators - Create Station Operator",
        "GET /api/operatorassignments/summary - Assignment Overview",
        "POST /api/seed-data - Initialize Default Data",
        "GET /api/dev/health - Health Check"
    },
    features = new[] {
        "?? JWT Authentication with Role-based Access",
        "?? User Management (BackOffice/StationOperator)",
        "?? EV Owner Management with NIC Validation",
        "? Charging Station Management",
        "?? Booking System with Business Rules",
        "?? Operator Assignment Management",
        "?? QR Code Integration",
        "?? Real-time Analytics & Statistics",
        "??? Secure Authorization with Station Assignments"
    }
});

Console.WriteLine("?? XPoint Connect API Starting...");
Console.WriteLine($"??? Environment: {app.Environment.EnvironmentName}");
Console.WriteLine($"? Server: Kestrel (Development Server)");
Console.WriteLine("?? Access your API at:");
Console.WriteLine("   � HTTP:    http://localhost:5034");
Console.WriteLine("   � Swagger: http://localhost:5034/swagger");
Console.WriteLine("? Ready for development!");

app.Run();
