import { useState, useEffect } from 'react';
import api from '../utils/api';
import { Search, Check, X, Eye, Calendar, Clock, MapPin, User, Plus, Edit } from 'lucide-react';
import { formatDateTime, formatDate, isWithin12Hours, isWithin7Days } from '../utils/helpers';
import toast from 'react-hot-toast';

const BookingManagement = () => {
  const [bookings, setBookings] = useState([]);
  const [evOwners, setEvOwners] = useState([]);
  const [chargingStations, setChargingStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('All');
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);

  useEffect(() => {
    fetchBookings();
    fetchEvOwners();
    fetchChargingStations();
  }, []);

  const fetchBookings = async () => {
    try {
      setLoading(true);
      const response = await api.get('/bookings');
      // Validate and clean booking data
      const cleanedBookings = response.data.map((booking) => ({
        ...booking,
        status: typeof booking.status === 'string' ? booking.status : 'Unknown',
      }));
      setBookings(cleanedBookings);
    } catch (error) {
      toast.error('Failed to fetch bookings');
    } finally {
      setLoading(false);
    }
  };

  const fetchEvOwners = async () => {
    try {
      const response = await api.get('/evowners');
      setEvOwners(response.data.filter((owner) => owner.isActive));
    } catch (error) {
      console.log('Failed to fetch EV owners:', error);
    }
  };

  const fetchChargingStations = async () => {
    try {
      const response = await api.get('/chargingstations');
      setChargingStations(response.data.filter((station) => station.isActive));
    } catch (error) {
      console.log('Failed to fetch charging stations:', error);
    }
  };

  const handleCreateBooking = async (formData) => {
    if (!isWithin7Days(formData.reservationDateTime)) {
      toast.error('Reservation must be within 7 days from today');
      return;
    }

    try {
      const bookingData = {
        evOwnerNic: formData.evOwnerNic,
        chargingStationId: formData.chargingStationId,
        reservationDateTime: formData.reservationDateTime,
        durationMinutes: parseInt(formData.durationMinutes),
        notes: formData.notes,
      };

      await api.post('/bookings', bookingData);
      toast.success('Booking created successfully');
      setShowCreateModal(false);
      fetchBookings();
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to create booking';
      toast.error(message);
    }
  };

  const handleUpdateBooking = async (formData) => {
    if (!isWithin12Hours(formData.reservationDateTime)) {
      toast.error('Reservations can only be updated at least 12 hours before the reservation time');
      return;
    }

    if (!isWithin7Days(formData.reservationDateTime)) {
      toast.error('Reservation must be within 7 days from today');
      return;
    }

    try {
      const bookingData = {
        reservationDateTime: formData.reservationDateTime,
        durationMinutes: parseInt(formData.durationMinutes),
        notes: formData.notes,
      };

      await api.put(`/bookings/${selectedBooking.id}`, bookingData);
      toast.success('Booking updated successfully');
      setShowEditModal(false);
      setSelectedBooking(null);
      fetchBookings();
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update booking';
      toast.error(message);
    }
  };

  const handleApproveBooking = async (bookingId) => {
    try {
      await api.post(`/bookings/${bookingId}/approve`);
      toast.success('Booking approved successfully');
      fetchBookings();
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to approve booking';
      toast.error(message);
    }
  };

  const handleCancelBooking = async (bookingId, reason = 'Cancelled by admin') => {
    if (window.confirm('Are you sure you want to cancel this booking?')) {
      try {
        await api.post(`/bookings/${bookingId}/cancel`, { reason });
        toast.success('Booking cancelled successfully');
        fetchBookings();
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to cancel booking';
        toast.error(message);
      }
    }
  };

  const viewBookingDetails = (booking) => {
    setSelectedBooking(booking);
    setShowDetailsModal(true);
  };

  const openCreateModal = () => {
    setShowCreateModal(true);
  };

  const openEditModal = (booking) => {
    setSelectedBooking(booking);
    setShowEditModal(true);
  };

  const getStatusBadgeColor = (status) => {
    // Ensure status is a string before calling toLowerCase
    const statusStr = typeof status === 'string' ? status.toLowerCase() : 'unknown';
    switch (statusStr) {
      case 'pending':
        return 'badge-warning';
      case 'approved':
        return 'badge-success';
      case 'completed':
        return 'badge-primary';
      case 'cancelled':
        return 'badge-danger';
      case 'active':
        return 'badge-success';
      default:
        return 'badge-secondary';
    }
  };

  const canModifyBooking = (booking) => {
    // Ensure status is a string or handle as 'unknown'
    const statusStr = typeof booking.status === 'string' ? booking.status.toLowerCase() : 'unknown';
    if (statusStr === 'cancelled' || statusStr === 'completed') {
      return false;
    }
    return isWithin12Hours(booking.reservationDateTime);
  };

  const filteredBookings = bookings.filter((booking) => {
    const matchesSearch =
      booking.evOwner?.nic?.toLowerCase()?.includes(searchTerm.toLowerCase()) ||
      booking.evOwner?.fullName?.toLowerCase()?.includes(searchTerm.toLowerCase()) ||
      booking.station?.name?.toLowerCase()?.includes(searchTerm.toLowerCase()) ||
      booking.qrCode?.toLowerCase()?.includes(searchTerm.toLowerCase());

    const statusStr = typeof booking.status === 'string' ? booking.status.toLowerCase() : 'unknown';
    const matchesStatus = statusFilter === 'All' || statusStr === statusFilter.toLowerCase();

    return matchesSearch && matchesStatus;
  });

  const BookingModal = ({ isOpen, onClose, onSubmit, title, isEdit = false, booking = null, evOwners, chargingStations }) => {
    const [formData, setFormData] = useState({
      evOwnerNic: '',
      chargingStationId: '',
      reservationDateTime: '',
      durationMinutes: 60,
      notes: '',
    });

    useEffect(() => {
      if (isEdit && booking) {
        setFormData({
          evOwnerNic: booking.evOwner?.nic || '',
          chargingStationId: booking.station?.id || '',
          reservationDateTime: booking.reservationDateTime
            ? new Date(booking.reservationDateTime).toISOString().slice(0, 16)
            : '',
          durationMinutes: booking.durationMinutes || 60,
          notes: booking.notes || '',
        });
      } else {
        setFormData({
          evOwnerNic: '',
          chargingStationId: '',
          reservationDateTime: '',
          durationMinutes: 60,
          notes: '',
        });
      }
    }, [isOpen, isEdit, booking]);

    if (!isOpen) return null;

    const handleInputChange = (e) => {
      const { name, value } = e.target;
      setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
      e.preventDefault();
      onSubmit(formData);
    };

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4">{title}</h3>

          <form onSubmit={handleSubmit} className="space-y-4">
            {!isEdit && (
              <div>
                <label className="label text-secondary-700 mb-2 block">EV Owner</label>
                <select
                  name="evOwnerNic"
                  className="input"
                  value={formData.evOwnerNic}
                  onChange={handleInputChange}
                  required
                >
                  <option value="">Select EV Owner</option>
                  {evOwners.map((owner) => (
                    <option key={owner.nic} value={owner.nic}>
                      {owner.fullName} ({owner.nic})
                    </option>
                  ))}
                </select>
              </div>
            )}

            {!isEdit && (
              <div>
                <label className="label text-secondary-700 mb-2 block">Charging Station</label>
                <select
                  name="chargingStationId"
                  className="input"
                  value={formData.chargingStationId}
                  onChange={handleInputChange}
                  required
                >
                  <option value="">Select Charging Station</option>
                  {chargingStations.map((station) => (
                    <option key={station.id} value={station.id}>
                      {station.name} - {station.location?.city} ({station.availableSlots}/{station.totalSlots} slots)
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div>
              <label className="label text-secondary-700 mb-2 block">
                Reservation Date & Time
                <span className="text-xs text-secondary-500 ml-2">
                  (Must be within 7 days from today)
                </span>
              </label>
              <input
                type="datetime-local"
                name="reservationDateTime"
                className="input"
                value={formData.reservationDateTime}
                onChange={handleInputChange}
                min={new Date().toISOString().slice(0, 16)}
                max={new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16)}
                required
              />
            </div>

            <div>
              <label className="label text-secondary-700 mb-2 block">Duration (minutes)</label>
              <select
                name="durationMinutes"
                className="input"
                value={formData.durationMinutes}
                onChange={handleInputChange}
                required
              >
                <option value={30}>30 minutes</option>
                <option value={60}>1 hour</option>
                <option value={90}>1.5 hours</option>
                <option value={120}>2 hours</option>
                <option value={180}>3 hours</option>
                <option value={240}>4 hours</option>
              </select>
            </div>

            <div>
              <label className="label text-secondary-700 mb-2 block">Notes (Optional)</label>
              <textarea
                name="notes"
                className="input"
                rows="3"
                value={formData.notes}
                onChange={handleInputChange}
                placeholder="Any additional notes for this booking..."
              />
            </div>

            {isEdit && (
              <div className="bg-warning-50 border border-warning-200 rounded-lg p-3">
                <p className="text-sm text-warning-700">
                  <strong>Note:</strong> Reservations can only be updated at least 12 hours before the reservation time.
                </p>
              </div>
            )}

            <div className="flex space-x-3 pt-4">
              <button type="button" onClick={onClose} className="btn btn-secondary btn-md flex-1">
                Cancel
              </button>
              <button type="submit" className="btn btn-primary btn-md flex-1">
                {isEdit ? 'Update Booking' : 'Create Booking'}
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  const DetailsModal = () => {
    if (!showDetailsModal || !selectedBooking) return null;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-xl font-semibold text-secondary-900">Booking Details</h3>
            <button
              onClick={() => setShowDetailsModal(false)}
              className="text-secondary-400 hover:text-secondary-600"
            >
              <X className="w-6 h-6" />
            </button>
          </div>

          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-secondary-700">Booking ID</label>
                  <p className="text-secondary-900 font-mono">{selectedBooking.id}</p>
                </div>

                <div>
                  <label className="text-sm font-medium text-secondary-700">QR Code</label>
                  <p className="text-secondary-900 font-mono">{selectedBooking.qrCode}</p>
                </div>

                <div>
                  <label className="text-sm font-medium text-secondary-700">EV Owner</label>
                  <p className="text-secondary-900">{selectedBooking.evOwner?.fullName}</p>
                  <p className="text-sm text-secondary-600">{selectedBooking.evOwner?.nic}</p>
                </div>

                <div>
                  <label className="text-sm font-medium text-secondary-700">Contact</label>
                  <p className="text-secondary-900">{selectedBooking.evOwner?.email}</p>
                  <p className="text-sm text-secondary-600">{selectedBooking.evOwner?.phoneNumber}</p>
                </div>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-secondary-700">Charging Station</label>
                  <p className="text-secondary-900">{selectedBooking.station?.name}</p>
                  <p className="text-sm text-secondary-600">{selectedBooking.station?.location?.address}</p>
                </div>

                <div>
                  <label className="text-sm font-medium text-secondary-700">Reservation Date & Time</label>
                  <p className="text-secondary-900">{formatDateTime(selectedBooking.reservationDateTime)}</p>
                </div>

                <div>
                  <label className="text-sm font-medium text-secondary-700">Duration</label>
                  <p className="text-secondary-900">{selectedBooking.durationMinutes} minutes</p>
                </div>

                <div>
                  <label className="text-sm font-medium text-secondary-700">Status</label>
                  <div className="flex items-center space-x-2">
                    <span className={`badge ${getStatusBadgeColor(selectedBooking.status)}`}>
                      {typeof selectedBooking.status === 'string' ? selectedBooking.status : 'Unknown'}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {selectedBooking.notes && (
              <div>
                <label className="text-sm font-medium text-secondary-700">Notes</label>
                <p className="text-secondary-900 mt-1 p-3 bg-secondary-50 rounded-lg">{selectedBooking.notes}</p>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-secondary-700">Created</label>
                <p className="text-secondary-900">{formatDateTime(selectedBooking.createdAt)}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-secondary-700">Last Updated</label>
                <p className="text-secondary-900">{formatDateTime(selectedBooking.updatedAt)}</p>
              </div>
            </div>
          </div>

          <div className="flex space-x-3 mt-6 pt-6 border-t border-secondary-200">
            <button
              onClick={() => setShowDetailsModal(false)}
              className="btn btn-secondary btn-md flex-1"
            >
              Close
            </button>

            {canModifyBooking(selectedBooking) && (
              <button
                onClick={() => {
                  setShowDetailsModal(false);
                  openEditModal(selectedBooking);
                }}
                className="btn btn-warning btn-md flex-1"
              >
                Edit Booking
              </button>
            )}

            {typeof selectedBooking.status === 'string' && selectedBooking.status.toLowerCase() === 'pending' && (
              <>
                <button
                  onClick={() => {
                    handleApproveBooking(selectedBooking.id);
                    setShowDetailsModal(false);
                  }}
                  className="btn btn-success btn-md flex-1"
                >
                  Approve
                </button>
                <button
                  onClick={() => {
                    handleCancelBooking(selectedBooking.id);
                    setShowDetailsModal(false);
                  }}
                  className="btn btn-danger btn-md flex-1"
                >
                  Cancel
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-secondary-900">Booking Management</h1>
          <p className="text-secondary-600 mt-1">Manage charging station reservations</p>
        </div>
        <button
          onClick={openCreateModal}
          className="btn btn-primary btn-md flex items-center space-x-2"
        >
          <Plus className="w-4 h-4" />
          <span>Create Booking</span>
        </button>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-secondary-200">
        <div className="p-6 border-b border-secondary-200">
          <div className="flex flex-col md:flex-row md:items-center space-y-4 md:space-y-0 md:space-x-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-secondary-400 w-4 h-4" />
              <input
                type="text"
                placeholder="Search by NIC, name, station, or QR code..."
                className="input pl-10"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div className="md:w-48">
              <select
                className="input"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="All">All Status</option>
                <option value="Pending">Pending</option>
                <option value="Approved">Approved</option>
                <option value="Active">Active</option>
                <option value="Completed">Completed</option>
                <option value="Cancelled">Cancelled</option>
                <option value="Unknown">Unknown</option>
              </select>
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-secondary-50 border-b border-secondary-200">
              <tr>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Booking ID</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">EV Owner</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Station</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Reservation</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Duration</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Status</th>
                <th className="text-right py-3 px-6 text-sm font-semibold text-secondary-900">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredBookings.map((booking) => (
                <tr key={booking.id} className="border-b border-secondary-100 hover:bg-secondary-50 transition-colors">
                  <td className="py-4 px-6">
                    <div className="font-mono text-sm font-medium text-secondary-900">
                      {booking.id?.slice(-8)}
                    </div>
                    <div className="font-mono text-xs text-secondary-600">{booking.qrCode}</div>
                  </td>
                  <td className="py-4 px-6">
                    <div className="font-medium text-secondary-900">{booking.evOwner?.fullName}</div>
                    <div className="text-sm text-secondary-600">{booking.evOwner?.nic}</div>
                  </td>
                  <td className="py-4 px-6">
                    <div className="text-secondary-900">{booking.station?.name}</div>
                    <div className="text-sm text-secondary-600">{booking.station?.location?.city}</div>
                  </td>
                  <td className="py-4 px-6">
                    <div className="text-secondary-900">{formatDate(booking.reservationDateTime)}</div>
                    <div className="text-sm text-secondary-600">{formatDateTime(booking.reservationDateTime).split(' ').pop()}</div>
                  </td>
                  <td className="py-4 px-6 text-secondary-900">{booking.durationMinutes} min</td>
                  <td className="py-4 px-6">
                    <span className={`badge ${getStatusBadgeColor(booking.status)}`}>
                      {typeof booking.status === 'string' ? booking.status : 'Unknown'}
                    </span>
                  </td>
                  <td className="py-4 px-6">
                    <div className="flex items-center space-x-2 justify-end">
                      <button
                        onClick={() => viewBookingDetails(booking)}
                        className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                        title="View details"
                      >
                        <Eye className="w-4 h-4" />
                      </button>

                      {canModifyBooking(booking) && (
                        <button
                          onClick={() => openEditModal(booking)}
                          className="p-2 text-warning-600 hover:bg-warning-50 rounded-lg transition-colors"
                          title="Edit booking"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                      )}

                      {typeof booking.status === 'string' && booking.status.toLowerCase() === 'pending' && (
                        <>
                          <button
                            onClick={() => handleApproveBooking(booking.id)}
                            className="p-2 text-success-600 hover:bg-success-50 rounded-lg transition-colors"
                            title="Approve booking"
                          >
                            <Check className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleCancelBooking(booking.id)}
                            className="p-2 text-danger-600 hover:bg-danger-50 rounded-lg transition-colors"
                            title="Cancel booking"
                          >
                            <X className="w-4 h-4" />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredBookings.length === 0 && (
          <div className="text-center py-12">
            <Calendar className="mx-auto h-12 w-12 text-secondary-400 mb-4" />
            <p className="text-secondary-500">No bookings found</p>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        {['pending', 'approved', 'active', 'completed', 'cancelled', 'unknown'].map((status) => {
          const count = bookings.filter((b) => {
            const statusStr = typeof b.status === 'string' ? b.status.toLowerCase() : 'unknown';
            return statusStr === status;
          }).length;
          return (
            <div key={status} className="bg-white rounded-lg border border-secondary-200 p-4 text-center">
              <p className="text-2xl font-bold text-secondary-900">{count}</p>
              <p className="text-sm text-secondary-600 capitalize">{status}</p>
            </div>
          );
        })}
      </div>

      <BookingModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreateBooking}
        title="Create New Booking"
        isEdit={false}
        evOwners={evOwners}
        chargingStations={chargingStations}
      />

      <BookingModal
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false);
          setSelectedBooking(null);
        }}
        onSubmit={handleUpdateBooking}
        title="Edit Booking"
        isEdit={true}
        booking={selectedBooking}
        evOwners={evOwners}
        chargingStations={chargingStations}
      />

      <DetailsModal />
    </div>
  );
};

export default BookingManagement;