<script setup>
import { ref } from 'vue'

const activeTab = ref('seckill') // 'seckill' | 'coupon'

// 秒杀活动
const seckillList = ref([])
const showSeckillForm = ref(false)
const seckillForm = ref({ name: '', startTime: '', endTime: '' })

// 优惠券
const couponList = ref([])
const showCouponForm = ref(false)
const couponForm = ref({ name: '', type: 1, discountValue: '', minOrderAmount: 0, totalCount: '', startTime: '', endTime: '' })

const submitSeckill = async () => {
  if (!seckillForm.value.name || !seckillForm.value.startTime || !seckillForm.value.endTime) return
  // TODO: 接入真实接口
  // await request.post('/merchant/seckill/activity', seckillForm.value)
  showSeckillForm.value = false
  seckillForm.value = { name: '', startTime: '', endTime: '' }
}

const submitCoupon = async () => {
  if (!couponForm.value.name || !couponForm.value.discountValue) return
  // TODO: 接入真实接口
  // await request.post('/merchant/coupon', couponForm.value)
  showCouponForm.value = false
  couponForm.value = { name: '', type: 1, discountValue: '', minOrderAmount: 0, totalCount: '', startTime: '', endTime: '' }
}
</script>

<template>
  <div class="mc-page">
    <!-- Tab 切换 -->
    <div class="mc-tabs">
      <button class="mc-tab" :class="{ active: activeTab === 'seckill' }" @click="activeTab = 'seckill'">
        <span class="material-symbols-outlined">flash_on</span> 秒杀活动
      </button>
      <button class="mc-tab" :class="{ active: activeTab === 'coupon' }" @click="activeTab = 'coupon'">
        <span class="material-symbols-outlined">local_offer</span> 优惠券
      </button>
    </div>

    <!-- 秒杀活动 -->
    <div v-if="activeTab === 'seckill'">
      <div class="mc-toolbar">
        <h3>秒杀活动列表</h3>
        <button class="mc-add-btn" @click="showSeckillForm = true">
          <span class="material-symbols-outlined">add</span> 创建活动
        </button>
      </div>

      <div v-if="showSeckillForm" class="mc-form-card">
        <div class="mc-form-head">
          <h4>创建秒杀活动</h4>
          <button class="mc-close" @click="showSeckillForm = false"><span class="material-symbols-outlined">close</span></button>
        </div>
        <form class="mc-form" @submit.prevent="submitSeckill">
          <div class="mc-field"><label>活动名称 *</label><input v-model="seckillForm.name" type="text" placeholder="如：618 限时秒杀" required /></div>
          <div class="mc-field"><label>开始时间 *</label><input v-model="seckillForm.startTime" type="datetime-local" required /></div>
          <div class="mc-field"><label>结束时间 *</label><input v-model="seckillForm.endTime" type="datetime-local" required /></div>
          <div class="mc-form-tip">
            <span class="material-symbols-outlined">info</span>
            创建活动后，可在活动详情中添加秒杀商品并设置秒杀价和库存
          </div>
          <div class="mc-form-actions">
            <button type="button" class="mc-cancel" @click="showSeckillForm = false">取消</button>
            <button type="submit" class="mc-submit">创建活动</button>
          </div>
        </form>
      </div>

      <div v-if="seckillList.length === 0" class="mc-empty">
        <span class="material-symbols-outlined">flash_on</span>
        <p>暂无秒杀活动</p>
        <span>创建秒杀活动，吸引用户抢购</span>
      </div>
      <div v-else class="mc-list">
        <div v-for="item in seckillList" :key="item.id" class="mc-item">
          <div class="mc-item-info">
            <div class="mc-item-name">{{ item.name }}</div>
            <div class="mc-item-time">{{ item.startTime }} ~ {{ item.endTime }}</div>
          </div>
          <span class="mc-status-badge" :class="item.status === 1 ? 'active' : 'pending'">
            {{ item.status === 1 ? '进行中' : '未开始' }}
          </span>
        </div>
      </div>
    </div>

    <!-- 优惠券 -->
    <div v-if="activeTab === 'coupon'">
      <div class="mc-toolbar">
        <h3>优惠券列表</h3>
        <button class="mc-add-btn" @click="showCouponForm = true">
          <span class="material-symbols-outlined">add</span> 发布优惠券
        </button>
      </div>

      <div v-if="showCouponForm" class="mc-form-card">
        <div class="mc-form-head">
          <h4>发布优惠券</h4>
          <button class="mc-close" @click="showCouponForm = false"><span class="material-symbols-outlined">close</span></button>
        </div>
        <form class="mc-form" @submit.prevent="submitCoupon">
          <div class="mc-field"><label>券名称 *</label><input v-model="couponForm.name" type="text" placeholder="如：满100减20" required /></div>
          <div class="mc-field">
            <label>券类型</label>
            <select v-model="couponForm.type">
              <option :value="1">满减券</option>
              <option :value="2">折扣券</option>
            </select>
          </div>
          <div class="mc-field">
            <label>{{ couponForm.type === 1 ? '减免金额（元）' : '折扣率（如 0.8 = 8折）' }} *</label>
            <input v-model="couponForm.discountValue" type="number" step="0.01" placeholder="0.00" required />
          </div>
          <div class="mc-field"><label>最低消费门槛（元，0=无门槛）</label><input v-model="couponForm.minOrderAmount" type="number" step="0.01" placeholder="0" /></div>
          <div class="mc-field"><label>发行总量（0=不限量）</label><input v-model="couponForm.totalCount" type="number" placeholder="0" /></div>
          <div class="mc-field"><label>有效期开始 *</label><input v-model="couponForm.startTime" type="datetime-local" required /></div>
          <div class="mc-field"><label>有效期结束 *</label><input v-model="couponForm.endTime" type="datetime-local" required /></div>
          <div class="mc-form-actions">
            <button type="button" class="mc-cancel" @click="showCouponForm = false">取消</button>
            <button type="submit" class="mc-submit">发布优惠券</button>
          </div>
        </form>
      </div>

      <div v-if="couponList.length === 0" class="mc-empty">
        <span class="material-symbols-outlined">local_offer</span>
        <p>暂无优惠券</p>
        <span>发布优惠券，提升用户购买转化率</span>
      </div>
      <div v-else class="mc-list">
        <div v-for="c in couponList" :key="c.id" class="mc-item">
          <div class="mc-coupon-icon"><span class="material-symbols-outlined">local_offer</span></div>
          <div class="mc-item-info">
            <div class="mc-item-name">{{ c.name }}</div>
            <div class="mc-item-time">{{ c.startTime }} ~ {{ c.endTime }}</div>
          </div>
          <div class="mc-coupon-stats">
            <span>已领 {{ c.getCount }}</span>
            <span>已用 {{ c.usedCount }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mc-page { display: flex; flex-direction: column; gap: 20px; }
.mc-tabs { display: flex; gap: 4px; background: #fff; border-radius: 10px; padding: 6px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); width: fit-content; }
.mc-tab { display: flex; align-items: center; gap: 6px; padding: 8px 18px; border: 0; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; color: #8c8c8c; background: transparent; font-family: inherit; transition: all 0.15s; }
.mc-tab.active { background: #1890ff; color: #fff; box-shadow: 0 4px 10px rgba(24,144,255,0.25); }
.mc-tab span { font-size: 18px; }

.mc-toolbar { display: flex; justify-content: space-between; align-items: center; }
.mc-toolbar h3 { margin: 0; font-size: 16px; font-weight: 700; color: #262626; }
.mc-add-btn { display: flex; align-items: center; gap: 6px; padding: 9px 16px; background: #1890ff; color: #fff; border: 0; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; font-family: inherit; }

.mc-form-card { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.mc-form-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.mc-form-head h4 { margin: 0; font-size: 15px; font-weight: 700; color: #262626; }
.mc-close { border: 0; background: transparent; color: #8c8c8c; cursor: pointer; display: flex; align-items: center; }
.mc-form { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.mc-field { display: flex; flex-direction: column; gap: 6px; }
.mc-field label { font-size: 12px; font-weight: 600; color: #595959; }
.mc-field input, .mc-field select { height: 40px; border: 1px solid #d9d9d9; border-radius: 8px; padding: 0 12px; font-size: 14px; outline: none; font-family: inherit; background: #fff; }
.mc-field input:focus, .mc-field select:focus { border-color: #1890ff; }
.mc-form-tip { grid-column: 1/-1; display: flex; align-items: center; gap: 8px; font-size: 12px; color: #8c8c8c; background: #e6f7ff; padding: 10px 14px; border-radius: 8px; }
.mc-form-tip span { font-size: 16px; color: #1890ff; }
.mc-form-actions { grid-column: 1/-1; display: flex; justify-content: flex-end; gap: 12px; }
.mc-cancel { padding: 9px 18px; border: 1px solid #d9d9d9; border-radius: 8px; background: #fff; color: #595959; font-size: 13px; font-weight: 600; cursor: pointer; font-family: inherit; }
.mc-submit { padding: 9px 22px; border: 0; border-radius: 8px; background: #1890ff; color: #fff; font-size: 13px; font-weight: 600; cursor: pointer; font-family: inherit; }

.mc-empty { text-align: center; padding: 60px 0; color: #8c8c8c; display: flex; flex-direction: column; align-items: center; gap: 8px; background: #fff; border-radius: 12px; }
.mc-empty span.material-symbols-outlined { font-size: 48px; color: #d9d9d9; }
.mc-empty p { margin: 0; font-size: 15px; font-weight: 600; color: #595959; }

.mc-list { display: flex; flex-direction: column; gap: 10px; }
.mc-item { background: #fff; border-radius: 10px; padding: 16px 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.mc-coupon-icon { width: 40px; height: 40px; border-radius: 10px; background: #e6f7ff; color: #1890ff; display: grid; place-items: center; flex-shrink: 0; }
.mc-item-info { flex: 1; min-width: 0; }
.mc-item-name { font-size: 14px; font-weight: 600; color: #262626; }
.mc-item-time { font-size: 12px; color: #8c8c8c; margin-top: 2px; }
.mc-status-badge { font-size: 11px; font-weight: 700; padding: 3px 10px; border-radius: 999px; }
.mc-status-badge.active { background: #f6ffed; color: #52c41a; }
.mc-status-badge.pending { background: #fffbe6; color: #faad14; }
.mc-coupon-stats { display: flex; gap: 12px; font-size: 12px; color: #8c8c8c; }
</style>
