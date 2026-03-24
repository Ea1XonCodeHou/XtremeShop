<script setup>
import request from '@/utils/request'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const mode = ref('login') // 'login' | 'register'

const phone = ref('')
const password = ref('')
const name = ref('')
const showPassword = ref(false)

const toastList = ref([])
let toastId = 0
const pushToast = (type, text) => {
  const id = ++toastId
  toastList.value.push({ id, type, text })
  setTimeout(() => { toastList.value = toastList.value.filter(t => t.id !== id) }, 2400)
}

const switchMode = (m) => {
  mode.value = m
  phone.value = ''
  password.value = ''
  name.value = ''
}

const handleLogin = async () => {
  if (!phone.value || !password.value) return pushToast('error', '请填写手机号和密码')
  try {
    const res = await request.post('/merchant/login', { phone: phone.value, password: password.value })
    if (res.data.code === 1) {
      const { merchantId, token, name: merchantName } = res.data.data
      localStorage.setItem('merchantToken', token)
      localStorage.setItem('merchantId', String(merchantId))
      localStorage.setItem('merchantName', merchantName ?? '')
      pushToast('success', '登录成功')
      setTimeout(() => router.push('/merchant/dashboard'), 500)
    } else {
      pushToast('error', res.data.message || '登录失败')
    }
  } catch {
    pushToast('error', '网络异常，请稍后重试')
  }
}

const handleRegister = async () => {
  if (!phone.value || !password.value || !name.value) return pushToast('error', '请填写所有必填项')
  if (password.value.length < 6 || password.value.length > 16) return pushToast('error', '密码长度为 6-16 位')
  try {
    const res = await request.post('/merchant/register', { phone: phone.value, password: password.value, name: name.value })
    if (res.data.code === 1) {
      pushToast('success', '注册成功，请登录')
      setTimeout(() => switchMode('login'), 600)
    } else {
      pushToast('error', res.data.message || '注册失败')
    }
  } catch {
    pushToast('error', '网络异常，请稍后重试')
  }
}
</script>

<template>
  <div class="m-page">
    <!-- 背景光晕 -->
    <div class="glow glow-tr"></div>
    <div class="glow glow-bl"></div>

    <main class="m-main">
      <!-- 左侧品牌区 -->
      <section class="m-brand">
        <div class="m-logo">
          <span class="material-symbols-outlined">storefront</span>
        </div>
        <h1>Xtreme-Shop</h1>
        <p class="m-tagline">商家管理平台</p>
        <p class="m-desc">精准管理您的店铺，发布秒杀活动，实时掌握销售数据。</p>
        <div class="m-stats">
          <div class="m-stat"><span class="m-stat-num">99.9%</span><span class="m-stat-label">服务可用性</span></div>
          <div class="m-stat"><span class="m-stat-num">24/7</span><span class="m-stat-label">全天候支持</span></div>
        </div>
      </section>

      <!-- 右侧表单卡片 -->
      <section class="m-card">
        <div class="m-card-accent"></div>
        <header class="m-card-head">
          <h2>{{ mode === 'login' ? '商家登录' : '商家注册' }}</h2>
          <p>{{ mode === 'login' ? '欢迎回来，请输入您的账号信息' : '创建您的商家账号，开始经营' }}</p>
        </header>

        <!-- 登录表单 -->
        <form v-if="mode === 'login'" class="m-form" @submit.prevent="handleLogin">
          <div class="m-field">
            <label>手机号</label>
            <div class="m-input-wrap">
              <span class="material-symbols-outlined">person</span>
              <input v-model="phone" type="tel" placeholder="请输入注册手机号" />
            </div>
          </div>
          <div class="m-field">
            <label>密码</label>
            <div class="m-input-wrap">
              <span class="material-symbols-outlined">lock</span>
              <input v-model="password" :type="showPassword ? 'text' : 'password'" placeholder="请输入密码" />
              <button type="button" class="m-ghost-btn" @click="showPassword = !showPassword">
                <span class="material-symbols-outlined">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
              </button>
            </div>
          </div>
          <button class="m-submit-btn" type="submit">
            登录后台
            <span class="material-symbols-outlined">arrow_forward</span>
          </button>
        </form>

        <!-- 注册表单 -->
        <form v-else class="m-form" @submit.prevent="handleRegister">
          <div class="m-field">
            <label>店铺名称</label>
            <div class="m-input-wrap">
              <span class="material-symbols-outlined">storefront</span>
              <input v-model="name" type="text" placeholder="请输入店铺名称" />
            </div>
          </div>
          <div class="m-field">
            <label>手机号</label>
            <div class="m-input-wrap">
              <span class="material-symbols-outlined">person</span>
              <input v-model="phone" type="tel" placeholder="请输入手机号" />
            </div>
          </div>
          <div class="m-field">
            <label>设置密码</label>
            <div class="m-input-wrap">
              <span class="material-symbols-outlined">lock</span>
              <input v-model="password" :type="showPassword ? 'text' : 'password'" placeholder="6-16 位字母或数字" />
              <button type="button" class="m-ghost-btn" @click="showPassword = !showPassword">
                <span class="material-symbols-outlined">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
              </button>
            </div>
          </div>
          <button class="m-submit-btn" type="submit">
            立即注册
            <span class="material-symbols-outlined">arrow_forward</span>
          </button>
        </form>

        <!-- 底部切换 -->
        <footer class="m-card-footer">
          <template v-if="mode === 'login'">
            还没有账号？<button class="m-link-btn" @click="switchMode('register')">注册商家</button>
          </template>
          <template v-else>
            已有账号？<button class="m-link-btn" @click="switchMode('login')">立即登录</button>
          </template>
          <div class="m-divider-row">
            <span class="m-dot"></span>
            <span class="m-security-label">企业级安全标准</span>
            <span class="m-dot"></span>
          </div>
        </footer>
      </section>
    </main>

    <!-- Toast -->
    <div class="toast-wrap">
      <div v-for="item in toastList" :key="item.id" class="toast-item" :class="`toast-${item.type}`">
        {{ item.text }}
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.m-page {
  min-height: 100vh;
  background: #f2f4f7;
  font-family: 'Plus Jakarta Sans', 'Inter', sans-serif;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}
.glow { position: absolute; border-radius: 50%; pointer-events: none; filter: blur(100px); opacity: 0.12; }
.glow-tr { width: 500px; height: 500px; top: -10%; right: -10%; background: #005daa; }
.glow-bl { width: 400px; height: 400px; bottom: -10%; left: -10%; background: #d4e5ed; opacity: 0.2; }

.m-main { position: relative; z-index: 1; width: 100%; max-width: 1100px; display: flex; align-items: center; gap: 80px; }

.m-brand { flex: 1; min-width: 0; }
.m-logo { width: 52px; height: 52px; border-radius: 12px; background: linear-gradient(135deg, #005daa 0%, #0075d5 100%); color: #fff; display: grid; place-items: center; box-shadow: 0 8px 24px rgba(0,93,170,0.25); margin-bottom: 20px; }
.m-logo span { font-size: 28px; }
.m-brand h1 { margin: 0 0 6px; font-size: 36px; font-weight: 800; color: #005daa; letter-spacing: -0.5px; }
.m-tagline { margin: 0 0 16px; font-size: 22px; font-weight: 700; color: #191c1e; }
.m-desc { margin: 0 0 36px; font-size: 15px; color: #404753; line-height: 1.7; max-width: 380px; }
.m-stats { display: flex; gap: 20px; }
.m-stat { padding: 16px 20px; background: #fff; border-radius: 12px; display: flex; flex-direction: column; gap: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.m-stat-num { font-size: 22px; font-weight: 800; color: #005daa; }
.m-stat-label { font-size: 11px; font-weight: 600; color: #707785; text-transform: uppercase; letter-spacing: 0.08em; }

.m-card { width: 440px; flex-shrink: 0; background: rgba(255,255,255,0.88); backdrop-filter: blur(20px); border-radius: 16px; padding: 40px; box-shadow: 0 40px 64px -24px rgba(0,0,0,0.06); border: 1px solid rgba(192,199,214,0.2); position: relative; overflow: hidden; }
.m-card-accent { position: absolute; top: 0; left: 0; right: 0; height: 3px; background: linear-gradient(90deg, #005daa 0%, #0075d5 100%); }
.m-card-head { margin-bottom: 28px; }
.m-card-head h2 { margin: 0 0 6px; font-size: 22px; font-weight: 700; color: #191c1e; }
.m-card-head p { margin: 0; font-size: 13px; color: #707785; }

.m-form { display: grid; gap: 18px; }
.m-field { display: grid; gap: 6px; }
.m-field label { font-size: 11px; font-weight: 700; color: #707785; text-transform: uppercase; letter-spacing: 0.08em; }
.m-input-wrap { height: 50px; border-radius: 10px; background: #f2f4f7; border: 1px solid transparent; display: flex; align-items: center; gap: 10px; padding: 0 14px; transition: border-color 0.2s, background 0.2s; }
.m-input-wrap:focus-within { border-color: #005daa; background: #fff; }
.m-input-wrap > span { color: #707785; font-size: 20px; }
.m-input-wrap input { flex: 1; border: 0; background: transparent; outline: 0; font-size: 15px; color: #191c1e; font-family: inherit; }
.m-ghost-btn { border: 0; background: transparent; color: #707785; cursor: pointer; display: flex; align-items: center; }

.m-submit-btn { margin-top: 8px; height: 50px; border: 0; border-radius: 10px; background: linear-gradient(135deg, #005daa 0%, #0075d5 100%); color: #fff; font-size: 15px; font-weight: 700; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; box-shadow: 0 8px 20px rgba(0,93,170,0.25); transition: box-shadow 0.2s, transform 0.1s; font-family: inherit; }
.m-submit-btn:hover { box-shadow: 0 12px 28px rgba(0,93,170,0.35); }
.m-submit-btn:active { transform: scale(0.98); }

.m-card-footer { margin-top: 28px; padding-top: 24px; border-top: 1px solid #e6e8eb; text-align: center; color: #707785; font-size: 14px; display: flex; flex-direction: column; align-items: center; gap: 14px; }
.m-link-btn { border: 0; background: transparent; color: #005daa; font-weight: 700; font-size: 14px; cursor: pointer; margin-left: 4px; font-family: inherit; }
.m-divider-row { display: flex; align-items: center; gap: 8px; }
.m-dot { width: 4px; height: 4px; border-radius: 50%; background: #c0c7d6; }
.m-security-label { font-size: 10px; font-weight: 600; color: #707785; text-transform: uppercase; letter-spacing: 0.1em; }

.toast-wrap { position: fixed; right: 24px; top: 24px; z-index: 999; display: grid; gap: 10px; }
.toast-item { min-width: 220px; padding: 12px 16px; border-radius: 10px; color: #fff; font-size: 14px; font-weight: 600; box-shadow: 0 8px 20px rgba(15,23,42,0.16); }
.toast-success { background: #16a34a; }
.toast-error { background: #ef4444; }

@media (max-width: 860px) {
  .m-main { flex-direction: column; gap: 32px; }
  .m-brand { text-align: center; }
  .m-desc { margin-left: auto; margin-right: auto; }
  .m-stats { justify-content: center; }
  .m-card { width: 100%; max-width: 480px; }
}
</style>
