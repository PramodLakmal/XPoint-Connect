import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { Link } from 'react-router-dom'
import api from '../utils/api'
import { 
  Users, 
  UserCog, 
  Battery, 
  Calendar,
  TrendingUp,
  TrendingDown,
  Activity,
  MapPin,
  Clock,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  Zap
} from 'lucide-react'

const Dashboard = () => {
  const { user, isBackOffice, isStationOperator } = useAuth()
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [recentBookings, setRecentBookings] = useState([])
  const [systemAlerts, setSystemAlerts] = useState([])

  useEffect(() => {
    fetchDashboardData()
  }, [])

  const fetchDashboardData = async () => {
    try {
      setLoading(true)
      
      // Fetch system stats
      const statsResponse = await api.get('/dev/system-stats')
      setStats(statsResponse.data)

      // Fetch recent bookings if BackOffice
      if (isBackOffice()) {
        try {
          const bookingsResponse = await api.get('/bookings?limit=5')
          const bookings = Array.isArray(bookingsResponse.data) ? bookingsResponse.data : []
          setRecentBookings(bookings.slice(0, 5))
          
          // Generate system alerts based on data
          generateSystemAlerts(statsResponse.data, bookings)
        } catch (error) {
          console.log('Could not fetch recent bookings:', error)
        }
      }
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const generateSystemAlerts = (systemStats, bookings) => {
    const alerts = []
    
    // Check for stations with low availability
    if (systemStats?.ChargingStations) {
      const totalSlots = systemStats.ChargingStations.TotalSlots || 0
      const availableSlots = systemStats.ChargingStations.AvailableSlots || 0
      const occupancyRate = totalSlots > 0 ? ((totalSlots - availableSlots) / totalSlots) * 100 : 0
      
      if (occupancyRate > 85) {
        alerts.push({
          type: 'warning',
          message: `High occupancy rate: ${occupancyRate.toFixed(1)}% of charging slots are occupied`,
          action: 'Consider adding more stations'
        })
      }
    }
    
    // Check for pending bookings
    const pendingBookings = bookings.filter(b => b.status?.toLowerCase() === 'pending').length
    if (pendingBookings > 5) {
      alerts.push({
        type: 'warning',
        message: `${pendingBookings} bookings pending approval`,
        action: 'Review and process pending bookings'
      })
    }
    
    // Check for inactive EV owners requiring reactivation
    if (systemStats?.EVOwners?.RequiringReactivation > 0) {
      alerts.push({
        type: 'info',
        message: `${systemStats.EVOwners.RequiringReactivation} EV owners requesting reactivation`,
        action: 'Review reactivation requests'
      })
    }
    
    setSystemAlerts(alerts)
  }

  const StatCard = ({ title, value, icon: Icon, trend, trendValue, color = 'primary', link = null }) => {
    const colorClasses = {
      primary: 'bg-primary-50 text-primary-600 border-primary-200',
      success: 'bg-success-50 text-success-600 border-success-200',
      warning: 'bg-warning-50 text-warning-600 border-warning-200',
      danger: 'bg-danger-50 text-danger-600 border-danger-200',
    }

    const CardContent = () => (
      <div className="bg-white rounded-lg border border-secondary-200 p-6 hover:shadow-md transition-shadow">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-secondary-600 text-sm font-medium mb-1">{title}</p>
            <p className="text-2xl font-bold text-secondary-900">{value}</p>
            {trend && (
              <div className="flex items-center mt-2">
                {trend === 'up' ? (
                  <TrendingUp className="w-4 h-4 text-success-500 mr-1" />
                ) : (
                  <TrendingDown className="w-4 h-4 text-danger-500 mr-1" />
                )}
                <span className={`text-sm ${trend === 'up' ? 'text-success-600' : 'text-danger-600'}`}>
                  {trendValue}
                </span>
              </div>
            )}
          </div>
          <div className={`p-3 rounded-lg border ${colorClasses[color]}`}>
            <Icon className="w-6 h-6" />
          </div>
        </div>
      </div>
    )

    return link ? (
      <Link to={link} className="block">
        <CardContent />
      </Link>
    ) : (
      <CardContent />
    )
  }

  const AlertCard = ({ alert }) => {
    const getAlertIcon = (type) => {
      switch (type) {
        case 'warning':
          return AlertTriangle
        case 'success':
          return CheckCircle2
        case 'danger':
          return XCircle
        default:
          return Activity
      }
    }

    const getAlertColor = (type) => {
      switch (type) {
        case 'warning':
          return 'text-warning-600 bg-warning-50'
        case 'success':
          return 'text-success-600 bg-success-50'
        case 'danger':
          return 'text-danger-600 bg-danger-50'
        default:
          return 'text-primary-600 bg-primary-50'
      }
    }

    const Icon = getAlertIcon(alert.type)

    return (
      <div className={`p-4 rounded-lg ${getAlertColor(alert.type)} border border-current border-opacity-20`}>
        <div className="flex items-start space-x-3">
          <Icon className="w-5 h-5 mt-0.5" />
          <div className="flex-1">
            <p className="text-sm font-medium">{alert.message}</p>
            {alert.action && (
              <p className="text-xs mt-1 opacity-80">{alert.action}</p>
            )}
          </div>
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="spinner"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-primary-500 to-primary-600 rounded-lg p-6 text-white">
        <h1 className="text-2xl font-bold mb-2">
          Welcome back, {user?.username}!
        </h1>
        <p className="text-primary-100 mb-4">
          Here's what's happening with your EV charging network today.
        </p>
        <div className="flex items-center space-x-4 text-sm text-primary-100">
          <div className="flex items-center">
            <Clock className="w-4 h-4 mr-1" />
            Last updated: {new Date().toLocaleTimeString()}
          </div>
          <div className="flex items-center">
            <Activity className="w-4 h-4 mr-1" />
            System Status: Operational
          </div>
        </div>
      </div>

      {/* System Alerts */}
      {systemAlerts.length > 0 && (
        <div className="space-y-3">
          <h3 className="text-lg font-semibold text-secondary-900">System Alerts</h3>
          <div className="grid grid-cols-1 gap-3">
            {systemAlerts.map((alert, index) => (
              <AlertCard key={index} alert={alert} />
            ))}
          </div>
        </div>
      )}

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {isBackOffice() && (
          <StatCard
            title="Total Users"
            value={stats?.Users?.Total || 0}
            icon={Users}
            trend="up"
            trendValue="+2 this week"
            color="primary"
            link="/users"
          />
        )}
        
        <StatCard
          title="EV Owners"
          value={stats?.EVOwners?.Total || 0}
          icon={UserCog}
          trend="up"
          trendValue="+12 this month"
          color="success"
          link="/evowners"
        />
        
        <StatCard
          title="Charging Stations"
          value={stats?.ChargingStations?.Total || 0}
          icon={Battery}
          trend="up"
          trendValue="+3 this month"
          color="warning"
          link="/stations"
        />
        
        <StatCard
          title="Available Slots"
          value={stats?.ChargingStations?.AvailableSlots || 0}
          icon={Zap}
          trend="up"
          trendValue="98% uptime"
          color="danger"
          link="/stations"
        />
      </div>

      {/* Detailed Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg border border-secondary-200 p-6">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4 flex items-center">
            <Battery className="w-5 h-5 mr-2 text-primary-600" />
            Station Overview
          </h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-secondary-600">AC Stations</span>
              <span className="font-semibold text-secondary-900">{stats?.ChargingStations?.AC || 0}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-secondary-600">DC Stations</span>
              <span className="font-semibold text-secondary-900">{stats?.ChargingStations?.DC || 0}</span>
            </div>
            <div className="flex items-center justify-between border-t pt-3">
              <span className="text-secondary-600">Total Slots</span>
              <span className="font-semibold text-primary-600">{stats?.ChargingStations?.TotalSlots || 0}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-secondary-600">Available</span>
              <span className="font-semibold text-success-600">{stats?.ChargingStations?.AvailableSlots || 0}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-secondary-600">Occupancy Rate</span>
              <span className="font-semibold text-warning-600">
                {stats?.ChargingStations?.TotalSlots > 0 
                  ? Math.round(((stats.ChargingStations.TotalSlots - stats.ChargingStations.AvailableSlots) / stats.ChargingStations.TotalSlots) * 100)
                  : 0}%
              </span>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg border border-secondary-200 p-6">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4 flex items-center">
            <UserCog className="w-5 h-5 mr-2 text-success-600" />
            EV Owner Status
          </h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-secondary-600">Active</span>
              <span className="font-semibold text-success-600">{stats?.EVOwners?.Active || 0}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-secondary-600">Inactive</span>
              <span className="font-semibold text-danger-600">
                {(stats?.EVOwners?.Total || 0) - (stats?.EVOwners?.Active || 0)}
              </span>
            </div>
            <div className="flex items-center justify-between border-t pt-3">
              <span className="text-secondary-600">Reactivation Requests</span>
              <span className="font-semibold text-warning-600">{stats?.EVOwners?.RequiringReactivation || 0}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-secondary-600">Total Registered</span>
              <span className="font-semibold text-primary-600">{stats?.EVOwners?.Total || 0}</span>
            </div>
          </div>
        </div>

        {isBackOffice() && (
          <div className="bg-white rounded-lg border border-secondary-200 p-6">
            <h3 className="text-lg font-semibold text-secondary-900 mb-4 flex items-center">
              <Users className="w-5 h-5 mr-2 text-primary-600" />
              System Users
            </h3>
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-secondary-600">BackOffice</span>
                <span className="font-semibold text-primary-600">{stats?.Users?.BackOffice || 0}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-secondary-600">Station Operators</span>
                <span className="font-semibold text-secondary-900">{stats?.Users?.StationOperators || 0}</span>
              </div>
              <div className="flex items-center justify-between border-t pt-3">
                <span className="text-secondary-600">Active Users</span>
                <span className="font-semibold text-success-600">{stats?.Users?.Active || 0}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-secondary-600">Total Users</span>
                <span className="font-semibold text-primary-600">{stats?.Users?.Total || 0}</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Recent Activity & Quick Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Bookings */}
        {isBackOffice() && (
          <div className="bg-white rounded-lg border border-secondary-200">
            <div className="p-6 border-b border-secondary-200">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold text-secondary-900 flex items-center">
                  <Calendar className="w-5 h-5 mr-2 text-primary-600" />
                  Recent Bookings
                </h3>
                <Link to="/bookings" className="text-primary-600 hover:text-primary-700 text-sm font-medium">
                  View All
                </Link>
              </div>
            </div>
            <div className="p-6">
              <div className="space-y-4">
                {recentBookings.length > 0 ? (
                  recentBookings.map((booking, index) => (
                    <div key={index} className="flex items-center space-x-3 text-sm">
                      <div className={`w-2 h-2 rounded-full ${
                        booking.status?.toLowerCase() === 'pending' ? 'bg-warning-500' :
                        booking.status?.toLowerCase() === 'approved' ? 'bg-success-500' :
                        booking.status?.toLowerCase() === 'cancelled' ? 'bg-danger-500' : 'bg-primary-500'
                      }`}></div>
                      <div className="flex-1">
                        <span className="text-secondary-900 font-medium">
                          {booking.evOwner?.fullName || 'Unknown Owner'}
                        </span>
                        <span className="text-secondary-600 mx-2">•</span>
                        <span className="text-secondary-600">
                          {booking.station?.name || 'Unknown Station'}
                        </span>
                      </div>
                      <span className={`badge badge-${
                        booking.status?.toLowerCase() === 'pending' ? 'warning' :
                        booking.status?.toLowerCase() === 'approved' ? 'success' :
                        booking.status?.toLowerCase() === 'cancelled' ? 'danger' : 'primary'
                      }`}>
                        {booking.status || 'Unknown'}
                      </span>
                    </div>
                  ))
                ) : (
                  <p className="text-secondary-500 text-center py-4">No recent bookings</p>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Quick Actions */}
        <div className="bg-white rounded-lg border border-secondary-200 p-6">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4 flex items-center">
            <Activity className="w-5 h-5 mr-2 text-primary-600" />
            Quick Actions
          </h3>
          <div className="grid grid-cols-1 gap-3">
            {isBackOffice() && (
              <Link to="/users" className="p-4 border border-secondary-200 rounded-lg hover:bg-secondary-50 transition-colors text-left block">
                <div className="flex items-center space-x-3">
                  <Users className="w-6 h-6 text-primary-600" />
                  <div>
                    <p className="font-medium text-secondary-900">Manage Users</p>
                    <p className="text-sm text-secondary-600">Add or edit system users</p>
                  </div>
                </div>
              </Link>
            )}
            
            <Link to="/evowners" className="p-4 border border-secondary-200 rounded-lg hover:bg-secondary-50 transition-colors text-left block">
              <div className="flex items-center space-x-3">
                <UserCog className="w-6 h-6 text-success-600" />
                <div>
                  <p className="font-medium text-secondary-900">EV Owner Management</p>
                  <p className="text-sm text-secondary-600">Manage EV owner accounts</p>
                </div>
              </div>
            </Link>
            
            <Link to="/stations" className="p-4 border border-secondary-200 rounded-lg hover:bg-secondary-50 transition-colors text-left block">
              <div className="flex items-center space-x-3">
                <Battery className="w-6 h-6 text-warning-600" />
                <div>
                  <p className="font-medium text-secondary-900">Charging Stations</p>
                  <p className="text-sm text-secondary-600">Manage station network</p>
                </div>
              </div>
            </Link>
            
            <Link to="/bookings" className="p-4 border border-secondary-200 rounded-lg hover:bg-secondary-50 transition-colors text-left block">
              <div className="flex items-center space-x-3">
                <Calendar className="w-6 h-6 text-danger-600" />
                <div>
                  <p className="font-medium text-secondary-900">Booking Management</p>
                  <p className="text-sm text-secondary-600">Handle reservations</p>
                </div>
              </div>
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard