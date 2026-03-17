import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '@/views/Home/index.vue'
import LoginView from '@/views/Login/index.vue'
import RegisterView from '@/views/Register/index.vue'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/register', name: 'register', component: RegisterView },
  { path: '/home', name: 'home', component: HomeView },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
