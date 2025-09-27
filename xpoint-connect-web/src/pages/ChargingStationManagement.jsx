import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import api from '../utils/api'
import { Plus, Edit, Trash2, Search, Power, PowerOff, MapPin, Clock, Battery } from 'lucide-react'
import { formatDate } from '../utils/helpers'
import toast from 'react-hot-toast'

const ChargingStationManagement = () => {
  const { hasAccess, isBackOffice } = useAuth()
  const [stations, setStations] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [selectedStation, setSelectedStation] = useState(null)
  const [formData, setFormData] = useState({
    name: '',
    address: '',
    city: '',
    province: '',
    latitude: '',
    longitude: '',
    type: 'AC',
    totalSlots: 1,
    chargingRate: 0,
    description: '',
    amenities: []
  })

  useEffect(() => {
    fetchStations()
  }, [])

  const fetchStations = async () => {
    try {
      setLoading(true)
      const response = await api.get('/chargingstations')
      setStations(response.data)
    } catch (error) {
      toast.error('Failed to fetch charging stations')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateStation = async (e) => {
    e.preventDefault()
    
    try {
      const stationData = {
        ...formData,
        location: {
          latitude: parseFloat(formData.latitude),
          longitude: parseFloat(formData.longitude),
          address: formData.address,
          city: formData.city,
          province: formData.province
        },
        totalSlots: parseInt(formData.totalSlots),
        chargingRate: parseFloat(formData.chargingRate),
        amenities: formData.amenities.filter(a => a.trim() !== '')
      }
      
      delete stationData.latitude
      delete stationData.longitude
      delete stationData.address
      delete stationData.city
      delete stationData.province

      await api.post('/chargingstations', stationData)
      toast.success('Charging station created successfully')
      setShowCreateModal(false)
      resetForm()
      fetchStations()
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to create charging station'
      toast.error(message)
    }
  }

  const handleUpdateStation = async (e) => {
    e.preventDefault()
    
    try {
      const stationData = {
        ...formData,
        location: {
          latitude: parseFloat(formData.latitude),
          longitude: parseFloat(formData.longitude),
          address: formData.address,
          city: formData.city,
          province: formData.province
        },
        totalSlots: parseInt(formData.totalSlots),
        chargingRate: parseFloat(formData.chargingRate),
        amenities: formData.amenities.filter(a => a.trim() !== '')
      }
      
      delete stationData.latitude
      delete stationData.longitude
      delete stationData.address
      delete stationData.city
      delete stationData.province

      await api.put(`/chargingstations/${selectedStation.id}`, stationData)
      toast.success('Charging station updated successfully')
      setShowEditModal(false)
      resetForm()
      fetchStations()
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update charging station'
      toast.error(message)
    }
  }

  const handleDeactivateStation = async (stationId) => {
    if (window.confirm('Are you sure you want to deactivate this charging station?')) {
      try {
        await api.post(`/chargingstations/${stationId}/deactivate`)
        toast.success('Charging station deactivated successfully')
        fetchStations()
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to deactivate charging station'
        toast.error(message)
      }
    }
  }

  const handleDeleteStation = async (stationId) => {
    if (window.confirm('Are you sure you want to delete this charging station? This action cannot be undone.')) {
      try {
        await api.delete(`/chargingstations/${stationId}`)
        toast.success('Charging station deleted successfully')
        fetchStations()
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to delete charging station'
        toast.error(message)
      }
    }
  }

  const resetForm = () => {
    setFormData({
      name: '',
      address: '',
      city: '',
      province: '',
      latitude: '',
      longitude: '',
      type: 'AC',
      totalSlots: 1,
      chargingRate: 0,
      description: '',
      amenities: []
    })
    setSelectedStation(null)
  }

  const openCreateModal = () => {
    resetForm()
    setShowCreateModal(true)
  }

  const openEditModal = (station) => {
    setSelectedStation(station)
    setFormData({
      name: station.name,
      address: station.location?.address || '',
      city: station.location?.city || '',
      province: station.location?.province || '',
      latitude: station.location?.latitude?.toString() || '',
      longitude: station.location?.longitude?.toString() || '',
      type: station.type,
      totalSlots: station.totalSlots,
      chargingRate: station.chargingRate,
      description: station.description || '',
      amenities: station.amenities || []
    })
    setShowEditModal(true)
  }

  const addAmenity = () => {
    setFormData(prev => ({
      ...prev,
      amenities: [...prev.amenities, '']
    }))
  }

  const updateAmenity = (index, value) => {
    setFormData(prev => ({
      ...prev,
      amenities: prev.amenities.map((amenity, i) => i === index ? value : amenity)
    }))
  }

  const removeAmenity = (index) => {
    setFormData(prev => ({
      ...prev,
      amenities: prev.amenities.filter((_, i) => i !== index)
    }))
  }

  const filteredStations = stations.filter(station =>
    station.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    station.location?.city?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    station.location?.province?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    station.type.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const StationModal = ({ isOpen, onClose, onSubmit, title, isEdit = false }) => {
    if (!isOpen) return null

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-4xl mx-4 max-h-[90vh] overflow-y-auto">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4">{title}</h3>
          
          <form onSubmit={onSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="label text-secondary-700 mb-2 block">Station Name</label>
                <input
                  type="text"
                  className="input"
                  value={formData.name}
                  onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                  required
                />
              </div>
              
              <div>
                <label className="label text-secondary-700 mb-2 block">Type</label>
                <select
                  className="input"
                  value={formData.type}
                  onChange={(e) => setFormData(prev => ({ ...prev, type: e.target.value }))}
                  required
                >
                  <option value="AC">AC Charging</option>
                  <option value="DC">DC Fast Charging</option>
                </select>
              </div>
              
              <div>
                <label className="label text-secondary-700 mb-2 block">Total Slots</label>
                <input
                  type="number"
                  min="1"
                  max="20"
                  className="input"
                  value={formData.totalSlots}
                  onChange={(e) => setFormData(prev => ({ ...prev, totalSlots: e.target.value }))}
                  required
                />
              </div>
              
              <div>
                <label className="label text-secondary-700 mb-2 block">Charging Rate (kW)</label>
                <input
                  type="number"
                  min="0"
                  step="0.1"
                  className="input"
                  value={formData.chargingRate}
                  onChange={(e) => setFormData(prev => ({ ...prev, chargingRate: e.target.value }))}
                  required
                />
              </div>
            </div>

            <div>
              <label className="label text-secondary-700 mb-2 block">Address</label>
              <input
                type="text"
                className="input"
                value={formData.address}
                onChange={(e) => setFormData(prev => ({ ...prev, address: e.target.value }))}
                required
              />
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="label text-secondary-700 mb-2 block">City</label>
                <input
                  type="text"
                  className="input"
                  value={formData.city}
                  onChange={(e) => setFormData(prev => ({ ...prev, city: e.target.value }))}
                  required
                />
              </div>
              
              <div>
                <label className="label text-secondary-700 mb-2 block">Province</label>
                <select
                  className="input"
                  value={formData.province}
                  onChange={(e) => setFormData(prev => ({ ...prev, province: e.target.value }))}
                  required
                >
                  <option value="">Select Province</option>
                  <option value="Western">Western</option>
                  <option value="Central">Central</option>
                  <option value="Southern">Southern</option>
                  <option value="Northern">Northern</option>
                  <option value="Eastern">Eastern</option>
                  <option value="North Western">North Western</option>
                  <option value="North Central">North Central</option>
                  <option value="Uva">Uva</option>
                  <option value="Sabaragamuwa">Sabaragamuwa</option>
                </select>
              </div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="label text-secondary-700 mb-2 block">Latitude</label>
                <input
                  type="number"
                  step="any"
                  className="input"
                  value={formData.latitude}
                  onChange={(e) => setFormData(prev => ({ ...prev, latitude: e.target.value }))}
                  required
                />
              </div>
              
              <div>
                <label className="label text-secondary-700 mb-2 block">Longitude</label>
                <input
                  type="number"
                  step="any"
                  className="input"
                  value={formData.longitude}
                  onChange={(e) => setFormData(prev => ({ ...prev, longitude: e.target.value }))}
                  required
                />
              </div>
            </div>
            
            <div>
              <label className="label text-secondary-700 mb-2 block">Description</label>
              <textarea
                className="input"
                rows="3"
                value={formData.description}
                onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
              />
            </div>
            
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="label text-secondary-700">Amenities</label>
                <button
                  type="button"
                  onClick={addAmenity}
                  className="btn btn-secondary btn-sm"
                >
                  Add Amenity
                </button>
              </div>
              <div className="space-y-2">
                {formData.amenities.map((amenity, index) => (
                  <div key={index} className="flex space-x-2">
                    <input
                      type="text"
                      className="input flex-1"
                      value={amenity}
                      onChange={(e) => updateAmenity(index, e.target.value)}
                      placeholder="e.g., Free WiFi, Parking, Restroom"
                    />
                    <button
                      type="button"
                      onClick={() => removeAmenity(index)}
                      className="btn btn-danger btn-sm"
                    >
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            </div>
            
            <div className="flex space-x-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                className="btn btn-secondary btn-md flex-1"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn btn-primary btn-md flex-1"
              >
                {isEdit ? 'Update' : 'Create'}
              </button>
            </div>
          </form>
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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-secondary-900">Charging Station Management</h1>
          <p className="text-secondary-600 mt-1">Manage charging stations and their availability</p>
        </div>
        {hasAccess('BackOffice') && (
          <button
            onClick={openCreateModal}
            className="btn btn-primary btn-md flex items-center space-x-2"
          >
            <Plus className="w-4 h-4" />
            <span>Add Station</span>
          </button>
        )}
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-secondary-200">
        <div className="p-6 border-b border-secondary-200">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-secondary-400 w-4 h-4" />
            <input
              type="text"
              placeholder="Search stations..."
              className="input pl-10"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-secondary-50 border-b border-secondary-200">
              <tr>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Station Name</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Location</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Type</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Slots</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Rate (kW)</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Status</th>
                <th className="text-right py-3 px-6 text-sm font-semibold text-secondary-900">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredStations.map((station) => (
                <tr key={station.id} className="border-b border-secondary-100 hover:bg-secondary-50 transition-colors">
                  <td className="py-4 px-6">
                    <div className="font-medium text-secondary-900">{station.name}</div>
                    <div className="text-sm text-secondary-600">{station.description}</div>
                  </td>
                  <td className="py-4 px-6">
                    <div className="text-secondary-900">{station.location?.city}, {station.location?.province}</div>
                    <div className="text-sm text-secondary-600">{station.location?.address}</div>
                  </td>
                  <td className="py-4 px-6">
                    <span className={`badge ${station.type === 'DC' ? 'badge-warning' : 'badge-primary'}`}>
                      {station.type} Charging
                    </span>
                  </td>
                  <td className="py-4 px-6">
                    <div className="text-secondary-900">
                      {station.availableSlots}/{station.totalSlots}
                    </div>
                    <div className="text-sm text-secondary-600">available</div>
                  </td>
                  <td className="py-4 px-6 text-secondary-900">{station.chargingRate} kW</td>
                  <td className="py-4 px-6">
                    <span className={`badge ${station.isActive ? 'badge-success' : 'badge-danger'}`}>
                      {station.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="py-4 px-6">
                    <div className="flex items-center space-x-2 justify-end">
                      <button
                        onClick={() => openEditModal(station)}
                        className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                        title="Edit station"
                      >
                        <Edit className="w-4 h-4" />
                      </button>
                      
                      {hasAccess('BackOffice') && station.isActive && (
                        <button
                          onClick={() => handleDeactivateStation(station.id)}
                          className="p-2 text-warning-600 hover:bg-warning-50 rounded-lg transition-colors"
                          title="Deactivate station"
                        >
                          <PowerOff className="w-4 h-4" />
                        </button>
                      )}
                      
                      {hasAccess('BackOffice') && (
                        <button
                          onClick={() => handleDeleteStation(station.id)}
                          className="p-2 text-danger-600 hover:bg-danger-50 rounded-lg transition-colors"
                          title="Delete station"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredStations.length === 0 && (
          <div className="text-center py-12">
            <p className="text-secondary-500">No charging stations found</p>
          </div>
        )}
      </div>

      <StationModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreateStation}
        title="Create New Charging Station"
      />

      <StationModal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        onSubmit={handleUpdateStation}
        title="Edit Charging Station"
        isEdit={true}
      />
    </div>
  )
}

export default ChargingStationManagement