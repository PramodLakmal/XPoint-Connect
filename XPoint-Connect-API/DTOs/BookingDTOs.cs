using System.ComponentModel.DataAnnotations;
using XPoint_Connect_API.Models;

namespace XPoint_Connect_API.DTOs
{
    // Booking DTOs
    public class CreateBookingDto
    {
        [Required]
        public string EVOwnerNIC { get; set; } = string.Empty;

        [Required]
        public string ChargingStationId { get; set; } = string.Empty;

        [Required]
        public DateTime ReservationDateTime { get; set; }

        [Range(15, 480)] // 15 minutes to 8 hours
        public int DurationMinutes { get; set; } = 60;
    }

    public class UpdateBookingDto
    {
        public DateTime? ReservationDateTime { get; set; }
        public int? DurationMinutes { get; set; }
        public BookingStatus? Status { get; set; }
        public string? CancellationReason { get; set; }
        public string? OperatorNotes { get; set; }
    }

    public class BookingResponseDto
    {
        public string Id { get; set; } = string.Empty;
        public string EVOwnerNIC { get; set; } = string.Empty;
        public string EVOwnerName { get; set; } = string.Empty;
        public string ChargingStationId { get; set; } = string.Empty;
        public string ChargingStationName { get; set; } = string.Empty;
        public DateTime ReservationDateTime { get; set; }
        public DateTime BookingDate { get; set; }
        public int DurationMinutes { get; set; }
        public BookingStatus Status { get; set; }
        public double TotalAmount { get; set; }
        public string QRCode { get; set; } = string.Empty;
        public DateTime? CheckInTime { get; set; }
        public DateTime? CheckOutTime { get; set; }
        public string? CancellationReason { get; set; }
        public DateTime? CancelledAt { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
        public string? OperatorNotes { get; set; }
    }

    public class QRCodeScanDto
    {
        [Required]
        public string QRCode { get; set; } = string.Empty;
    }

    public class CheckInDto
    {
        [Required]
        public string BookingId { get; set; } = string.Empty;
        
        public string? OperatorNotes { get; set; }
    }

    public class CheckOutDto
    {
        [Required]
        public string BookingId { get; set; } = string.Empty;
        
        public string? OperatorNotes { get; set; }
    }

    public class BookingStatsDto
    {
        public int PendingBookings { get; set; }
        public int ApprovedBookings { get; set; }
        public int CompletedBookings { get; set; }
        public int CancelledBookings { get; set; }
        public double TotalRevenue { get; set; }
    }

    public class DashboardStatsDto
    {
        public int PendingReservations { get; set; }
        public int ApprovedFutureReservations { get; set; }
        public int CompletedBookingsThisMonth { get; set; }
        public double TotalSpentThisMonth { get; set; }
        public List<ChargingStationResponseDto> NearbyStations { get; set; } = new();
    }
}