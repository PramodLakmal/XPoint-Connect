using System.ComponentModel.DataAnnotations;
using XPoint_Connect_API.Models;

namespace XPoint_Connect_API.DTOs
{
    // Charging Station DTOs
    public class CreateChargingStationDto
    {
        [Required]
        public string Name { get; set; } = string.Empty;

        [Required]
        public LocationDto Location { get; set; } = new();

        [Required]
        public ChargingStationType Type { get; set; }

        [Required]
        [Range(1, 100)]
        public int TotalSlots { get; set; }

        public List<TimeSlotDto> Schedule { get; set; } = new();

        public string OperatorId { get; set; } = string.Empty;

        [Required]
        [Range(0.01, 1000)]
        public double ChargingRate { get; set; }

        public string Description { get; set; } = string.Empty;

        public List<string> Amenities { get; set; } = new();
    }

    public class UpdateChargingStationDto
    {
        public string? Name { get; set; }
        public LocationDto? Location { get; set; }
        public ChargingStationType? Type { get; set; }
        public int? TotalSlots { get; set; }
        public int? AvailableSlots { get; set; }
        public List<TimeSlotDto>? Schedule { get; set; }
        public bool? IsActive { get; set; }
        public string? OperatorId { get; set; }
        public double? ChargingRate { get; set; }
        public string? Description { get; set; }
        public List<string>? Amenities { get; set; }
    }

    public class ChargingStationResponseDto
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public LocationDto Location { get; set; } = new();
        public ChargingStationType Type { get; set; }
        public int TotalSlots { get; set; }
        public int AvailableSlots { get; set; }
        public List<TimeSlotDto> Schedule { get; set; } = new();
        public bool IsActive { get; set; }
        public string OperatorId { get; set; } = string.Empty;
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
        public double ChargingRate { get; set; }
        public string Description { get; set; } = string.Empty;
        public List<string> Amenities { get; set; } = new();
        public double? Distance { get; set; } // For nearby searches
    }

    public class LocationDto
    {
        [Required]
        [Range(-90, 90)]
        public double Latitude { get; set; }

        [Required]
        [Range(-180, 180)]
        public double Longitude { get; set; }

        [Required]
        public string Address { get; set; } = string.Empty;

        public string City { get; set; } = string.Empty;

        public string Province { get; set; } = string.Empty;
    }

    public class TimeSlotDto
    {
        [Required]
        public DateTime StartTime { get; set; }

        [Required]
        public DateTime EndTime { get; set; }

        [Required]
        [Range(0, 100)]
        public int AvailableSlots { get; set; }
    }

    public class NearbyStationsRequestDto
    {
        [Required]
        [Range(-90, 90)]
        public double Latitude { get; set; }

        [Required]
        [Range(-180, 180)]
        public double Longitude { get; set; }

        [Range(1, 100)]
        public double RadiusKm { get; set; } = 10; // Default 10km radius
    }
}