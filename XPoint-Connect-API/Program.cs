using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Text;
using XPoint_Connect_API.Configuration;
using XPoint_Connect_API.Services;

var builder = WebApplication.CreateBuilder(args);

// Add configuration
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

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

// Configure Swagger with JWT Authentication that automatically adds "Bearer " prefix
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo 
    { 
        Title = "XPoint Connect API", 
        Version = "v1",
        Description = "EV Charging Station Booking System API"
    });

    // Configure JWT Authentication for Swagger with automatic "Bearer " prefix
    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Description = "JWT Authorization header. Enter your token below (Bearer prefix will be added automatically):",
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
        "POST /api/seed-data - Initialize Default Data",
        "GET /api/dev/health - Health Check"
    }
});

Console.WriteLine("?? XPoint Connect API Starting...");
Console.WriteLine($"?? Environment: {app.Environment.EnvironmentName}");
Console.WriteLine($"?? Server: Kestrel (Development Server)");
Console.WriteLine("?? Access your API at:");
Console.WriteLine("   • HTTP:    http://localhost:5034");
Console.WriteLine("   • Swagger: http://localhost:5034/swagger");
Console.WriteLine("? Ready for development!");

app.Run();
