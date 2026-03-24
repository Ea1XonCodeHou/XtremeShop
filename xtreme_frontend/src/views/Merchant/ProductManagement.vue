<script setup>
import { onMounted, ref } from 'vue'
import request from '@/utils/request'

const products = ref([])
const loading = ref(true)
const showForm = ref(false)

const form = ref({ name: '', price: '', stock: '', categoryId: '', description: '', coverUrl: '' })
const uploading = ref(false)
const fileInput = ref(null)

onMounted(async () => {
  await loadProducts()
})

const loadProducts = async () => {
  loading.value = true
  try {
    // TODO: 接入真实接口
    // const res = await request.get('/merchant/products')
    // products.value = res.data.data || []
    products.value = []
  } finally {
    loading.value = false
  }
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    // TODO: 接入 OSS 上传接口
    // const res = await request.post('/merchant/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    // form.value.coverUrl = res.data.data
    form.value.coverUrl = URL.createObjectURL(file) // 本地预览，上传接口接入后替换
  } finally {
    uploading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.price) return
  try {
    // TODO: 接入真实接口
    // await request.post('/merchant/products', form.value)
    showForm.value = false
    form.value = { name: '', price: '', stock: '', categoryId: '', description: '', coverUrl: '' }
    await loadProducts()
  } catch {
    alert('发布失败，请重试')
  }
}

const toggleSale = async (product) => {
  // TODO: 接入上下架接口
  // await request.put(`/merchant/products/${product.id}/toggle`)
  product.isOnSale = product.isOnSale === 1 ? 0 : 1
}
</script>

<template>
  <div class="pm-page">
    <div class="pm-toolbar">
      <h2>商品列表</h2>
      <button class="pm-add-btn" @click="showForm = true">
        <span class="material-symbols-outlined">add</span> 发布商品
      </button>
    </div>

    <!-- 发布商品表单 -->
    <div v-if="showForm" class="pm-form-wrap">
      <div class="pm-form-head">
        <h3>发布新商品</h3>
        <button class="pm-close-btn" @click="showForm = false"><span class="material-symbols-outlined">close</span></button>
      </div>
      <form class="pm-form" @submit.prevent="handleSubmit">
        <!-- 封面图上传 -->
        <div class="pm-cover-upload" @click="fileInput.click()">
          <img v-if="form.coverUrl" :src="form.coverUrl" class="pm-cover-preview" alt="封面" />
          <div v-else class="pm-cover-placeholder">
            <span class="material-symbols-outlined">add_photo_alternate</span>
            <span>{{ uploading ? '上传中...' : '点击上传封面图' }}</span>
          </div>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileChange" />
        </div>

        <div class="pm-form-grid">
          <div class="pm-field">
            <label>商品名称 *</label>
            <input v-model="form.name" type="text" placeholder="请输入商品名称" required />
          </div>
          <div class="pm-field">
            <label>原价（元）*</label>
            <input v-model="form.price" type="number" step="0.01" placeholder="0.00" required />
          </div>
          <div class="pm-field">
            <label>库存数量</label>
            <input v-model="form.stock" type="number" placeholder="0" />
          </div>
          <div class="pm-field">
            <label>商品简介</label>
            <input v-model="form.description" type="text" placeholder="一句话描述商品" />
          </div>
        </div>

        <div class="pm-form-actions">
          <button type="button" class="pm-cancel-btn" @click="showForm = false">取消</button>
          <button type="submit" class="pm-submit-btn">发布商品</button>
        </div>
      </form>
    </div>

    <!-- 商品列表 -->
    <div v-if="loading" class="pm-empty">加载中...</div>
    <div v-else-if="products.length === 0" class="pm-empty">
      <span class="material-symbols-outlined">inventory_2</span>
      <p>还没有商品</p>
      <span>点击「发布商品」开始添加您的第一件商品</span>
    </div>
    <div v-else class="pm-grid">
      <div v-for="p in products" :key="p.id" class="pm-card">
        <div class="pm-card-img">
          <img v-if="p.coverUrl" :src="p.coverUrl" :alt="p.name" />
          <div v-else class="pm-img-placeholder"><span class="material-symbols-outlined">image</span></div>
          <span class="pm-sale-badge" :class="p.isOnSale === 1 ? 'on' : 'off'">{{ p.isOnSale === 1 ? '上架' : '下架' }}</span>
        </div>
        <div class="pm-card-body">
          <div class="pm-card-name">{{ p.name }}</div>
          <div class="pm-card-price">¥{{ p.price }}</div>
          <div class="pm-card-meta">库存 {{ p.stock }} · 已售 {{ p.soldCount }}</div>
        </div>
        <div class="pm-card-actions">
          <button class="pm-toggle-btn" :class="p.isOnSale === 1 ? 'off' : 'on'" @click="toggleSale(p)">
            {{ p.isOnSale === 1 ? '下架' : '上架' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pm-page { display: flex; flex-direction: column; gap: 20px; }
.pm-toolbar { display: flex; justify-content: space-between; align-items: center; }
.pm-toolbar h2 { margin: 0; font-size: 18px; font-weight: 700; color: #262626; }
.pm-add-btn { display: flex; align-items: center; gap: 6px; padding: 10px 18px; background: #1890ff; color: #fff; border: 0; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; }
.pm-add-btn:hover { background: #096dd9; }

.pm-form-wrap { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.pm-form-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.pm-form-head h3 { margin: 0; font-size: 16px; font-weight: 700; color: #262626; }
.pm-close-btn { border: 0; background: transparent; color: #8c8c8c; cursor: pointer; display: flex; align-items: center; }

.pm-cover-upload { width: 160px; height: 160px; border-radius: 10px; border: 2px dashed #d9d9d9; cursor: pointer; overflow: hidden; margin-bottom: 20px; display: flex; align-items: center; justify-content: center; background: #fafafa; transition: border-color 0.2s; }
.pm-cover-upload:hover { border-color: #1890ff; }
.pm-cover-preview { width: 100%; height: 100%; object-fit: cover; }
.pm-cover-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #8c8c8c; font-size: 12px; }
.pm-cover-placeholder span.material-symbols-outlined { font-size: 32px; color: #d9d9d9; }

.pm-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.pm-field { display: flex; flex-direction: column; gap: 6px; }
.pm-field label { font-size: 12px; font-weight: 600; color: #595959; }
.pm-field input { height: 40px; border: 1px solid #d9d9d9; border-radius: 8px; padding: 0 12px; font-size: 14px; outline: none; font-family: inherit; }
.pm-field input:focus { border-color: #1890ff; }

.pm-form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.pm-cancel-btn { padding: 10px 20px; border: 1px solid #d9d9d9; border-radius: 8px; background: #fff; color: #595959; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; }
.pm-submit-btn { padding: 10px 24px; border: 0; border-radius: 8px; background: #1890ff; color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; }

.pm-empty { text-align: center; padding: 60px 0; color: #8c8c8c; display: flex; flex-direction: column; align-items: center; gap: 8px; background: #fff; border-radius: 12px; }
.pm-empty span.material-symbols-outlined { font-size: 48px; color: #d9d9d9; }
.pm-empty p { margin: 0; font-size: 15px; font-weight: 600; color: #595959; }

.pm-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.pm-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.pm-card-img { position: relative; aspect-ratio: 1/1; background: #f5f5f5; }
.pm-card-img img { width: 100%; height: 100%; object-fit: cover; }
.pm-img-placeholder { width: 100%; height: 100%; display: grid; place-items: center; color: #d9d9d9; }
.pm-img-placeholder span { font-size: 40px; }
.pm-sale-badge { position: absolute; top: 8px; left: 8px; font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 999px; }
.pm-sale-badge.on { background: #f6ffed; color: #52c41a; }
.pm-sale-badge.off { background: #fff1f0; color: #ff4d4f; }
.pm-card-body { padding: 12px; }
.pm-card-name { font-size: 13px; font-weight: 600; color: #262626; margin-bottom: 4px; }
.pm-card-price { font-size: 16px; font-weight: 800; color: #1890ff; }
.pm-card-meta { font-size: 11px; color: #8c8c8c; margin-top: 4px; }
.pm-card-actions { padding: 0 12px 12px; }
.pm-toggle-btn { width: 100%; height: 32px; border-radius: 6px; border: 0; font-size: 12px; font-weight: 600; cursor: pointer; font-family: inherit; }
.pm-toggle-btn.off { background: #fff1f0; color: #ff4d4f; }
.pm-toggle-btn.on { background: #f6ffed; color: #52c41a; }
</style>
