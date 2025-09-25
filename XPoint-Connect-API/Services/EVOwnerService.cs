using MongoDB.Driver;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Models;

namespace XPoint_Connect_API.Services
{
    public interface IEVOwnerService
    {
        Task<EVOwnerResponseDto?> CreateEVOwnerAsync(CreateEVOwnerDto createEVOwnerDto);
        Task<EVOwnerResponseDto?> GetEVOwnerByNICAsync(string nic);
        Task<EVOwner?> GetEVOwnerModelByNICAsync(string nic); // Add this method for authentication
        Task<List<EVOwnerResponseDto>> GetAllEVOwnersAsync();
        Task<EVOwnerResponseDto?> UpdateEVOwnerAsync(string nic, UpdateEVOwnerDto updateEVOwnerDto);
        Task<bool> DeleteEVOwnerAsync(string nic);
        Task<EVOwnerLoginResponseDto?> AuthenticateAsync(EVOwnerLoginDto loginRequest);
        Task<bool> ActivateEVOwnerAsync(string nic);
        Task<bool> DeactivateEVOwnerAsync(string nic);
        Task<List<EVOwnerResponseDto>> GetEVOwnersRequiringReactivationAsync();
    }

    public class EVOwnerService : IEVOwnerService
    {
        private readonly IMongoDbContext _context;
        private readonly IPasswordHashingService _passwordHashingService;
        private readonly IJwtService _jwtService;

        public EVOwnerService(
            IMongoDbContext context,
            IPasswordHashingService passwordHashingService,
            IJwtService jwtService)
        {
            _context = context;
            _passwordHashingService = passwordHashingService;
            _jwtService = jwtService;
        }

        public async Task<EVOwnerResponseDto?> CreateEVOwnerAsync(CreateEVOwnerDto createEVOwnerDto)
        {
            // Check if NIC already exists
            var existingOwner = await _context.EVOwners
                .Find(o => o.NIC == createEVOwnerDto.NIC)
                .FirstOrDefaultAsync();

            if (existingOwner != null)
                return null;

            var evOwner = new EVOwner
            {
                NIC = createEVOwnerDto.NIC,
                FirstName = createEVOwnerDto.FirstName,
                LastName = createEVOwnerDto.LastName,
                Email = createEVOwnerDto.Email,
                PhoneNumber = createEVOwnerDto.PhoneNumber,
                Address = createEVOwnerDto.Address,
                PasswordHash = _passwordHashingService.HashPassword(createEVOwnerDto.Password),
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await _context.EVOwners.InsertOneAsync(evOwner);

            return MapToResponseDto(evOwner);
        }

        public async Task<EVOwnerResponseDto?> GetEVOwnerByNICAsync(string nic)
        {
            var evOwner = await _context.EVOwners
                .Find(o => o.NIC == nic)
                .FirstOrDefaultAsync();

            return evOwner != null ? MapToResponseDto(evOwner) : null;
        }

        public async Task<EVOwner?> GetEVOwnerModelByNICAsync(string nic)
        {
            return await _context.EVOwners
                .Find(o => o.NIC == nic)
                .FirstOrDefaultAsync();
        }

        public async Task<List<EVOwnerResponseDto>> GetAllEVOwnersAsync()
        {
            var evOwners = await _context.EVOwners
                .Find(_ => true)
                .ToListAsync();

            return evOwners.Select(MapToResponseDto).ToList();
        }

        public async Task<EVOwnerResponseDto?> UpdateEVOwnerAsync(string nic, UpdateEVOwnerDto updateEVOwnerDto)
        {
            var filter = Builders<EVOwner>.Filter.Eq(o => o.NIC, nic);
            var updateDefinition = Builders<EVOwner>.Update.Set(o => o.UpdatedAt, DateTime.UtcNow);

            if (!string.IsNullOrEmpty(updateEVOwnerDto.FirstName))
                updateDefinition = updateDefinition.Set(o => o.FirstName, updateEVOwnerDto.FirstName);

            if (!string.IsNullOrEmpty(updateEVOwnerDto.LastName))
                updateDefinition = updateDefinition.Set(o => o.LastName, updateEVOwnerDto.LastName);

            if (!string.IsNullOrEmpty(updateEVOwnerDto.Email))
                updateDefinition = updateDefinition.Set(o => o.Email, updateEVOwnerDto.Email);

            if (!string.IsNullOrEmpty(updateEVOwnerDto.PhoneNumber))
                updateDefinition = updateDefinition.Set(o => o.PhoneNumber, updateEVOwnerDto.PhoneNumber);

            if (!string.IsNullOrEmpty(updateEVOwnerDto.Address))
                updateDefinition = updateDefinition.Set(o => o.Address, updateEVOwnerDto.Address);

            if (!string.IsNullOrEmpty(updateEVOwnerDto.Password))
                updateDefinition = updateDefinition.Set(o => o.PasswordHash, _passwordHashingService.HashPassword(updateEVOwnerDto.Password));

            if (updateEVOwnerDto.IsActive.HasValue)
                updateDefinition = updateDefinition.Set(o => o.IsActive, updateEVOwnerDto.IsActive.Value);

            if (updateEVOwnerDto.RequiresReactivation.HasValue)
                updateDefinition = updateDefinition.Set(o => o.RequiresReactivation, updateEVOwnerDto.RequiresReactivation.Value);

            var result = await _context.EVOwners.FindOneAndUpdateAsync(
                filter,
                updateDefinition,
                new FindOneAndUpdateOptions<EVOwner> { ReturnDocument = ReturnDocument.After }
            );

            return result != null ? MapToResponseDto(result) : null;
        }

        public async Task<bool> DeleteEVOwnerAsync(string nic)
        {
            var result = await _context.EVOwners.DeleteOneAsync(o => o.NIC == nic);
            return result.DeletedCount > 0;
        }

        public async Task<EVOwnerLoginResponseDto?> AuthenticateAsync(EVOwnerLoginDto loginRequest)
        {
            var evOwner = await _context.EVOwners
                .Find(o => o.NIC == loginRequest.NIC && o.IsActive && !o.RequiresReactivation)
                .FirstOrDefaultAsync();

            if (evOwner == null || !_passwordHashingService.VerifyPassword(loginRequest.Password, evOwner.PasswordHash))
                return null;

            var token = _jwtService.GenerateEVOwnerToken(evOwner);
            var expiresAt = DateTime.UtcNow.AddHours(24); // Should match JWT settings

            return new EVOwnerLoginResponseDto
            {
                Token = token,
                NIC = evOwner.NIC,
                FullName = $"{evOwner.FirstName} {evOwner.LastName}",
                ExpiresAt = expiresAt
            };
        }

        public async Task<bool> ActivateEVOwnerAsync(string nic)
        {
            var filter = Builders<EVOwner>.Filter.Eq(o => o.NIC, nic);
            var update = Builders<EVOwner>.Update
                .Set(o => o.IsActive, true)
                .Set(o => o.RequiresReactivation, false)
                .Set(o => o.UpdatedAt, DateTime.UtcNow);

            var result = await _context.EVOwners.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<bool> DeactivateEVOwnerAsync(string nic)
        {
            var filter = Builders<EVOwner>.Filter.Eq(o => o.NIC, nic);
            var update = Builders<EVOwner>.Update
                .Set(o => o.IsActive, false)
                .Set(o => o.RequiresReactivation, true)
                .Set(o => o.UpdatedAt, DateTime.UtcNow);

            var result = await _context.EVOwners.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<List<EVOwnerResponseDto>> GetEVOwnersRequiringReactivationAsync()
        {
            var evOwners = await _context.EVOwners
                .Find(o => o.RequiresReactivation)
                .ToListAsync();

            return evOwners.Select(MapToResponseDto).ToList();
        }

        private static EVOwnerResponseDto MapToResponseDto(EVOwner evOwner)
        {
            return new EVOwnerResponseDto
            {
                NIC = evOwner.NIC,
                FirstName = evOwner.FirstName,
                LastName = evOwner.LastName,
                Email = evOwner.Email,
                PhoneNumber = evOwner.PhoneNumber,
                Address = evOwner.Address,
                IsActive = evOwner.IsActive,
                CreatedAt = evOwner.CreatedAt,
                UpdatedAt = evOwner.UpdatedAt,
                RequiresReactivation = evOwner.RequiresReactivation
            };
        }
    }
}