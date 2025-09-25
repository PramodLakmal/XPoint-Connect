using MongoDB.Driver;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Models;

namespace XPoint_Connect_API.Services
{
    public interface IChargingStationService
    {
        Task<ChargingStationResponseDto?> CreateStationAsync(CreateChargingStationDto createStationDto);
        Task<ChargingStationResponseDto?> GetStationByIdAsync(string id);
        Task<List<ChargingStationResponseDto>> GetAllStationsAsync();
        Task<List<ChargingStationResponseDto>> GetActiveStationsAsync();
        Task<ChargingStationResponseDto?> UpdateStationAsync(string id, UpdateChargingStationDto updateStationDto);
        Task<bool> DeleteStationAsync(string id);
        Task<bool> DeactivateStationAsync(string id);
        Task<List<ChargingStationResponseDto>> GetNearbyStationsAsync(double latitude, double longitude, double radiusKm);
        Task<List<ChargingStationResponseDto>> GetStationsByOperatorAsync(string operatorId);
        Task<bool> UpdateStationScheduleAsync(string id, List<TimeSlotDto> schedule);
        Task<bool> HasActiveBookingsAsync(string stationId);
    }

    public class ChargingStationService : IChargingStationService
    {
        private readonly IMongoDbContext _context;

        public ChargingStationService(IMongoDbContext context)
        {
            _context = context;
        }

        public async Task<ChargingStationResponseDto?> CreateStationAsync(CreateChargingStationDto createStationDto)
        {
            var station = new ChargingStation
            {
                Name = createStationDto.Name,
                Location = MapToLocation(createStationDto.Location),
                Type = createStationDto.Type,
                TotalSlots = createStationDto.TotalSlots,
                AvailableSlots = createStationDto.TotalSlots, // Initially all slots are available
                Schedule = createStationDto.Schedule.Select(MapToTimeSlot).ToList(),
                OperatorId = createStationDto.OperatorId,
                ChargingRate = createStationDto.ChargingRate,
                Description = createStationDto.Description,
                Amenities = createStationDto.Amenities,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await _context.ChargingStations.InsertOneAsync(station);

            return MapToResponseDto(station);
        }

        public async Task<ChargingStationResponseDto?> GetStationByIdAsync(string id)
        {
            var station = await _context.ChargingStations
                .Find(s => s.Id == id)
                .FirstOrDefaultAsync();

            return station != null ? MapToResponseDto(station) : null;
        }

        public async Task<List<ChargingStationResponseDto>> GetAllStationsAsync()
        {
            var stations = await _context.ChargingStations
                .Find(_ => true)
                .ToListAsync();

            return stations.Select(MapToResponseDto).ToList();
        }

        public async Task<List<ChargingStationResponseDto>> GetActiveStationsAsync()
        {
            var stations = await _context.ChargingStations
                .Find(s => s.IsActive)
                .ToListAsync();

            return stations.Select(MapToResponseDto).ToList();
        }

        public async Task<ChargingStationResponseDto?> UpdateStationAsync(string id, UpdateChargingStationDto updateStationDto)
        {
            var filter = Builders<ChargingStation>.Filter.Eq(s => s.Id, id);
            var updateDefinition = Builders<ChargingStation>.Update.Set(s => s.UpdatedAt, DateTime.UtcNow);

            if (!string.IsNullOrEmpty(updateStationDto.Name))
                updateDefinition = updateDefinition.Set(s => s.Name, updateStationDto.Name);

            if (updateStationDto.Location != null)
                updateDefinition = updateDefinition.Set(s => s.Location, MapToLocation(updateStationDto.Location));

            if (updateStationDto.Type.HasValue)
                updateDefinition = updateDefinition.Set(s => s.Type, updateStationDto.Type.Value);

            if (updateStationDto.TotalSlots.HasValue)
                updateDefinition = updateDefinition.Set(s => s.TotalSlots, updateStationDto.TotalSlots.Value);

            if (updateStationDto.AvailableSlots.HasValue)
                updateDefinition = updateDefinition.Set(s => s.AvailableSlots, updateStationDto.AvailableSlots.Value);

            if (updateStationDto.Schedule != null)
                updateDefinition = updateDefinition.Set(s => s.Schedule, updateStationDto.Schedule.Select(MapToTimeSlot).ToList());

            if (updateStationDto.IsActive.HasValue)
                updateDefinition = updateDefinition.Set(s => s.IsActive, updateStationDto.IsActive.Value);

            if (!string.IsNullOrEmpty(updateStationDto.OperatorId))
                updateDefinition = updateDefinition.Set(s => s.OperatorId, updateStationDto.OperatorId);

            if (updateStationDto.ChargingRate.HasValue)
                updateDefinition = updateDefinition.Set(s => s.ChargingRate, updateStationDto.ChargingRate.Value);

            if (!string.IsNullOrEmpty(updateStationDto.Description))
                updateDefinition = updateDefinition.Set(s => s.Description, updateStationDto.Description);

            if (updateStationDto.Amenities != null)
                updateDefinition = updateDefinition.Set(s => s.Amenities, updateStationDto.Amenities);

            var result = await _context.ChargingStations.FindOneAndUpdateAsync(
                filter,
                updateDefinition,
                new FindOneAndUpdateOptions<ChargingStation> { ReturnDocument = ReturnDocument.After }
            );

            return result != null ? MapToResponseDto(result) : null;
        }

        public async Task<bool> DeleteStationAsync(string id)
        {
            var result = await _context.ChargingStations.DeleteOneAsync(s => s.Id == id);
            return result.DeletedCount > 0;
        }

        public async Task<bool> DeactivateStationAsync(string id)
        {
            // Check if station has active bookings
            if (await HasActiveBookingsAsync(id))
                return false;

            var filter = Builders<ChargingStation>.Filter.Eq(s => s.Id, id);
            var update = Builders<ChargingStation>.Update
                .Set(s => s.IsActive, false)
                .Set(s => s.UpdatedAt, DateTime.UtcNow);

            var result = await _context.ChargingStations.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<List<ChargingStationResponseDto>> GetNearbyStationsAsync(double latitude, double longitude, double radiusKm)
        {
            var stations = await _context.ChargingStations
                .Find(s => s.IsActive)
                .ToListAsync();

            var nearbyStations = stations
                .Select(s => new { Station = s, Distance = CalculateDistance(latitude, longitude, s.Location.Latitude, s.Location.Longitude) })
                .Where(x => x.Distance <= radiusKm)
                .OrderBy(x => x.Distance)
                .Select(x =>
                {
                    var dto = MapToResponseDto(x.Station);
                    dto.Distance = Math.Round(x.Distance, 2);
                    return dto;
                })
                .ToList();

            return nearbyStations;
        }

        public async Task<List<ChargingStationResponseDto>> GetStationsByOperatorAsync(string operatorId)
        {
            var stations = await _context.ChargingStations
                .Find(s => s.OperatorId == operatorId)
                .ToListAsync();

            return stations.Select(MapToResponseDto).ToList();
        }

        public async Task<bool> UpdateStationScheduleAsync(string id, List<TimeSlotDto> schedule)
        {
            var filter = Builders<ChargingStation>.Filter.Eq(s => s.Id, id);
            var update = Builders<ChargingStation>.Update
                .Set(s => s.Schedule, schedule.Select(MapToTimeSlot).ToList())
                .Set(s => s.UpdatedAt, DateTime.UtcNow);

            var result = await _context.ChargingStations.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<bool> HasActiveBookingsAsync(string stationId)
        {
            var activeBookings = await _context.Bookings
                .Find(b => b.ChargingStationId == stationId && 
                          (b.Status == BookingStatus.Approved || b.Status == BookingStatus.CheckedIn))
                .AnyAsync();

            return activeBookings;
        }

        private static double CalculateDistance(double lat1, double lon1, double lat2, double lon2)
        {
            const double R = 6371; // Earth's radius in kilometers

            var dLat = ToRadians(lat2 - lat1);
            var dLon = ToRadians(lon2 - lon1);

            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(ToRadians(lat1)) * Math.Cos(ToRadians(lat2)) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);

            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));

            return R * c;
        }

        private static double ToRadians(double degrees)
        {
            return degrees * (Math.PI / 180);
        }

        private static Location MapToLocation(LocationDto locationDto)
        {
            return new Location
            {
                Latitude = locationDto.Latitude,
                Longitude = locationDto.Longitude,
                Address = locationDto.Address,
                City = locationDto.City,
                Province = locationDto.Province
            };
        }

        private static TimeSlot MapToTimeSlot(TimeSlotDto timeSlotDto)
        {
            return new TimeSlot
            {
                StartTime = timeSlotDto.StartTime,
                EndTime = timeSlotDto.EndTime,
                AvailableSlots = timeSlotDto.AvailableSlots
            };
        }

        private static ChargingStationResponseDto MapToResponseDto(ChargingStation station)
        {
            return new ChargingStationResponseDto
            {
                Id = station.Id ?? string.Empty,
                Name = station.Name,
                Location = new LocationDto
                {
                    Latitude = station.Location.Latitude,
                    Longitude = station.Location.Longitude,
                    Address = station.Location.Address,
                    City = station.Location.City,
                    Province = station.Location.Province
                },
                Type = station.Type,
                TotalSlots = station.TotalSlots,
                AvailableSlots = station.AvailableSlots,
                Schedule = station.Schedule.Select(ts => new TimeSlotDto
                {
                    StartTime = ts.StartTime,
                    EndTime = ts.EndTime,
                    AvailableSlots = ts.AvailableSlots
                }).ToList(),
                IsActive = station.IsActive,
                OperatorId = station.OperatorId,
                CreatedAt = station.CreatedAt,
                UpdatedAt = station.UpdatedAt,
                ChargingRate = station.ChargingRate,
                Description = station.Description,
                Amenities = station.Amenities
            };
        }
    }
}