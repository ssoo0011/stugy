import { request } from './http.js'

export function getStudyGroups() {
  return request('/api/study-groups')
}

export function getMyStudyGroups() {
  return request('/api/study-groups/mine')
}

export function getStudyGroupNotifications() {
  return request('/api/study-groups/notifications')
}

export function readStudyGroupNotifications() {
  return request('/api/study-groups/notifications/read', {
    method: 'POST',
  })
}

export function createStudyGroup(form) {
  return request('/api/study-groups', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form),
  })
}

export function applyToStudyGroup(studyGroupId) {
  return request(`/api/study-groups/${studyGroupId}/applications`, {
    method: 'POST',
  })
}

export function getStudyGroupManagement(studyGroupId) {
  return request(`/api/study-groups/${studyGroupId}/management`)
}

export function createStudyGroupSchedule(studyGroupId, form) {
  return request(`/api/study-groups/${studyGroupId}/schedules`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form),
  })
}

export function updateStudyGroupSchedule(studyGroupId, scheduleId, form) {
  return request(`/api/study-groups/${studyGroupId}/schedules/${scheduleId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form),
  })
}

export function deleteStudyGroupSchedule(studyGroupId, scheduleId) {
  return request(`/api/study-groups/${studyGroupId}/schedules/${scheduleId}`, {
    method: 'DELETE',
  })
}

export function acceptStudyGroupApplication(studyGroupId, applicationId) {
  return request(`/api/study-groups/${studyGroupId}/applications/${applicationId}/accept`, {
    method: 'POST',
  })
}

export function rejectStudyGroupApplication(studyGroupId, applicationId) {
  return request(`/api/study-groups/${studyGroupId}/applications/${applicationId}/reject`, {
    method: 'POST',
  })
}
