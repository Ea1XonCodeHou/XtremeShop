import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '@/views/Home/index.vue'
import LoginView from '@/views/Login/index.vue'
import MerchantAuth from '@/views/Merchant/MerchantAuth.vue'
import MerchantLayout from '@/views/Merchant/MerchantLayout.vue'
import Dashboard from '@/views/Merchant/Dashboard.vue'
import ProductManagement from '@/views/Merchant/ProductManagement.vue'
import MarketingCenter from '@/views/Merchant/MarketingCenter.vue'
import StoreSettings from '@/views/Merchant/StoreSettings.vue'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', name: 'home', component: HomeView },
  // 用户端
  { path: '/login', name: 'login', component: LoginView },
  { path: '/register', redirect: '/login' },
  // 商家端登录注册
  { path: '/merchant/login', name: 'merchantAuth', component: MerchantAuth },
  // 商家后台（嵌套路由，共享 Layout）
  {
    path: '/merchant',
    component: MerchantLayout,
    meta: { requiresMerchant: true },
    children: [
      { path: 'dashboard',  name: 'merchantDashboard',  component: Dashboard },
      { path: 'products',   name: 'merchantProducts',   component: ProductManagement },
      { path: 'marketing',  name: 'merchantMarketing',  component: MarketingCenter },
      { path: 'settings',   name: 'merchantSettings',   component: StoreSettings },
      { path: '', redirect: 'dashboard' },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 商家路由守卫：未登录跳转到商家登录页
router.beforeEach((to) => {
  if (to.meta.requiresMerchant && !localStorage.getItem('merchantToken')) {
    return { name: 'merchantAuth' }
  }
})

export default router
