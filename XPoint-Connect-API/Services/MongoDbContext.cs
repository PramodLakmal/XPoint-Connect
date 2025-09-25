using MongoDB.Driver;
using XPoint_Connect_API.Configuration;
using XPoint_Connect_API.Models;

namespace XPoint_Connect_API.Services
{
    public interface IMongoDbContext
    {
        IMongoCollection<User> Users { get; }
        IMongoCollection<EVOwner> EVOwners { get; }
        IMongoCollection<ChargingStation> ChargingStations { get; }
        IMongoCollection<Booking> Bookings { get; }
    }

    public class MongoDbContext : IMongoDbContext
    {
        private readonly IMongoDatabase _database;

        public MongoDbContext(MongoDbSettings settings)
        {
            var client = new MongoClient(settings.ConnectionString);
            _database = client.GetDatabase(settings.DatabaseName);
        }

        public IMongoCollection<User> Users => 
            _database.GetCollection<User>("Users");

        public IMongoCollection<EVOwner> EVOwners => 
            _database.GetCollection<EVOwner>("EVOwners");

        public IMongoCollection<ChargingStation> ChargingStations => 
            _database.GetCollection<ChargingStation>("ChargingStations");

        public IMongoCollection<Booking> Bookings => 
            _database.GetCollection<Booking>("Bookings");
    }
}