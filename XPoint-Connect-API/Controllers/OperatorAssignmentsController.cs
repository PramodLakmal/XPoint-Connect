/*
 * OperatorAssignmentsController.cs
 * Manages operator assignments to charging stations
 */
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.ComponentModel.DataAnnotations;
using Swashbuckle.AspNetCore.Annotations;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Services;

namespace XPoint_Connect_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "BackOffice")]
    [SwaggerTag("Manage station operator accounts and assign them to charging stations")]
    public class OperatorAssignmentsController : ControllerBase
    {
        private readonly IOperatorAssignmentService _operatorAssignmentService;

        public OperatorAssignmentsController(IOperatorAssignmentService operatorAssignmentService)
        {
            _operatorAssignmentService = operatorAssignmentService;
        }

        /// <summary>
        /// Create a new Station Operator and optionally assign them to stations
        /// </summary>
        /// <param name="createOperatorDto">Operator creation details with optional station assignments</param>
        /// <returns>Created operator details</returns>
        /// <response code="201">Operator created successfully</response>
        /// <response code="400">Invalid input data</response>
        /// <response code="401">Unauthorized - JWT token required</response>
        /// <response code="403">Forbidden - BackOffice role required</response>
        /// <response code="409">Username already exists</response>
        [HttpPost("operators")]
        [SwaggerOperation(
            Summary = "Create Station Operator",
            Description = "Creates a new Station Operator account with optional station assignments. Only BackOffice users can create operators.",
            OperationId = "CreateStationOperator"
        )]
        [SwaggerResponse(201, "Operator created successfully", typeof(UserResponseDto))]
        [SwaggerResponse(400, "Invalid input data")]
        [SwaggerResponse(401, "Unauthorized - JWT token required")]
        [SwaggerResponse(403, "Forbidden - BackOffice role required")]
        [SwaggerResponse(409, "Username already exists")]
        public async Task<ActionResult<UserResponseDto>> CreateStationOperator([FromBody] CreateStationOperatorDto createOperatorDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _operatorAssignmentService.CreateStationOperatorAsync(createOperatorDto);
            
            if (result == null)
                return Conflict("Username already exists");

            return CreatedAtAction(nameof(GetOperatorWithStations), new { operatorId = result.Id }, result);
        }

        /// <summary>
        /// Assign an operator to a specific station
        /// </summary>
        /// <param name="assignDto">Assignment details</param>
        /// <returns>Success message</returns>
        [HttpPost("assign")]
        [SwaggerOperation(
            Summary = "Assign Operator to Station",
            Description = "Assigns a station operator to a specific charging station. If the station is already assigned to another operator, it will be automatically reassigned.",
            OperationId = "AssignOperatorToStation"
        )]
        [SwaggerResponse(200, "Operator assigned successfully")]
        [SwaggerResponse(400, "Invalid data or assignment failed")]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<IActionResult> AssignOperatorToStation([FromBody] AssignOperatorToStationDto assignDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var success = await _operatorAssignmentService.AssignOperatorToStationAsync(assignDto.OperatorId, assignDto.StationId);
            
            if (!success)
                return BadRequest("Failed to assign operator to station. Check if operator and station exist.");

            return Ok(new { message = "Operator assigned to station successfully" });
        }

        /// <summary>
        /// Assign an operator to multiple stations
        /// </summary>
        /// <param name="bulkAssignDto">Bulk assignment details</param>
        /// <returns>Success message with count</returns>
        [HttpPost("bulk-assign")]
        [SwaggerOperation(
            Summary = "Bulk Assign Operator to Stations",
            Description = "Assigns a station operator to multiple charging stations simultaneously. Existing assignments will be updated.",
            OperationId = "BulkAssignOperatorToStations"
        )]
        [SwaggerResponse(200, "Stations assigned successfully")]
        [SwaggerResponse(400, "Invalid data or assignment failed")]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<IActionResult> BulkAssignOperatorToStations([FromBody] BulkAssignOperatorDto bulkAssignDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var success = await _operatorAssignmentService.BulkAssignOperatorToStationsAsync(bulkAssignDto.OperatorId, bulkAssignDto.StationIds);
            
            if (!success)
                return BadRequest("Failed to assign operator to stations. Check if operator exists.");

            return Ok(new { message = $"Operator assigned to {bulkAssignDto.StationIds.Count} stations" });
        }

        /// <summary>
        /// Unassign an operator from a specific station
        /// </summary>
        /// <param name="stationId">Station ID to unassign</param>
        /// <returns>Success message</returns>
        [HttpDelete("stations/{stationId}/operator")]
        [SwaggerOperation(
            Summary = "Unassign Operator from Station",
            Description = "Removes the operator assignment from a specific charging station, making it unassigned.",
            OperationId = "UnassignOperatorFromStation"
        )]
        [SwaggerResponse(200, "Operator unassigned successfully")]
        [SwaggerResponse(400, "Unassignment failed")]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<IActionResult> UnassignOperatorFromStation(string stationId)
        {
            var success = await _operatorAssignmentService.UnassignOperatorFromStationAsync(stationId);
            
            if (!success)
                return BadRequest("Failed to unassign operator from station");

            return Ok(new { message = "Operator unassigned from station successfully" });
        }

        /// <summary>
        /// Unassign an operator from all their stations
        /// </summary>
        /// <param name="operatorId">Operator ID to unassign from all stations</param>
        /// <returns>Success message</returns>
        [HttpDelete("operators/{operatorId}/stations")]
        [SwaggerOperation(
            Summary = "Unassign Operator from All Stations",
            Description = "Removes the operator from all assigned stations, making all their stations unassigned.",
            OperationId = "UnassignOperatorFromAllStations"
        )]
        [SwaggerResponse(200, "Operator unassigned from all stations")]
        [SwaggerResponse(400, "Unassignment failed")]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<IActionResult> UnassignOperatorFromAllStations(string operatorId)
        {
            var success = await _operatorAssignmentService.UnassignOperatorFromAllStationsAsync(operatorId);
            
            if (!success)
                return BadRequest("Failed to unassign operator from stations");

            return Ok(new { message = "Operator unassigned from all stations successfully" });
        }

        /// <summary>
        /// Get all operators with their assigned stations
        /// </summary>
        /// <returns>List of operators with their station assignments</returns>
        [HttpGet("operators")]
        [SwaggerOperation(
            Summary = "Get Operators with Stations",
            Description = "Retrieves all station operators with their assigned charging stations for management overview.",
            OperationId = "GetOperatorsWithStations"
        )]
        [SwaggerResponse(200, "Operators retrieved successfully", typeof(List<OperatorWithStationsDto>))]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<ActionResult<List<OperatorWithStationsDto>>> GetOperatorsWithStations()
        {
            var operators = await _operatorAssignmentService.GetOperatorsWithStationsAsync();
            return Ok(operators);
        }

        /// <summary>
        /// Get a specific operator with their assigned stations
        /// </summary>
        /// <param name="operatorId">Operator ID</param>
        /// <returns>Operator's assigned stations</returns>
        [HttpGet("operators/{operatorId}")]
        [SwaggerOperation(
            Summary = "Get Operator's Assigned Stations",
            Description = "Retrieves all stations assigned to a specific operator.",
            OperationId = "GetOperatorStations"
        )]
        [SwaggerResponse(200, "Operator stations retrieved successfully", typeof(List<AssignedStationDto>))]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<ActionResult<List<AssignedStationDto>>> GetOperatorWithStations(string operatorId)
        {
            var stations = await _operatorAssignmentService.GetOperatorStationsAsync(operatorId);
            return Ok(stations);
        }

        /// <summary>
        /// Get all unassigned stations
        /// </summary>
        /// <returns>List of stations not assigned to any operator</returns>
        [HttpGet("unassigned-stations")]
        [SwaggerOperation(
            Summary = "Get Unassigned Stations",
            Description = "Retrieves all charging stations that are not currently assigned to any operator.",
            OperationId = "GetUnassignedStations"
        )]
        [SwaggerResponse(200, "Unassigned stations retrieved successfully", typeof(List<UnassignedStationDto>))]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<ActionResult<List<UnassignedStationDto>>> GetUnassignedStations()
        {
            var stations = await _operatorAssignmentService.GetUnassignedStationsAsync();
            return Ok(stations);
        }

        /// <summary>
        /// Get assignment summary with statistics
        /// </summary>
        /// <returns>Comprehensive assignment summary</returns>
        [HttpGet("summary")]
        [SwaggerOperation(
            Summary = "Get Assignment Summary",
            Description = "Provides a comprehensive overview of station assignments including statistics and lists.",
            OperationId = "GetAssignmentSummary"
        )]
        [SwaggerResponse(200, "Assignment summary retrieved successfully", typeof(StationAssignmentSummaryDto))]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<ActionResult<StationAssignmentSummaryDto>> GetAssignmentSummary()
        {
            var summary = await _operatorAssignmentService.GetAssignmentSummaryAsync();
            return Ok(summary);
        }

        /// <summary>
        /// Reassign a station from one operator to another
        /// </summary>
        /// <param name="stationId">Station ID to reassign</param>
        /// <param name="reassignDto">New operator assignment</param>
        /// <returns>Success message</returns>
        [HttpPut("stations/{stationId}/reassign")]
        [SwaggerOperation(
            Summary = "Reassign Station to Different Operator",
            Description = "Transfers a station from its current operator to a new operator.",
            OperationId = "ReassignStation"
        )]
        [SwaggerResponse(200, "Station reassigned successfully")]
        [SwaggerResponse(400, "Reassignment failed")]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<IActionResult> ReassignStation(string stationId, [FromBody] ReassignStationDto reassignDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // First unassign current operator
            await _operatorAssignmentService.UnassignOperatorFromStationAsync(stationId);
            
            // Then assign to new operator
            var success = await _operatorAssignmentService.AssignOperatorToStationAsync(reassignDto.NewOperatorId, stationId);
            
            if (!success)
                return BadRequest("Failed to reassign station to new operator");

            return Ok(new { message = "Station reassigned successfully" });
        }

        /// <summary>
        /// Get assignment statistics for dashboard
        /// </summary>
        /// <returns>Assignment statistics and metrics</returns>
        [HttpGet("statistics")]
        [SwaggerOperation(
            Summary = "Get Assignment Statistics",
            Description = "Provides detailed statistics about station assignments for dashboard and analytics purposes.",
            OperationId = "GetAssignmentStatistics"
        )]
        [SwaggerResponse(200, "Assignment statistics retrieved successfully", typeof(AssignmentStatisticsDto))]
        [SwaggerResponse(401, "Unauthorized")]
        [SwaggerResponse(403, "Forbidden")]
        public async Task<ActionResult<AssignmentStatisticsDto>> GetAssignmentStatistics()
        {
            var summary = await _operatorAssignmentService.GetAssignmentSummaryAsync();
            
            var statistics = new AssignmentStatisticsDto
            {
                TotalStations = summary.TotalStations,
                AssignedStations = summary.AssignedStations,
                UnassignedStations = summary.UnassignedStations,
                TotalOperators = summary.TotalOperators,
                ActiveOperators = summary.ActiveOperators,
                AssignmentPercentage = summary.TotalStations > 0 ? (double)summary.AssignedStations / summary.TotalStations * 100 : 0,
                OperatorsWithoutStations = summary.OperatorsWithStations.Count(o => o.AssignedStations.Count == 0),
                AverageStationsPerOperator = summary.ActiveOperators > 0 ? (double)summary.AssignedStations / summary.ActiveOperators : 0
            };

            return Ok(statistics);
        }
    }

    /// <summary>
    /// DTO for reassigning a station to a different operator
    /// </summary>
    public class ReassignStationDto
    {
        /// <summary>
        /// ID of the new operator to assign the station to
        /// </summary>
        [Required]
        [SwaggerSchema("ID of the new operator to assign the station to")]
        public string NewOperatorId { get; set; } = string.Empty;
    }

    /// <summary>
    /// Assignment statistics for dashboard and analytics
    /// </summary>
    public class AssignmentStatisticsDto
    {
        /// <summary>
        /// Total number of charging stations in the system
        /// </summary>
        public int TotalStations { get; set; }

        /// <summary>
        /// Number of stations currently assigned to operators
        /// </summary>
        public int AssignedStations { get; set; }

        /// <summary>
        /// Number of stations not assigned to any operator
        /// </summary>
        public int UnassignedStations { get; set; }

        /// <summary>
        /// Total number of station operators in the system
        /// </summary>
        public int TotalOperators { get; set; }

        /// <summary>
        /// Number of active station operators
        /// </summary>
        public int ActiveOperators { get; set; }

        /// <summary>
        /// Percentage of stations that are assigned (0-100)
        /// </summary>
        public double AssignmentPercentage { get; set; }

        /// <summary>
        /// Number of operators who have no stations assigned
        /// </summary>
        public int OperatorsWithoutStations { get; set; }

        /// <summary>
        /// Average number of stations per active operator
        /// </summary>
        public double AverageStationsPerOperator { get; set; }
    }
}
