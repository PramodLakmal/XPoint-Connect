using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.ComponentModel.DataAnnotations;

namespace XPoint_Connect_API.Models
{
    public class Booking
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [Required]
        public string EVOwnerNIC { get; set; } = string.Empty;

        [Required]
        public string ChargingStationId { get; set; } = string.Empty;

        [Required]
        public DateTime ReservationDateTime { get; set; }

        [Required]
        public DateTime BookingDate { get; set; } = DateTime.UtcNow;

        public int DurationMinutes { get; set; } = 60; 

        public BookingStatus Status { get; set; } = BookingStatus.Pending;

        public double TotalAmount { get; set; }

        public string QRCode { get; set; } = string.Empty;

        public DateTime? CheckInTime { get; set; }

        public DateTime? CheckOutTime { get; set; }

        public string? CancellationReason { get; set; }

        public DateTime? CancelledAt { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        public string? OperatorNotes { get; set; }
    }

    public enum BookingStatus
    {
        Pending,
        Approved,
        CheckedIn,
        Completed,
        Cancelled,
        NoShow
    }
}