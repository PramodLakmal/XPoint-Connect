namespace XPoint_Connect_API.Configuration
{
    public class MongoDbSettings
    {
        public string ConnectionString { get; set; } = string.Empty;
        public string DatabaseName { get; set; } = string.Empty;
        public string UsersCollection { get; set; } = "Users";
        public string EVOwnersCollection { get; set; } = "EVOwners";
        public string ChargingStationsCollection { get; set; } = "ChargingStations";
        public string BookingsCollection { get; set; } = "Bookings";
    }
}