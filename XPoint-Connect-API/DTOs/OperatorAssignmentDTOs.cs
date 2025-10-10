/*
 * OperatorAssignmentDTOs.cs
 * Data transfer objects for operator assignments
 */
using System.ComponentModel.DataAnnotations;
using Swashbuckle.AspNetCore.Annotations;

namespace XPoint_Connect_API.DTOs
{
    /// <summary>
    /// DTO for creating a new Station Operator with optional station assignments
    /// </summary>
    public class CreateStationOperatorDto
    {
        /// <summary>
        /// Unique username for the operator (required)
        /// </summary>
        [Required]
        [SwaggerSchema("Unique username for the operator")]
        public string Username { get; set; } = string.Empty;

        /// <summary>
        /// Email address for the operator (required)
        /// </summary>
        [Required]
        [EmailAddress]
        [SwaggerSchema("Valid email address for the operator")]
        public string Email { get; set; } = string.Empty;

        /// <summary>
        /// Password for the operator account (minimum 6 characters)
        /// </summary>
        [Required]
        [MinLength(6)]
        [SwaggerSchema("Password for the operator account (minimum 6 characters)")]
        public string Password { get; set; } = string.Empty;

        /// <summary>
        /// List of station IDs to assign to this operator during creation (optional)
        /// </summary>
        [SwaggerSchema("List of station IDs to assign to this operator during creation")]
        public List<string> AssignedStationIds { get; set; } = new();
    }

    /// <summary>
    /// DTO for assigning an operator to a specific station
    /// </summary>
    public class AssignOperatorToStationDto
    {
        /// <summary>
        /// ID of the operator to assign
        /// </summary>
        [Required]
        [SwaggerSchema("ID of the operator to assign")]
        public string OperatorId { get; set; } = string.Empty;

        /// <summary>
        /// ID of the station to assign the operator to
        /// </summary>
        [Required]
        [SwaggerSchema("ID of the station to assign the operator to")]
        public string StationId { get; set; } = string.Empty;
    }

    /// <summary>
    /// DTO for bulk assigning an operator to multiple stations
    /// </summary>
    public class BulkAssignOperatorDto
    {
        /// <summary>
        /// ID of the operator to assign
        /// </summary>
        [Required]
        [SwaggerSchema("ID of the operator to assign")]
        public string OperatorId { get; set; } = string.Empty;

        /// <summary>
        /// List of station IDs to assign to the operator
        /// </summary>
        [Required]
        [SwaggerSchema("List of station IDs to assign to the operator")]
        public List<string> StationIds { get; set; } = new();
    }

    /// <summary>
    /// Assignment information for operator and station
    /// </summary>
    public class OperatorAssignmentDto
    {
        /// <summary>
        /// ID of the assigned operator
        /// </summary>
        public string OperatorId { get; set; } = string.Empty;

        /// <summary>
        /// Username of the assigned operator
        /// </summary>
        public string OperatorUsername { get; set; } = string.Empty;

        /// <summary>
        /// Email of the assigned operator
        /// </summary>
        public string OperatorEmail { get; set; } = string.Empty;

        /// <summary>
        /// ID of the assigned station
        /// </summary>
        public string StationId { get; set; } = string.Empty;

        /// <summary>
        /// Name of the assigned station
        /// </summary>
        public string StationName { get; set; } = string.Empty;

        /// <summary>
        /// Location description of the assigned station
        /// </summary>
        public string StationLocation { get; set; } = string.Empty;

        /// <summary>
        /// When the assignment was created
        /// </summary>
        public DateTime AssignedAt { get; set; }
    }

    /// <summary>
    /// Operator information with assigned stations
    /// </summary>
    public class OperatorWithStationsDto
    {
        /// <summary>
        /// Operator's unique ID
        /// </summary>
        public string Id { get; set; } = string.Empty;

        /// <summary>
        /// Operator's username
        /// </summary>
        public string Username { get; set; } = string.Empty;

        /// <summary>
        /// Operator's email address
        /// </summary>
        public string Email { get; set; } = string.Empty;

        /// <summary>
        /// Whether the operator account is active
        /// </summary>
        public bool IsActive { get; set; }

        /// <summary>
        /// When the operator account was created
        /// </summary>
        public DateTime CreatedAt { get; set; }

        /// <summary>
        /// List of stations assigned to this operator
        /// </summary>
        public List<AssignedStationDto> AssignedStations { get; set; } = new();
    }

    /// <summary>
    /// Station information for assigned stations
    /// </summary>
    public class AssignedStationDto
    {
        /// <summary>
        /// Station's unique ID
        /// </summary>
        public string Id { get; set; } = string.Empty;

        /// <summary>
        /// Station name
        /// </summary>
        public string Name { get; set; } = string.Empty;

        /// <summary>
        /// Station address
        /// </summary>
        public string Address { get; set; } = string.Empty;

        /// <summary>
        /// City where the station is located
        /// </summary>
        public string City { get; set; } = string.Empty;

        /// <summary>
        /// Type of charging station (AC or DC)
        /// </summary>
        public string Type { get; set; } = string.Empty;

        /// <summary>
        /// Total number of charging slots
        /// </summary>
        public int TotalSlots { get; set; }

        /// <summary>
        /// Number of currently available slots
        /// </summary>
        public int AvailableSlots { get; set; }

        /// <summary>
        /// Whether the station is active
        /// </summary>
        public bool IsActive { get; set; }
    }

    /// <summary>
    /// Station information for unassigned stations
    /// </summary>
    public class UnassignedStationDto
    {
        /// <summary>
        /// Station's unique ID
        /// </summary>
        public string Id { get; set; } = string.Empty;

        /// <summary>
        /// Station name
        /// </summary>
        public string Name { get; set; } = string.Empty;

        /// <summary>
        /// Station address
        /// </summary>
        public string Address { get; set; } = string.Empty;

        /// <summary>
        /// City where the station is located
        /// </summary>
        public string City { get; set; } = string.Empty;

        /// <summary>
        /// Type of charging station (AC or DC)
        /// </summary>
        public string Type { get; set; } = string.Empty;

        /// <summary>
        /// Total number of charging slots
        /// </summary>
        public int TotalSlots { get; set; }

        /// <summary>
        /// Whether the station is active
        /// </summary>
        public bool IsActive { get; set; }
    }

    /// <summary>
    /// Comprehensive summary of station assignments
    /// </summary>
    public class StationAssignmentSummaryDto
    {
        /// <summary>
        /// Total number of stations in the system
        /// </summary>
        public int TotalStations { get; set; }

        /// <summary>
        /// Number of stations assigned to operators
        /// </summary>
        public int AssignedStations { get; set; }

        /// <summary>
        /// Number of stations not assigned to any operator
        /// </summary>
        public int UnassignedStations { get; set; }

        /// <summary>
        /// Total number of station operators
        /// </summary>
        public int TotalOperators { get; set; }

        /// <summary>
        /// Number of active station operators
        /// </summary>
        public int ActiveOperators { get; set; }

        /// <summary>
        /// List of unassigned stations
        /// </summary>
        public List<UnassignedStationDto> UnassignedStationsList { get; set; } = new();

        /// <summary>
        /// List of operators with their assigned stations
        /// </summary>
        public List<OperatorWithStationsDto> OperatorsWithStations { get; set; } = new();
    }
}
