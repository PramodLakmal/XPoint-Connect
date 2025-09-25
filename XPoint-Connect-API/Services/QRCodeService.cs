using System.Text;
using System.Text.Json;

namespace XPoint_Connect_API.Services
{
    public interface IQRCodeService
    {
        string GenerateBookingQRCode(string bookingId, string evOwnerNIC, string chargingStationId);
        BookingQRData? DecodeQRCode(string qrCode);
    }

    public class QRCodeService : IQRCodeService
    {
        public string GenerateBookingQRCode(string bookingId, string evOwnerNIC, string chargingStationId)
        {
            var qrData = new BookingQRData
            {
                BookingId = bookingId,
                EVOwnerNIC = evOwnerNIC,
                ChargingStationId = chargingStationId,
                GeneratedAt = DateTime.UtcNow
            };

            var json = JsonSerializer.Serialize(qrData);
            var bytes = Encoding.UTF8.GetBytes(json);
            return Convert.ToBase64String(bytes);
        }

        public BookingQRData? DecodeQRCode(string qrCode)
        {
            try
            {
                var bytes = Convert.FromBase64String(qrCode);
                var json = Encoding.UTF8.GetString(bytes);
                return JsonSerializer.Deserialize<BookingQRData>(json);
            }
            catch
            {
                return null;
            }
        }
    }

    public class BookingQRData
    {
        public string BookingId { get; set; } = string.Empty;
        public string EVOwnerNIC { get; set; } = string.Empty;
        public string ChargingStationId { get; set; } = string.Empty;
        public DateTime GeneratedAt { get; set; }
    }
}