<script setup>
import request from '@/utils/request'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const mode = ref('login') // 'login' | 'register'

// 表单字段
const phone = ref('')
const password = ref('')
const showPassword = ref(false)
const agree = ref(false)

// Toast
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
  agree.value = false
}

const handleLogin = async () => {
  if (!phone.value || !password.value) return pushToast('error', '请填写手机号和密码')
  try {
    const res = await request.post('/user/login', { phone: phone.value, password: password.value })
    if (res.data.code === 1) {
      const { sessionId, userId, username } = res.data.data
      sessionStorage.setItem('sessionId', sessionId)
      sessionStorage.setItem('userId', String(userId))
      sessionStorage.setItem('username', username ?? '')
      pushToast('success', '登录成功')
      setTimeout(() => router.push('/home'), 500)
    } else {
      pushToast('error', res.data.message || '登录失败')
    }
  } catch {
    pushToast('error', '网络异常，请稍后重试')
  }
}

const handleRegister = async () => {
  if (!phone.value || !password.value) return pushToast('error', '请填写手机号和密码')
  if (password.value.length < 6 || password.value.length > 16) return pushToast('error', '密码长度为 6-16 位')
  if (!agree.value) return pushToast('error', '请先勾选用户协议')
  try {
    const res = await request.post('/user/register', { phone: phone.value, password: password.value })
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
  <div class="auth-page">
    <!-- Logo -->
    <div class="auth-logo-wrap">
      <div class="logo-box"><span class="material-symbols-outlined">bolt</span></div>
      <h1>Xtreme-Shop</h1>
      <p>极致秒杀 · 极速体验</p>
    </div>

    <!-- 卡片 -->
    <section class="auth-card">
      <h2>{{ mode === 'login' ? '欢迎回来' : '创建账号' }}</h2>

      <!-- 登录表单 -->
      <form v-if="mode === 'login'" class="auth-form" @submit.prevent="handleLogin">
        <label>手机号</label>
        <div class="input-wrap">
          <span class="material-symbols-outlined">smartphone</span>
          <input v-model="phone" type="tel" placeholder="请输入手机号" />
        </div>

        <label>密码</label>
        <div class="input-wrap">
          <span class="material-symbols-outlined">lock</span>
          <input v-model="password" :type="showPassword ? 'text' : 'password'" placeholder="请输入密码" />
          <button type="button" class="ghost-btn" @click="showPassword = !showPassword">
            <span class="material-symbols-outlined">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
          </button>
        </div>

        <div class="row-between">
          <label class="remember"><input type="checkbox" /> 记住我</label>
          <a href="javascript:void(0)">忘记密码？</a>
        </div>

        <button class="submit-btn" type="submit">登 录</button>
      </form>

      <!-- 注册表单 -->
      <form v-else class="auth-form" @submit.prevent="handleRegister">
        <label>手机号</label>
        <div class="input-wrap">
          <span class="material-symbols-outlined">smartphone</span>
          <input v-model="phone" type="tel" placeholder="请输入手机号" />
        </div>

        <label>设置密码</label>
        <div class="input-wrap">
          <span class="material-symbols-outlined">lock</span>
          <input v-model="password" :type="showPassword ? 'text' : 'password'" placeholder="6-16 位字母或数字" />
          <button type="button" class="ghost-btn" @click="showPassword = !showPassword">
            <span class="material-symbols-outlined">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
          </button>
        </div>

        <label class="agreement">
          <input v-model="agree" type="checkbox" />
          我已阅读并同意 <a href="javascript:void(0)">用户协议</a> 和 <a href="javascript:void(0)">隐私政策</a>
        </label>

        <button class="submit-btn" type="submit">立即注册</button>
      </form>

      <!-- 底部切换 -->
      <div class="auth-footer">
        <template v-if="mode === 'login'">
          还没有账号？<button class="link-btn" @click="switchMode('register')">注册账号</button>
        </template>
        <template v-else>
          已有账号？<button class="link-btn" @click="switchMode('login')">立即登录</button>
        </template>
      </div>
    </section>

    <!-- Toast -->
    <div class="toast-wrap">
      <div v-for="item in toastList" :key="item.id" class="toast-item" :class="`toast-${item.type}`">
        {{ item.text }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f8f7f5;
}
.auth-logo-wrap { text-align: center; margin-bottom: 34px; }
.logo-box {
  width: 64px; height: 64px; margin: 0 auto 16px;
  border-radius: 12px; background: #ff6600; color: #fff;
  display: grid; place-items: center;
  box-shadow: 0 10px 25px rgba(255, 102, 0, 0.2);
}
.logo-box span { font-size: 34px; }
h1 { margin: 0; font-size: 48px; line-height: 56px; color: #111827; font-weight: 700; }
.auth-logo-wrap p { margin: 8px 0 0; font-size: 20px; font-weight: 500; color: #ff6600; }

.auth-card {
  width: 100%; max-width: 660px;
  border-radius: 14px; border: 1px solid #e5e7eb;
  background: #fff;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  padding: 38px;
}
h2 { margin: 0 0 28px; text-align: center; font-size: 32px; font-weight: 700; color: #111827; }

.auth-form { display: grid; gap: 14px; }
.auth-form label { font-size: 18px; font-weight: 600; color: #334155; }
.input-wrap {
  height: 56px; border-radius: 10px;
  border: 1px solid #d9dee5; background: #f6f8fb;
  display: flex; align-items: center; gap: 10px; padding: 0 14px;
}
.input-wrap > span { color: #94a3b8; }
.input-wrap input { flex: 1; border: 0; background: transparent; outline: 0; font-size: 16px; color: #0f172a; }
.ghost-btn { border: 0; background: transparent; color: #94a3b8; cursor: pointer; }

.row-between { margin-top: 6px; display: flex; justify-content: space-between; align-items: center; }
.remember { font-size: 15px !important; color: #475569 !important; font-weight: 500 !important; }
.row-between a { color: #ff6600; text-decoration: none; font-size: 14px; font-weight: 600; }

.agreement { margin-top: 4px; font-size: 14px !important; color: #64748b !important; font-weight: 500 !important; }
.agreement a { color: #ff6600; text-decoration: none; }

.submit-btn {
  margin-top: 10px; height: 56px; border: 0;
  border-radius: 10px; background: #ff6600; color: #fff;
  font-size: 26px; font-weight: 700; cursor: pointer;
}
.submit-btn:hover { background: #f45f00; }

.auth-footer {
  margin-top: 24px; border-top: 1px solid #eef1f4; padding-top: 22px;
  text-align: center; color: #64748b; font-size: 15px;
}
.link-btn {
  border: 0; background: transparent;
  color: #ff6600; font-weight: 700; font-size: 15px; cursor: pointer; margin-left: 6px;
}

.toast-wrap { position: fixed; right: 24px; top: 24px; z-index: 999; display: grid; gap: 10px; }
.toast-item {
  min-width: 220px; padding: 12px 14px; border-radius: 10px;
  color: #fff; font-size: 14px; font-weight: 600;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.16);
}
.toast-success { background: #16a34a; }
.toast-error { background: #ef4444; }
</style>
