import { createContext, useContext, useState, useEffect } from 'react'
import api from '../utils/api'
import toast from 'react-hot-toast'

const AuthContext = createContext({})

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const userData = localStorage.getItem('user')
    
    if (token && userData) {
      try {
        setUser(JSON.parse(userData))
      } catch (error) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    }
    setLoading(false)
  }, [])

  const login = async (username, password) => {
    try {
      const response = await api.post('/auth/login', {
        username,
        password,
      })

      const { token, userId, username: userName, role } = response.data
      
      const userData = {
        id: userId,
        username: userName,
        role,
      }

      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(userData))
      setUser(userData)

      toast.success(`Welcome back, ${userName}!`)
      return true
    } catch (error) {
      const message = error.response?.data?.message || 'Login failed'
      toast.error(message)
      return false
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
    toast.success('Logged out successfully')
  }

  const isBackOffice = () => {
    return user?.role === 'BackOffice'
  }

  const isStationOperator = () => {
    return user?.role === 'StationOperator'
  }

  const hasAccess = (requiredRole) => {
    if (!user) return false
    if (requiredRole === 'BackOffice') return isBackOffice()
    if (requiredRole === 'StationOperator') return isStationOperator() || isBackOffice()
    return true
  }

  const value = {
    user,
    login,
    logout,
    loading,
    isAuthenticated: !!user,
    isBackOffice,
    isStationOperator,
    hasAccess,
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}