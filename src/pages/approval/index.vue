<template>
  <view>
    <view class="tabs">
      <view class="tab" v-for="t in tabs" :key="t.key" :class="{ on: tab === t.key }" @click="switchTab(t.key)">
        {{ t.label }}
        <text class="dot" v-if="counts[t.countKey]">{{ counts[t.countKey] }}</text>
      </view>
    </view>

    <view class="notice" v-if="denied">当前账号无审批权限，请在电脑端使用审批中心。</view>

    <block v-else>
      <view class="empty" v-if="!loading && !list.length">暂无待审批单据</view>
      <view class="card" v-for="it in list" :key="it.id">
        <view class="row-head">
          <text class="row-title">{{ it._title }}</text>
          <text class="tag" style="background: #e6a23c">待审批</text>
        </view>
        <view class="row-sub">{{ it._sub }}</view>
        <view class="row-foot">
          <text class="row-time">{{ it.empName || '' }} · {{ fmt(it.createTime) }}</text>
          <view class="ops">
            <text class="btn-mini" @click="audit(it, '1')">通过</text>
            <text class="btn-mini btn-mini-gray refuse" @click="audit(it, '2')">驳回</text>
          </view>
        </view>
      </view>
      <view class="load-more" v-if="list.length" @click="loadMore">{{ hasMore ? '加载更多…' : '— 已加载全部 —' }}</view>
    </block>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getApprovalPending,
  getApprovalLeavePage,
  getApprovalExpensePage,
  getApprovalCorrectionPage,
  auditLeave,
  auditExpense,
  auditCorrection
} from '../../api'
import { getUser } from '../../utils/auth'
import { dictLabel } from '../../utils/dict'

const tabs = [
  { key: 'leave', label: '请假', countKey: 'leaveCount' },
  { key: 'expense', label: '报销', countKey: 'expenseCount' },
  { key: 'correction', label: '补卡', countKey: 'correctionCount' }
]
const tab = ref('leave')
const loading = ref(false)
const denied = ref(false)
const list = ref([])
const total = ref(0)
const counts = reactive({})
const queryParams = reactive({ pageNo: 1, pageSize: 15 })

const hasMore = computed(() => list.value.length < total.value)
const fmt = (t) => (typeof t === 'string' ? t.replace('T', ' ').slice(5, 16) : '')

const decorate = (it) => {
  if (tab.value === 'leave') {
    it._title = `${dictLabel('biz_leave_type', it.leaveType)} ${it.days} 天`
    it._sub = `${it.startDate} ~ ${it.endDate}${it.reason ? ' · ' + it.reason : ''}`
  } else if (tab.value === 'expense') {
    it._title = `${dictLabel('biz_expense_type', it.category)} ￥${it.amount}`
    it._sub = `${it.expenseDate}${it.reason ? ' · ' + it.reason : ''}`
  } else {
    it._title = `${it.workDate} ${dictLabel('biz_correction_type', it.correctType)}`
    it._sub = `${it.correctTime || ''}${it.reason ? ' · ' + it.reason : ''}`
  }
  return it
}

const load = async (append) => {
  if (denied.value) return
  loading.value = true
  try {
    const api = tab.value === 'leave' ? getApprovalLeavePage : tab.value === 'expense' ? getApprovalExpensePage : getApprovalCorrectionPage
    const page = await api(queryParams)
    list.value = append ? list.value.concat((page.list || []).map(decorate)) : (page.list || []).map(decorate)
    total.value = page.total || 0
    denied.value = false
  } catch (e) {
    if (String(e?.message || e).includes('权限') || String(e?.message || e).includes('403')) denied.value = true
  } finally {
    loading.value = false
  }
}

const loadCounts = async () => {
  try {
    Object.assign(counts, await getApprovalPending())
  } catch {}
}

const switchTab = (k) => {
  tab.value = k
  queryParams.pageNo = 1
  list.value = []
  load()
}
const loadMore = () => {
  if (!hasMore.value) return
  queryParams.pageNo++
  load(true)
}

const audit = (it, status) => {
  const pass = status === '1'
  uni.showModal({
    title: pass ? '通过审批' : '驳回审批',
    content: pass ? '确认通过该申请？' : '确认驳回该申请？',
    editable: !pass,
    placeholderText: pass ? '' : '请填写驳回原因',
    success: async (m) => {
      if (!m.confirm) return
      if (!pass && !m.content) return uni.showToast({ title: '请填写驳回原因', icon: 'none' })
      const remark = pass ? '审批通过' : m.content
      const api = tab.value === 'leave' ? auditLeave : tab.value === 'expense' ? auditExpense : auditCorrection
      try {
        await api(it.id, status, remark)
        uni.showToast({ title: pass ? '已通过' : '已驳回', icon: 'success' })
        queryParams.pageNo = 1
        setTimeout(() => {
          load()
          loadCounts()
        }, 600)
      } catch {}
    }
  })
}

onShow(() => {
  const perms = getUser()?.permissions || []
  if (!perms.includes('*:*:*') && !perms.includes('biz:approval:query')) {
    denied.value = true
    return
  }
  queryParams.pageNo = 1
  load()
  loadCounts()
})
</script>

<style scoped>
.tabs {
  display: flex;
  background: #fff;
}
.tab {
  flex: 1;
  text-align: center;
  line-height: 92rpx;
  font-size: 28rpx;
  color: #606266;
  position: relative;
}
.tab.on {
  color: #1ab394;
  font-weight: 600;
}
.tab.on::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 56rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: #1ab394;
}
.dot {
  background: #f56c6c;
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  padding: 0 10rpx;
  margin-left: 8rpx;
  font-weight: 400;
}
.notice {
  margin: 24rpx;
  padding: 24rpx;
  background: #fdf6ec;
  color: #b88230;
  border-radius: 12rpx;
  font-size: 26rpx;
}
.ops {
  display: flex;
}
.ops .btn-mini {
  margin-left: 16rpx;
}
.refuse {
  border-color: #f56c6c;
  color: #f56c6c;
}
.load-more {
  text-align: center;
  color: #c0c4cc;
  font-size: 24rpx;
  padding: 24rpx 0 40rpx;
}
</style>
