import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import api from '../utils/api'
import { Battery, Calendar, MapPin, Activity, CheckCircle2, XCircle } from 'lucide-react'

const OperatorDashboard = () => {
  const { user } = useAuth()
  const [stations, setStations] = useState([])
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true)
        // Stations assigned to this operator
        const stationsRes = await api.get(`/chargingstations/operator/${user.id}`)
        setStations(Array.isArray(stationsRes.data) ? stationsRes.data : [])

        // Bookings are automatically scoped for StationOperator by backend
        const bookingsRes = await api.get('/bookings')
        const data = Array.isArray(bookingsRes.data) ? bookingsRes.data : []
        // Sort by reservation time desc and limit
        setBookings(data.sort((a, b) => new Date(b.reservationDateTime) - new Date(a.reservationDateTime)).slice(0, 10))
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [user?.id])

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="spinner"></div>
      </div>
    )
  }

  const totalSlots = stations.reduce((sum, s) => sum + (s.totalSlots || 0), 0)
  const availableSlots = stations.reduce((sum, s) => sum + (s.availableSlots || 0), 0)
  const occupancyRate = totalSlots > 0 ? Math.round(((totalSlots - availableSlots) / totalSlots) * 100) : 0

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-primary-500 to-primary-600 rounded-lg p-6 text-white">
        <h1 className="text-2xl font-bold mb-2">Station Operator Dashboard</h1>
        <p className="text-primary-100">Overview for your assigned stations</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg border border-secondary-200 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-secondary-600 text-sm">Assigned Stations</p>
              <p className="text-2xl font-bold text-secondary-900">{stations.length}</p>
            </div>
            <Battery className="w-6 h-6 text-primary-600" />
          </div>
        </div>
        <div className="bg-white rounded-lg border border-secondary-200 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-secondary-600 text-sm">Upcoming Bookings (7d)</p>
              <p className="text-2xl font-bold text-secondary-900">{bookings.filter(b => new Date(b.reservationDateTime) > new Date()).length}</p>
            </div>
            <Calendar className="w-6 h-6 text-warning-600" />
          </div>
        </div>
        <div className="bg-white rounded-lg border border-secondary-200 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-secondary-600 text-sm">Occupancy Rate</p>
              <p className="text-2xl font-bold text-secondary-900">{occupancyRate}%</p>
            </div>
            <Activity className="w-6 h-6 text-success-600" />
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg border border-secondary-200">
          <div className="p-6 border-b border-secondary-200">
            <h3 className="text-lg font-semibold text-secondary-900 flex items-center">
              <MapPin className="w-5 h-5 mr-2 text-primary-600" />
              Your Stations
            </h3>
          </div>
          <div className="p-6 space-y-4">
            {stations.length === 0 ? (
              <p className="text-secondary-500">No stations assigned</p>
            ) : (
              stations.map((s) => (
                <div key={s.id} className="flex items-center justify-between text-sm">
                  <div>
                    <p className="text-secondary-900 font-medium">{s.name}</p>
                    <p className="text-secondary-600">{s.location?.city}, {s.location?.province}</p>
                  </div>
                  <div className="text-right">
                    <div className="text-secondary-900">{s.availableSlots}/{s.totalSlots}</div>
                    <div className="text-secondary-600">available</div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="bg-white rounded-lg border border-secondary-200">
          <div className="p-6 border-b border-secondary-200">
            <h3 className="text-lg font-semibold text-secondary-900 flex items-center">
              <Calendar className="w-5 h-5 mr-2 text-danger-600" />
              Recent Bookings
            </h3>
          </div>
          <div className="p-6 space-y-3">
            {bookings.length === 0 ? (
              <p className="text-secondary-500">No bookings found</p>
            ) : (
              bookings.map((b) => (
                <div key={b.id} className="flex items-center justify-between text-sm">
                  <div className="flex-1 min-w-0">
                    <p className="text-secondary-900 font-medium truncate">{b.evOwner?.fullName || b.evOwnerName || 'Unknown Owner'}</p>
                    <p className="text-secondary-600 truncate">{b.station?.name || b.chargingStationName}</p>
                  </div>
                  <span className={`badge ${
                    b.status?.toLowerCase() === 'approved' ? 'badge-success' :
                    b.status?.toLowerCase() === 'pending' ? 'badge-warning' :
                    b.status?.toLowerCase() === 'cancelled' ? 'badge-danger' : 'badge-primary'
                  }`}>{b.status || 'Unknown'}</span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default OperatorDashboard


