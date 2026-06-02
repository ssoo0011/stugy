import { request } from './http.js'

export function getCurrentUser() {
  return request('/api/auth/me')
}

export function login(credentials) {
  return request('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials),
  })
}

export function logout() {
  return request('/api/auth/logout', { method: 'POST' })
}
