# XPoint Connect API - Development Quick Start

## ?? Quick Start (Easy Way)

### Option 1: Double-click to start
1. **Double-click `start-dev.bat`** - That's it! 
2. **Open browser:** http://localhost:5034/swagger

### Option 2: Command line
```bash
dotnet run --launch-profile http
```

### Option 3: PowerShell script
```powershell
.\start-dev.ps1
```

## ?? Development URLs

- **Main API:** http://localhost:5034/
- **Swagger UI:** http://localhost:5034/swagger
- **Health Check:** http://localhost:5034/api/dev/health

## ?? Test the API

### 1. Initialize Data
```bash
curl -X POST http://localhost:5034/api/seed-data
```

### 2. Register an EV Owner
```bash
curl -X POST http://localhost:5034/api/auth/evowner/register \
  -H "Content-Type: application/json" \
  -d '{
    "nic": "123456789V",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "0771234567",
    "address": "123 Main St",
    "password": "Password123!"
  }'
```

### 3. Get Charging Stations
```bash
curl http://localhost:5034/api/chargingstations
```

## ?? Development Benefits

? **No IIS complexity** - Just `dotnet run`
? **Auto-reload** - Changes apply automatically  
? **Full Swagger UI** - Complete API documentation
? **CORS enabled** - Works with any frontend
? **Hot reload** - Instant feedback
? **Easy debugging** - Full Visual Studio support

## ?? Ready for Mobile & Web Integration

Your API is now ready to integrate with:
- **Android Mobile App** (Pure Android + SQLite)
- **Web Application** (React/Bootstrap/Tailwind)
- **Any HTTP client**

## ??? Development vs Production

| Aspect | Development (Current) | Production (IIS) |
|--------|----------------------|------------------|
| **Startup** | `dotnet run` | IIS Service |
| **Server** | Kestrel | IIS + Kestrel |
| **HTTPS** | Optional | Required |
| **Performance** | Good enough | Optimized |
| **Setup** | Instant | Complex |
| **Debugging** | Full support | Limited |

## ?? When to Use IIS?

Use IIS deployment when:
- **Final deployment** to production server
- **Assignment submission** (requirement specified)
- **Performance testing** under load
- **Windows Server** environment

## ?? Quick Development Workflow

1. **Start API:** Double-click `start-dev.bat`
2. **Seed Data:** Visit http://localhost:5034 and click seed data link
3. **Open Swagger:** http://localhost:5034/swagger
4. **Test endpoints** in Swagger UI
5. **Develop frontend** using the API
6. **Make changes** - API auto-reloads!

## ?? Troubleshooting

**Port already in use?**
- Change port in `Properties/launchSettings.json`
- Or kill the process: `netstat -ano | findstr :5034`

**MongoDB not running?**
- Install MongoDB Community Server
- Or use MongoDB Atlas (cloud)
- API structure works without MongoDB for testing

**Want to switch back to IIS?**
- Run `.\deploy-to-iis.ps1` anytime
- All IIS files are preserved

---

**?? Happy Development!** Your API is production-ready but development-friendly!