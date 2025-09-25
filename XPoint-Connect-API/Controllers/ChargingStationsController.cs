using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Services;

namespace XPoint_Connect_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ChargingStationsController : ControllerBase
    {
        private readonly IChargingStationService _chargingStationService;

        public ChargingStationsController(IChargingStationService chargingStationService)
        {
            _chargingStationService = chargingStationService;
        }

        [HttpGet]
        [AllowAnonymous]
        public async Task<ActionResult<List<ChargingStationResponseDto>>> GetAllStations([FromQuery] bool activeOnly = true)
        {
            var stations = activeOnly 
                ? await _chargingStationService.GetActiveStationsAsync()
                : await _chargingStationService.GetAllStationsAsync();
            
            return Ok(stations);
        }

        [HttpGet("{id}")]
        [AllowAnonymous]
        public async Task<ActionResult<ChargingStationResponseDto>> GetStation(string id)
        {
            var station = await _chargingStationService.GetStationByIdAsync(id);
            
            if (station == null)
                return NotFound();

            return Ok(station);
        }

        [HttpPost]
        [Authorize(Roles = "BackOffice")]
        public async Task<ActionResult<ChargingStationResponseDto>> CreateStation([FromBody] CreateChargingStationDto createStationDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _chargingStationService.CreateStationAsync(createStationDto);
            
            if (result == null)
                return BadRequest("Failed to create charging station");

            return CreatedAtAction(nameof(GetStation), new { id = result.Id }, result);
        }

        [HttpPut("{id}")]
        [Authorize(Roles = "BackOffice,StationOperator")]
        public async Task<ActionResult<ChargingStationResponseDto>> UpdateStation(string id, [FromBody] UpdateChargingStationDto updateStationDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // Station operators can only update their own stations
            if (User.IsInRole("StationOperator"))
            {
                var currentUserId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var station = await _chargingStationService.GetStationByIdAsync(id);
                
                if (station == null)
                    return NotFound();

                if (station.OperatorId != currentUserId)
                    return Forbid();

                // Station operators can only update certain fields
                var restrictedUpdate = new UpdateChargingStationDto
                {
                    AvailableSlots = updateStationDto.AvailableSlots,
                    Schedule = updateStationDto.Schedule,
                    Description = updateStationDto.Description
                };

                updateStationDto = restrictedUpdate;
            }

            var result = await _chargingStationService.UpdateStationAsync(id, updateStationDto);
            
            if (result == null)
                return NotFound();

            return Ok(result);
        }

        [HttpDelete("{id}")]
        [Authorize(Roles = "BackOffice")]
        public async Task<IActionResult> DeleteStation(string id)
        {
            var success = await _chargingStationService.DeleteStationAsync(id);
            
            if (!success)
                return NotFound();

            return NoContent();
        }

        [HttpPost("{id}/deactivate")]
        [Authorize(Roles = "BackOffice")]
        public async Task<IActionResult> DeactivateStation(string id)
        {
            var success = await _chargingStationService.DeactivateStationAsync(id);
            
            if (!success)
                return BadRequest("Cannot deactivate station with active bookings");

            return NoContent();
        }

        [HttpPost("nearby")]
        [AllowAnonymous]
        public async Task<ActionResult<List<ChargingStationResponseDto>>> GetNearbyStations([FromBody] NearbyStationsRequestDto request)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var stations = await _chargingStationService.GetNearbyStationsAsync(
                request.Latitude, 
                request.Longitude, 
                request.RadiusKm);

            return Ok(stations);
        }

        [HttpGet("operator/{operatorId}")]
        [Authorize(Roles = "BackOffice,StationOperator")]
        public async Task<ActionResult<List<ChargingStationResponseDto>>> GetStationsByOperator(string operatorId)
        {
            // Station operators can only view their own stations
            if (User.IsInRole("StationOperator"))
            {
                var currentUserId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (operatorId != currentUserId)
                    return Forbid();
            }

            var stations = await _chargingStationService.GetStationsByOperatorAsync(operatorId);
            return Ok(stations);
        }

        [HttpPut("{id}/schedule")]
        [Authorize(Roles = "BackOffice,StationOperator")]
        public async Task<IActionResult> UpdateStationSchedule(string id, [FromBody] List<TimeSlotDto> schedule)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // Station operators can only update their own stations
            if (User.IsInRole("StationOperator"))
            {
                var currentUserId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var station = await _chargingStationService.GetStationByIdAsync(id);
                
                if (station == null)
                    return NotFound();

                if (station.OperatorId != currentUserId)
                    return Forbid();
            }

            var success = await _chargingStationService.UpdateStationScheduleAsync(id, schedule);
            
            if (!success)
                return NotFound();

            return NoContent();
        }
    }
}