using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Models;
using XPoint_Connect_API.Services;

namespace XPoint_Connect_API.Services
{
    public interface IDataSeedService
    {
        Task SeedDefaultDataAsync();
    }

    public class DataSeedService : IDataSeedService
    {
        private readonly IUserService _userService;
        private readonly IChargingStationService _chargingStationService;
        private readonly ILogger<DataSeedService> _logger;

        public DataSeedService(
            IUserService userService,
            IChargingStationService chargingStationService,
            ILogger<DataSeedService> logger)
        {
            _userService = userService;
            _chargingStationService = chargingStationService;
            _logger = logger;
        }

        public async Task SeedDefaultDataAsync()
        {
            try
            {
                await SeedDefaultUsersAsync();
                await SeedDefaultChargingStationsAsync();
                _logger.LogInformation("Data seeding completed successfully");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred during data seeding");
            }
        }

        private async Task SeedDefaultUsersAsync()
        {
            // Check if admin user exists
            var existingAdmin = await _userService.GetUserByUsernameAsync("admin");
            if (existingAdmin == null)
            {
                var adminUser = new CreateUserDto
                {
                    Username = "admin",
                    Email = "admin@xpointconnect.com",
                    Password = "Admin123!",
                    Role = UserRole.BackOffice
                };

                await _userService.CreateUserAsync(adminUser);
                _logger.LogInformation("Default admin user created");
            }

            // Check if station operator exists
            var existingOperator = await _userService.GetUserByUsernameAsync("operator1");
            if (existingOperator == null)
            {
                var operatorUser = new CreateUserDto
                {
                    Username = "operator1",
                    Email = "operator1@xpointconnect.com",
                    Password = "Operator123!",
                    Role = UserRole.StationOperator
                };

                var createdOperator = await _userService.CreateUserAsync(operatorUser);
                _logger.LogInformation("Default station operator created");
            }
        }

        private async Task SeedDefaultChargingStationsAsync()
        {
            var stations = await _chargingStationService.GetAllStationsAsync();
            if (stations.Count == 0)
            {
                // Create sample charging stations in Colombo area
                var sampleStations = new List<CreateChargingStationDto>
                {
                    new CreateChargingStationDto
                    {
                        Name = "Colombo City Center - AC Charging",
                        Location = new LocationDto
                        {
                            Latitude = 6.9271,
                            Longitude = 79.8612,
                            Address = "No. 137, Sir Chittampalam A. Gardiner Mawatha, Colombo 02",
                            City = "Colombo",
                            Province = "Western"
                        },
                        Type = ChargingStationType.AC,
                        TotalSlots = 4,
                        ChargingRate = 50.0,
                        Description = "AC charging station located in the heart of Colombo city",
                        Amenities = new List<string> { "Free WiFi", "Parking", "Restroom", "Cafe nearby" },
                        OperatorId = "" // Will be updated after operator creation
                    },
                    new CreateChargingStationDto
                    {
                        Name = "Independence Square - DC Fast Charging",
                        Location = new LocationDto
                        {
                            Latitude = 6.9022,
                            Longitude = 79.8607,
                            Address = "Independence Avenue, Colombo 07",
                            City = "Colombo",
                            Province = "Western"
                        },
                        Type = ChargingStationType.DC,
                        TotalSlots = 6,
                        ChargingRate = 120.0,
                        Description = "DC fast charging station near Independence Square",
                        Amenities = new List<string> { "24/7 Access", "Security", "Park nearby" },
                        OperatorId = ""
                    },
                    new CreateChargingStationDto
                    {
                        Name = "Galle Face Green - AC Charging",
                        Location = new LocationDto
                        {
                            Latitude = 6.9214,
                            Longitude = 79.8448,
                            Address = "Galle Face Green, Colombo 03",
                            City = "Colombo",
                            Province = "Western"
                        },
                        Type = ChargingStationType.AC,
                        TotalSlots = 3,
                        ChargingRate = 45.0,
                        Description = "Scenic charging location near Galle Face Green",
                        Amenities = new List<string> { "Ocean View", "Food Courts", "Walking Area" },
                        OperatorId = ""
                    },
                    new CreateChargingStationDto
                    {
                        Name = "Bambalapitiya Junction - Mixed Charging",
                        Location = new LocationDto
                        {
                            Latitude = 6.8905,
                            Longitude = 79.8565,
                            Address = "Galle Road, Bambalapitiya, Colombo 04",
                            City = "Colombo",
                            Province = "Western"
                        },
                        Type = ChargingStationType.DC,
                        TotalSlots = 8,
                        ChargingRate = 100.0,
                        Description = "High capacity charging station at Bambalapitiya Junction",
                        Amenities = new List<string> { "Shopping Mall", "Restaurants", "24/7 Security" },
                        OperatorId = ""
                    },
                    new CreateChargingStationDto
                    {
                        Name = "Nugegoda Town - AC Charging",
                        Location = new LocationDto
                        {
                            Latitude = 6.8714,
                            Longitude = 79.8883,
                            Address = "High Level Road, Nugegoda",
                            City = "Nugegoda",
                            Province = "Western"
                        },
                        Type = ChargingStationType.AC,
                        TotalSlots = 5,
                        ChargingRate = 40.0,
                        Description = "Convenient charging station in Nugegoda town center",
                        Amenities = new List<string> { "Public Transport Access", "Banks nearby", "Shopping" },
                        OperatorId = ""
                    }
                };

                foreach (var station in sampleStations)
                {
                    await _chargingStationService.CreateStationAsync(station);
                }

                _logger.LogInformation($"Created {sampleStations.Count} sample charging stations");
            }
        }
    }
}