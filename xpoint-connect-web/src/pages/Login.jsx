import { useState, useEffect } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { Eye, EyeOff, Zap, Lock, User } from 'lucide-react'
import toast from 'react-hot-toast'

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  })
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { login, isAuthenticated } = useAuth()

  // Redirect if already authenticated
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!formData.username || !formData.password) {
      toast.error('Please fill in all fields')
      return
    }

    setIsLoading(true)
    
    try {
      const success = await login(formData.username, formData.password)
      if (success) {
        // Redirect will be handled by the auth context
      }
    } catch (error) {
      // Error handling is done in the auth context
    } finally {
      setIsLoading(false)
    }
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <div className="flex items-center justify-center w-16 h-16 bg-white rounded-full mx-auto mb-4">
            <Zap className="w-8 h-8 text-primary-600" />
          </div>
          <h2 className="text-3xl font-bold text-white mb-2">
            XPoint Connect
          </h2>
          <p className="text-primary-100">
            EV Charging Management System
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-xl p-8">
          <div className="text-center mb-6">
            <h3 className="text-2xl font-semibold text-secondary-900 mb-2">
              Welcome Back
            </h3>
            <p className="text-secondary-600">
              Sign in to your account to continue
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="username" className="label text-secondary-700 mb-2 block">
                Username
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <User className="h-5 w-5 text-secondary-400" />
                </div>
                <input
                  id="username"
                  name="username"
                  type="text"
                  required
                  className="input pl-10"
                  placeholder="Enter your username"
                  value={formData.username}
                  onChange={handleChange}
                  autoComplete="username"
                />
              </div>
            </div>

            <div>
              <label htmlFor="password" className="label text-secondary-700 mb-2 block">
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-5 w-5 text-secondary-400" />
                </div>
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  className="input pl-10 pr-10"
                  placeholder="Enter your password"
                  value={formData.password}
                  onChange={handleChange}
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-5 w-5 text-secondary-400 hover:text-secondary-600" />
                  ) : (
                    <Eye className="h-5 w-5 text-secondary-400 hover:text-secondary-600" />
                  )}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full btn btn-primary btn-md flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <>
                  <div className="spinner"></div>
                  <span>Signing in...</span>
                </>
              ) : (
                <span>Sign In</span>
              )}
            </button>
          </form>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-secondary-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-secondary-500">Demo Credentials</span>
              </div>
            </div>
            <div className="mt-4 grid grid-cols-1 gap-3">
              <div className="bg-secondary-50 p-3 rounded-lg">
                <p className="text-xs font-semibold text-secondary-700 mb-1">BackOffice Admin</p>
                <p className="text-xs text-secondary-600">Username: admin</p>
                <p className="text-xs text-secondary-600">Password: Admin123!</p>
              </div>
              <div className="bg-secondary-50 p-3 rounded-lg">
                <p className="text-xs font-semibold text-secondary-700 mb-1">Station Operator</p>
                <p className="text-xs text-secondary-600">Username: operator1</p>
                <p className="text-xs text-secondary-600">Password: Operator123!</p>
              </div>
            </div>
          </div>
        </div>

        <p className="text-center text-primary-100 text-sm">
          © 2024 XPoint Connect. All rights reserved.
        </p>
      </div>
    </div>
  )
}

export default Login