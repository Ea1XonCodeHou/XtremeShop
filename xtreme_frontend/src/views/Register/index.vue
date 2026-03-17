<script setup>
import request from '@/utils/request'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const phone = ref('')
const password = ref('')
const showPassword = ref(false)
const agree = ref(false)
const toastList = ref([])
let toastId = 0

const pushToast = (type, text) => {
  const id = ++toastId
  toastList.value.push({ id, type, text })
  setTimeout(() => {
    toastList.value = toastList.value.filter((item) => item.id !== id)
  }, 2400)
}

const handleRegister = async () => {
  if (!phone.value || !password.value) {
    pushToast('error', '请填写手机号和密码')
    return
  }
  if (password.value.length < 6 || password.value.length > 16) {
    pushToast('error', '密码长度为6-16位')
    return
  }
  if (!agree.value) {
    pushToast('error', '请先勾选用户协议')
    return
  }

  try {
    const res = await request.post('/user/register', {
      phone: phone.value,
      password: password.value,
    })
    if (res.data.code === 1) {
      pushToast('success', '注册成功，请登录')
      setTimeout(() => router.push('/login'), 600)
    } else {
      pushToast('error', res.data.message || '注册失败')
    }
  } catch (e) {
    pushToast('error', '网络异常，请稍后重试')
  }
}
</script>

<template>
  <div class="auth-page">
    <header class="brand-head">
      <div class="logo-box"><span class="material-symbols-outlined">shopping_cart</span></div>
      <div>
        <h1>Xtreme-Shop</h1>
        <p>极致购物，极速体验</p>
      </div>
    </header>

    <section class="auth-card">
      <h2>创建新账号</h2>
      <form class="auth-form" @submit.prevent="handleRegister">
        <label>手机号</label>
        <div class="input-wrap">
          <span class="material-symbols-outlined">smartphone</span>
          <input v-model="phone" type="tel" placeholder="请输入手机号" />
        </div>

        <label>设置密码</label>
        <div class="input-wrap">
          <span class="material-symbols-outlined">lock</span>
          <input v-model="password" :type="showPassword ? 'text' : 'password'" placeholder="6-16位字母或数字" />
          <button type="button" class="ghost-btn" @click="showPassword = !showPassword">
            <span class="material-symbols-outlined">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
          </button>
        </div>

        <label class="agreement"><input v-model="agree" type="checkbox" /> 我已阅读并同意 <a href="javascript:void(0)">用户协议</a> 和 <a href="javascript:void(0)">隐私政策</a></label>
        <button class="submit-btn" type="submit">立即注册</button>
      </form>

      <div class="auth-footer">已有账号？<RouterLink to="/login">登录</RouterLink></div>
    </section>

    <div class="toast-wrap">
      <div v-for="item in toastList" :key="item.id" class="toast-item" :class="`toast-${item.type}`">
        {{ item.text }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page { min-height: 100vh; background: #f8f7f5; display: flex; flex-direction: column; align-items: center; padding: 44px 16px 40px; }
.brand-head { width: 100%; max-width: 660px; display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.logo-box { width: 56px; height: 56px; border-radius: 10px; background: #ff6600; color: #fff; display: grid; place-items: center; }
.brand-head h1 { margin: 0; font-size: 46px; line-height: 56px; color: #111827; }
.brand-head p { margin: 8px 0 0; color: #64748b; font-size: 24px; }
.auth-card { width: 100%; max-width: 660px; border: 1px solid #e5e7eb; border-radius: 14px; background: #fff; padding: 34px; }
h2 { margin: 0 0 24px; text-align: center; font-size: 44px; line-height: 56px; color: #111827; }
.auth-form { display: grid; gap: 12px; }
.auth-form label { font-size: 18px; font-weight: 600; color: #334155; }
.input-wrap { height: 56px; border-radius: 10px; border: 1px solid #d9dee5; background: #f6f8fb; display: flex; align-items: center; gap: 10px; padding: 0 14px; }
.input-wrap span { color: #94a3b8; }
.input-wrap input { flex: 1; border: 0; background: transparent; outline: 0; font-size: 16px; color: #0f172a; }
.code-row { display: grid; grid-template-columns: 1fr 150px; gap: 12px; }
.code-btn { border: 1px solid #ffb787; background: #fff4ed; color: #ff6600; border-radius: 10px; font-size: 18px; font-weight: 700; cursor: pointer; }
.ghost-btn { border: 0; background: transparent; color: #94a3b8; cursor: pointer; }
.agreement { margin-top: 8px; font-size: 14px !important; color: #64748b !important; font-weight: 500 !important; }
.agreement a { color: #ff6600; text-decoration: none; }
.submit-btn { margin-top: 4px; height: 56px; border: 0; border-radius: 10px; background: #ff6600; color: #fff; font-size: 28px; font-weight: 700; cursor: pointer; }
.submit-btn:hover { background: #f45f00; }
.auth-footer { text-align: center; margin-top: 18px; color: #64748b; font-size: 15px; }
.auth-footer a { margin-left: 8px; color: #ff6600; font-weight: 700; text-decoration: none; }

.toast-wrap {
  position: fixed;
  right: 24px;
  top: 24px;
  z-index: 999;
  display: grid;
  gap: 10px;
}

.toast-item {
  min-width: 260px;
  padding: 12px 14px;
  border-radius: 10px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.16);
}

.toast-success { background: #16a34a; }
.toast-error { background: #ef4444; }
</style>
