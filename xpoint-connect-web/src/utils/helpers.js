// Format date to readable string format (e.g., "Jan 15, 2024")
export const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

// Format date and time to readable string format (e.g., "Jan 15, 2024, 2:30 PM")
export const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// Format time to readable string format (e.g., "2:30 PM")
export const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

// Check if the given datetime is at least 12 hours in the future
export const isWithin12Hours = (dateTime) => {
  if (!dateTime) return false
  const now = new Date()
  const targetDate = new Date(dateTime)
  const diffInHours = (targetDate - now) / (1000 * 60 * 60)
  return diffInHours >= 12
}

// Check if the given datetime is within the next 7 days
export const isWithin7Days = (dateTime) => {
  if (!dateTime) return false
  const now = new Date()
  const targetDate = new Date(dateTime)
  const diffInDays = (targetDate - now) / (1000 * 60 * 60 * 24)
  return diffInDays <= 7 && diffInDays >= 0
}

// Validate if the given datetime is valid for making a reservation (future date within 7 days)
export const isValidReservationDate = (dateTime) => {
  if (!dateTime) return false
  const now = new Date()
  const targetDate = new Date(dateTime)
  const diffInHours = (targetDate - now) / (1000 * 60 * 60)
  // Must be in the future and within 7 days
  return diffInHours > 0 && diffInHours <= (7 * 24)
}

// Check if a booking can be updated based on status and time constraints
export const canUpdateBooking = (reservationDateTime, currentStatus) => {
  if (!reservationDateTime) return false
  
  // Cannot update if cancelled or completed
  const nonEditableStatuses = ['cancelled', 'completed']
  if (nonEditableStatuses.includes(currentStatus?.toLowerCase())) {
    return false
  }
  
  // Must be at least 12 hours before reservation
  return isWithin12Hours(reservationDateTime)
}

// Check if a booking can be cancelled based on status and time constraints
export const canCancelBooking = (reservationDateTime, currentStatus) => {
  if (!reservationDateTime) return false
  
  // Cannot cancel if already cancelled or completed
  const nonCancellableStatuses = ['cancelled', 'completed']
  if (nonCancellableStatuses.includes(currentStatus?.toLowerCase())) {
    return false
  }
  
  // Must be at least 12 hours before reservation
  return isWithin12Hours(reservationDateTime)
}

// Capitalize the first letter of a string and make the rest lowercase
export const capitalize = (str) => {
  if (!str) return ''
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()
}

// Validate Sri Lankan National Identity Card (NIC) format
export const validateNIC = (nic) => {
  // Sri Lankan NIC validation
  const oldNICPattern = /^[0-9]{9}[vVxX]$/
  const newNICPattern = /^[0-9]{12}$/
  return oldNICPattern.test(nic) || newNICPattern.test(nic)
}

// Validate email address format using regex pattern
export const validateEmail = (email) => {
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailPattern.test(email)
}

// Validate Sri Lankan phone number format
export const validatePhoneNumber = (phone) => {
  // Sri Lankan phone number validation
  const phonePattern = /^(\+94|0)?[1-9][0-9]{8}$/
  return phonePattern.test(phone)
}

// Validate password strength (minimum 6 characters with letters and numbers)
export const validatePassword = (password) => {
  // At least 6 characters, contain at least one letter and one number
  const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,}$/
  return passwordPattern.test(password)
}

// Validate reservation time and return validation result with message
export const getReservationTimeValidation = (dateTime) => {
  if (!dateTime) {
    return { isValid: false, message: 'Date and time are required' }
  }

  const now = new Date()
  const targetDate = new Date(dateTime)
  const diffInHours = (targetDate - now) / (1000 * 60 * 60)
  const diffInDays = diffInHours / 24

  if (diffInHours <= 0) {
    return { isValid: false, message: 'Reservation must be in the future' }
  }

  if (diffInHours > (7 * 24)) {
    return { isValid: false, message: 'Reservation must be within 7 days from now' }
  }

  return { isValid: true, message: 'Valid reservation time' }
}

// Get color class name based on booking status for UI styling
export const getBookingStatusColor = (status) => {
  switch (status?.toLowerCase()) {
    case 'pending':
      return 'warning'
    case 'approved':
      return 'success'
    case 'active':
      return 'primary'
    case 'completed':
      return 'success'
    case 'cancelled':
      return 'danger'
    default:
      return 'secondary'
  }
}

// Create a debounced version of a function to limit execution frequency
export const debounce = (func, delay) => {
  let timeoutId
  return (...args) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func.apply(null, args), delay)
  }
}

// Generate a random alphanumeric string of specified length
export const generateRandomString = (length = 8) => {
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * characters.length))
  }
  return result
}