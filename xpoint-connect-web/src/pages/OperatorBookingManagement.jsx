import { useEffect, useMemo, useState } from 'react'
import api from '../utils/api'
import { Search, Check, X, Eye } from 'lucide-react'
import toast from 'react-hot-toast'
import { QRCodeCanvas } from 'qrcode.react'

const formatDateTime = (dt) => {
  try {
    const d = new Date(dt)
    return d.toLocaleString()
  } catch {
    return ''
  }
}

const OperatorBookingManagement = () => {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('All')
  const [selected, setSelected] = useState(null)
  const [showDetails, setShowDetails] = useState(false)

  useEffect(() => {
    fetchBookings()
  }, [])

  const fetchBookings = async () => {
    try {
      setLoading(true)
      // Backend already scopes results to operator's stations for StationOperator role
      const res = await api.get('/bookings')
      setBookings(Array.isArray(res.data) ? res.data : [])
    } catch (e) {
      toast.error('Failed to fetch bookings')
    } finally {
      setLoading(false)
    }
  }

  const approveBooking = async (id) => {
    try {
      await api.post(`/bookings/${id}/approve`)
      toast.success('Booking approved')
      fetchBookings()
    } catch (e) {
      const msg = e.response?.data?.message || 'Failed to approve booking'
      toast.error(msg)
    }
  }

  const cancelBooking = async (id, reason = 'Cancelled by operator') => {
    if (!window.confirm('Cancel this booking?')) return
    try {
      await api.post(`/bookings/${id}/cancel`, { reason })
      toast.success('Booking cancelled')
      fetchBookings()
    } catch (e) {
      const msg = e.response?.data?.message || 'Failed to cancel booking'
      toast.error(msg)
    }
  }

  const openDetails = async (booking) => {
    try {
      // Fetch fresh details for this booking
      const res = await api.get(`/bookings/${booking.id}`)
      setSelected(res.data || booking)
    } catch {
      setSelected(booking)
    } finally {
      setShowDetails(true)
    }
  }

  const filtered = useMemo(() => {
    return bookings.filter((b) => {
      const q = searchTerm.toLowerCase()
      const matches =
        (b.evOwnerName || b.evOwner?.fullName || '').toLowerCase().includes(q) ||
        (b.evOwnerNIC || b.evOwner?.nic || '').toLowerCase().includes(q) ||
        (b.chargingStationName || b.station?.name || '').toLowerCase().includes(q) ||
        (b.id || '').toLowerCase().includes(q)
      const statusStr = (b.status || 'Unknown').toString().toLowerCase()
      const statusOk = statusFilter === 'All' || statusStr === statusFilter.toLowerCase()
      return matches && statusOk
    })
  }, [bookings, searchTerm, statusFilter])

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="spinner"></div>
      </div>
    )
  }

  const getStatusBadgeColor = (status) => {
    const s = (status || '').toString().toLowerCase()
    switch (s) {
      case 'pending': return 'badge-warning'
      case 'approved': return 'badge-success'
      case 'checkedin': return 'badge-primary'
      case 'completed': return 'badge-primary'
      case 'cancelled': return 'badge-danger'
      default: return 'badge-secondary'
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-secondary-900">Bookings</h1>
          <p className="text-secondary-600 mt-1">Manage bookings for your stations</p>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-secondary-200">
        <div className="p-6 border-b border-secondary-200">
          <div className="flex flex-col md:flex-row md:items-center space-y-4 md:space-y-0 md:space-x-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-secondary-400 w-4 h-4" />
              <input
                type="text"
                placeholder="Search by NIC, name, station, or ID..."
                className="input pl-10"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <div className="md:w-48">
              <select className="input" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                <option value="All">All Status</option>
                <option value="Pending">Pending</option>
                <option value="Approved">Approved</option>
                <option value="CheckedIn">CheckedIn</option>
                <option value="Completed">Completed</option>
                <option value="Cancelled">Cancelled</option>
              </select>
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-secondary-50 border-b border-secondary-200">
              <tr>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">ID</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">QR</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Owner</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Station</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">When</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Duration</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Status</th>
                <th className="text-right py-3 px-6 text-sm font-semibold text-secondary-900">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((b) => (
                <tr key={b.id} className="border-b border-secondary-100 hover:bg-secondary-50 transition-colors">
                  <td className="py-4 px-6"><div className="font-mono text-sm">{b.id?.slice(-8)}</div></td>
                  <td className="py-4 px-6">
                    {b.qrCode && b.qrCode !== '' && (b.status?.toLowerCase() === 'approved' || b.status?.toLowerCase() === 'checkedin') ? (
                      <QRCodeCanvas value={b.qrCode} size={48} />
                    ) : (
                      <div className="text-xs text-secondary-600 italic">Not generated</div>
                    )}
                  </td>
                  <td className="py-4 px-6"><div className="text-secondary-900">{b.evOwnerName || b.evOwner?.fullName}</div></td>
                  <td className="py-4 px-6"><div className="text-secondary-900">{b.chargingStationName || b.station?.name}</div></td>
                  <td className="py-4 px-6"><div className="text-secondary-900">{formatDateTime(b.reservationDateTime)}</div></td>
                  <td className="py-4 px-6 text-secondary-900">{b.durationMinutes} min</td>
                  <td className="py-4 px-6"><span className={`badge ${getStatusBadgeColor(b.status)}`}>{b.status || 'Unknown'}</span></td>
                  <td className="py-4 px-6">
                    <div className="flex items-center space-x-2 justify-end">
                      <button onClick={() => openDetails(b)} className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg" title="View details">
                        <Eye className="w-4 h-4" />
                      </button>
                      {b.status?.toLowerCase() === 'pending' && (
                        <button onClick={() => approveBooking(b.id)} className="p-2 text-success-600 hover:bg-success-50 rounded-lg" title="Approve">
                          <Check className="w-4 h-4" />
                        </button>
                      )}
                      {(b.status?.toLowerCase() === 'pending' || b.status?.toLowerCase() === 'approved') && (
                        <button onClick={() => cancelBooking(b.id)} className="p-2 text-danger-600 hover:bg-danger-50 rounded-lg" title="Cancel">
                          <X className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filtered.length === 0 && (
          <div className="text-center py-12">
            <p className="text-secondary-500">No bookings found</p>
          </div>
        )}
      </div>

      {/* Details Modal */}
      {showDetails && selected && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-xl font-semibold text-secondary-900">Booking Details</h3>
              <button onClick={() => setShowDetails(false)} className="text-secondary-400 hover:text-secondary-600">✕</button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-secondary-700">Booking ID</label>
                  <p className="text-secondary-900 font-mono">{selected.id}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-secondary-700">QR Code</label>
                  <div className="mt-1">
                    {selected.qrCode && selected.qrCode !== '' && (selected.status?.toLowerCase() === 'approved' || selected.status?.toLowerCase() === 'checkedin') ? (
                      <QRCodeCanvas value={selected.qrCode} size={128} />
                    ) : (
                      <p className="text-secondary-600 italic">Not generated</p>
                    )}
                  </div>
                </div>
                <div>
                  <label className="text-sm font-medium text-secondary-700">EV Owner</label>
                  <p className="text-secondary-900">{selected.evOwner?.fullName || selected.evOwnerName}</p>
                  <p className="text-sm text-secondary-600">{selected.evOwner?.nic || selected.evOwnerNIC}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-secondary-700">Contact</label>
                  <p className="text-secondary-900">{selected.evOwner?.email}</p>
                  <p className="text-sm text-secondary-600">{selected.evOwner?.phoneNumber}</p>
                </div>
              </div>
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-secondary-700">Charging Station</label>
                  <p className="text-secondary-900">{selected.station?.name || selected.chargingStationName}</p>
                  <p className="text-sm text-secondary-600">{selected.station?.location?.address}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-secondary-700">Reservation</label>
                  <p className="text-secondary-900">{formatDateTime(selected.reservationDateTime)}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-secondary-700">Duration</label>
                  <p className="text-secondary-900">{selected.durationMinutes} minutes</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-secondary-700">Status</label>
                  <span className={`badge ${getStatusBadgeColor(selected.status)}`}>{selected.status || 'Unknown'}</span>
                </div>
              </div>
            </div>

            {selected.notes && (
              <div className="mt-6">
                <label className="text-sm font-medium text-secondary-700">Notes</label>
                <p className="text-secondary-900 mt-1 p-3 bg-secondary-50 rounded-lg">{selected.notes}</p>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
              <div>
                <label className="text-sm font-medium text-secondary-700">Created</label>
                <p className="text-secondary-900">{formatDateTime(selected.createdAt)}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-secondary-700">Last Updated</label>
                <p className="text-secondary-900">{formatDateTime(selected.updatedAt)}</p>
              </div>
            </div>

            <div className="flex space-x-3 mt-6 pt-6 border-t border-secondary-200">
              <button onClick={() => setShowDetails(false)} className="btn btn-secondary btn-md flex-1">Close</button>
              {selected.status?.toLowerCase() === 'pending' && (
                <button onClick={() => { approveBooking(selected.id); setShowDetails(false) }} className="btn btn-success btn-md flex-1">Approve</button>
              )}
              {(selected.status?.toLowerCase() === 'pending' || selected.status?.toLowerCase() === 'approved') && (
                <button onClick={() => { cancelBooking(selected.id); setShowDetails(false) }} className="btn btn-danger btn-md flex-1">Cancel</button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default OperatorBookingManagement


