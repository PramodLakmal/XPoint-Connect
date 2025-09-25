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
    public class BookingsController : ControllerBase
    {
        private readonly IBookingService _bookingService;

        public BookingsController(IBookingService bookingService)
        {
            _bookingService = bookingService;
        }

        [HttpGet]
        [Authorize(Roles = "BackOffice")]
        public async Task<ActionResult<List<BookingResponseDto>>> GetAllBookings()
        {
            var bookings = await _bookingService.GetAllBookingsAsync();
            return Ok(bookings);
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<BookingResponseDto>> GetBooking(string id)
        {
            var booking = await _bookingService.GetBookingByIdAsync(id);
            
            if (booking == null)
                return NotFound();

            // EV Owners can only access their own bookings
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (booking.EVOwnerNIC != currentUserNIC)
                    return Forbid();
            }

            return Ok(booking);
        }

        [HttpPost]
        public async Task<ActionResult<BookingResponseDto>> CreateBooking([FromBody] CreateBookingDto createBookingDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // For EV Owners, ensure they're booking for themselves
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (createBookingDto.EVOwnerNIC != currentUserNIC)
                    return Forbid();
            }

            var result = await _bookingService.CreateBookingAsync(createBookingDto);
            
            if (result == null)
                return BadRequest("Failed to create booking. Check reservation date and station availability.");

            return CreatedAtAction(nameof(GetBooking), new { id = result.Id }, result);
        }

        [HttpPut("{id}")]
        public async Task<ActionResult<BookingResponseDto>> UpdateBooking(string id, [FromBody] UpdateBookingDto updateBookingDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // Check permissions
            var booking = await _bookingService.GetBookingByIdAsync(id);
            if (booking == null)
                return NotFound();

            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (booking.EVOwnerNIC != currentUserNIC)
                    return Forbid();

                // EV Owners can only modify certain fields
                var restrictedUpdate = new UpdateBookingDto
                {
                    ReservationDateTime = updateBookingDto.ReservationDateTime,
                    DurationMinutes = updateBookingDto.DurationMinutes
                };

                updateBookingDto = restrictedUpdate;
            }

            var result = await _bookingService.UpdateBookingAsync(id, updateBookingDto);
            
            if (result == null)
                return BadRequest("Cannot modify booking. Check booking status and time restrictions.");

            return Ok(result);
        }

        [HttpPost("{id}/cancel")]
        public async Task<IActionResult> CancelBooking(string id, [FromBody] CancelBookingDto cancelDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // Check permissions
            var booking = await _bookingService.GetBookingByIdAsync(id);
            if (booking == null)
                return NotFound();

            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (booking.EVOwnerNIC != currentUserNIC)
                    return Forbid();
            }

            var success = await _bookingService.CancelBookingAsync(id, cancelDto.CancellationReason);
            
            if (!success)
                return BadRequest("Cannot cancel booking. Check booking status and time restrictions.");

            return NoContent();
        }

        [HttpPost("{id}/approve")]
        [Authorize(Roles = "BackOffice,StationOperator")]
        public async Task<IActionResult> ApproveBooking(string id)
        {
            var success = await _bookingService.ApproveBookingAsync(id);
            
            if (!success)
                return BadRequest("Cannot approve booking. Check booking status.");

            return NoContent();
        }

        [HttpPost("{id}/checkin")]
        [Authorize(Roles = "StationOperator")]
        public async Task<ActionResult<BookingResponseDto>> CheckInBooking(string id, [FromBody] CheckInDto checkInDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _bookingService.CheckInBookingAsync(id, checkInDto.OperatorNotes);
            
            if (result == null)
                return BadRequest("Cannot check in booking. Check booking status.");

            return Ok(result);
        }

        [HttpPost("{id}/checkout")]
        [Authorize(Roles = "StationOperator")]
        public async Task<ActionResult<BookingResponseDto>> CheckOutBooking(string id, [FromBody] CheckOutDto checkOutDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _bookingService.CheckOutBookingAsync(id, checkOutDto.OperatorNotes);
            
            if (result == null)
                return BadRequest("Cannot check out booking. Check booking status.");

            return Ok(result);
        }

        [HttpGet("evowner/{nic}")]
        public async Task<ActionResult<List<BookingResponseDto>>> GetBookingsByEVOwner(string nic)
        {
            // EV Owners can only access their own bookings
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (nic != currentUserNIC)
                    return Forbid();
            }

            var bookings = await _bookingService.GetBookingsByEVOwnerAsync(nic);
            return Ok(bookings);
        }

        [HttpGet("station/{stationId}")]
        [Authorize(Roles = "BackOffice,StationOperator")]
        public async Task<ActionResult<List<BookingResponseDto>>> GetBookingsByStation(string stationId)
        {
            var bookings = await _bookingService.GetBookingsByStationAsync(stationId);
            return Ok(bookings);
        }

        [HttpPost("scan-qr")]
        [Authorize(Roles = "StationOperator")]
        public async Task<ActionResult<BookingResponseDto>> ScanQRCode([FromBody] QRCodeScanDto scanDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var booking = await _bookingService.GetBookingByQRCodeAsync(scanDto.QRCode);
            
            if (booking == null)
                return NotFound("Invalid QR Code");

            return Ok(booking);
        }

        [HttpGet("dashboard/{nic}")]
        public async Task<ActionResult<DashboardStatsDto>> GetDashboardStats(string nic)
        {
            // EV Owners can only access their own dashboard
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (nic != currentUserNIC)
                    return Forbid();
            }

            var stats = await _bookingService.GetDashboardStatsAsync(nic);
            return Ok(stats);
        }

        [HttpGet("stats")]
        [Authorize(Roles = "BackOffice")]
        public async Task<ActionResult<BookingStatsDto>> GetBookingStats()
        {
            var stats = await _bookingService.GetBookingStatsAsync();
            return Ok(stats);
        }

        [HttpGet("upcoming/{nic}")]
        public async Task<ActionResult<List<BookingResponseDto>>> GetUpcomingBookings(string nic)
        {
            // EV Owners can only access their own bookings
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (nic != currentUserNIC)
                    return Forbid();
            }

            var bookings = await _bookingService.GetUpcomingBookingsAsync(nic);
            return Ok(bookings);
        }

        [HttpGet("history/{nic}")]
        public async Task<ActionResult<List<BookingResponseDto>>> GetBookingHistory(string nic)
        {
            // EV Owners can only access their own history
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (nic != currentUserNIC)
                    return Forbid();
            }

            var bookings = await _bookingService.GetBookingHistoryAsync(nic);
            return Ok(bookings);
        }

        /// <summary>
        /// Get booking summary before confirmation - useful for mobile app
        /// </summary>
        [HttpPost("preview")]
        public async Task<ActionResult<BookingPreviewDto>> PreviewBooking([FromBody] CreateBookingDto createBookingDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // For EV Owners, ensure they're previewing for themselves
            var userType = User.FindFirst("UserType")?.Value;
            if (userType == "EVOwner")
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (createBookingDto.EVOwnerNIC != currentUserNIC)
                    return Forbid();
            }

            // This is a preview, so we don't actually create the booking
            var result = await _bookingService.CreateBookingAsync(createBookingDto);
            
            if (result == null)
                return BadRequest("Invalid booking parameters. Check reservation date and station availability.");

            // Delete the preview booking immediately
            // In a real implementation, you might want to create a separate preview method
            // For now, we'll create a preview DTO
            
            var preview = new BookingPreviewDto
            {
                ChargingStationName = result.ChargingStationName,
                ReservationDateTime = result.ReservationDateTime,
                DurationMinutes = result.DurationMinutes,
                TotalAmount = result.TotalAmount,
                EstimatedEndTime = result.ReservationDateTime.AddMinutes(result.DurationMinutes)
            };

            return Ok(preview);
        }
    }

    public class CancelBookingDto
    {
        public string CancellationReason { get; set; } = string.Empty;
    }

    public class BookingPreviewDto
    {
        public string ChargingStationName { get; set; } = string.Empty;
        public DateTime ReservationDateTime { get; set; }
        public int DurationMinutes { get; set; }
        public double TotalAmount { get; set; }
        public DateTime EstimatedEndTime { get; set; }
    }
}