import { useState, useEffect } from 'react'
import api from '../utils/api'
import { Search, Eye, UserCheck, UserX, AlertCircle, Plus, Edit } from 'lucide-react'
import { formatDate, validateNIC, validateEmail, validatePhoneNumber } from '../utils/helpers'
import toast from 'react-hot-toast'

const EVOwnerManagement = () => {
  const [evOwners, setEvOwners] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedOwner, setSelectedOwner] = useState(null)
  const [showDetailsModal, setShowDetailsModal] = useState(false)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [reactivationRequests, setReactivationRequests] = useState([])

  useEffect(() => {
    fetchEVOwners()
    fetchReactivationRequests()
  }, [])

  const fetchEVOwners = async () => {
    try {
      setLoading(true)
      const response = await api.get('/evowners')
      setEvOwners(response.data)
    } catch (error) {
      toast.error('Failed to fetch EV owners')
    } finally {
      setLoading(false)
    }
  }

  const fetchReactivationRequests = async () => {
    try {
      const response = await api.get('/evowners/reactivation-requests')
      setReactivationRequests(response.data)
    } catch (error) {
      console.log('Failed to fetch reactivation requests:', error)
    }
  }

  // Note: now receives data object from modal
  const handleCreateEVOwner = async (data) => {
    // Validation
    if (!validateNIC(data.nic)) {
      toast.error('Please enter a valid Sri Lankan NIC')
      return
    }

    if (!validateEmail(data.email)) {
      toast.error('Please enter a valid email address')
      return
    }

    if (!validatePhoneNumber(data.phoneNumber)) {
      toast.error('Please enter a valid Sri Lankan phone number')
      return
    }

    try {
      await api.post('/evowners', data)
      toast.success('EV Owner created successfully')
      setShowCreateModal(false)
      setSelectedOwner(null)
      fetchEVOwners()
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to create EV owner'
      toast.error(message)
    }
  }

  // Note: now receives data object from modal
  const handleUpdateEVOwner = async (data) => {
    try {
      const updateData = { ...data }
      if (!updateData.password) {
        delete updateData.password
      }
      // NIC cannot be updated on server; remove from payload
      delete updateData.nic

      const nicToUpdate = selectedOwner?.nic || data.nic
      await api.put(`/evowners/${nicToUpdate}`, updateData)
      toast.success('EV Owner updated successfully')
      setShowEditModal(false)
      setSelectedOwner(null)
      fetchEVOwners()
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update EV owner'
      toast.error(message)
    }
  }

  const handleActivateOwner = async (nic) => {
    try {
      await api.post(`/evowners/${nic}/activate`)
      toast.success('EV owner activated successfully')
      fetchEVOwners()
      fetchReactivationRequests()
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to activate EV owner'
      toast.error(message)
    }
  }

  const handleDeactivateOwner = async (nic) => {
    if (window.confirm('Are you sure you want to deactivate this EV owner?')) {
      try {
        await api.post(`/evowners/${nic}/deactivate`)
        toast.success('EV owner deactivated successfully')
        fetchEVOwners()
        fetchReactivationRequests()
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to deactivate EV owner'
        toast.error(message)
      }
    }
  }

  const viewOwnerDetails = (owner) => {
    setSelectedOwner(owner)
    setShowDetailsModal(true)
  }

  const openCreateModal = () => {
    setSelectedOwner(null) // ensure no owner is selected for create
    setShowCreateModal(true)
  }

  const openEditModal = (owner) => {
    // DO NOT set parent form state here — modal will initialize its own local state from owner
    setSelectedOwner(owner)
    setShowEditModal(true)
  }

  const filteredOwners = evOwners.filter(owner =>
    owner.nic.toLowerCase().includes(searchTerm.toLowerCase()) ||
    owner.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    owner.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    owner.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    owner.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    owner.phoneNumber.includes(searchTerm)
  )

  /**
   * EVOwnerModal now manages its own form state internally.
   * onSubmit will receive the final data object from the modal (not an event).
   */
  const EVOwnerModal = ({ isOpen, onClose, onSubmit, title, isEdit = false, owner = null }) => {
    const [localForm, setLocalForm] = useState({
      nic: '',
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: '',
      address: '',
      password: ''
    })

    // Initialize local form when modal opens or owner changes
    useEffect(() => {
      if (isEdit && owner) {
        setLocalForm({
          nic: owner.nic || '',
          firstName: owner.firstName || '',
          lastName: owner.lastName || '',
          email: owner.email || '',
          phoneNumber: owner.phoneNumber || '',
          address: owner.address || '',
          password: ''
        })
      } else {
        // reset for create
        setLocalForm({
          nic: '',
          firstName: '',
          lastName: '',
          email: '',
          phoneNumber: '',
          address: '',
          password: ''
        })
      }
      // we intentionally depend on isOpen, isEdit, owner
    }, [isOpen, isEdit, owner])

    if (!isOpen) return null

    const submitHandler = (e) => {
      e.preventDefault()
      // pass the localForm data object to parent onSubmit
      onSubmit(localForm)
    }

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4">{title}</h3>
          
          <form onSubmit={submitHandler} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="label text-secondary-700 mb-2 block">NIC</label>
                <input
                  type="text"
                  className="input"
                  value={localForm.nic}
                  onChange={(e) => setLocalForm(prev => ({ ...prev, nic: e.target.value }))}
                  required
                  disabled={isEdit}
                  placeholder="200012345678 or 911234567V"
                />
              </div>
              
              <div>
                <label className="label text-secondary-700 mb-2 block">Email</label>
                <input
                  type="email"
                  className="input"
                  value={localForm.email}
                  onChange={(e) => setLocalForm(prev => ({ ...prev, email: e.target.value }))}
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="label text-secondary-700 mb-2 block">First Name</label>
                <input
                  type="text"
                  className="input"
                  value={localForm.firstName}
                  onChange={(e) => setLocalForm(prev => ({ ...prev, firstName: e.target.value }))}
                  required
                />
              </div>
              
              <div>
                <label className="label text-secondary-700 mb-2 block">Last Name</label>
                <input
                  type="text"
                  className="input"
                  value={localForm.lastName}
                  onChange={(e) => setLocalForm(prev => ({ ...prev, lastName: e.target.value }))}
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="label text-secondary-700 mb-2 block">Phone Number</label>
                <input
                  type="tel"
                  className="input"
                  value={localForm.phoneNumber}
                  onChange={(e) => setLocalForm(prev => ({ ...prev, phoneNumber: e.target.value }))}
                  required
                  placeholder="0771234567 or +94771234567"
                />
              </div>
              
              <div>
                <label className="label text-secondary-700 mb-2 block">
                  Password {isEdit && "(Leave blank to keep current password)"}
                </label>
                <input
                  type="password"
                  className="input"
                  value={localForm.password}
                  onChange={(e) => setLocalForm(prev => ({ ...prev, password: e.target.value }))}
                  required={!isEdit}
                />
              </div>
            </div>

            <div>
              <label className="label text-secondary-700 mb-2 block">Address (Optional)</label>
              <textarea
                className="input"
                rows="3"
                value={localForm.address}
                onChange={(e) => setLocalForm(prev => ({ ...prev, address: e.target.value }))}
                placeholder="Enter full address"
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
                {isEdit ? 'Update EV Owner' : 'Create EV Owner'}
              </button>
            </div>
          </form>
        </div>
      </div>
    )
  }

  const DetailsModal = () => {
    if (!showDetailsModal || !selectedOwner) return null

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-xl font-semibold text-secondary-900">EV Owner Details</h3>
            <button
              onClick={() => setShowDetailsModal(false)}
              className="text-secondary-400 hover:text-secondary-600"
            >
              �
            </button>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <label className="text-sm font-medium text-secondary-700">NIC</label>
                <p className="text-secondary-900 font-mono">{selectedOwner.nic}</p>
              </div>
              
              <div>
                <label className="text-sm font-medium text-secondary-700">Full Name</label>
                <p className="text-secondary-900">{selectedOwner.fullName}</p>
              </div>
              
              <div>
                <label className="text-sm font-medium text-secondary-700">Email</label>
                <p className="text-secondary-900">{selectedOwner.email}</p>
              </div>
              
              <div>
                <label className="text-sm font-medium text-secondary-700">Phone Number</label>
                <p className="text-secondary-900">{selectedOwner.phoneNumber}</p>
              </div>
            </div>
            
            <div className="space-y-4">
              <div>
                <label className="text-sm font-medium text-secondary-700">Address</label>
                <p className="text-secondary-900">{selectedOwner.address || 'Not provided'}</p>
              </div>
              
              <div>
                <label className="text-sm font-medium text-secondary-700">Status</label>
                <div className="flex items-center space-x-2">
                  <span className={`badge ${selectedOwner.isActive ? 'badge-success' : 'badge-danger'}`}>
                    {selectedOwner.isActive ? 'Active' : 'Inactive'}
                  </span>
                  {selectedOwner.requiresReactivation && (
                    <span className="badge badge-warning">Requires Reactivation</span>
                  )}
                </div>
              </div>
              
              <div>
                <label className="text-sm font-medium text-secondary-700">Registered</label>
                <p className="text-secondary-900">{formatDate(selectedOwner.createdAt)}</p>
              </div>
              
              <div>
                <label className="text-sm font-medium text-secondary-700">Last Updated</label>
                <p className="text-secondary-900">{formatDate(selectedOwner.updatedAt)}</p>
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
            
            <button
              onClick={() => {
                setShowDetailsModal(false)
                openEditModal(selectedOwner)
              }}
              className="btn btn-warning btn-md flex-1"
            >
              Edit
            </button>
            
            {selectedOwner.isActive ? (
              <button
                onClick={() => {
                  handleDeactivateOwner(selectedOwner.nic)
                  setShowDetailsModal(false)
                }}
                className="btn btn-danger btn-md flex-1"
              >
                Deactivate
              </button>
            ) : (
              <button
                onClick={() => {
                  handleActivateOwner(selectedOwner.nic)
                  setShowDetailsModal(false)
                }}
                className="btn btn-success btn-md flex-1"
              >
                Activate
              </button>
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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-secondary-900">EV Owner Management</h1>
          <p className="text-secondary-600 mt-1">Manage electric vehicle owners and their accounts</p>
        </div>
        <button
          onClick={openCreateModal}
          className="btn btn-primary btn-md flex items-center space-x-2"
        >
          <Plus className="w-4 h-4" />
          <span>Add EV Owner</span>
        </button>
      </div>

      {/* Reactivation Requests Alert */}
      {reactivationRequests.length > 0 && (
        <div className="bg-warning-50 border border-warning-200 rounded-lg p-4">
          <div className="flex items-start space-x-3">
            <AlertCircle className="w-5 h-5 text-warning-600 mt-0.5" />
            <div>
              <h3 className="text-sm font-medium text-warning-800">
                Pending Reactivation Requests ({reactivationRequests.length})
              </h3>
              <p className="text-sm text-warning-700 mt-1">
                {reactivationRequests.length} EV owner{reactivationRequests.length > 1 ? 's' : ''} 
                {reactivationRequests.length > 1 ? ' are' : ' is'} requesting account reactivation.
              </p>
            </div>
          </div>
        </div>
      )}

      <div className="bg-white rounded-lg shadow-sm border border-secondary-200">
        <div className="p-6 border-b border-secondary-200">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-secondary-400 w-4 h-4" />
            <input
              type="text"
              placeholder="Search by NIC, name, email, or phone..."
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
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">NIC</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Full Name</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Email</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Phone</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Status</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Registered</th>
                <th className="text-right py-3 px-6 text-sm font-semibold text-secondary-900">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredOwners.map((owner) => (
                <tr key={owner.nic} className="border-b border-secondary-100 hover:bg-secondary-50 transition-colors">
                  <td className="py-4 px-6">
                    <div className="font-mono text-sm font-medium text-secondary-900">{owner.nic}</div>
                  </td>
                  <td className="py-4 px-6">
                    <div className="font-medium text-secondary-900">{owner.fullName}</div>
                  </td>
                  <td className="py-4 px-6 text-secondary-600">{owner.email}</td>
                  <td className="py-4 px-6 text-secondary-600">{owner.phoneNumber}</td>
                  <td className="py-4 px-6">
                    <div className="flex flex-col space-y-1">
                      <span className={`badge ${owner.isActive ? 'badge-success' : 'badge-danger'}`}>
                        {owner.isActive ? 'Active' : 'Inactive'}
                      </span>
                      {owner.requiresReactivation && (
                        <span className="badge badge-warning text-xs">Reactivation Pending</span>
                      )}
                    </div>
                  </td>
                  <td className="py-4 px-6 text-secondary-600">
                    {formatDate(owner.createdAt)}
                  </td>
                  <td className="py-4 px-6">
                    <div className="flex items-center space-x-2 justify-end">
                      <button
                        onClick={() => viewOwnerDetails(owner)}
                        className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                        title="View details"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                      
                      <button
                        onClick={() => openEditModal(owner)}
                        className="p-2 text-warning-600 hover:bg-warning-50 rounded-lg transition-colors"
                        title="Edit owner"
                      >
                        <Edit className="w-4 h-4" />
                      </button>
                      
                      {owner.isActive ? (
                        <button
                          onClick={() => handleDeactivateOwner(owner.nic)}
                          className="p-2 text-danger-600 hover:bg-danger-50 rounded-lg transition-colors"
                          title="Deactivate"
                        >
                          <UserX className="w-4 h-4" />
                        </button>
                      ) : (
                        <button
                          onClick={() => handleActivateOwner(owner.nic)}
                          className="p-2 text-success-600 hover:bg-success-50 rounded-lg transition-colors"
                          title="Activate"
                        >
                          <UserCheck className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredOwners.length === 0 && (
          <div className="text-center py-12">
            <p className="text-secondary-500">No EV owners found</p>
          </div>
        )}
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-secondary-200 p-6">
        <h3 className="text-lg font-semibold text-secondary-900 mb-4">Summary</h3>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="text-center p-4 bg-secondary-50 rounded-lg">
            <p className="text-2xl font-bold text-secondary-900">{evOwners.length}</p>
            <p className="text-sm text-secondary-600">Total EV Owners</p>
          </div>
          <div className="text-center p-4 bg-success-50 rounded-lg">
            <p className="text-2xl font-bold text-success-600">
              {evOwners.filter(owner => owner.isActive).length}
            </p>
            <p className="text-sm text-secondary-600">Active</p>
          </div>
          <div className="text-center p-4 bg-danger-50 rounded-lg">
            <p className="text-2xl font-bold text-danger-600">
              {evOwners.filter(owner => !owner.isActive).length}
            </p>
            <p className="text-sm text-secondary-600">Inactive</p>
          </div>
          <div className="text-center p-4 bg-warning-50 rounded-lg">
            <p className="text-2xl font-bold text-warning-600">{reactivationRequests.length}</p>
            <p className="text-sm text-secondary-600">Reactivation Requests</p>
          </div>
        </div>
      </div>

      <EVOwnerModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreateEVOwner}
        title="Create New EV Owner"
        isEdit={false}
        owner={null}
      />

      <EVOwnerModal
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false)
          setSelectedOwner(null)
        }}
        onSubmit={handleUpdateEVOwner}
        title="Edit EV Owner"
        isEdit={true}
        owner={selectedOwner}
      />

      <DetailsModal />
    </div>
  )
}

export default EVOwnerManagement
