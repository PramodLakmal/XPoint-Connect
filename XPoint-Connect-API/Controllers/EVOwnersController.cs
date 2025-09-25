using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Services;

namespace XPoint_Connect_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class EVOwnersController : ControllerBase
    {
        private readonly IEVOwnerService _evOwnerService;

        public EVOwnersController(IEVOwnerService evOwnerService)
        {
            _evOwnerService = evOwnerService;
        }

        [HttpGet]
        [Authorize(Roles = "BackOffice")]
        public async Task<ActionResult<List<EVOwnerResponseDto>>> GetAllEVOwners()
        {
            var evOwners = await _evOwnerService.GetAllEVOwnersAsync();
            return Ok(evOwners);
        }

        [HttpGet("{nic}")]
        [Authorize]
        public async Task<ActionResult<EVOwnerResponseDto>> GetEVOwner(string nic)
        {
            // EV Owners can only access their own data
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (currentUserNIC != nic)
                    return Forbid();
            }

            var evOwner = await _evOwnerService.GetEVOwnerByNICAsync(nic);
            
            if (evOwner == null)
                return NotFound();

            return Ok(evOwner);
        }

        [HttpPost]
        public async Task<ActionResult<EVOwnerResponseDto>> CreateEVOwner([FromBody] CreateEVOwnerDto createEVOwnerDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _evOwnerService.CreateEVOwnerAsync(createEVOwnerDto);
            
            if (result == null)
                return Conflict("EV Owner with this NIC already exists");

            return CreatedAtAction(nameof(GetEVOwner), new { nic = result.NIC }, result);
        }

        [HttpPut("{nic}")]
        [Authorize]
        public async Task<ActionResult<EVOwnerResponseDto>> UpdateEVOwner(string nic, [FromBody] UpdateEVOwnerDto updateEVOwnerDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // EV Owners can only update their own data
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (currentUserNIC != nic)
                    return Forbid();

                // EV Owners cannot change their own active status or reactivation requirement
                updateEVOwnerDto.IsActive = null;
                updateEVOwnerDto.RequiresReactivation = null;
            }

            var result = await _evOwnerService.UpdateEVOwnerAsync(nic, updateEVOwnerDto);
            
            if (result == null)
                return NotFound();

            return Ok(result);
        }

        [HttpDelete("{nic}")]
        [Authorize(Roles = "BackOffice")]
        public async Task<IActionResult> DeleteEVOwner(string nic)
        {
            var success = await _evOwnerService.DeleteEVOwnerAsync(nic);
            
            if (!success)
                return NotFound();

            return NoContent();
        }

        [HttpPost("{nic}/activate")]
        [Authorize(Roles = "BackOffice")]
        public async Task<IActionResult> ActivateEVOwner(string nic)
        {
            var success = await _evOwnerService.ActivateEVOwnerAsync(nic);
            
            if (!success)
                return NotFound();

            return NoContent();
        }

        [HttpPost("{nic}/deactivate")]
        [Authorize]
        public async Task<IActionResult> DeactivateEVOwner(string nic)
        {
            // EV Owners can deactivate their own account
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (currentUserNIC != nic)
                    return Forbid();
            }

            var success = await _evOwnerService.DeactivateEVOwnerAsync(nic);
            
            if (!success)
                return NotFound();

            return NoContent();
        }

        [HttpGet("reactivation-requests")]
        [Authorize(Roles = "BackOffice")]
        public async Task<ActionResult<List<EVOwnerResponseDto>>> GetReactivationRequests()
        {
            var evOwners = await _evOwnerService.GetEVOwnersRequiringReactivationAsync();
            return Ok(evOwners);
        }
    }
}