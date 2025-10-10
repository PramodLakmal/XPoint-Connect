/*
 * OperatorAssignmentService.cs
 * Service for operator assignment management
 */
using MongoDB.Driver;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Models;

namespace XPoint_Connect_API.Services
{
    public interface IOperatorAssignmentService
    {
        Task<UserResponseDto?> CreateStationOperatorAsync(CreateStationOperatorDto createOperatorDto);
        Task<bool> AssignOperatorToStationAsync(string operatorId, string stationId);
        Task<bool> BulkAssignOperatorToStationsAsync(string operatorId, List<string> stationIds);
        Task<bool> UnassignOperatorFromStationAsync(string stationId);
        Task<bool> UnassignOperatorFromAllStationsAsync(string operatorId);
        Task<List<OperatorWithStationsDto>> GetOperatorsWithStationsAsync();
        Task<List<UnassignedStationDto>> GetUnassignedStationsAsync();
        Task<StationAssignmentSummaryDto> GetAssignmentSummaryAsync();
        Task<List<AssignedStationDto>> GetOperatorStationsAsync(string operatorId);
    }

    public class OperatorAssignmentService : IOperatorAssignmentService
    {
        private readonly IMongoDbContext _context;
        private readonly IUserService _userService;
        private readonly IChargingStationService _chargingStationService;
        private readonly IPasswordHashingService _passwordHashingService;

        public OperatorAssignmentService(
            IMongoDbContext context,
            IUserService userService,
            IChargingStationService chargingStationService,
            IPasswordHashingService passwordHashingService)
        {
            _context = context;
            _userService = userService;
            _chargingStationService = chargingStationService;
            _passwordHashingService = passwordHashingService;
        }

        public async Task<UserResponseDto?> CreateStationOperatorAsync(CreateStationOperatorDto createOperatorDto)
        {
            // Check if username already exists
            var existingUser = await _context.Users
                .Find(u => u.Username == createOperatorDto.Username)
                .FirstOrDefaultAsync();

            if (existingUser != null)
                return null;

            // Create the operator user
            var user = new User
            {
                Username = createOperatorDto.Username,
                Email = createOperatorDto.Email,
                PasswordHash = _passwordHashingService.HashPassword(createOperatorDto.Password),
                Role = UserRole.StationOperator,
                IsActive = true,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await _context.Users.InsertOneAsync(user);

            // Assign stations if provided
            if (createOperatorDto.AssignedStationIds.Any())
            {
                await BulkAssignOperatorToStationsAsync(user.Id!, createOperatorDto.AssignedStationIds);
            }

            return new UserResponseDto
            {
                Id = user.Id!,
                Username = user.Username,
                Email = user.Email,
                Role = user.Role,
                IsActive = user.IsActive,
                CreatedAt = user.CreatedAt,
                UpdatedAt = user.UpdatedAt
            };
        }

        public async Task<bool> AssignOperatorToStationAsync(string operatorId, string stationId)
        {
            // Verify operator exists and is a StationOperator
            var operatorUser = await _context.Users
                .Find(u => u.Id == operatorId && u.Role == UserRole.StationOperator)
                .FirstOrDefaultAsync();

            if (operatorUser == null)
                return false;

            // Verify station exists
            var station = await _context.ChargingStations
                .Find(s => s.Id == stationId)
                .FirstOrDefaultAsync();

            if (station == null)
                return false;

            // Check if station is already assigned to another operator
            if (!string.IsNullOrEmpty(station.OperatorId) && station.OperatorId != operatorId)
            {
                // Unassign from previous operator first
                await UnassignOperatorFromStationAsync(stationId);
            }

            // Assign operator to station
            var filter = Builders<ChargingStation>.Filter.Eq(s => s.Id, stationId);
            var update = Builders<ChargingStation>.Update
                .Set(s => s.OperatorId, operatorId)
                .Set(s => s.UpdatedAt, DateTime.UtcNow);

            var result = await _context.ChargingStations.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<bool> BulkAssignOperatorToStationsAsync(string operatorId, List<string> stationIds)
        {
            // Verify operator exists and is a StationOperator
            var operatorUser = await _context.Users
                .Find(u => u.Id == operatorId && u.Role == UserRole.StationOperator)
                .FirstOrDefaultAsync();

            if (operatorUser == null)
                return false;

            var successCount = 0;
            foreach (var stationId in stationIds)
            {
                var success = await AssignOperatorToStationAsync(operatorId, stationId);
                if (success) successCount++;
            }

            return successCount > 0;
        }

        public async Task<bool> UnassignOperatorFromStationAsync(string stationId)
        {
            var filter = Builders<ChargingStation>.Filter.Eq(s => s.Id, stationId);
            var update = Builders<ChargingStation>.Update
                .Set(s => s.OperatorId, string.Empty)
                .Set(s => s.UpdatedAt, DateTime.UtcNow);

            var result = await _context.ChargingStations.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<bool> UnassignOperatorFromAllStationsAsync(string operatorId)
        {
            var filter = Builders<ChargingStation>.Filter.Eq(s => s.OperatorId, operatorId);
            var update = Builders<ChargingStation>.Update
                .Set(s => s.OperatorId, string.Empty)
                .Set(s => s.UpdatedAt, DateTime.UtcNow);

            var result = await _context.ChargingStations.UpdateManyAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<List<OperatorWithStationsDto>> GetOperatorsWithStationsAsync()
        {
            var operators = await _context.Users
                .Find(u => u.Role == UserRole.StationOperator)
                .ToListAsync();

            var result = new List<OperatorWithStationsDto>();

            foreach (var op in operators)
            {
                var assignedStations = await GetOperatorStationsAsync(op.Id!);
                
                result.Add(new OperatorWithStationsDto
                {
                    Id = op.Id!,
                    Username = op.Username,
                    Email = op.Email,
                    IsActive = op.IsActive,
                    CreatedAt = op.CreatedAt,
                    AssignedStations = assignedStations
                });
            }

            return result;
        }

        public async Task<List<UnassignedStationDto>> GetUnassignedStationsAsync()
        {
            var unassignedStations = await _context.ChargingStations
                .Find(s => string.IsNullOrEmpty(s.OperatorId))
                .ToListAsync();

            return unassignedStations.Select(s => new UnassignedStationDto
            {
                Id = s.Id!,
                Name = s.Name,
                Address = s.Location.Address,
                City = s.Location.City,
                Type = s.Type.ToString(),
                TotalSlots = s.TotalSlots,
                IsActive = s.IsActive
            }).ToList();
        }

        public async Task<StationAssignmentSummaryDto> GetAssignmentSummaryAsync()
        {
            var allStations = await _context.ChargingStations.Find(_ => true).ToListAsync();
            var allOperators = await _context.Users.Find(u => u.Role == UserRole.StationOperator).ToListAsync();

            var assignedStations = allStations.Where(s => !string.IsNullOrEmpty(s.OperatorId)).ToList();
            var unassignedStations = allStations.Where(s => string.IsNullOrEmpty(s.OperatorId)).ToList();

            var operatorsWithStations = await GetOperatorsWithStationsAsync();
            var unassignedStationsList = unassignedStations.Select(s => new UnassignedStationDto
            {
                Id = s.Id!,
                Name = s.Name,
                Address = s.Location.Address,
                City = s.Location.City,
                Type = s.Type.ToString(),
                TotalSlots = s.TotalSlots,
                IsActive = s.IsActive
            }).ToList();

            return new StationAssignmentSummaryDto
            {
                TotalStations = allStations.Count,
                AssignedStations = assignedStations.Count,
                UnassignedStations = unassignedStations.Count,
                TotalOperators = allOperators.Count,
                ActiveOperators = allOperators.Count(o => o.IsActive),
                UnassignedStationsList = unassignedStationsList,
                OperatorsWithStations = operatorsWithStations
            };
        }

        public async Task<List<AssignedStationDto>> GetOperatorStationsAsync(string operatorId)
        {
            var stations = await _context.ChargingStations
                .Find(s => s.OperatorId == operatorId)
                .ToListAsync();

            return stations.Select(s => new AssignedStationDto
            {
                Id = s.Id!,
                Name = s.Name,
                Address = s.Location.Address,
                City = s.Location.City,
                Type = s.Type.ToString(),
                TotalSlots = s.TotalSlots,
                AvailableSlots = s.AvailableSlots,
                IsActive = s.IsActive
            }).ToList();
        }
    }
}
