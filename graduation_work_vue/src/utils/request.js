import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

service.interceptors.request.use(
  (config) => {
    const url = config.url || ''
    if (url === '/auth/login' || url === '/auth/logout' || url === '/sms/send-code') {
      return config
    }

    config.headers = config.headers || {}
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

service.interceptors.response.use(
  (response) => {
    if (response.data && Object.prototype.hasOwnProperty.call(response.data, 'code')) {
      response.data.code = String(response.data.code)
    }

    const newToken = response.headers?.authorization
    if (newToken) {
      localStorage.setItem('token', newToken.startsWith('Bearer ') ? newToken.slice(7) : newToken)
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default service
