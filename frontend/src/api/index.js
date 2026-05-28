import axios from 'axios'

let _authToken = ''

export function setAuthToken(token) {
  _authToken = token
}

export function getAuthToken() {
  return _authToken
}

const api = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(
  config => {
    if (_authToken) config.headers.Authorization = `Bearer ${_authToken}`
    return config
  },
  error => Promise.reject(error)
)

api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 401) {
      _authToken = ''
      window.location.hash = '#/'
    }
    return data
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export function authApi() {
  return {
    login: (data) => api.post('/auth/login', data),
    register: (data) => api.post('/auth/register', data)
  }
}

export function userApi() {
  return {
    getProfile: () => api.get('/user/info'),
    updateProfile: (data) => api.put('/user/profile', data),
    changePassword: (data) => api.put('/user/password', data),
    uploadAvatar: (formData) => api.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export function missingPersonApi() {
  return {
    list: (params) => api.get('/missing-persons', { params }),
    searchAll: (params) => api.get('/missing-persons', { params }),
    getMyList: (params) => api.get('/missing-persons/my', { params }),
    getDetail: (id) => api.get(`/missing-persons/${id}`),
    create: (data) => api.post('/missing-persons', data),
    createWithPhotos: (formData) => api.post('/missing-persons/with-photos', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
    update: (id, data) => api.put(`/missing-persons/${id}`, data),
    updateWithPhotos: (id, formData) => api.put(`/missing-persons/${id}/with-photos`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
    delete: (id) => api.delete(`/missing-persons/${id}`),
    updateStatus: (id, status) => api.put(`/missing-persons/${id}/status`, { status })
  }
}

export function adminApi() {
  return {
    listUsers: (params) => api.get('/admin/users', { params }),
    createUser: (data) => api.post('/admin/users', data),
    updateUser: (id, data) => api.put(`/admin/users/${id}`, data),
    toggleUserStatus: (id, status) => api.put(`/admin/users/${id}/status?status=${status}`),
    deleteUser: (id) => api.delete(`/admin/users/${id}`),
    listMissingPersons: (params) => api.get('/admin/missing-persons', { params }),
    approveMissingPerson: (id, remark) => api.put(`/admin/missing-persons/${id}/approve?approvalRemark=${encodeURIComponent(remark || '')}`),
    rejectMissingPerson: (id, remark) => api.put(`/admin/missing-persons/${id}/reject?rejectionRemark=${encodeURIComponent(remark || '')}`),
    deleteMissingPerson: (id) => api.delete(`/admin/missing-persons/${id}`),
    getChangeLogs: (id) => api.get(`/admin/missing-persons/${id}/change-logs`),
    listTasks: (params) => api.get('/admin/tasks', { params }),
    createTask: (data) => api.post('/admin/tasks', data)
  }
}

export function clueApi() {
  return {
    listAll: (params) => api.get('/clues', { params }),
    getMyClues: (params) => api.get('/clues/my', { params }),
    getRelatedClues: (params) => api.get('/clues/seeker', { params }),
    getAssignedClues: (params) => api.get('/clues/my-assigned', { params }),
    create: (data) => api.post('/clues', data),
    getById: (id) => api.get(`/clues/${id}`),
    update: (id, data) => api.put(`/clues/${id}`, data),
    delete: (id) => api.delete(`/clues/${id}`),
    updateStatus: (id, status) => api.put(`/clues/${id}/status`, { status }),
    assign: (id, volunteerId) => api.put(`/clues/${id}/assign`, { volunteerId }),
    handle: (id, data) => api.put(`/clues/${id}/handle`, data),
    approve: (id, data) => api.put(`/clues/${id}/approve`, data),
    reject: (id, data) => api.put(`/clues/${id}/reject`, data),
    adminUpdate: (id, data) => api.put(`/clues/${id}/admin-update`, data),
    adminDelete: (id) => api.delete(`/clues/${id}/admin`)
  }
}

export function successCaseApi() {
  return {
    list: (params) => api.get('/success-cases', { params }),
    getById: (id) => api.get(`/success-cases/${id}`),
    create: (data) => api.post('/success-cases', data),
    createWithFiles: (formData) => api.post('/success-cases/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
    update: (id, data) => api.put(`/success-cases/${id}`, data),
    updateWithFiles: (id, formData) => api.put(`/success-cases/${id}/upload`, formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
    delete: (id) => api.delete(`/success-cases/${id}`)
  }
}

export function communityApi() {
  return {
    listPosts: (params) => api.get('/community/posts', { params }),
    createPost: (data) => api.post('/community/posts', data),
    deletePost: (id) => api.delete(`/community/posts/${id}`),
    likePost: (id) => api.post(`/community/posts/${id}/like`),
    unlikePost: (id) => api.delete(`/community/posts/${id}/like`),
    getComments: (postId) => api.get(`/community/posts/${postId}/comments`),
    addComment: (postId, data) => api.post(`/community/posts/${postId}/comments`, data),
    likeComment: (commentId) => api.post(`/community/comments/${commentId}/like`),
    unlikeComment: (commentId) => api.delete(`/community/comments/${commentId}/like`),
    getMyLikedPosts: () => api.get('/community/my/likes/posts'),
    getMyLikedComments: () => api.get('/community/my/likes/comments'),
    deleteComment: (commentId) => api.delete(`/community/comments/${commentId}`)
  }
}

export function notificationApi() {
  return {
    list: (params) => api.get('/notifications', { params }),
    getUnreadCount: () => api.get('/notifications/unread-count'),
    markRead: (id) => api.put(`/notifications/${id}/read`),
    markAllRead: () => api.put('/notifications/read-all'),
    delete: (id) => api.delete(`/notifications/${id}`)
  }
}

export function taskApi() {
  return {
    list: (params) => api.get('/tasks', { params }),
    getAvailable: (params) => api.get('/tasks', { params }),
    getMyTasks: (params) => api.get('/tasks/my', { params }),
    getMyPublished: (params) => api.get('/tasks/my-published', { params }),
    getDetail: (id) => api.get(`/tasks/${id}`),
    create: (data) => api.post('/tasks', data),
    update: (id, data) => api.put(`/tasks/${id}`, data),
    claim: (id) => api.post(`/tasks/${id}/claim`),
    complete: (id, data) => api.put(`/tasks/${id}/complete`, data),
    review: (id, data) => api.put(`/tasks/${id}/review`, data),
    updateProgress: (id, data) => api.put(`/tasks/${id}/progress`, data),
    cancel: (id) => api.put(`/tasks/${id}/cancel`),
    delete: (id) => api.delete(`/tasks/${id}`),
    getLogs: (id) => api.get(`/tasks/${id}/logs`)
  }
}

export function volunteerActivityApi() {
  return {
    list: (params) => api.get('/volunteer-activities', { params }),
    getMyActivities: (params) => api.get('/volunteer-activities/my', { params }),
    listAdmin: (params) => api.get('/volunteer-activities/admin', { params }),
    create: (formData) => api.post('/volunteer-activities/admin', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
    update: (id, data) => api.post(`/volunteer-activities/admin/${id}`, data),
    delete: (id) => api.delete(`/volunteer-activities/admin/${id}`),
    join: (id, data) => api.post(`/volunteer-activities/${id}/join`, data || {}),
    publish: (id) => api.post(`/volunteer-activities/admin/${id}/publish`),
    start: (id) => api.post(`/volunteer-activities/admin/${id}/start`),
    end: (id) => api.post(`/volunteer-activities/admin/${id}/end`),
    cancel: (id) => api.post(`/volunteer-activities/admin/${id}/cancel`),
    submitReport: (id, data) => api.post(`/volunteer-activities/${id}/report`, data, { headers: { 'Content-Type': 'multipart/form-data' } }),
    getMyReport: (id) => api.get(`/volunteer-activities/${id}/my-report`),
    getReports: (id, params) => api.get(`/volunteer-activities/admin/${id}/reports`, { params }),
    getReportDetail: (reportId) => api.get(`/volunteer-activities/admin/reports/${reportId}`),
    approveReport: (reportId, remark) => api.post(`/volunteer-activities/admin/reports/${reportId}/approve`, { reviewRemark: remark || '' }),
    rejectReport: (reportId, remark) => api.post(`/volunteer-activities/admin/reports/${reportId}/reject`, { reviewRemark: remark || '' }),
    getParticipants: (id, params) => api.get(`/volunteer-activities/admin/${id}/participants`, { params }),
    approveParticipant: (participantId, remark) => api.post(`/volunteer-activities/admin/participants/${participantId}/approve`, { adminRemark: remark || '' }),
    rejectParticipant: (participantId, remark) => api.post(`/volunteer-activities/admin/participants/${participantId}/reject`, { rejectReason: remark || '' }),
    checkin: (participantId) => api.post(`/volunteer-activities/admin/participants/${participantId}/checkin`),
    checkout: (participantId, workHours) => api.post(`/volunteer-activities/admin/participants/${participantId}/checkout`, { workHours }),
    checkinSelf: (id) => api.post(`/volunteer-activities/${id}/checkin`)
  }
}

export function aiApi() {
  return {
    ask: (data) => api.post('/ai/ask', data)
  }
}

export function volunteerApplyApi() {
  return {
    apply: (data) => api.post('/volunteer/apply', data)
  }
}

export default api