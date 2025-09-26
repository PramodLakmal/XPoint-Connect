import { useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { 
  LayoutDashboard, 
  Users, 
  UserCog, 
  Zap, 
  Calendar,
  ChevronLeft,
  ChevronRight,
  Battery
} from 'lucide-react'

const Sidebar = () => {
  const [collapsed, setCollapsed] = useState(false)
  const { user, hasAccess } = useAuth()
  const location = useLocation()

  const navigation = [
    {
      name: 'Dashboard',
      href: '/dashboard',
      icon: LayoutDashboard,
      requiredRole: null
    },
    {
      name: 'User Management',
      href: '/users',
      icon: Users,
      requiredRole: 'BackOffice'
    },
    {
      name: 'EV Owners',
      href: '/evowners',
      icon: UserCog,
      requiredRole: null
    },
    {
      name: 'Charging Stations',
      href: '/stations',
      icon: Battery,
      requiredRole: null
    },
    {
      name: 'Bookings',
      href: '/bookings',
      icon: Calendar,
      requiredRole: null
    },
  ]

  const filteredNavigation = navigation.filter(item => 
    !item.requiredRole || hasAccess(item.requiredRole)
  )

  return (
    <div className={`bg-white shadow-lg transition-all duration-300 ${collapsed ? 'w-16' : 'w-64'}`}>
      <div className="flex items-center justify-between p-4 border-b border-secondary-200">
        {!collapsed && (
          <div className="flex items-center space-x-2">
            <div className="flex items-center justify-center w-8 h-8 bg-primary-600 rounded-lg">
              <Zap className="w-5 h-5 text-white" />
            </div>
            <span className="text-lg font-semibold text-secondary-900">
              XPoint Connect
            </span>
          </div>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="p-1.5 rounded-lg hover:bg-secondary-100 transition-colors"
        >
          {collapsed ? (
            <ChevronRight className="w-5 h-5 text-secondary-600" />
          ) : (
            <ChevronLeft className="w-5 h-5 text-secondary-600" />
          )}
        </button>
      </div>

      <nav className="mt-6">
        <div className="px-2 space-y-1">
          {filteredNavigation.map((item) => {
            const isActive = location.pathname === item.href
            return (
              <NavLink
                key={item.name}
                to={item.href}
                className={`group flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-all duration-200 ${
                  isActive
                    ? 'bg-primary-100 text-primary-700 border-r-2 border-primary-600'
                    : 'text-secondary-600 hover:bg-secondary-100 hover:text-secondary-900'
                }`}
              >
                <item.icon className={`flex-shrink-0 w-5 h-5 ${collapsed ? '' : 'mr-3'}`} />
                {!collapsed && (
                  <span className="truncate">{item.name}</span>
                )}
              </NavLink>
            )
          })}
        </div>
      </nav>

      {!collapsed && (
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-secondary-200 bg-white">
          <div className="flex items-center space-x-3">
            <div className="flex-shrink-0">
              <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                <span className="text-sm font-medium text-primary-700">
                  {user?.username?.charAt(0).toUpperCase()}
                </span>
              </div>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-secondary-900 truncate">
                {user?.username}
              </p>
              <p className="text-xs text-secondary-500 truncate">
                {user?.role}
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default Sidebar