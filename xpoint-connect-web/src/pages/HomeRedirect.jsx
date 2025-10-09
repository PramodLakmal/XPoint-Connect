import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

const HomeRedirect = () => {
  const { isStationOperator } = useAuth()
  return isStationOperator() ? (
    <Navigate to="/operator/dashboard" replace />
  ) : (
    <Navigate to="/dashboard" replace />
  )
}

export default HomeRedirect


