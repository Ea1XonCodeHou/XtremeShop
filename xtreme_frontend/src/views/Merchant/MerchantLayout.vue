<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const merchantName = localStorage.getItem('merchantName') || '商家'
const logoUrl = localStorage.getItem('merchantLogoUrl') || ''

const navItems = [
  { path: '/merchant/dashboard',   icon: 'dashboard',    label: '控制台' },
  { path: '/merchant/products',    icon: 'inventory_2',  label: '商品管理' },
  { path: '/merchant/marketing',   icon: 'campaign',     label: '营销中心' },
  { path: '/merchant/settings',    icon: 'settings',     label: '店铺设置' },
]

const pageTitle = computed(() => {
  const item = navItems.find(n => route.path.startsWith(n.path))
  return item ? item.label : '商家后台'
})

const handleLogout = () => {
  localStorage.removeItem('merchantToken')
  localStorage.removeItem('merchantId')
  localStorage.removeItem('merchantName')
  localStorage.removeItem('merchantLogoUrl')
  router.push('/merchant/login')
}
</script>

<template>
  <div class="mc-shell">
    <!-- 左侧导航 -->
    <aside class="mc-sidebar">
      <div class="mc-sidebar-brand">
        <div class="mc-brand-logo"><span class="material-symbols-outlined">storefront</span></div>
        <div>
          <div class="mc-brand-name">Xtreme-Shop</div>
          <div class="mc-brand-sub">商家管理平台</div>
        </div>
      </div>

      <nav class="mc-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="mc-nav-item"
          :class="{ active: route.path.startsWith(item.path) }"
        >
          <span class="material-symbols-outlined">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <!-- 底部商家信息 -->
      <div class="mc-sidebar-footer">
        <img v-if="logoUrl" :src="logoUrl" class="mc-avatar" alt="logo" />
        <div v-else class="mc-avatar-placeholder"><span class="material-symbols-outlined">store</span></div>
        <div class="mc-footer-info">
          <div class="mc-footer-name">{{ merchantName }}</div>
          <button class="mc-logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <div class="mc-content">
      <!-- 顶部栏 -->
      <header class="mc-topbar">
        <h1 class="mc-page-title">{{ pageTitle }}</h1>
        <div class="mc-topbar-right">
          <span class="mc-welcome">欢迎，{{ merchantName }}</span>
        </div>
      </header>

      <!-- 页面内容（子路由） -->
      <main class="mc-main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

* { box-sizing: border-box; }

.mc-shell {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
  font-family: 'Plus Jakarta Sans', sans-serif;
}

/* 侧边栏 */
.mc-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #e8ecf0;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0; left: 0; bottom: 0;
  z-index: 50;
}
.mc-sidebar-brand {
  padding: 24px 20px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #f0f2f5;
}
.mc-brand-logo {
  width: 36px; height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #005daa 0%, #1890ff 100%);
  color: #fff;
  display: grid; place-items: center;
  flex-shrink: 0;
}
.mc-brand-logo span { font-size: 20px; }
.mc-brand-name { font-size: 14px; font-weight: 800; color: #005daa; }
.mc-brand-sub { font-size: 10px; font-weight: 600; color: #8c8c8c; text-transform: uppercase; letter-spacing: 0.06em; }

.mc-nav { flex: 1; padding: 16px 12px; display: flex; flex-direction: column; gap: 4px; }
.mc-nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px; font-weight: 600;
  color: #64748b;
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}
.mc-nav-item:hover { background: #f0f5ff; color: #1890ff; }
.mc-nav-item.active { background: #1890ff; color: #fff; box-shadow: 0 4px 12px rgba(24,144,255,0.25); }
.mc-nav-item span.material-symbols-outlined { font-size: 20px; }

.mc-sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f2f5;
  display: flex; align-items: center; gap: 12px;
}
.mc-avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; border: 2px solid #1890ff; flex-shrink: 0; }
.mc-avatar-placeholder {
  width: 36px; height: 36px; border-radius: 50%;
  background: #e6f7ff; color: #1890ff;
  display: grid; place-items: center; flex-shrink: 0;
}
.mc-avatar-placeholder span { font-size: 18px; }
.mc-footer-info { min-width: 0; }
.mc-footer-name { font-size: 12px; font-weight: 700; color: #262626; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.mc-logout-btn { border: 0; background: transparent; color: #8c8c8c; font-size: 11px; cursor: pointer; padding: 0; font-family: inherit; }
.mc-logout-btn:hover { color: #ef4444; }

/* 右侧内容 */
.mc-content { margin-left: 240px; flex: 1; display: flex; flex-direction: column; min-height: 100vh; }
.mc-topbar {
  position: sticky; top: 0; z-index: 40;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e8ecf0;
  padding: 0 32px;
  height: 60px;
  display: flex; align-items: center; justify-content: space-between;
}
.mc-page-title { font-size: 15px; font-weight: 700; color: #1890ff; margin: 0; }
.mc-topbar-right { display: flex; align-items: center; gap: 16px; }
.mc-welcome { font-size: 13px; color: #64748b; font-weight: 500; }

.mc-main { flex: 1; padding: 28px 32px; }
</style>
