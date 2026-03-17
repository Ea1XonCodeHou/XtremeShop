import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

// 请求拦截器：自动携带 sessionId
request.interceptors.request.use((config) => {
  const sessionId = sessionStorage.getItem('sessionId')
  if (sessionId) {
    config.headers['X-Session-Id'] = sessionId
  }
  return config
})

export default request
