import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import api from '../utils/api';
import { Plus, Edit, Trash2, Search, PowerOff, Power, UserPlus, UserMinus } from 'lucide-react';
import toast from 'react-hot-toast';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

// Fix leaflet marker icon issue
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const ChargingStationManagement = () => {
  const { hasAccess, isBackOffice } = useAuth();
  const [stations, setStations] = useState([]);
  const [operators, setOperators] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedStation, setSelectedStation] = useState(null);

  useEffect(() => {
    fetchStations();
    fetchOperators();
  }, []);

  const fetchStations = async () => {
    try {
      setLoading(true);
      const response = await api.get('/chargingstations?activeOnly=false');
      setStations(response.data);
    } catch (error) {
      toast.error('Failed to fetch charging stations');
    } finally {
      setLoading(false);
    }
  };

  const fetchOperators = async () => {
    try {
      const response = await api.get('/users');
      // Filter only operators (role "StationOperator")
      const operatorUsers = response.data.filter(user => user.role === "StationOperator");
      setOperators(operatorUsers);
    } catch (error) {
      console.log('Failed to fetch operators:', error);
    }
  };

  const handleCreateStation = async (formData) => {
    try {
      const stationData = {
        name: formData.name,
        location: {
          latitude: parseFloat(formData.latitude),
          longitude: parseFloat(formData.longitude),
          address: formData.address,
          city: formData.city,
          province: formData.province,
        },
        type: formData.type === 'AC' ? 0 : 1,
        totalSlots: parseInt(formData.totalSlots),
        chargingRate: parseFloat(formData.chargingRate),
        amenities: formData.amenities.filter((a) => a.trim() !== ''),
        description: formData.description || '',
        schedule: [],
      };

      const response = await api.post('/chargingstations', stationData);
      const newStationId = response.data.id;

      // Assign operator if selected
      if (formData.operatorId) {
        await api.post('/operatorassignments/assign', {
          OperatorId: formData.operatorId,
          StationId: newStationId
        });
      }

      toast.success('Charging station created successfully');
      setShowCreateModal(false);
      fetchStations();
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to create charging station';
      toast.error(message);
    }
  };

  const handleUpdateStation = async (formData) => {
    try {
      const stationData = {
        name: formData.name,
        location: {
          latitude: parseFloat(formData.latitude),
          longitude: parseFloat(formData.longitude),
          address: formData.address,
          city: formData.city,
          province: formData.province,
        },
        type: typeof formData.type === 'string'
          ? (formData.type === 'AC' ? 0 : 1)
          : formData.type,
        totalSlots: parseInt(formData.totalSlots),
        chargingRate: parseFloat(formData.chargingRate),
        amenities: formData.amenities.filter((a) => a.trim() !== ''),
        description: formData.description || '',
      };

      await api.put(`/chargingstations/${selectedStation.id}`, stationData);

      // Handle operator assignment changes
      const currentOperatorId = selectedStation.operatorId;
      const newOperatorId = formData.operatorId;

      if (currentOperatorId !== newOperatorId) {
        // Remove current operator if exists
        if (currentOperatorId) {
          await api.delete(`/operatorassignments/stations/${selectedStation.id}/operator`);
        }
        
        // Assign new operator if selected
        if (newOperatorId) {
          await api.post('/operatorassignments/assign', {
            OperatorId: newOperatorId,
            StationId: selectedStation.id
          });
        }
      }

      toast.success('Charging station updated successfully');
      setShowEditModal(false);
      setSelectedStation(null);
      fetchStations();
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update charging station';
      toast.error(message);
    }
  };

  const handleDeactivateStation = async (stationId) => {
    if (window.confirm('Are you sure you want to deactivate this charging station?')) {
      try {
        // Unassign operator if assigned
        const station = stations.find(s => s.id === stationId);
        if (station && station.operatorId) {
          await handleUnassignOperator(stationId);
        }

        await api.post(`/chargingstations/${stationId}/deactivate`);
        toast.success('Charging station deactivated successfully');
        fetchStations();
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to deactivate charging station';
        toast.error(message);
      }
    }
  };

  const handleActivateStation = async (stationId) => {
    if (window.confirm('Are you sure you want to activate this charging station?')) {
      try {
        await api.post(`/chargingstations/${stationId}/activate`);
        toast.success('Charging station activated successfully');
        fetchStations();
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to activate charging station';
        toast.error(message);
      }
    }
  };

  const handleDeleteStation = async (stationId) => {
    if (window.confirm('Are you sure you want to delete this charging station? This action cannot be undone.')) {
      try {
        // Unassign operator if assigned
        const station = stations.find(s => s.id === stationId);
        if (station && station.operatorId) {
          await handleUnassignOperator(stationId);
        }

        await api.delete(`/chargingstations/${stationId}`);
        toast.success('Charging station deleted successfully');
        fetchStations();
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to delete charging station';
        toast.error(message);
      }
    }
  };

  const handleAssignOperator = async (stationId, operatorId) => {
    try {
      await api.post('/operatorassignments/assign', {
        OperatorId: operatorId,
        StationId: stationId
      });
      toast.success('Operator assigned to station successfully');
      fetchStations();
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to assign operator to station';
      toast.error(message);
    }
  };

  const handleUnassignOperator = async (stationId) => {
    if (window.confirm('Are you sure you want to remove the operator from this station?')) {
      try {
        await api.delete(`/operatorassignments/stations/${stationId}/operator`);
        toast.success('Operator removed from station successfully');
        fetchStations();
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to remove operator from station';
        toast.error(message);
      }
    }
  };

  const openCreateModal = () => {
    setSelectedStation(null);
    setShowCreateModal(true);
  };

  const openEditModal = (station) => {
    setSelectedStation(station);
    setShowEditModal(true);
  };

  const openAssignModal = (station) => {
    setSelectedStation(station);
    setShowAssignModal(true);
  };

  const getAssignedOperator = (station) => {
    if (!station.operatorId) return null;
    return operators.find(operator => operator.id === station.operatorId);
  };

  const getUnassignedOperators = (excludeStationId = null) => {
    // Get all operator IDs that are currently assigned to stations (excluding the current station if editing)
    const assignedOperatorIds = stations
      .filter(station => 
        station.operatorId && 
        station.operatorId.trim() !== '' && 
        station.id !== excludeStationId
      )
      .map(station => station.operatorId);
    
    // Return operators who are not assigned to any station (or assigned to the excluded station)
    return operators.filter(operator => !assignedOperatorIds.includes(operator.id));
  };

  const filteredStations = stations.filter(
    (station) =>
      station.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      station.location?.city?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      station.location?.province?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (typeof station.type === 'string' ? station.type.toLowerCase() : '').includes(searchTerm.toLowerCase()),
  );

  // Map location picker
  const LocationPicker = ({ latitude, longitude, setLocation }) => {
    useMapEvents({
      click(e) {
        setLocation(e.latlng);
      },
    });
    return latitude && longitude ? <Marker position={[latitude, longitude]} /> : null;
  };

  const StationModal = ({ isOpen, onClose, onSubmit, title, isEdit = false, station = null }) => {
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
      amenities: [],
      operatorId: '',
    });

    useEffect(() => {
      if (isEdit && station) {
        // Set initial form data
        setFormData({
          name: station.name || '',
          address: station.location?.address || '',
          city: station.location?.city || '',
          province: station.location?.province || '',
          latitude: station.location?.latitude?.toString() || '',
          longitude: station.location?.longitude?.toString() || '',
          type: typeof station.type === 'number' ? (station.type === 0 ? 'AC' : 'DC') : station.type,
          totalSlots: station.totalSlots || 1,
          chargingRate: station.chargingRate || 0,
          description: station.description || '',
          amenities: station.amenities || [],
          operatorId: station.operatorId || '',
        });

      } else {
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
          amenities: [],
          operatorId: '',
        });
      }
    }, [isOpen, isEdit, station]);

    if (!isOpen) return null;

    const handleInputChange = (e) => {
      const { name, value } = e.target;
      setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const addAmenity = () => {
      setFormData((prev) => ({
        ...prev,
        amenities: [...prev.amenities, ''],
      }));
    };

    const updateAmenity = (index, value) => {
      setFormData((prev) => ({
        ...prev,
        amenities: prev.amenities.map((amenity, i) => (i === index ? value : amenity)),
      }));
    };

    const removeAmenity = (index) => {
      setFormData((prev) => ({
        ...prev,
        amenities: prev.amenities.filter((_, i) => i !== index),
      }));
    };

    // Map location setter
    const setLocation = ({ lat, lng }) => {
      setFormData((prev) => ({
        ...prev,
        latitude: lat.toString(),
        longitude: lng.toString(),
      }));
    };

    const handleSubmit = (e) => {
      e.preventDefault();
      onSubmit(formData);
    };

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-4xl mx-4 max-h-[90vh] overflow-y-auto">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4">{title}</h3>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="label text-secondary-700 mb-2 block">Station Name</label>
                <input
                  type="text"
                  name="name"
                  className="input"
                  value={formData.name}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div>
                <label className="label text-secondary-700 mb-2 block">Type</label>
                <select
                  name="type"
                  className="input"
                  value={formData.type}
                  onChange={handleInputChange}
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
                  name="totalSlots"
                  min="1"
                  max="20"
                  className="input"
                  value={formData.totalSlots}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div>
                <label className="label text-secondary-700 mb-2 block">Charging Rate (kW)</label>
                <input
                  type="number"
                  name="chargingRate"
                  min="0"
                  step="0.1"
                  className="input"
                  value={formData.chargingRate}
                  onChange={handleInputChange}
                  required
                />
              </div>
            </div>

            <div>
              <label className="label text-secondary-700 mb-2 block">Address</label>
              <input
                type="text"
                name="address"
                className="input"
                value={formData.address}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="label text-secondary-700 mb-2 block">City</label>
                <input
                  type="text"
                  name="city"
                  className="input"
                  value={formData.city}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div>
                <label className="label text-secondary-700 mb-2 block">Province</label>
                <select
                  name="province"
                  className="input"
                  value={formData.province}
                  onChange={handleInputChange}
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

            {/* Map location picker */}
            <div>
              <label className="label text-secondary-700 mb-2 block">Select Location on Map</label>
              <MapContainer
                center={[
                  formData.latitude ? parseFloat(formData.latitude) : 7.8731,
                  formData.longitude ? parseFloat(formData.longitude) : 80.7718,
                ]}
                zoom={8}
                style={{ height: '300px', width: '100%' }}
              >
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                <LocationPicker
                  latitude={formData.latitude ? parseFloat(formData.latitude) : null}
                  longitude={formData.longitude ? parseFloat(formData.longitude) : null}
                  setLocation={setLocation}
                />
              </MapContainer>
              <div className="mt-2 text-sm text-secondary-700">
                Click on the map to set latitude and longitude.
              </div>
              <div className="flex space-x-2 mt-2">
                <input
                  type="number"
                  name="latitude"
                  step="any"
                  className="input"
                  value={formData.latitude}
                  onChange={handleInputChange}
                  required
                  placeholder="Latitude"
                />
                <input
                  type="number"
                  name="longitude"
                  step="any"
                  className="input"
                  value={formData.longitude}
                  onChange={handleInputChange}
                  required
                  placeholder="Longitude"
                />
              </div>
            </div>

            <div>
              <label className="label text-secondary-700 mb-2 block">Description</label>
              <textarea
                name="description"
                className="input"
                rows="3"
                value={formData.description}
                onChange={handleInputChange}
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

            <div>
              <label className="label text-secondary-700 mb-2 block">Assigned Operator</label>
              <select
                name="operatorId"
                className="input"
                value={formData.operatorId}
                onChange={handleInputChange}
              >
                <option value="">Select an operator (optional)</option>
                {getUnassignedOperators(isEdit ? station?.id : null).map((operator) => (
                  <option key={operator.id} value={operator.id}>
                    {operator.username} ({operator.email})
                  </option>
                ))}
              </select>
              <p className="text-sm text-secondary-500 mt-1">
                You can assign an operator later or create one in the Station Operators page.
                {getUnassignedOperators(isEdit ? station?.id : null).length === 0 && (
                  <span className="text-warning-600 block mt-1">
                    No unassigned operators available. All operators are currently assigned to stations.
                  </span>
                )}
              </p>
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
    );
  };

  const OperatorAssignModal = ({ isOpen, onClose, station }) => {
    const [selectedOperatorId, setSelectedOperatorId] = useState('');

    useEffect(() => {
      if (isOpen && station) {
        setSelectedOperatorId(station.operatorId || '');
      }
    }, [isOpen, station]);

    if (!isOpen || !station) return null;

    const handleSubmit = (e) => {
      e.preventDefault();
      if (selectedOperatorId) {
        handleAssignOperator(station.id, selectedOperatorId);
        onClose();
      }
    };

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4">
            Assign Operator to {station.name}
          </h3>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label text-secondary-700 mb-2 block">Select Operator</label>
              <select
                value={selectedOperatorId}
                onChange={(e) => setSelectedOperatorId(e.target.value)}
                className="input"
                required
              >
                <option value="">Choose an operator</option>
                {getUnassignedOperators().map((operator) => (
                  <option key={operator.id} value={operator.id}>
                    {operator.username} ({operator.email})
                  </option>
                ))}
              </select>
              {getUnassignedOperators().length === 0 && (
                <p className="text-sm text-warning-600 mt-2">
                  No unassigned operators available. All operators are currently assigned to stations.
                </p>
              )}
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
                Assign Operator
              </button>
            </div>
          </form>
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
          <h1 className="text-2xl font-bold text-secondary-900">Charging Station Management</h1>
          <p className="text-secondary-600 mt-1">Manage charging stations and their availability</p>
        </div>
        <button
          onClick={openCreateModal}
          className="btn btn-primary btn-md flex items-center space-x-2"
        >
          <Plus className="w-4 h-4" />
          <span>Add Station</span>
        </button>
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
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Operator</th>
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
                    {(() => {
                      const assignedOperator = getAssignedOperator(station);
                      return assignedOperator ? (
                        <div className="space-y-1">
                          <div className="text-sm font-medium text-secondary-900">{assignedOperator.username}</div>
                          <div className="text-xs text-secondary-600">{assignedOperator.email}</div>
                        </div>
                      ) : (
                        <span className="text-sm text-secondary-500">No operator assigned</span>
                      );
                    })()}
                  </td>
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

                      {hasAccess('BackOffice') && (
                        <>
                          {getAssignedOperator(station) ? (
                            <button
                              onClick={() => handleUnassignOperator(station.id)}
                              className="p-2 text-warning-600 hover:bg-warning-50 rounded-lg transition-colors"
                              title="Remove operator"
                            >
                              <UserMinus className="w-4 h-4" />
                            </button>
                          ) : (
                            station.isActive && (
                              <button
                                onClick={() => openAssignModal(station)}
                                className="p-2 text-success-600 hover:bg-success-50 rounded-lg transition-colors"
                                title="Assign operator"
                              >
                                <UserPlus className="w-4 h-4" />
                              </button>
                            )
                          )}
                          {station.isActive ? (
                            <button
                              onClick={() => handleDeactivateStation(station.id)}
                              className="p-2 text-warning-600 hover:bg-warning-50 rounded-lg transition-colors"
                              title="Deactivate station"
                            >
                              <PowerOff className="w-4 h-4" />
                            </button>
                          ) : (
                            <button
                              onClick={() => handleActivateStation(station.id)}
                              className="p-2 text-success-600 hover:bg-success-50 rounded-lg transition-colors"
                              title="Activate station"
                            >
                              <Power className="w-4 h-4" />
                            </button>
                          )}
                          <button
                            onClick={() => handleDeleteStation(station.id)}
                            className="p-2 text-danger-600 hover:bg-danger-50 rounded-lg transition-colors"
                            title="Delete station"
                          >
                            <Trash2 className="w-4 h-4" />
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
        isEdit={false}
      />

      <StationModal
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false);
          setSelectedStation(null);
        }}
        onSubmit={handleUpdateStation}
        title="Edit Charging Station"
        isEdit={true}
        station={selectedStation}
      />

      <OperatorAssignModal
        isOpen={showAssignModal}
        onClose={() => {
          setShowAssignModal(false);
          setSelectedStation(null);
        }}
        station={selectedStation}
      />
    </div>
  );
};

export default ChargingStationManagement;
