<template>
  <view class="home">
    <!-- 顶部问候卡 -->
    <view class="hero">
      <view class="hero-top">
        <view>
          <view class="hello">{{ greeting }}，{{ nickname }}</view>
          <view class="date">{{ data.today }} · 工作时间 {{ data.workStart || '09:00' }} - {{ data.workEnd || '18:00' }}</view>
        </view>
        <view class="clock">{{ clock }}</view>
      </view>
      <view class="punch-row">
        <view class="punch-btn" :class="{ done: !!data.checkIn }" @click="doPunch('in')">
          <text class="punch-label">上班打卡</text>
          <text class="punch-time">{{ data.checkIn || '未打卡' }}</text>
        </view>
        <view class="punch-btn" :class="{ done: !!data.checkOut }" @click="doPunch('out')">
          <text class="punch-label">下班打卡</text>
          <text class="punch-time">{{ data.checkOut || '未打卡' }}</text>
        </view>
      </view>
      <view class="hero-foot">
        <text>本月加班 {{ overtimeHours }} 小时</text>
        <text v-if="statusLabel" class="att-tag">{{ statusLabel }}</text>
      </view>
    </view>

    <!-- 假期余额 -->
    <view class="card" v-if="quotaList.length">
      <view class="card-title">我的假期余额</view>
      <view class="quota-row">
        <view class="quota-item" v-for="q in quotaList" :key="q.label">
          <view class="quota-num" :class="{ zero: q.remain <= 0 }">{{ q.remain }}</view>
          <view class="quota-label">{{ q.label }}</view>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="card">
      <view class="card-title">快捷服务</view>
      <view class="grid">
        <view class="grid-item" v-for="g in grids" :key="g.text" @click="go(g)">
          <view class="grid-icon" :style="{ background: g.bg }">{{ g.icon }}</view>
          <view class="grid-text">{{ g.text }}</view>
          <view class="grid-badge" v-if="g.badge">{{ g.badge }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getPortalIndexData, punch, getApprovalPending } from '../../api'
import { isLoggedIn, getUser } from '../../utils/auth'
import { dictLabel } from '../../utils/dict'

const data = ref({})
const clock = ref('--:--:--')
const pending = ref({})
const nickname = computed(() => getUser()?.nickname || '同事')

const greeting = computed(() => {
  const h = new Date().getHours()
  return h < 6 ? '夜深了' : h < 12 ? '早上好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
})
const overtimeHours = computed(() => Math.round((data.value.monthOvertimeMinutes || 0) / 6) / 10)
const statusLabel = computed(() => (data.value.status ? dictLabel('biz_attendance_status', data.value.status) : ''))
const LEAVE_LABELS = { '1': '事假', '2': '病假', '3': '年假', '4': '调休' }
const quotaList = computed(() =>
  Object.entries(data.value.quotas || {}).map(([type, remain]) => ({
    label: LEAVE_LABELS[type] || type + '假',
    remain: Number(remain)
  }))
)
const pendingTotal = computed(
  () => (pending.value.leaveCount || 0) + (pending.value.expenseCount || 0) + (pending.value.correctionCount || 0)
)
const grids = computed(() => [
  { text: '我的请假', icon: '休', bg: '#e6f7f2', url: '/pages/leave/index', badge: 0 },
  { text: '我的报销', icon: '报', bg: '#fdf3e3', url: '/pages/expense/index', badge: 0 },
  { text: '我的补卡', icon: '补', bg: '#eaeafc', url: '/pages/correction/index', badge: 0 },
  { text: '业务汇报', icon: '周', bg: '#fdeaea', url: '/pages/report/index', badge: 0 },
  { text: '审批中心', icon: '审', bg: '#e3f2fd', url: 'tab:/pages/approval/index', badge: pendingTotal.value },
  { text: '消息', icon: '信', bg: '#fff0f6', url: 'tab:/pages/message/index', badge: 0 }
])

let timer = null
const tick = () => {
  const d = new Date()
  const p = (n) => (n < 10 ? '0' + n : n)
  clock.value = `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

const load = async () => {
  try {
    data.value = await getPortalIndexData()
    const me = getUser()
    const perms = me?.permissions || []
    if (perms.includes('*:*:*') || perms.includes('biz:approval:query')) {
      pending.value = await getApprovalPending().catch(() => ({}))
    }
  } catch {}
}

const doPunch = async (type) => {
  if (!isLoggedIn()) return uni.reLaunch({ url: '/pages/login/index' })
  const confirmed = await new Promise((r) =>
    uni.showModal({
      title: type === 'in' ? '上班打卡' : '下班打卡',
      content: `当前时间 ${clock.value}，确认打卡？`,
      success: (m) => r(m.confirm)
    })
  )
  if (!confirmed) return
  try {
    const msg = await punch(type)
    uni.showToast({ title: msg || '打卡成功', icon: 'success' })
    setTimeout(load, 800)
  } catch (e) {
    uni.showModal({ title: '打卡失败', content: e?.message || String(e), showCancel: false })
  }
}

const go = (g) => {
  if (!isLoggedIn()) return uni.reLaunch({ url: '/pages/login/index' })
  if (g.url.startsWith('tab:')) uni.switchTab({ url: g.url.slice(4) })
  else uni.navigateTo({ url: g.url })
}

onShow(() => {
  if (!isLoggedIn()) return uni.reLaunch({ url: '/pages/login/index' })
  tick()
  if (!timer) timer = setInterval(tick, 1000)
  load()
})
</script>

<style scoped>
.home {
  padding-bottom: 40rpx;
}
.hero {
  background: linear-gradient(135deg, #1ab394, #149c80);
  padding: 40rpx 32rpx 32rpx;
  color: #fff;
  border-radius: 0 0 32rpx 32rpx;
}
.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.hello {
  font-size: 36rpx;
  font-weight: 700;
}
.date {
  font-size: 22rpx;
  opacity: 0.85;
  margin-top: 10rpx;
}
.clock {
  font-size: 48rpx;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  letter-spacing: 2rpx;
}
.punch-row {
  display: flex;
  margin-top: 36rpx;
}
.punch-btn {
  flex: 1;
  background: rgba(255, 255, 255, 0.16);
  border-radius: 20rpx;
  padding: 24rpx 0;
  text-align: center;
  margin-right: 24rpx;
}
.punch-btn:last-child {
  margin-right: 0;
}
.punch-btn.done {
  background: rgba(255, 255, 255, 0.3);
}
.punch-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}
.punch-time {
  display: block;
  font-size: 24rpx;
  opacity: 0.9;
  margin-top: 8rpx;
}
.hero-foot {
  display: flex;
  justify-content: space-between;
  margin-top: 24rpx;
  font-size: 24rpx;
  opacity: 0.9;
}
.att-tag {
  background: rgba(255, 255, 255, 0.25);
  border-radius: 8rpx;
  padding: 2rpx 16rpx;
}
.quota-row {
  display: flex;
}
.quota-item {
  flex: 1;
  text-align: center;
}
.quota-num {
  font-size: 44rpx;
  font-weight: 700;
  color: #1ab394;
}
.quota-num.zero {
  color: #f56c6c;
}
.quota-label {
  font-size: 24rpx;
  color: #909399;
  margin-top: 4rpx;
}
.grid {
  display: flex;
  flex-wrap: wrap;
}
.grid-item {
  width: 33.33%;
  text-align: center;
  padding: 20rpx 0;
  position: relative;
}
.grid-icon {
  width: 92rpx;
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 24rpx;
  margin: 0 auto 12rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #1ab394;
}
.grid-text {
  font-size: 26rpx;
  color: #606266;
}
.grid-badge {
  position: absolute;
  top: 12rpx;
  right: 32rpx;
  min-width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  border-radius: 18rpx;
  background: #f56c6c;
  color: #fff;
  font-size: 22rpx;
  padding: 0 8rpx;
}
</style>
