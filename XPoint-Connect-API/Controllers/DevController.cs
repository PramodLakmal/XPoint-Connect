using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using XPoint_Connect_API.Services;

namespace XPoint_Connect_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DevController : ControllerBase
    {
        private readonly IDataSeedService _dataSeedService;
        private readonly IBookingService _bookingService;
        private readonly IChargingStationService _chargingStationService;
        private readonly IUserService _userService;
        private readonly IEVOwnerService _evOwnerService;

        public DevController(
            IDataSeedService dataSeedService,
            IBookingService bookingService,
            IChargingStationService chargingStationService,
            IUserService userService,
            IEVOwnerService evOwnerService)
        {
            _dataSeedService = dataSeedService;
            _bookingService = bookingService;
            _chargingStationService = chargingStationService;
            _userService = userService;
            _evOwnerService = evOwnerService;
        }

        /// <summary>
        /// Seeds the database with default data for development and testing
        /// </summary>
        [HttpPost("seed-data")]
        public async Task<IActionResult> SeedData()
        {
            try
            {
                await _dataSeedService.SeedDefaultDataAsync();
                return Ok(new { message = "Data seeding completed successfully" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = "Data seeding failed", error = ex.Message });
            }
        }

        /// <summary>
        /// Gets system statistics for development monitoring
        /// </summary>
        [HttpGet("system-stats")]
        public async Task<IActionResult> GetSystemStats()
        {
            try
            {
                var users = await _userService.GetAllUsersAsync();
                var evOwners = await _evOwnerService.GetAllEVOwnersAsync();
                var stations = await _chargingStationService.GetAllStationsAsync();
                var bookingStats = await _bookingService.GetBookingStatsAsync();

                var stats = new
                {
                    Users = new
                    {
                        Total = users.Count,
                        BackOffice = users.Count(u => u.Role == Models.UserRole.BackOffice),
                        StationOperators = users.Count(u => u.Role == Models.UserRole.StationOperator),
                        Active = users.Count(u => u.IsActive)
                    },
                    EVOwners = new
                    {
                        Total = evOwners.Count,
                        Active = evOwners.Count(o => o.IsActive),
                        RequiringReactivation = evOwners.Count(o => o.RequiresReactivation)
                    },
                    ChargingStations = new
                    {
                        Total = stations.Count,
                        Active = stations.Count(s => s.IsActive),
                        AC = stations.Count(s => s.Type == Models.ChargingStationType.AC),
                        DC = stations.Count(s => s.Type == Models.ChargingStationType.DC),
                        TotalSlots = stations.Sum(s => s.TotalSlots),
                        AvailableSlots = stations.Sum(s => s.AvailableSlots)
                    },
                    Bookings = bookingStats
                };

                return Ok(stats);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = "Failed to get system stats", error = ex.Message });
            }
        }

        /// <summary>
        /// Health check endpoint
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new 
            { 
                status = "OK",
                timestamp = DateTime.UtcNow,
                environment = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") ?? "Development",
                message = "XPoint Connect API is running successfully!",
                version = "1.0.0"
            });
        }

        /// <summary>
        /// Gets API information and available endpoints
        /// </summary>
        [HttpGet("info")]
        public IActionResult GetApiInfo()
        {
            var info = new
            {
                ApiName = "XPoint Connect API",
                Version = "1.0.0",
                Environment = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") ?? "Development",
                ServerTime = DateTime.UtcNow,
                Documentation = "/swagger",
                QuickStart = new
                {
                    Step1 = "POST /api/seed-data - Initialize default data",
                    Step2 = "POST /api/auth/quick-login with admin/Admin123!",
                    Step3 = "Copy bearerToken and click ?? Authorize in Swagger",
                    Step4 = "Test protected endpoints!"
                },
                Endpoints = new
                {
                    Authentication = new[]
                    {
                        "POST /api/auth/login - User Login",
                        "POST /api/auth/quick-login - Easy token copy",
                        "POST /api/auth/evowner/login - EV Owner Login",
                        "POST /api/auth/evowner/register - EV Owner Registration",
                        "POST /api/auth/register - Create User (requires BackOffice)"
                    },
                    UserManagement = new[]
                    {
                        "GET /api/users - Get all users",
                        "POST /api/users - Create user",
                        "PUT /api/users/{id} - Update user"
                    },
                    EVOwnerManagement = new[]
                    {
                        "GET /api/evowners - Get all EV owners",
                        "POST /api/evowners/{nic}/activate - Activate EV owner",
                        "POST /api/evowners/{nic}/deactivate - Deactivate EV owner"
                    },
                    ChargingStations = new[]
                    {
                        "GET /api/chargingstations - Get all stations",
                        "POST /api/chargingstations/nearby - Find nearby stations",
                        "POST /api/chargingstations - Create station"
                    },
                    Bookings = new[]
                    {
                        "POST /api/bookings - Create booking",
                        "POST /api/bookings/{id}/approve - Approve booking",
                        "POST /api/bookings/scan-qr - Scan QR code",
                        "GET /api/bookings/dashboard/{nic} - Dashboard stats"
                    }
                },
                SupportContact = "dev-team@xpointconnect.com"
            };

            return Ok(info);
        }

        /// <summary>
        /// Super quick way to get admin token - Development only
        /// </summary>
        [HttpPost("get-admin-token")]
        public async Task<IActionResult> GetAdminToken()
        {
            // Only allow in development
            var environment = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT");
            if (environment != "Development")
            {
                return Forbid("This endpoint is only available in development environment");
            }

            try
            {
                var loginRequest = new DTOs.LoginRequestDto
                {
                    Username = "admin",
                    Password = "Admin123!"
                };

                var result = await _userService.AuthenticateAsync(loginRequest);
                
                if (result == null)
                {
                    return BadRequest(new
                    {
                        error = "Admin user not found",
                        solution = "Run POST /api/seed-data first to create the admin user"
                    });
                }

                return Ok(new
                {
                    message = "?? Admin token ready! Copy the bearerToken below:",
                    bearerToken = $"Bearer {result.Token}",
                    instructions = new[]
                    {
                        "1. Copy the bearerToken above",
                        "2. Click ?? Authorize in Swagger",  
                        "3. Paste and click Authorize",
                        "4. You're ready to test protected endpoints!"
                    },
                    username = "admin",
                    role = "BackOffice",
                    expiresAt = result.ExpiresAt.ToString("yyyy-MM-dd HH:mm:ss UTC")
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new
                {
                    error = "Failed to authenticate admin user",
                    details = ex.Message,
                    solution = "Make sure to run POST /api/seed-data first"
                });
            }
        }
    }
}