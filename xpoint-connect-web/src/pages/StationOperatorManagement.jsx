import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import api from '../utils/api';
import { Plus, Edit, Trash2, Search, User, Mail, Phone, MapPin } from 'lucide-react';
import toast from 'react-hot-toast';

const StationOperatorManagement = () => {
  const { hasAccess } = useAuth();
  const [operators, setOperators] = useState([]);
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedOperator, setSelectedOperator] = useState(null);

  useEffect(() => {
    fetchOperators();
    fetchStations();
  }, []);

  const fetchOperators = async () => {
    try {
      setLoading(true);
      const response = await api.get('/users');
      
      // Filter only operators (role "StationOperator") and handle both field name formats
      const operatorUsers = response.data.filter(user => 
        (user.role === "StationOperator" || user.Role === "StationOperator" || user.role === 1 || user.Role === 1) && 
        (user.IsActive !== false && user.isActive !== false)
      );
      
      setOperators(operatorUsers);
    } catch (error) {
      console.error('Error fetching operators:', error);
      toast.error('Failed to fetch operators');
    } finally {
      setLoading(false);
    }
  };

  const fetchStations = async () => {
    try {
      const response = await api.get('/chargingstations');
      setStations(response.data);
    } catch (error) {
      console.log('Failed to fetch charging stations:', error);
    }
  };

  const handleCreateOperator = async (formData) => {
    try {
      const operatorPayload = {
        Username: formData.username,
        Email: formData.email,
        Password: formData.password,
        Role: 1, // Operator role
      };

      await api.post('/users', operatorPayload);
      toast.success('Operator created successfully');
      setShowCreateModal(false);
      fetchOperators();
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to create operator';
      toast.error(message);
    }
  };

  const handleUpdateOperator = async (formData) => {
    try {
      const updatePayload = {
        Username: formData.username,
        Email: formData.email,
      };
      
      if (formData.password) {
        updatePayload.Password = formData.password;
      }

      await api.put(`/users/${selectedOperator.id}`, updatePayload);
      toast.success('Operator updated successfully');
      setShowEditModal(false);
      setSelectedOperator(null);
      fetchOperators();
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update operator';
      toast.error(message);
    }
  };

  const handleDeleteOperator = async (operatorId) => {
    if (window.confirm('Are you sure you want to delete this operator? This action cannot be undone.')) {
      try {
        await api.delete(`/users/${operatorId}`);
        toast.success('Operator deleted successfully');
        fetchOperators();
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to delete operator';
        toast.error(message);
      }
    }
  };

  const openCreateModal = () => {
    setSelectedOperator(null);
    setShowCreateModal(true);
  };

  const openEditModal = (operator) => {
    setSelectedOperator(operator);
    setShowEditModal(true);
  };

  const getAssignedStations = (operatorId) => {
    return stations.filter(station => station.operatorId === operatorId);
  };

  const filteredOperators = operators.filter(
    (operator) => {
      const username = operator.username || operator.Username || '';
      const email = operator.email || operator.Email || '';
      return username.toLowerCase().includes(searchTerm.toLowerCase()) ||
             email.toLowerCase().includes(searchTerm.toLowerCase());
    }
  );

  const OperatorModal = ({ isOpen, onClose, onSubmit, title, isEdit = false, operator = null }) => {
    const [formData, setFormData] = useState({
      username: '',
      email: '',
      password: '',
    });

    useEffect(() => {
      if (isEdit && operator) {
        setFormData({
          username: operator.username || operator.Username || '',
          email: operator.email || operator.Email || '',
          password: '',
        });
      } else {
        setFormData({
          username: '',
          email: '',
          password: '',
        });
      }
    }, [isOpen, isEdit, operator]);

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
        <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4">{title}</h3>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label text-secondary-700 mb-2 block">Username</label>
              <input
                type="text"
                name="username"
                className="input"
                value={formData.username}
                onChange={handleInputChange}
                required
              />
            </div>

            <div>
              <label className="label text-secondary-700 mb-2 block">Email</label>
              <input
                type="email"
                name="email"
                className="input"
                value={formData.email}
                onChange={handleInputChange}
                required
              />
            </div>

            <div>
              <label className="label text-secondary-700 mb-2 block">Password</label>
              <input
                type="password"
                name="password"
                className="input"
                value={formData.password}
                onChange={handleInputChange}
                required={!isEdit}
                placeholder={isEdit ? 'Leave blank to keep current password' : ''}
                minLength={6}
              />
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
          <h1 className="text-2xl font-bold text-secondary-900">Station Operator Management</h1>
          <p className="text-secondary-600 mt-1">Manage charging station operators</p>
        </div>
        {hasAccess('BackOffice') && (
          <button
            onClick={openCreateModal}
            className="btn btn-primary btn-md flex items-center space-x-2"
          >
            <Plus className="w-4 h-4" />
            <span>Add Operator</span>
          </button>
        )}
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-secondary-200">
        <div className="p-6 border-b border-secondary-200">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-secondary-400 w-4 h-4" />
            <input
              type="text"
              placeholder="Search operators..."
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
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Operator</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Contact</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Assigned Stations</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Status</th>
                <th className="text-right py-3 px-6 text-sm font-semibold text-secondary-900">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredOperators.map((operator) => {
                const assignedStations = getAssignedStations(operator.id);
                return (
                  <tr key={operator.id} className="border-b border-secondary-100 hover:bg-secondary-50 transition-colors">
                    <td className="py-4 px-6">
                      <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                          <User className="w-5 h-5 text-primary-600" />
                        </div>
                        <div>
                          <div className="font-medium text-secondary-900">{operator.username || operator.Username}</div>
                          <div className="text-sm text-secondary-600">ID: {operator.id?.slice(-8)}</div>
                        </div>
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <div className="space-y-1">
                        <div className="flex items-center space-x-2 text-sm text-secondary-900">
                          <Mail className="w-4 h-4 text-secondary-400" />
                          <span>{operator.email || operator.Email}</span>
                        </div>
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <div className="space-y-1">
                        {assignedStations.length > 0 ? (
                          assignedStations.map((station) => (
                            <div key={station.id} className="flex items-center space-x-2 text-sm">
                              <MapPin className="w-4 h-4 text-secondary-400" />
                              <span className="text-secondary-900">{station.name}</span>
                              <span className={`badge ${station.isActive ? 'badge-success' : 'badge-danger'}`}>
                                {station.isActive ? 'Active' : 'Inactive'}
                              </span>
                            </div>
                          ))
                        ) : (
                          <span className="text-sm text-secondary-500">No stations assigned</span>
                        )}
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <span className={`badge ${(operator.isActive !== false && operator.IsActive !== false) ? 'badge-success' : 'badge-danger'}`}>
                        {(operator.isActive !== false && operator.IsActive !== false) ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="py-4 px-6">
                      <div className="flex items-center space-x-2 justify-end">
                        {hasAccess('BackOffice') && (
                          <>
                            <button
                              onClick={() => openEditModal(operator)}
                              className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                              title="Edit operator"
                            >
                              <Edit className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => handleDeleteOperator(operator.id)}
                              className="p-2 text-danger-600 hover:bg-danger-50 rounded-lg transition-colors"
                              title="Delete operator"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {filteredOperators.length === 0 && (
          <div className="text-center py-12">
            <User className="mx-auto h-12 w-12 text-secondary-400 mb-4" />
            <p className="text-secondary-500">No operators found</p>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white rounded-lg border border-secondary-200 p-4 text-center">
          <p className="text-2xl font-bold text-secondary-900">{operators.length}</p>
          <p className="text-sm text-secondary-600">Total Operators</p>
        </div>
        <div className="bg-white rounded-lg border border-secondary-200 p-4 text-center">
          <p className="text-2xl font-bold text-secondary-900">
            {operators.filter(op => getAssignedStations(op.id).length > 0).length}
          </p>
          <p className="text-sm text-secondary-600">Assigned Operators</p>
        </div>
        <div className="bg-white rounded-lg border border-secondary-200 p-4 text-center">
          <p className="text-2xl font-bold text-secondary-900">
            {operators.filter(op => getAssignedStations(op.id).length === 0).length}
          </p>
          <p className="text-sm text-secondary-600">Unassigned Operators</p>
        </div>
      </div>

      <OperatorModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreateOperator}
        title="Create New Operator"
        isEdit={false}
      />

      <OperatorModal
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false);
          setSelectedOperator(null);
        }}
        onSubmit={handleUpdateOperator}
        title="Edit Operator"
        isEdit={true}
        operator={selectedOperator}
      />
    </div>
  );
};

export default StationOperatorManagement;
