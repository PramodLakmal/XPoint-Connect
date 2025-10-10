import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider } from './contexts/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import HomeRedirect from './pages/HomeRedirect'
import UserManagement from './pages/UserManagement'
import EVOwnerManagement from './pages/EVOwnerManagement'
import ChargingStationManagement from './pages/ChargingStationManagement'
import StationOperatorManagement from './pages/StationOperatorManagement'
import BookingManagement from './pages/BookingManagement'
import OperatorDashboard from './pages/OperatorDashboard'
import OperatorBookingManagement from './pages/OperatorBookingManagement'
import './App.css'

// Main application component that sets up routing and authentication context
function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-secondary-50">
          <Toaster 
            position="top-right"
            toastOptions={{
              duration: 4000,
              style: {
                background: '#fff',
                color: '#374151',
                border: '1px solid #e5e7eb',
                borderRadius: '0.5rem',
                boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
              },
              success: {
                iconTheme: {
                  primary: '#10b981',
                  secondary: '#fff',
                },
              },
              error: {
                iconTheme: {
                  primary: '#ef4444',
                  secondary: '#fff',
                },
              },
            }}
          />
          
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<HomeRedirect />} />
            
            <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/users" element={<UserManagement />} />
              <Route path="/evowners" element={<EVOwnerManagement />} />
              <Route path="/stations" element={<ChargingStationManagement />} />
              <Route path="/operators" element={<StationOperatorManagement />} />
              <Route path="/bookings" element={<BookingManagement />} />
              {/* Operator-only simplified routes (guarded by sidebar filtering) */}
              <Route path="/operator/dashboard" element={<OperatorDashboard />} />
              <Route path="/operator/bookings" element={<OperatorBookingManagement />} />
            </Route>
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  )
}

export default App