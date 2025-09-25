using MongoDB.Driver;
using XPoint_Connect_API.DTOs;
using XPoint_Connect_API.Models;

namespace XPoint_Connect_API.Services
{
    public interface IBookingService
    {
        Task<BookingResponseDto?> CreateBookingAsync(CreateBookingDto createBookingDto);
        Task<BookingResponseDto?> GetBookingByIdAsync(string id);
        Task<List<BookingResponseDto>> GetBookingsByEVOwnerAsync(string evOwnerNIC);
        Task<List<BookingResponseDto>> GetBookingsByStationAsync(string stationId);
        Task<List<BookingResponseDto>> GetAllBookingsAsync();
        Task<BookingResponseDto?> UpdateBookingAsync(string id, UpdateBookingDto updateBookingDto);
        Task<bool> CancelBookingAsync(string id, string cancellationReason);
        Task<bool> ApproveBookingAsync(string id);
        Task<BookingResponseDto?> CheckInBookingAsync(string id, string? operatorNotes);
        Task<BookingResponseDto?> CheckOutBookingAsync(string id, string? operatorNotes);
        Task<BookingResponseDto?> GetBookingByQRCodeAsync(string qrCode);
        Task<DashboardStatsDto> GetDashboardStatsAsync(string evOwnerNIC);
        Task<BookingStatsDto> GetBookingStatsAsync();
        Task<List<BookingResponseDto>> GetUpcomingBookingsAsync(string evOwnerNIC);
        Task<List<BookingResponseDto>> GetBookingHistoryAsync(string evOwnerNIC);
        Task<bool> CanModifyBookingAsync(string id);
    }

    public class BookingService : IBookingService
    {
        private readonly IMongoDbContext _context;
        private readonly IQRCodeService _qrCodeService;
        private readonly IChargingStationService _chargingStationService;
        private readonly IEVOwnerService _evOwnerService;

        public BookingService(
            IMongoDbContext context,
            IQRCodeService qrCodeService,
            IChargingStationService chargingStationService,
            IEVOwnerService evOwnerService)
        {
            _context = context;
            _qrCodeService = qrCodeService;
            _chargingStationService = chargingStationService;
            _evOwnerService = evOwnerService;
        }

        public async Task<BookingResponseDto?> CreateBookingAsync(CreateBookingDto createBookingDto)
        {
            // Validate reservation date (within 7 days from booking date)
            var daysDifference = (createBookingDto.ReservationDateTime - DateTime.UtcNow).TotalDays;
            if (daysDifference > 7 || daysDifference < 0)
                return null;

            // Get station details for pricing
            var station = await _chargingStationService.GetStationByIdAsync(createBookingDto.ChargingStationId);
            if (station == null || !station.IsActive)
                return null;

            // Calculate total amount
            var totalAmount = CalculateTotalAmount(station.ChargingRate, createBookingDto.DurationMinutes);

            var booking = new Booking
            {
                EVOwnerNIC = createBookingDto.EVOwnerNIC,
                ChargingStationId = createBookingDto.ChargingStationId,
                ReservationDateTime = createBookingDto.ReservationDateTime,
                DurationMinutes = createBookingDto.DurationMinutes,
                TotalAmount = totalAmount,
                Status = BookingStatus.Pending,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await _context.Bookings.InsertOneAsync(booking);

            return await MapToResponseDto(booking);
        }

        public async Task<BookingResponseDto?> GetBookingByIdAsync(string id)
        {
            var booking = await _context.Bookings
                .Find(b => b.Id == id)
                .FirstOrDefaultAsync();

            return booking != null ? await MapToResponseDto(booking) : null;
        }

        public async Task<List<BookingResponseDto>> GetBookingsByEVOwnerAsync(string evOwnerNIC)
        {
            var bookings = await _context.Bookings
                .Find(b => b.EVOwnerNIC == evOwnerNIC)
                .SortByDescending(b => b.CreatedAt)
                .ToListAsync();

            var result = new List<BookingResponseDto>();
            foreach (var booking in bookings)
            {
                var dto = await MapToResponseDto(booking);
                if (dto != null) result.Add(dto);
            }

            return result;
        }

        public async Task<List<BookingResponseDto>> GetBookingsByStationAsync(string stationId)
        {
            var bookings = await _context.Bookings
                .Find(b => b.ChargingStationId == stationId)
                .SortByDescending(b => b.CreatedAt)
                .ToListAsync();

            var result = new List<BookingResponseDto>();
            foreach (var booking in bookings)
            {
                var dto = await MapToResponseDto(booking);
                if (dto != null) result.Add(dto);
            }

            return result;
        }

        public async Task<List<BookingResponseDto>> GetAllBookingsAsync()
        {
            var bookings = await _context.Bookings
                .Find(_ => true)
                .SortByDescending(b => b.CreatedAt)
                .ToListAsync();

            var result = new List<BookingResponseDto>();
            foreach (var booking in bookings)
            {
                var dto = await MapToResponseDto(booking);
                if (dto != null) result.Add(dto);
            }

            return result;
        }

        public async Task<BookingResponseDto?> UpdateBookingAsync(string id, UpdateBookingDto updateBookingDto)
        {
            var booking = await _context.Bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
            if (booking == null || !await CanModifyBookingAsync(id))
                return null;

            var filter = Builders<Booking>.Filter.Eq(b => b.Id, id);
            var updateDefinition = Builders<Booking>.Update.Set(b => b.UpdatedAt, DateTime.UtcNow);

            if (updateBookingDto.ReservationDateTime.HasValue)
            {
                // Validate new reservation date
                var daysDifference = (updateBookingDto.ReservationDateTime.Value - DateTime.UtcNow).TotalDays;
                if (daysDifference > 7 || daysDifference < 0)
                    return null;

                updateDefinition = updateDefinition.Set(b => b.ReservationDateTime, updateBookingDto.ReservationDateTime.Value);
            }

            if (updateBookingDto.DurationMinutes.HasValue)
            {
                updateDefinition = updateDefinition.Set(b => b.DurationMinutes, updateBookingDto.DurationMinutes.Value);
                
                // Recalculate total amount
                var station = await _chargingStationService.GetStationByIdAsync(booking.ChargingStationId);
                if (station != null)
                {
                    var totalAmount = CalculateTotalAmount(station.ChargingRate, updateBookingDto.DurationMinutes.Value);
                    updateDefinition = updateDefinition.Set(b => b.TotalAmount, totalAmount);
                }
            }

            if (updateBookingDto.Status.HasValue)
                updateDefinition = updateDefinition.Set(b => b.Status, updateBookingDto.Status.Value);

            if (!string.IsNullOrEmpty(updateBookingDto.OperatorNotes))
                updateDefinition = updateDefinition.Set(b => b.OperatorNotes, updateBookingDto.OperatorNotes);

            var result = await _context.Bookings.FindOneAndUpdateAsync(
                filter,
                updateDefinition,
                new FindOneAndUpdateOptions<Booking> { ReturnDocument = ReturnDocument.After }
            );

            return result != null ? await MapToResponseDto(result) : null;
        }

        public async Task<bool> CancelBookingAsync(string id, string cancellationReason)
        {
            if (!await CanModifyBookingAsync(id))
                return false;

            var filter = Builders<Booking>.Filter.Eq(b => b.Id, id);
            var update = Builders<Booking>.Update
                .Set(b => b.Status, BookingStatus.Cancelled)
                .Set(b => b.CancellationReason, cancellationReason)
                .Set(b => b.CancelledAt, DateTime.UtcNow)
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            var result = await _context.Bookings.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<bool> ApproveBookingAsync(string id)
        {
            var booking = await _context.Bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
            if (booking == null || booking.Status != BookingStatus.Pending)
                return false;

            // Generate QR code
            var qrCode = _qrCodeService.GenerateBookingQRCode(id, booking.EVOwnerNIC, booking.ChargingStationId);

            var filter = Builders<Booking>.Filter.Eq(b => b.Id, id);
            var update = Builders<Booking>.Update
                .Set(b => b.Status, BookingStatus.Approved)
                .Set(b => b.QRCode, qrCode)
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            var result = await _context.Bookings.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<BookingResponseDto?> CheckInBookingAsync(string id, string? operatorNotes)
        {
            var booking = await _context.Bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
            if (booking == null || booking.Status != BookingStatus.Approved)
                return null;

            var filter = Builders<Booking>.Filter.Eq(b => b.Id, id);
            var update = Builders<Booking>.Update
                .Set(b => b.Status, BookingStatus.CheckedIn)
                .Set(b => b.CheckInTime, DateTime.UtcNow)
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            if (!string.IsNullOrEmpty(operatorNotes))
                update = update.Set(b => b.OperatorNotes, operatorNotes);

            var result = await _context.Bookings.FindOneAndUpdateAsync(
                filter,
                update,
                new FindOneAndUpdateOptions<Booking> { ReturnDocument = ReturnDocument.After }
            );

            return result != null ? await MapToResponseDto(result) : null;
        }

        public async Task<BookingResponseDto?> CheckOutBookingAsync(string id, string? operatorNotes)
        {
            var booking = await _context.Bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
            if (booking == null || booking.Status != BookingStatus.CheckedIn)
                return null;

            var filter = Builders<Booking>.Filter.Eq(b => b.Id, id);
            var update = Builders<Booking>.Update
                .Set(b => b.Status, BookingStatus.Completed)
                .Set(b => b.CheckOutTime, DateTime.UtcNow)
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            if (!string.IsNullOrEmpty(operatorNotes))
                update = update.Set(b => b.OperatorNotes, operatorNotes);

            var result = await _context.Bookings.FindOneAndUpdateAsync(
                filter,
                update,
                new FindOneAndUpdateOptions<Booking> { ReturnDocument = ReturnDocument.After }
            );

            return result != null ? await MapToResponseDto(result) : null;
        }

        public async Task<BookingResponseDto?> GetBookingByQRCodeAsync(string qrCode)
        {
            var qrData = _qrCodeService.DecodeQRCode(qrCode);
            if (qrData == null)
                return null;

            return await GetBookingByIdAsync(qrData.BookingId);
        }

        public async Task<DashboardStatsDto> GetDashboardStatsAsync(string evOwnerNIC)
        {
            var now = DateTime.UtcNow;
            var startOfMonth = new DateTime(now.Year, now.Month, 1);

            var bookings = await _context.Bookings
                .Find(b => b.EVOwnerNIC == evOwnerNIC)
                .ToListAsync();

            var pendingReservations = bookings.Count(b => b.Status == BookingStatus.Pending);
            var approvedFutureReservations = bookings.Count(b => b.Status == BookingStatus.Approved && b.ReservationDateTime > now);
            var completedBookingsThisMonth = bookings.Count(b => b.Status == BookingStatus.Completed && b.CheckOutTime >= startOfMonth);
            var totalSpentThisMonth = bookings
                .Where(b => b.Status == BookingStatus.Completed && b.CheckOutTime >= startOfMonth)
                .Sum(b => b.TotalAmount);

            // Get nearby stations (default location - this should be parameterized in real app)
            var nearbyStations = await _chargingStationService.GetNearbyStationsAsync(6.9271, 79.8612, 10); // Colombo coordinates as default

            return new DashboardStatsDto
            {
                PendingReservations = pendingReservations,
                ApprovedFutureReservations = approvedFutureReservations,
                CompletedBookingsThisMonth = completedBookingsThisMonth,
                TotalSpentThisMonth = totalSpentThisMonth,
                NearbyStations = nearbyStations
            };
        }

        public async Task<BookingStatsDto> GetBookingStatsAsync()
        {
            var bookings = await _context.Bookings.Find(_ => true).ToListAsync();

            return new BookingStatsDto
            {
                PendingBookings = bookings.Count(b => b.Status == BookingStatus.Pending),
                ApprovedBookings = bookings.Count(b => b.Status == BookingStatus.Approved),
                CompletedBookings = bookings.Count(b => b.Status == BookingStatus.Completed),
                CancelledBookings = bookings.Count(b => b.Status == BookingStatus.Cancelled),
                TotalRevenue = bookings.Where(b => b.Status == BookingStatus.Completed).Sum(b => b.TotalAmount)
            };
        }

        public async Task<List<BookingResponseDto>> GetUpcomingBookingsAsync(string evOwnerNIC)
        {
            var now = DateTime.UtcNow;
            var bookings = await _context.Bookings
                .Find(b => b.EVOwnerNIC == evOwnerNIC && 
                          (b.Status == BookingStatus.Approved || b.Status == BookingStatus.CheckedIn) &&
                          b.ReservationDateTime > now)
                .SortBy(b => b.ReservationDateTime)
                .ToListAsync();

            var result = new List<BookingResponseDto>();
            foreach (var booking in bookings)
            {
                var dto = await MapToResponseDto(booking);
                if (dto != null) result.Add(dto);
            }

            return result;
        }

        public async Task<List<BookingResponseDto>> GetBookingHistoryAsync(string evOwnerNIC)
        {
            var bookings = await _context.Bookings
                .Find(b => b.EVOwnerNIC == evOwnerNIC && 
                          (b.Status == BookingStatus.Completed || b.Status == BookingStatus.Cancelled))
                .SortByDescending(b => b.ReservationDateTime)
                .ToListAsync();

            var result = new List<BookingResponseDto>();
            foreach (var booking in bookings)
            {
                var dto = await MapToResponseDto(booking);
                if (dto != null) result.Add(dto);
            }

            return result;
        }

        public async Task<bool> CanModifyBookingAsync(string id)
        {
            var booking = await _context.Bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
            if (booking == null || booking.Status == BookingStatus.Completed || booking.Status == BookingStatus.Cancelled)
                return false;

            // Can only modify if reservation is at least 12 hours away
            var hoursUntilReservation = (booking.ReservationDateTime - DateTime.UtcNow).TotalHours;
            return hoursUntilReservation >= 12;
        }

        private static double CalculateTotalAmount(double hourlyRate, int durationMinutes)
        {
            var hours = durationMinutes / 60.0;
            return Math.Round(hourlyRate * hours, 2);
        }

        private async Task<BookingResponseDto?> MapToResponseDto(Booking booking)
        {
            var station = await _chargingStationService.GetStationByIdAsync(booking.ChargingStationId);
            var evOwner = await _evOwnerService.GetEVOwnerByNICAsync(booking.EVOwnerNIC);

            if (station == null || evOwner == null)
                return null;

            return new BookingResponseDto
            {
                Id = booking.Id ?? string.Empty,
                EVOwnerNIC = booking.EVOwnerNIC,
                EVOwnerName = evOwner.FullName,
                ChargingStationId = booking.ChargingStationId,
                ChargingStationName = station.Name,
                ReservationDateTime = booking.ReservationDateTime,
                BookingDate = booking.BookingDate,
                DurationMinutes = booking.DurationMinutes,
                Status = booking.Status,
                TotalAmount = booking.TotalAmount,
                QRCode = booking.QRCode,
                CheckInTime = booking.CheckInTime,
                CheckOutTime = booking.CheckOutTime,
                CancellationReason = booking.CancellationReason,
                CancelledAt = booking.CancelledAt,
                CreatedAt = booking.CreatedAt,
                UpdatedAt = booking.UpdatedAt,
                OperatorNotes = booking.OperatorNotes
            };
        }
    }
}