import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/public/Home.vue') },
      { path: 'missing-persons-search', name: 'MissingPersonSearch', component: () => import('@/views/public/MissingPersonSearch.vue') },
      { path: 'success-cases', name: 'SuccessCases', component: () => import('@/views/public/SuccessCases.vue') },
      { path: 'community', name: 'Community', component: () => import('@/views/public/Community.vue') },
      { path: 'volunteer', name: 'Volunteer', component: () => import('@/views/public/Volunteer.vue') },
      { path: 'missing-persons/:id', name: 'MissingPersonDetail', component: () => import('@/views/public/MissingPersonDetail.vue') },
      { path: 'login', name: 'Login', component: () => import('@/views/auth/Login.vue') },
      { path: 'register', name: 'Register', component: () => import('@/views/auth/Register.vue') }
    ]
  },
  {
    path: '/dashboard',
    component: () => import('@/layouts/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardHome.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/dashboard/Profile.vue') },
      { path: 'users', name: 'UserManagement', component: () => import('@/views/dashboard/admin/UserManagement.vue'), meta: { role: 'ADMIN' } },
      { path: 'missing-persons', name: 'MissingPersonManagement', component: () => import('@/views/dashboard/admin/MissingPersonManagement.vue'), meta: { role: 'ADMIN' } },
      { path: 'clues', name: 'ClueManagement', component: () => import('@/views/dashboard/admin/ClueManagement.vue'), meta: { role: 'ADMIN' } },
      { path: 'success-cases-manage', name: 'SuccessCaseManagement', component: () => import('@/views/dashboard/admin/SuccessCaseManagement.vue'), meta: { role: 'ADMIN' } },
      { path: 'volunteer-activities-manage', name: 'VolunteerActivityManagement', component: () => import('@/views/dashboard/admin/VolunteerActivityManagement.vue'), meta: { role: 'ADMIN' } },
      { path: 'tasks', name: 'TaskManagement', component: () => import('@/views/dashboard/admin/TaskManagement.vue'), meta: { role: 'ADMIN' } },
      { path: 'notifications', name: 'Notifications', component: () => import('@/views/dashboard/Notifications.vue') },
      { path: 'admin-community', name: 'AdminCommunity', component: () => import('@/views/dashboard/admin/AdminCommunity.vue'), meta: { role: 'ADMIN' } },
      { path: 'seeker-community', name: 'SeekerCommunity', component: () => import('@/views/dashboard/seeker/SeekerCommunity.vue'), meta: { role: 'SEEKER' } },
      { path: 'volunteer-community', name: 'VolunteerCommunity', component: () => import('@/views/dashboard/volunteer/VolunteerCommunity.vue'), meta: { role: 'VOLUNTEER' } },
      { path: 'clue-provider-community', name: 'ClueProviderCommunity', component: () => import('@/views/dashboard/clue-provider/ClueProviderCommunity.vue'), meta: { role: 'CLUE_PROVIDER' } },
      { path: 'create-missing-person', name: 'CreateMissingPerson', component: () => import('@/views/dashboard/seeker/CreateMissingPerson.vue'), meta: { role: 'SEEKER' } },
      { path: 'my-missing-persons', name: 'MyMissingPersons', component: () => import('@/views/dashboard/seeker/MyMissingPersons.vue'), meta: { role: 'SEEKER' } },
      { path: 'seeker-clues', name: 'SeekerClues', component: () => import('@/views/dashboard/seeker/SeekerClues.vue'), meta: { role: 'SEEKER' } },
      { path: 'seeker-tasks', name: 'SeekerTasks', component: () => import('@/views/dashboard/seeker/SeekerTasks.vue'), meta: { role: 'SEEKER' } },
      { path: 'volunteer-clues', name: 'VolunteerClueManagement', component: () => import('@/views/dashboard/volunteer/VolunteerClueManagement.vue'), meta: { role: 'VOLUNTEER' } },
      { path: 'volunteer-activities', name: 'VolunteerActivities', component: () => import('@/views/dashboard/volunteer/VolunteerActivities.vue'), meta: { role: 'VOLUNTEER' } },
      { path: 'task-hall', name: 'TaskHall', component: () => import('@/views/dashboard/volunteer/TaskHall.vue'), meta: { role: 'VOLUNTEER' } },
      { path: 'my-tasks', name: 'MyTasks', component: () => import('@/views/dashboard/volunteer/MyTasks.vue'), meta: { role: 'VOLUNTEER' } },
      { path: 'my-clues', name: 'MyClues', component: () => import('@/views/dashboard/clue-provider/MyClues.vue'), meta: { role: 'CLUE_PROVIDER' } },
      { path: 'submit-clue', name: 'SubmitClue', component: () => import('@/views/dashboard/clue-provider/SubmitClue.vue'), meta: { role: 'CLUE_PROVIDER' } },
      { path: 'ai', name: 'AIAssistant', component: () => import('@/views/dashboard/AIAssistant.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const auth = useAuthStore()
    if (!auth.isLoggedIn) {
      next({ name: 'Home' })
      return
    }
    if (to.meta.role && auth.role !== to.meta.role && auth.role !== 'ADMIN') {
      next({ name: 'Dashboard' })
      return
    }
  }
  next()
})

export default router