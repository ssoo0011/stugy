import { request } from './http.js'

export function signUp(form, profileImage) {
  const body = new FormData()
  Object.entries(form).forEach(([key, value]) => body.append(key, value))
  if (profileImage) body.append('profileImage', profileImage)

  return request('/api/users', { method: 'POST', body })
}
