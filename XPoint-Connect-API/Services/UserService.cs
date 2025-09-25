using MongoDB.Driver;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Models;

namespace XPoint_Connect_API.Services
{
    public interface IUserService
    {
        Task<UserResponseDto?> CreateUserAsync(CreateUserDto createUserDto);
        Task<UserResponseDto?> GetUserByIdAsync(string id);
        Task<UserResponseDto?> GetUserByUsernameAsync(string username);
        Task<User?> GetUserModelByUsernameAsync(string username); // Add this method for authentication
        Task<List<UserResponseDto>> GetAllUsersAsync();
        Task<UserResponseDto?> UpdateUserAsync(string id, UpdateUserDto updateUserDto);
        Task<bool> DeleteUserAsync(string id);
        Task<LoginResponseDto?> AuthenticateAsync(LoginRequestDto loginRequest);
    }

    public class UserService : IUserService
    {
        private readonly IMongoDbContext _context;
        private readonly IPasswordHashingService _passwordHashingService;
        private readonly IJwtService _jwtService;

        public UserService(
            IMongoDbContext context,
            IPasswordHashingService passwordHashingService,
            IJwtService jwtService)
        {
            _context = context;
            _passwordHashingService = passwordHashingService;
            _jwtService = jwtService;
        }

        public async Task<UserResponseDto?> CreateUserAsync(CreateUserDto createUserDto)
        {
            // Check if username already exists
            var existingUser = await _context.Users
                .Find(u => u.Username == createUserDto.Username)
                .FirstOrDefaultAsync();

            if (existingUser != null)
                return null;

            var user = new User
            {
                Username = createUserDto.Username,
                Email = createUserDto.Email,
                PasswordHash = _passwordHashingService.HashPassword(createUserDto.Password),
                Role = createUserDto.Role,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await _context.Users.InsertOneAsync(user);

            return MapToResponseDto(user);
        }

        public async Task<UserResponseDto?> GetUserByIdAsync(string id)
        {
            var user = await _context.Users
                .Find(u => u.Id == id)
                .FirstOrDefaultAsync();

            return user != null ? MapToResponseDto(user) : null;
        }

        public async Task<UserResponseDto?> GetUserByUsernameAsync(string username)
        {
            var user = await _context.Users
                .Find(u => u.Username == username)
                .FirstOrDefaultAsync();

            return user != null ? MapToResponseDto(user) : null;
        }

        public async Task<User?> GetUserModelByUsernameAsync(string username)
        {
            return await _context.Users
                .Find(u => u.Username == username)
                .FirstOrDefaultAsync();
        }

        public async Task<List<UserResponseDto>> GetAllUsersAsync()
        {
            var users = await _context.Users
                .Find(_ => true)
                .ToListAsync();

            return users.Select(MapToResponseDto).ToList();
        }

        public async Task<UserResponseDto?> UpdateUserAsync(string id, UpdateUserDto updateUserDto)
        {
            var filter = Builders<User>.Filter.Eq(u => u.Id, id);
            var updateDefinition = Builders<User>.Update.Set(u => u.UpdatedAt, DateTime.UtcNow);

            if (!string.IsNullOrEmpty(updateUserDto.Username))
                updateDefinition = updateDefinition.Set(u => u.Username, updateUserDto.Username);

            if (!string.IsNullOrEmpty(updateUserDto.Email))
                updateDefinition = updateDefinition.Set(u => u.Email, updateUserDto.Email);

            if (!string.IsNullOrEmpty(updateUserDto.Password))
                updateDefinition = updateDefinition.Set(u => u.PasswordHash, _passwordHashingService.HashPassword(updateUserDto.Password));

            if (updateUserDto.IsActive.HasValue)
                updateDefinition = updateDefinition.Set(u => u.IsActive, updateUserDto.IsActive.Value);

            var result = await _context.Users.FindOneAndUpdateAsync(
                filter,
                updateDefinition,
                new FindOneAndUpdateOptions<User> { ReturnDocument = ReturnDocument.After }
            );

            return result != null ? MapToResponseDto(result) : null;
        }

        public async Task<bool> DeleteUserAsync(string id)
        {
            var result = await _context.Users.DeleteOneAsync(u => u.Id == id);
            return result.DeletedCount > 0;
        }

        public async Task<LoginResponseDto?> AuthenticateAsync(LoginRequestDto loginRequest)
        {
            var user = await _context.Users
                .Find(u => u.Username == loginRequest.Username && u.IsActive)
                .FirstOrDefaultAsync();

            if (user == null || !_passwordHashingService.VerifyPassword(loginRequest.Password, user.PasswordHash))
                return null;

            var token = _jwtService.GenerateToken(user);
            var expiresAt = DateTime.UtcNow.AddHours(24); // Should match JWT settings

            return new LoginResponseDto
            {
                Token = token,
                UserId = user.Id ?? string.Empty,
                Username = user.Username,
                Role = user.Role,
                ExpiresAt = expiresAt
            };
        }

        private static UserResponseDto MapToResponseDto(User user)
        {
            return new UserResponseDto
            {
                Id = user.Id ?? string.Empty,
                Username = user.Username,
                Email = user.Email,
                Role = user.Role,
                IsActive = user.IsActive,
                CreatedAt = user.CreatedAt,
                UpdatedAt = user.UpdatedAt
            };
        }
    }
}