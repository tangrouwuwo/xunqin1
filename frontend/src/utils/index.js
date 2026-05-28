export function getRoleName(role) {
  const names = { ADMIN: '管理员', SEEKER: '寻亲者', VOLUNTEER: '志愿者', CLUE_PROVIDER: '线索提供者' }
  return names[role] || role
}

export function getRoleClass(role) {
  const classes = { ADMIN: 'admin', SEEKER: 'seeker', VOLUNTEER: 'volunteer', CLUE_PROVIDER: 'clue' }
  return classes[role] || ''
}

export function getDefaultAvatar(username) {
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username || 'user'}`
}

export function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

export function timeAgo(dateStr) {
  if (!dateStr) return ''
  const now = new Date()
  const date = new Date(dateStr)
  const diff = Math.floor((now - date) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  if (diff < 2592000) return `${Math.floor(diff / 86400)}天前`
  return formatDate(dateStr)
}