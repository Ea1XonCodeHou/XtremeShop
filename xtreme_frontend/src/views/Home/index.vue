<script setup>
import request from '@/utils/request'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = ref(false)
const COUNTDOWN_SECONDS = 60 * 60
const totalSeconds = ref(COUNTDOWN_SECONDS)
let timer = null

const hourText = computed(() => String(Math.floor(totalSeconds.value / 3600)).padStart(2, '0'))
const minuteText = computed(() => String(Math.floor((totalSeconds.value % 3600) / 60)).padStart(2, '0'))
const secondText = computed(() => String(totalSeconds.value % 60).padStart(2, '0'))

onMounted(() => {
  isLoggedIn.value = !!sessionStorage.getItem('sessionId')

  timer = setInterval(() => {
    if (totalSeconds.value <= 0) {
      totalSeconds.value = COUNTDOWN_SECONDS
      return
    }
    totalSeconds.value -= 1
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const handleLogout = async () => {
  try {
    await request.post('/user/logout')
  } catch (_) {}
  sessionStorage.removeItem('sessionId')
  sessionStorage.removeItem('userId')
  sessionStorage.removeItem('username')
  isLoggedIn.value = false
  router.push('/login')
}

const flashProducts = [
  { id: 1, title: '高端极简智能腕表', price: '1,299', oldPrice: '2,599', tag: '50% OFF', image: 'https://images.unsplash.com/photo-1524592094714-0f0654e20314?auto=format&fit=crop&w=800&q=80' },
  { id: 2, title: '无线降噪HIFI耳机', price: '899', oldPrice: '1,280', tag: '30% OFF', image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80' },
  { id: 3, title: '极速运动跑鞋系列', price: '459', oldPrice: '799', tag: 'HOT', image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80' },
]

const products = [
  { id: 1, title: '旗舰级 5G 智能手机', price: '4,999', sold: '1.2万+人已购', image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=680&q=80' },
  { id: 2, title: '商务办公轻薄本', price: '6,588', sold: '5000+人已购', image: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=680&q=80' },
  { id: 3, title: '舒适透气基础款白T', price: '89', sold: '2.5万+人已购', image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=680&q=80' },
  { id: 4, title: '健康轻食能量餐', price: '32', sold: '8000+人已购', image: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=680&q=80' },
  { id: 5, title: '沉浸式无线耳机', price: '499', sold: '1.8万+人已购', image: 'https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=680&q=80' },
  { id: 6, title: '创作生产力平板', price: '3,299', sold: '4500+人已购', image: 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=680&q=80' },
  { id: 7, title: '复古真皮夹克', price: '1,280', sold: '1200+人已购', image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=680&q=80' },
  { id: 8, title: '手作网红甜甜圈', price: '12', sold: '3.1万+人已购', image: 'https://images.unsplash.com/photo-1551024506-0bccd828d307?auto=format&fit=crop&w=680&q=80' },
]
</script>

<template>
  <div class="home-page">
    <header class="topbar">
      <div class="brand"><span class="material-symbols-outlined brand-logo">bolt</span><strong>Xtreme-Shop</strong></div>
      <div class="search"><span class="material-symbols-outlined">search</span><input type="text" placeholder="搜索心仪商品..." /></div>
      <nav>
        <a class="active" href="javascript:void(0)">秒杀专区</a>
        <a href="javascript:void(0)">数码</a>
        <a href="javascript:void(0)">服饰</a>
        <a href="javascript:void(0)">食品</a>
      </nav>
      <div class="actions">
        <RouterLink to="/merchant/login" class="merchant-btn">我是商家</RouterLink>
        <span v-if="!isLoggedIn" class="login-link"><RouterLink to="/login">登录</RouterLink> / <RouterLink to="/register">注册</RouterLink></span>
        <button v-else @click="handleLogout" class="logout-btn">退出登录</button>
      </div>
    </header>

    <main class="container">
      <section class="flash-wrap">
        <div class="flash-head">
          <div class="left"><span class="badge">限时秒杀</span><h2>距离本场秒杀结束还有</h2></div>
          <div class="countdown"><b>{{ hourText }}</b><span>:</span><b>{{ minuteText }}</b><span>:</span><b>{{ secondText }}</b></div>
        </div>
        <div class="flash-list">
          <article v-for="item in flashProducts" :key="item.id" class="flash-card">
            <div class="img-wrap"><img :src="item.image" :alt="item.title" /><em>{{ item.tag }}</em></div>
            <h3>{{ item.title }}</h3>
            <p><strong>￥{{ item.price }}</strong><span>￥{{ item.oldPrice }}</span></p>
            <button>立即抢购</button>
          </article>
        </div>
      </section>

      <section class="content-wrap">
        <aside class="sidebar">
          <h4>全部分类</h4>
          <a class="active" href="javascript:void(0)">为您推荐</a>
          <a href="javascript:void(0)">手机数码</a>
          <a href="javascript:void(0)">家用电器</a>
          <a href="javascript:void(0)">电脑办公</a>
          <a href="javascript:void(0)">美妆护肤</a>
          <a href="javascript:void(0)">食品饮料</a>
          <a href="javascript:void(0)">男装女装</a>
          <a href="javascript:void(0)">运动户外</a>
        </aside>

        <div class="product-grid">
          <article v-for="item in products" :key="item.id" class="product-card">
            <div class="pic"><img :src="item.image" :alt="item.title" /><em>秒杀</em></div>
            <h5>{{ item.title }}</h5>
            <div class="meta">
              <div><strong>￥{{ item.price }}</strong><small>{{ item.sold }}</small></div>
              <button><span class="material-symbols-outlined">add_shopping_cart</span></button>
            </div>
          </article>
        </div>
      </section>

      <div class="load-more"><button>加载更多商品</button></div>
    </main>
  </div>
</template>

<style scoped>
.home-page { min-height: 100vh; background: #f8f7f5; color: #111827; }
.topbar { height: 64px; position: sticky; top: 0; z-index: 10; padding: 0 24px; background: #fff; border-bottom: 1px solid #e5e7eb; display: grid; grid-template-columns: 200px minmax(340px, 520px) 1fr 200px; gap: 16px; align-items: center; }
.brand { display: flex; align-items: center; gap: 8px; color: #ff6600; min-width: 0; }
.brand strong { font-size: 28px; line-height: 1; white-space: nowrap; }
.brand-logo { background: #ff6600; color: #fff; border-radius: 8px; width: 32px; height: 32px; aspect-ratio: 1 / 1; display: grid; place-items: center; font-size: 20px; }
.search { position: relative; display: flex; align-items: center; width: 100%; justify-self: center; margin-left: 40px; }
.search span { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #94a3b8; font-size: 18px; }
.search input { width: 100%; max-width: 520px; height: 40px; border: 0; border-radius: 24px; background: #f1f5f9; padding: 0 14px 0 36px; }
nav { display: flex; gap: 18px; justify-content: center; }
nav a { color: #374151; text-decoration: none; font-size: 14px; font-weight: 600; }
nav .active { color: #ff6600; border-bottom: 2px solid #ff6600; }
.actions { text-align: right; display: flex; align-items: center; justify-content: flex-end; gap: 10px; }
.merchant-btn { font-size: 13px; font-weight: 600; color: #005daa; text-decoration: none; padding: 6px 12px; border-radius: 6px; border: 1px solid #005daa; white-space: nowrap; }
.merchant-btn:hover { background: #005daa; color: #fff; }
.login-link { color: #64748b; text-decoration: none; font-weight: 600; font-size: 14px; white-space: nowrap; }
.login-link a { color: #ff6600; text-decoration: none; font-weight: 700; }
.logout-btn { background: #ff6600; color: #fff; border: 0; padding: 8px 16px; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 14px; }
.container { max-width: 1360px; margin: 20px auto 0; padding: 0 16px 32px; }
.flash-wrap { background: #fff; border-radius: 12px; border: 1px solid #edf0f4; overflow: hidden; }
.flash-head { padding: 16px; border-bottom: 1px solid #f0f2f5; display: flex; justify-content: space-between; align-items: center; }
.left { display: flex; align-items: center; gap: 14px; }
.badge { background: #fff1e8; color: #ff6600; font-weight: 700; font-size: 12px; border-radius: 4px; padding: 6px 10px; }
.left h2 { margin: 0; font-size: 30px; }
.countdown { display: flex; align-items: center; gap: 8px; }
.countdown b { width: 50px; height: 40px; display: grid; place-items: center; background: #ff6600; color: #fff; border-radius: 8px; font-size: 34px; }
.countdown span { color: #ff6600; font-size: 30px; font-weight: 700; }
.flash-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; padding: 16px; }
.flash-card { min-width: 0; }
.img-wrap { position: relative; border-radius: 10px; overflow: hidden; aspect-ratio: 1/1; background: #f8fafc; }
.img-wrap img { width: 100%; height: 100%; object-fit: cover; }
.img-wrap em { position: absolute; top: 8px; left: 8px; background: #ff6600; color: #fff; padding: 3px 8px; border-radius: 4px; font-size: 11px; font-style: normal; font-weight: 700; }
.flash-card h3 { margin: 12px 0 8px; font-size: 16px; }
.flash-card p { margin: 0 0 10px; display: flex; gap: 10px; align-items: baseline; }
.flash-card strong { color: #ff6600; font-size: 34px; }
.flash-card span { color: #94a3b8; text-decoration: line-through; }
.flash-card button { width: 100%; height: 42px; background: #ff6600; color: #fff; border: 0; border-radius: 8px; font-weight: 700; cursor: pointer; }
.content-wrap { margin-top: 18px; display: grid; grid-template-columns: 220px 1fr; gap: 18px; }
.sidebar { background: #eef1f5; border-radius: 12px; padding: 14px; display: grid; gap: 6px; align-content: start; }
.sidebar h4 { margin: 0 0 8px; color: #64748b; font-size: 12px; font-weight: 700; }
.sidebar a { text-decoration: none; color: #111827; padding: 10px 12px; border-radius: 8px; font-size: 14px; }
.sidebar a.active { background: #ff6600; color: #fff; font-weight: 700; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: #fff; border: 1px solid #edf0f4; border-radius: 10px; overflow: hidden; }
.pic { position: relative; aspect-ratio: 4/5; background: #f8fafc; }
.pic img { width: 100%; height: 100%; object-fit: cover; }
.pic em { position: absolute; top: 8px; left: 8px; background: #ff6600; color: #fff; font-style: normal; font-size: 11px; padding: 2px 6px; border-radius: 4px; }
.product-card h5 { margin: 10px; font-size: 14px; min-height: 38px; }
.meta { margin: 0 10px 10px; display: flex; justify-content: space-between; align-items: center; }
.meta strong { display: block; color: #ff6600; font-size: 32px; line-height: 1; }
.meta small { color: #94a3b8; font-size: 11px; }
.meta button { width: 28px; height: 28px; border: 0; border-radius: 14px; color: #ff6600; background: #fff1e8; display: grid; place-items: center; cursor: pointer; }
.load-more { text-align: center; margin: 24px 0; }
.load-more button { height: 48px; padding: 0 30px; border-radius: 24px; border: 2px solid #ff6600; color: #ff6600; font-weight: 700; background: transparent; }
@media (max-width: 1200px) {
  .topbar { grid-template-columns: 190px minmax(260px, 1fr) 100px; }
  nav { display: none; }
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
