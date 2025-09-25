using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.ComponentModel.DataAnnotations;

namespace XPoint_Connect_API.Models
{
    public class ChargingStation
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [Required]
        public string Name { get; set; } = string.Empty;

        [Required]
        public Location Location { get; set; } = new();

        [Required]
        public ChargingStationType Type { get; set; }

        [Required]
        public int TotalSlots { get; set; }

        public int AvailableSlots { get; set; }

        public List<TimeSlot> Schedule { get; set; } = new();

        public bool IsActive { get; set; } = true;

        public string OperatorId { get; set; } = string.Empty; // Reference to Station Operator

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        public double ChargingRate { get; set; } // Per hour rate

        public string Description { get; set; } = string.Empty;

        public List<string> Amenities { get; set; } = new();
    }

    public class Location
    {
        [Required]
        public double Latitude { get; set; }

        [Required]
        public double Longitude { get; set; }

        [Required]
        public string Address { get; set; } = string.Empty;

        public string City { get; set; } = string.Empty;

        public string Province { get; set; } = string.Empty;
    }

    public class TimeSlot
    {
        public DateTime StartTime { get; set; }

        public DateTime EndTime { get; set; }

        public int AvailableSlots { get; set; }
    }

    public enum ChargingStationType
    {
        AC,
        DC
    }
}