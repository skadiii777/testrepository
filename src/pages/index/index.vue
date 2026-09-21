<template>
  <view class="page">
    <!-- 顶部：主色沉浸头 -->
    <view class="hero">
      <view class="hero-row">
        <Avatar :name="nickname" :size="72" />
        <view class="hero-info">
          <view class="hero-name">{{ nickname }}</view>
          <view class="hero-date">{{ data.today || today }} · {{ weekday }}</view>
        </view>
        <view class="clock">{{ clock }}</view>
      </view>
    </view>

    <!-- 打卡区：单主按钮，按状态切换动作 -->
    <view class="punch-wrap">
      <view class="punch-ring" :class="{ done: !punchAction.type }" @click="doPunch">
        <text class="punch-label">{{ punchAction.label }}</text>
        <text class="punch-time">{{ clockShort }}</text>
      </view>
      <view class="punch-hint">{{ punchHint }}</view>
    </view>

    <!-- 指标卡（上移覆盖） -->
    <view class="stats">
      <view class="stat" @click="goApproval">
        <view class="stat-num">{{ overtimeHours }}</view>
        <view class="stat-label">本月加班 (h)</view>
      </view>
      <view class="stat" @click="goApproval">
        <view class="stat-num" :class="{ warn: !statusLabel }">{{ statusLabel || '未打卡' }}</view>
        <view class="stat-label">今日考勤</view>
      </view>
      <view class="stat" @click="goApproval">
        <view class="stat-num" :class="{ accent: pendingTotal > 0 }">{{ pendingTotal }}</view>
        <view class="stat-label">待办审批</view>
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

    <!-- 常用应用 -->
    <view class="card">
      <view class="card-title">常用应用</view>
      <view class="grid">
        <view class="grid-item" v-for="g in grids" :key="g.text" @click="go(g)">
          <view class="grid-icon" :style="{ background: g.bg }">
            <AppIcon :name="g.icon" :size="22" :color="g.fg" />
            <text v-if="g.badge" class="grid-badge">{{ g.badge > 99 ? '99+' : g.badge }}</text>
          </view>
          <view class="grid-text">{{ g.text }}</view>
        </view>
      </view>
    </view>

    <view class="safe-bottom" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import Avatar from '../../components/Avatar.vue'
import AppIcon from '../../components/AppIcon.vue'
import { getPortalIndexData, punch, getApprovalPending } from '../../api'
import { isLoggedIn, getUser } from '../../utils/auth'
import { dictLabel } from '../../utils/dict'

const data = ref({})
const clock = ref('--:--:--')
const pending = ref({})
const nickname = computed(() => getUser()?.nickname || '同事')

const LEAVE_LABELS = { '1': '事假', '2': '病假', '3': '年假', '4': '调休' }

const today = computed(() => {
  const d = new Date()
  const p = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
})

const weekday = computed(() => '周' + '日一二三四五六'[new Date().getDay()])

const clockShort = computed(() => clock.value.slice(0, 5))

const overtimeHours = computed(() => Math.round((data.value.monthOvertimeMinutes || 0) / 6) / 10)
const statusLabel = computed(() =>
  data.value.status ? dictLabel('biz_attendance_status', data.value.status) : ''
)

const quotaList = computed(() =>
  Object.entries(data.value.quotas || {}).map(([type, remain]) => ({
    label: LEAVE_LABELS[type] || type + '假',
    remain: Number(remain)
  }))
)

const pendingTotal = computed(
  () => (pending.value.leaveCount || 0) + (pending.value.expenseCount || 0) + (pending.value.correctionCount || 0)
)

/** 打卡按钮状态机：未上班→上班打卡；已上班未下班→下班打卡；都打了→已完成 */
const punchAction = computed(() => {
  if (!data.value.checkIn) return { type: 'in', label: '上班打卡' }
  if (!data.value.checkOut) return { type: 'out', label: '下班打卡' }
  return { type: null, label: '已完成' }
})

const punchHint = computed(() => {
  const inT = data.value.checkIn ? `上班 ${data.value.checkIn}` : '上班未打卡'
  const outT = data.value.checkOut ? `下班 ${data.value.checkOut}` : '下班未打卡'
  return `${inT} · ${outT}`
})

const grids = computed(() => [
  { text: '请假', icon: 'calendar', bg: '#E1F5EE', fg: '#0F6E56', url: '/pages/leave/index' },
  { text: '报销', icon: 'bill', bg: '#E6F1FB', fg: '#185FA5', url: '/pages/expense/index' },
  { text: '补卡', icon: 'clock', bg: '#FAEEDA', fg: '#854F0B', url: '/pages/correction/index' },
  { text: '汇报', icon: 'doc', bg: '#EEEDFE', fg: '#534AB7', url: '/pages/report/index' },
  {
    text: '审批',
    icon: 'check',
    bg: '#FCEBEB',
    fg: '#A32D2D',
    url: 'tab:/pages/approval/index',
    badge: pendingTotal.value
  },
  { text: '聊天', icon: 'chat', bg: '#F1EFE8', fg: '#5F5E5A', url: '/pages/chat/index' },
  { text: '消息', icon: 'bell', bg: '#FBEAF0', fg: '#993556', url: 'tab:/pages/message/index' },
  { text: '电脑端', icon: 'monitor', bg: '#EAF3DE', fg: '#3B6D11', url: 'pc' }
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

const doPunch = async () => {
  if (!isLoggedIn()) return uni.reLaunch({ url: '/pages/login/index' })
  if (!punchAction.value.type) {
    return uni.showToast({ title: '今日打卡已完成', icon: 'none' })
  }
  const type = punchAction.value.type
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

const goApproval = () => uni.switchTab({ url: '/pages/approval/index' })

const go = (g) => {
  if (!isLoggedIn()) return uni.reLaunch({ url: '/pages/login/index' })
  if (g.url === 'pc') {
    // #ifdef H5
    window.open(location.origin + '/', '_blank')
    // #endif
    // #ifndef H5
    uni.setClipboardData({ data: 'http://8.155.128.225' })
    // #endif
    return
  }
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

<style lang="scss" scoped>
/* ---------- 顶部 ---------- */
.hero {
  background: $c-primary;
  padding: 24rpx 32rpx 20rpx;
}
.hero-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.hero-info {
  flex: 1;
  min-width: 0;
}
.hero-name {
  font-size: 32rpx;
  font-weight: 500;
  color: #ffffff;
}
.hero-date {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.75);
  margin-top: 6rpx;
}
.clock {
  font-size: 30rpx;
  font-weight: 500;
  color: #ffffff;
  font-variant-numeric: tabular-nums;
  letter-spacing: 1rpx;
}

/* ---------- 打卡 ---------- */
.punch-wrap {
  background: $c-primary;
  padding: 24rpx 32rpx 64rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.punch-ring {
  width: 196rpx;
  height: 196rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.14);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.punch-ring:active {
  background: rgba(255, 255, 255, 0.24);
}
.punch-ring.done {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.08);
}
.punch-label {
  font-size: 30rpx;
  font-weight: 500;
  color: #ffffff;
}
.punch-time {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
  font-variant-numeric: tabular-nums;
}
.punch-hint {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.78);
  margin-top: 24rpx;
}

/* ---------- 指标卡 ---------- */
.stats {
  display: flex;
  margin: -48rpx 24rpx 0;
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: $shadow-card;
  position: relative;
  z-index: 2;
}
.stat {
  flex: 1;
  text-align: center;
  padding: 26rpx 0;
  position: relative;
}
.stat::after {
  content: '';
  position: absolute;
  right: 0;
  top: 26rpx;
  bottom: 26rpx;
  width: 1rpx;
  background: $c-divider;
}
.stat:last-child::after {
  display: none;
}
.stat-num {
  font-size: 34rpx;
  font-weight: 500;
  color: $c-text-1;
}
.stat-num.warn {
  color: $c-text-3;
  font-size: 28rpx;
}
.stat-num.accent {
  color: $c-danger;
}
.stat-label {
  font-size: 22rpx;
  color: $c-text-3;
  margin-top: 6rpx;
}

/* ---------- 假期余额 ---------- */
.quota-row {
  display: flex;
}
.quota-item {
  flex: 1;
  text-align: center;
}
.quota-num {
  font-size: 40rpx;
  font-weight: 500;
  color: $c-primary;
}
.quota-num.zero {
  color: $c-text-4;
}
.quota-label {
  font-size: 22rpx;
  color: $c-text-3;
  margin-top: 6rpx;
}

/* ---------- 应用网格 ---------- */
.grid {
  display: flex;
  flex-wrap: wrap;
}
.grid-item {
  width: 25%;
  text-align: center;
  padding: 18rpx 0;
}
.grid-icon {
  width: 84rpx;
  height: 84rpx;
  border-radius: 26rpx;
  margin: 0 auto 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.grid-icon:active {
  opacity: 0.75;
}
.grid-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  padding: 0 8rpx;
  border-radius: 16rpx;
  background: $c-danger;
  color: #ffffff;
  font-size: 20rpx;
  text-align: center;
  box-sizing: border-box;
}
.grid-text {
  font-size: 24rpx;
  color: $c-text-2;
}
</style>
