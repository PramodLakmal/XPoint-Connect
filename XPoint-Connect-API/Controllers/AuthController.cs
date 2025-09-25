using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Services;

namespace XPoint_Connect_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly IUserService _userService;
        private readonly IEVOwnerService _evOwnerService;

        public AuthController(
            IUserService userService,
            IEVOwnerService evOwnerService)
        {
            _userService = userService;
            _evOwnerService = evOwnerService;
        }

        [HttpPost("login")]
        public async Task<ActionResult<LoginResponseDto>> Login([FromBody] LoginRequestDto loginRequest)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _userService.AuthenticateAsync(loginRequest);
            
            if (result == null)
                return Unauthorized("Invalid username or password");

            return Ok(result);
        }

        [HttpPost("evowner/login")]
        public async Task<ActionResult<EVOwnerLoginResponseDto>> EVOwnerLogin([FromBody] EVOwnerLoginDto loginRequest)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _evOwnerService.AuthenticateAsync(loginRequest);
            
            if (result == null)
                return Unauthorized("Invalid NIC or password");

            return Ok(result);
        }

        [HttpPost("evowner/register")]
        public async Task<ActionResult<EVOwnerResponseDto>> RegisterEVOwner([FromBody] CreateEVOwnerDto createEVOwnerDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _evOwnerService.CreateEVOwnerAsync(createEVOwnerDto);
            
            if (result == null)
                return Conflict("EV Owner with this NIC already exists");

            return CreatedAtAction(nameof(GetEVOwner), new { nic = result.NIC }, result);
        }

        [HttpGet("evowner/{nic}")]
        public async Task<ActionResult<EVOwnerResponseDto>> GetEVOwner(string nic)
        {
            var result = await _evOwnerService.GetEVOwnerByNICAsync(nic);
            
            if (result == null)
                return NotFound();

            return Ok(result);
        }

        [HttpPost("register")]
        [Authorize(Roles = "BackOffice")]
        public async Task<ActionResult<UserResponseDto>> RegisterUser([FromBody] CreateUserDto createUserDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _userService.CreateUserAsync(createUserDto);
            
            if (result == null)
                return Conflict("User with this username already exists");

            return CreatedAtAction(nameof(GetUser), new { id = result.Id }, result);
        }

        [HttpGet("user/{id}")]
        [Authorize(Roles = "BackOffice")]
        public async Task<ActionResult<UserResponseDto>> GetUser(string id)
        {
            var result = await _userService.GetUserByIdAsync(id);
            
            if (result == null)
                return NotFound();

            return Ok(result);
        }
    }
}