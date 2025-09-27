export const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

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

export const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

export const isWithin12Hours = (dateTime) => {
  if (!dateTime) return false
  const now = new Date()
  const targetDate = new Date(dateTime)
  const diffInHours = (targetDate - now) / (1000 * 60 * 60)
  return diffInHours >= 12
}

export const isWithin7Days = (dateTime) => {
  if (!dateTime) return false
  const now = new Date()
  const targetDate = new Date(dateTime)
  const diffInDays = (targetDate - now) / (1000 * 60 * 60 * 24)
  return diffInDays <= 7 && diffInDays >= 0
}

export const isValidReservationDate = (dateTime) => {
  if (!dateTime) return false
  const now = new Date()
  const targetDate = new Date(dateTime)
  const diffInHours = (targetDate - now) / (1000 * 60 * 60)
  // Must be in the future and within 7 days
  return diffInHours > 0 && diffInHours <= (7 * 24)
}

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

export const capitalize = (str) => {
  if (!str) return ''
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()
}

export const validateNIC = (nic) => {
  // Sri Lankan NIC validation
  const oldNICPattern = /^[0-9]{9}[vVxX]$/
  const newNICPattern = /^[0-9]{12}$/
  return oldNICPattern.test(nic) || newNICPattern.test(nic)
}

export const validateEmail = (email) => {
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailPattern.test(email)
}

export const validatePhoneNumber = (phone) => {
  // Sri Lankan phone number validation
  const phonePattern = /^(\+94|0)?[1-9][0-9]{8}$/
  return phonePattern.test(phone)
}

export const validatePassword = (password) => {
  // At least 6 characters, contain at least one letter and one number
  const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,}$/
  return passwordPattern.test(password)
}

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

export const debounce = (func, delay) => {
  let timeoutId
  return (...args) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func.apply(null, args), delay)
  }
}

export const generateRandomString = (length = 8) => {
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * characters.length))
  }
  return result
}