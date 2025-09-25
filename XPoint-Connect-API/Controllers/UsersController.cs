using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Services;

namespace XPoint_Connect_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "BackOffice")]
    public class UsersController : ControllerBase
    {
        private readonly IUserService _userService;

        public UsersController(IUserService userService)
        {
            _userService = userService;
        }

        [HttpGet]
        public async Task<ActionResult<List<UserResponseDto>>> GetAllUsers()
        {
            var users = await _userService.GetAllUsersAsync();
            return Ok(users);
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<UserResponseDto>> GetUser(string id)
        {
            var user = await _userService.GetUserByIdAsync(id);
            
            if (user == null)
                return NotFound();

            return Ok(user);
        }

        [HttpPost]
        public async Task<ActionResult<UserResponseDto>> CreateUser([FromBody] CreateUserDto createUserDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _userService.CreateUserAsync(createUserDto);
            
            if (result == null)
                return Conflict("User with this username already exists");

            return CreatedAtAction(nameof(GetUser), new { id = result.Id }, result);
        }

        [HttpPut("{id}")]
        public async Task<ActionResult<UserResponseDto>> UpdateUser(string id, [FromBody] UpdateUserDto updateUserDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _userService.UpdateUserAsync(id, updateUserDto);
            
            if (result == null)
                return NotFound();

            return Ok(result);
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteUser(string id)
        {
            var success = await _userService.DeleteUserAsync(id);
            
            if (!success)
                return NotFound();

            return NoContent();
        }
    }
}