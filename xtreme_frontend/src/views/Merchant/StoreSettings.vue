<script setup>
import { onMounted, ref } from 'vue'

const form = ref({ name: '', description: '', logoUrl: '' })
const merchantPhone = ref('')
const uploading = ref(false)
const saving = ref(false)
const fileInput = ref(null)
const toastMsg = ref('')

onMounted(() => {
  form.value.name = localStorage.getItem('merchantName') || ''
  form.value.logoUrl = localStorage.getItem('merchantLogoUrl') || ''
  merchantPhone.value = localStorage.getItem('merchantPhone') || '已绑定'
})

const handleLogoChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  uploading.value = true
  try {
    // TODO: 接入 OSS 上传接口
    // const fd = new FormData()
    // fd.append('file', file)
    // const res = await request.post('/merchant/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    // form.value.logoUrl = res.data.data
    form.value.logoUrl = URL.createObjectURL(file)
    localStorage.setItem('merchantLogoUrl', form.value.logoUrl)
  } finally {
    uploading.value = false
  }
}

const handleSave = async () => {
  if (!form.value.name) return
  saving.value = true
  try {
    // TODO: 接入真实接口
    // await request.put('/merchant/info', form.value)
    localStorage.setItem('merchantName', form.value.name)
    localStorage.setItem('merchantLogoUrl', form.value.logoUrl)
    toastMsg.value = '保存成功'
    setTimeout(() => { toastMsg.value = '' }, 2000)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="ss-page">
    <div class="ss-card">
      <h3 class="ss-card-title">店铺基本信息</h3>

      <!-- Logo 上传 -->
      <div class="ss-logo-section">
        <div class="ss-logo-wrap" @click="fileInput.click()">
          <img v-if="form.logoUrl" :src="form.logoUrl" class="ss-logo-img" alt="店铺Logo" />
          <div v-else class="ss-logo-placeholder">
            <span class="material-symbols-outlined">add_photo_alternate</span>
            <span>上传 Logo</span>
          </div>
          <div class="ss-logo-overlay">
            <span class="material-symbols-outlined">photo_camera</span>
          </div>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleLogoChange" />
        </div>
        <div class="ss-logo-tip">
          <p>店铺 Logo</p>
          <span>建议尺寸 200×200px，支持 JPG/PNG，上传后自动保存至阿里云 OSS</span>
          <span v-if="uploading" class="ss-uploading">上传中...</span>
        </div>
      </div>

      <!-- 表单 -->
      <form class="ss-form" @submit.prevent="handleSave">
        <div class="ss-field">
          <label>店铺名称 *</label>
          <input v-model="form.name" type="text" placeholder="请输入店铺名称" required />
        </div>
        <div class="ss-field">
          <label>店铺简介</label>
          <textarea v-model="form.description" placeholder="一句话介绍您的店铺..." rows="3"></textarea>
        </div>
        <div class="ss-actions">
          <button type="submit" class="ss-save-btn" :disabled="saving">
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </form>
    </div>

    <!-- 账号安全 -->
    <div class="ss-card">
      <h3 class="ss-card-title">账号安全</h3>
      <div class="ss-security-list">
        <div class="ss-security-item">
          <div class="ss-security-icon"><span class="material-symbols-outlined">smartphone</span></div>
          <div class="ss-security-info">
            <div class="ss-security-label">登录手机号</div>
            <div class="ss-security-value">{{ merchantPhone }}</div>
          </div>
          <span class="ss-security-status ok">已验证</span>
        </div>
        <div class="ss-security-item">
          <div class="ss-security-icon"><span class="material-symbols-outlined">lock</span></div>
          <div class="ss-security-info">
            <div class="ss-security-label">登录密码</div>
            <div class="ss-security-value">已设置</div>
          </div>
          <button class="ss-change-btn">修改密码</button>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div v-if="toastMsg" class="ss-toast">{{ toastMsg }}</div>
  </div>
</template>

<style scoped>
.ss-page { display: flex; flex-direction: column; gap: 20px; max-width: 720px; }
.ss-card { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.ss-card-title { margin: 0 0 20px; font-size: 15px; font-weight: 700; color: #262626; padding-bottom: 16px; border-bottom: 1px solid #f0f2f5; }

.ss-logo-section { display: flex; align-items: center; gap: 24px; margin-bottom: 24px; }
.ss-logo-wrap { position: relative; width: 88px; height: 88px; border-radius: 12px; overflow: hidden; cursor: pointer; flex-shrink: 0; border: 2px dashed #d9d9d9; background: #fafafa; }
.ss-logo-wrap:hover .ss-logo-overlay { opacity: 1; }
.ss-logo-img { width: 100%; height: 100%; object-fit: cover; }
.ss-logo-placeholder { width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; color: #8c8c8c; font-size: 11px; }
.ss-logo-placeholder span.material-symbols-outlined { font-size: 28px; color: #d9d9d9; }
.ss-logo-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.4); display: grid; place-items: center; color: #fff; opacity: 0; transition: opacity 0.2s; }
.ss-logo-tip p { margin: 0 0 4px; font-size: 14px; font-weight: 600; color: #262626; }
.ss-logo-tip span { display: block; font-size: 12px; color: #8c8c8c; line-height: 1.6; }
.ss-uploading { color: #1890ff !important; font-weight: 600 !important; }

.ss-form { display: flex; flex-direction: column; gap: 16px; }
.ss-field { display: flex; flex-direction: column; gap: 6px; }
.ss-field label { font-size: 13px; font-weight: 600; color: #595959; }
.ss-field input, .ss-field textarea { border: 1px solid #d9d9d9; border-radius: 8px; padding: 10px 12px; font-size: 14px; outline: none; font-family: inherit; resize: vertical; }
.ss-field input:focus, .ss-field textarea:focus { border-color: #1890ff; }
.ss-actions { display: flex; justify-content: flex-end; }
.ss-save-btn { padding: 10px 28px; border: 0; border-radius: 8px; background: #1890ff; color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; }
.ss-save-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.ss-security-list { display: flex; flex-direction: column; gap: 12px; }
.ss-security-item { display: flex; align-items: center; gap: 16px; padding: 14px 16px; background: #f9fbfd; border-radius: 10px; }
.ss-security-icon { width: 36px; height: 36px; border-radius: 8px; background: #e6f7ff; color: #1890ff; display: grid; place-items: center; flex-shrink: 0; }
.ss-security-info { flex: 1; }
.ss-security-label { font-size: 13px; font-weight: 600; color: #262626; }
.ss-security-value { font-size: 12px; color: #8c8c8c; margin-top: 2px; }
.ss-security-status.ok { font-size: 11px; font-weight: 700; background: #f6ffed; color: #52c41a; padding: 3px 10px; border-radius: 999px; }
.ss-change-btn { padding: 6px 14px; border: 1px solid #d9d9d9; border-radius: 6px; background: #fff; color: #595959; font-size: 12px; font-weight: 600; cursor: pointer; font-family: inherit; }

.ss-toast { position: fixed; bottom: 32px; left: 50%; transform: translateX(-50%); background: #52c41a; color: #fff; padding: 12px 24px; border-radius: 8px; font-size: 14px; font-weight: 600; box-shadow: 0 8px 20px rgba(0,0,0,0.15); z-index: 999; }
</style>
