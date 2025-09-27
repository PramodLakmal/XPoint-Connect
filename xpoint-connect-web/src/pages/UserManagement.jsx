import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import api from '../utils/api'
import { Plus, Edit, Trash2, Search, Eye, EyeOff, UserCheck, UserX } from 'lucide-react'
import toast from 'react-hot-toast'

const UserManagement = () => {
  const { hasAccess } = useAuth()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [selectedUser, setSelectedUser] = useState(null)
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    role: 'StationOperator'
  })
  const [showPassword, setShowPassword] = useState(false)

  // Check access permission
  if (!hasAccess('BackOffice')) {
    return (
      <div className="text-center py-12">
        <h1 className="text-2xl font-bold text-secondary-900 mb-4">Access Denied</h1>
        <p className="text-secondary-600">You don't have permission to manage users.</p>
      </div>
    )
  }

  useEffect(() => {
    fetchUsers()
  }, [])

  const fetchUsers = async () => {
    try {
      setLoading(true)
      const response = await api.get('/users')
      setUsers(response.data)
    } catch (error) {
      toast.error('Failed to fetch users')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateUser = async (e) => {
    e.preventDefault()
    
    try {
      await api.post('/auth/register', formData)
      toast.success(`${formData.role} user created successfully`)
      setShowCreateModal(false)
      resetForm()
      fetchUsers()
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to create user'
      toast.error(message)
    }
  }

  const handleUpdateUser = async (e) => {
    e.preventDefault()
    
    try {
      const updateData = { ...formData }
      if (!updateData.password) {
        delete updateData.password
      }
      
      await api.put(`/users/${selectedUser.id}`, updateData)
      toast.success('User updated successfully')
      setShowEditModal(false)
      resetForm()
      fetchUsers()
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update user'
      toast.error(message)
    }
  }

  const handleDeleteUser = async (userId) => {
    if (window.confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      try {
        await api.delete(`/users/${userId}`)
        toast.success('User deleted successfully')
        fetchUsers()
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to delete user'
        toast.error(message)
      }
    }
  }

  const handleToggleUserStatus = async (userId, isActive) => {
    try {
      const action = isActive ? 'deactivate' : 'activate'
      await api.post(`/users/${userId}/${action}`)
      toast.success(`User ${action}d successfully`)
      fetchUsers()
    } catch (error) {
      const message = error.response?.data?.message || `Failed to ${isActive ? 'deactivate' : 'activate'} user`
      toast.error(message)
    }
  }

  const resetForm = () => {
    setFormData({
      username: '',
      email: '',
      password: '',
      role: 'StationOperator'
    })
    setSelectedUser(null)
    setShowPassword(false)
  }

  const openCreateModal = () => {
    resetForm()
    setShowCreateModal(true)
  }

  const openEditModal = (user) => {
    setSelectedUser(user)
    setFormData({
      username: user.username,
      email: user.email,
      password: '',
      role: user.role
    })
    setShowEditModal(true)
  }

  const filteredUsers = users.filter(user =>
    user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.role.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const UserModal = ({ isOpen, onClose, onSubmit, title, isEdit = false }) => {
    if (!isOpen) return null

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4">
          <h3 className="text-lg font-semibold text-secondary-900 mb-4">{title}</h3>
          
          <form onSubmit={onSubmit} className="space-y-4">
            <div>
              <label className="label text-secondary-700 mb-2 block">Username</label>
              <input
                type="text"
                className="input"
                value={formData.username}
                onChange={(e) => setFormData(prev => ({ ...prev, username: e.target.value }))}
                required
                placeholder="Enter unique username"
              />
            </div>
            
            <div>
              <label className="label text-secondary-700 mb-2 block">Email</label>
              <input
                type="email"
                className="input"
                value={formData.email}
                onChange={(e) => setFormData(prev => ({ ...prev, email: e.target.value }))}
                required
                placeholder="user@example.com"
              />
            </div>
            
            <div>
              <label className="label text-secondary-700 mb-2 block">Role</label>
              <select
                className="input"
                value={formData.role}
                onChange={(e) => setFormData(prev => ({ ...prev, role: e.target.value }))}
                required
              >
                <option value="StationOperator">Station Operator</option>
                <option value="BackOffice">BackOffice Administrator</option>
              </select>
              <p className="text-xs text-secondary-500 mt-1">
                {formData.role === 'BackOffice' 
                  ? 'Full system access including user management' 
                  : 'Access to stations, bookings, and EV owners only'
                }
              </p>
            </div>
            
            <div>
              <label className="label text-secondary-700 mb-2 block">
                Password {isEdit && "(Leave blank to keep current password)"}
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="input pr-10"
                  value={formData.password}
                  onChange={(e) => setFormData(prev => ({ ...prev, password: e.target.value }))}
                  required={!isEdit}
                  placeholder={isEdit ? "Leave blank to keep current" : "Enter strong password"}
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-5 w-5 text-secondary-400" />
                  ) : (
                    <Eye className="h-5 w-5 text-secondary-400" />
                  )}
                </button>
              </div>
              {!isEdit && (
                <p className="text-xs text-secondary-500 mt-1">
                  Password should be at least 6 characters with letters and numbers
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
                {isEdit ? 'Update User' : 'Create User'}
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
          <h1 className="text-2xl font-bold text-secondary-900">User Management</h1>
          <p className="text-secondary-600 mt-1">Manage system users and their roles</p>
        </div>
        <button
          onClick={openCreateModal}
          className="btn btn-primary btn-md flex items-center space-x-2"
        >
          <Plus className="w-4 h-4" />
          <span>Add User</span>
        </button>
      </div>

      {/* Role Information Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-primary-50 border border-primary-200 rounded-lg p-4">
          <h3 className="font-semibold text-primary-800 mb-2">BackOffice Administrator</h3>
          <ul className="text-sm text-primary-700 space-y-1">
            <li>• Complete system administration access</li>
            <li>• User management capabilities</li>
            <li>• Full access to all features</li>
            <li>• System configuration and monitoring</li>
          </ul>
        </div>
        <div className="bg-secondary-50 border border-secondary-200 rounded-lg p-4">
          <h3 className="font-semibold text-secondary-800 mb-2">Station Operator</h3>
          <ul className="text-sm text-secondary-700 space-y-1">
            <li>• EV owner account management</li>
            <li>• Charging station operations</li>
            <li>• Booking management and approval</li>
            <li>• Limited to operational tasks</li>
          </ul>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-secondary-200">
        <div className="p-6 border-b border-secondary-200">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-secondary-400 w-4 h-4" />
            <input
              type="text"
              placeholder="Search users by username, email, or role..."
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
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Username</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Email</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Role</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Status</th>
                <th className="text-left py-3 px-6 text-sm font-semibold text-secondary-900">Created</th>
                <th className="text-right py-3 px-6 text-sm font-semibold text-secondary-900">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((user) => (
                <tr key={user.id} className="border-b border-secondary-100 hover:bg-secondary-50 transition-colors">
                  <td className="py-4 px-6">
                    <div className="font-medium text-secondary-900">{user.username}</div>
                  </td>
                  <td className="py-4 px-6 text-secondary-600">{user.email}</td>
                  <td className="py-4 px-6">
                    <span className={`badge ${user.role === 'BackOffice' ? 'badge-primary' : 'badge-secondary'}`}>
                      {user.role === 'BackOffice' ? 'BackOffice Admin' : 'Station Operator'}
                    </span>
                  </td>
                  <td className="py-4 px-6">
                    <span className={`badge ${user.isActive ? 'badge-success' : 'badge-danger'}`}>
                      {user.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="py-4 px-6 text-secondary-600">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </td>
                  <td className="py-4 px-6">
                    <div className="flex items-center space-x-2 justify-end">
                      <button
                        onClick={() => openEditModal(user)}
                        className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                        title="Edit user"
                      >
                        <Edit className="w-4 h-4" />
                      </button>
                      
                      <button
                        onClick={() => handleToggleUserStatus(user.id, user.isActive)}
                        className={`p-2 rounded-lg transition-colors ${
                          user.isActive 
                            ? 'text-warning-600 hover:bg-warning-50' 
                            : 'text-success-600 hover:bg-success-50'
                        }`}
                        title={user.isActive ? 'Deactivate user' : 'Activate user'}
                      >
                        {user.isActive ? <UserX className="w-4 h-4" /> : <UserCheck className="w-4 h-4" />}
                      </button>
                      
                      <button
                        onClick={() => handleDeleteUser(user.id)}
                        className="p-2 text-danger-600 hover:bg-danger-50 rounded-lg transition-colors"
                        title="Delete user"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredUsers.length === 0 && (
          <div className="text-center py-12">
            <p className="text-secondary-500">No users found</p>
          </div>
        )}
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="text-center p-4 bg-secondary-50 rounded-lg border border-secondary-200">
          <p className="text-2xl font-bold text-secondary-900">{users.length}</p>
          <p className="text-sm text-secondary-600">Total Users</p>
        </div>
        <div className="text-center p-4 bg-primary-50 rounded-lg border border-primary-200">
          <p className="text-2xl font-bold text-primary-600">
            {users.filter(user => user.role === 'BackOffice').length}
          </p>
          <p className="text-sm text-secondary-600">BackOffice Admins</p>
        </div>
        <div className="text-center p-4 bg-secondary-50 rounded-lg border border-secondary-200">
          <p className="text-2xl font-bold text-secondary-600">
            {users.filter(user => user.role === 'StationOperator').length}
          </p>
          <p className="text-sm text-secondary-600">Station Operators</p>
        </div>
        <div className="text-center p-4 bg-success-50 rounded-lg border border-success-200">
          <p className="text-2xl font-bold text-success-600">
            {users.filter(user => user.isActive).length}
          </p>
          <p className="text-sm text-secondary-600">Active Users</p>
        </div>
      </div>

      <UserModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreateUser}
        title="Create New User"
      />

      <UserModal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        onSubmit={handleUpdateUser}
        title="Edit User"
        isEdit={true}
      />
    </div>
  )
}

export default UserManagement