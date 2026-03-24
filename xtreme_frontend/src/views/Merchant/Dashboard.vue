<script setup>
import { onMounted, ref } from 'vue'
import request from '@/utils/request'

const merchantName = localStorage.getItem('merchantName') || '商家'
const stats = ref({ totalRevenue: 0, totalOrders: 0, totalProducts: 0 })
const recentOrders = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const token = localStorage.getItem('merchantToken')
    if (!token) return
    // TODO: 接入真实接口后替换
    // const res = await request.get('/merchant/dashboard/stats')
    // stats.value = res.data.data
    stats.value = { totalRevenue: 0, totalOrders: 0, totalProducts: 0 }
    recentOrders.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="db-page">
    <!-- 欢迎语 -->
    <div class="db-welcome">
      <h2>欢迎回来，{{ merchantName }} 👋</h2>
      <p>这是您的店铺今日概览</p>
    </div>

    <!-- 统计卡片 -->
    <div class="db-stats">
      <div class="db-stat-card">
        <div class="db-stat-header">
          <span class="db-stat-label">累计销售额</span>
          <span class="db-stat-icon blue"><span class="material-symbols-outlined">payments</span></span>
        </div>
        <div class="db-stat-value">¥{{ stats.totalRevenue.toLocaleString() }}</div>
        <div class="db-stat-bar"><div class="db-stat-fill" style="width:60%"></div></div>
      </div>
      <div class="db-stat-card">
        <div class="db-stat-header">
          <span class="db-stat-label">累计订单数</span>
          <span class="db-stat-icon blue"><span class="material-symbols-outlined">receipt_long</span></span>
        </div>
        <div class="db-stat-value">{{ stats.totalOrders.toLocaleString() }}</div>
        <div class="db-stat-bar"><div class="db-stat-fill" style="width:45%"></div></div>
      </div>
      <div class="db-stat-card">
        <div class="db-stat-header">
          <span class="db-stat-label">在售商品数</span>
          <span class="db-stat-icon blue"><span class="material-symbols-outlined">inventory_2</span></span>
        </div>
        <div class="db-stat-value">{{ stats.totalProducts }}</div>
        <div class="db-stat-bar"><div class="db-stat-fill" style="width:30%"></div></div>
      </div>
    </div>

    <!-- 近期订单 -->
    <div class="db-section">
      <div class="db-section-head">
        <h3>近期订单</h3>
        <span class="db-badge">实时</span>
      </div>
      <div v-if="loading" class="db-empty">加载中...</div>
      <div v-else-if="recentOrders.length === 0" class="db-empty">
        <span class="material-symbols-outlined">receipt_long</span>
        <p>暂无订单数据</p>
        <span>发布商品并开启秒杀活动后，订单将在此显示</span>
      </div>
      <table v-else class="db-table">
        <thead>
          <tr><th>订单号</th><th>商品</th><th>金额</th><th>状态</th><th>时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="order in recentOrders" :key="order.orderNo">
            <td>{{ order.orderNo }}</td>
            <td>{{ order.productName }}</td>
            <td>¥{{ order.actualAmount }}</td>
            <td><span class="db-status" :class="order.status === 1 ? 'paid' : 'pending'">{{ order.status === 1 ? '已支付' : '待支付' }}</span></td>
            <td>{{ order.createdAt }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 快捷操作 -->
    <div class="db-section">
      <div class="db-section-head"><h3>快捷操作</h3></div>
      <div class="db-shortcuts">
        <RouterLink to="/merchant/products" class="db-shortcut">
          <span class="material-symbols-outlined">add_box</span>
          <span>发布商品</span>
        </RouterLink>
        <RouterLink to="/merchant/marketing" class="db-shortcut">
          <span class="material-symbols-outlined">flash_on</span>
          <span>创建秒杀</span>
        </RouterLink>
        <RouterLink to="/merchant/marketing" class="db-shortcut">
          <span class="material-symbols-outlined">local_offer</span>
          <span>发布优惠券</span>
        </RouterLink>
        <RouterLink to="/merchant/settings" class="db-shortcut">
          <span class="material-symbols-outlined">store</span>
          <span>店铺设置</span>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.db-page { display: flex; flex-direction: column; gap: 24px; }
.db-welcome h2 { margin: 0 0 4px; font-size: 20px; font-weight: 700; color: #262626; }
.db-welcome p { margin: 0; font-size: 13px; color: #8c8c8c; }

.db-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.db-stat-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); border-left: 4px solid #1890ff; }
.db-stat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.db-stat-label { font-size: 12px; font-weight: 600; color: #8c8c8c; text-transform: uppercase; letter-spacing: 0.06em; }
.db-stat-icon.blue { width: 32px; height: 32px; border-radius: 8px; background: #e6f7ff; color: #1890ff; display: grid; place-items: center; }
.db-stat-icon span { font-size: 18px; }
.db-stat-value { font-size: 28px; font-weight: 800; color: #1890ff; letter-spacing: -0.5px; }
.db-stat-bar { margin-top: 12px; height: 4px; background: #f0f2f5; border-radius: 2px; overflow: hidden; }
.db-stat-fill { height: 100%; background: #1890ff; border-radius: 2px; }

.db-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.db-section-head { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.db-section-head h3 { margin: 0; font-size: 14px; font-weight: 700; color: #262626; }
.db-badge { font-size: 10px; font-weight: 700; background: #e6f7ff; color: #1890ff; padding: 2px 8px; border-radius: 999px; }

.db-empty { text-align: center; padding: 40px 0; color: #8c8c8c; display: flex; flex-direction: column; align-items: center; gap: 8px; }
.db-empty span.material-symbols-outlined { font-size: 40px; color: #d9d9d9; }
.db-empty p { margin: 0; font-size: 14px; font-weight: 600; color: #595959; }
.db-empty span:last-child { font-size: 12px; }

.db-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.db-table th { text-align: left; padding: 8px 12px; font-size: 11px; font-weight: 700; color: #8c8c8c; text-transform: uppercase; border-bottom: 1px solid #f0f2f5; }
.db-table td { padding: 12px; border-bottom: 1px solid #f9f9f9; color: #262626; }
.db-status { padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 700; }
.db-status.paid { background: #f6ffed; color: #52c41a; }
.db-status.pending { background: #fffbe6; color: #faad14; }

.db-shortcuts { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.db-shortcut { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 20px 12px; border-radius: 10px; background: #f0f5ff; color: #1890ff; text-decoration: none; font-size: 13px; font-weight: 600; transition: background 0.15s; }
.db-shortcut:hover { background: #1890ff; color: #fff; }
.db-shortcut span.material-symbols-outlined { font-size: 24px; }
</style>
