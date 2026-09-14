<template>
  <view>
    <!-- 列表视图 -->
    <block v-if="mode === 'list'">
      <view class="fab" @click="openForm">＋ 请假申请</view>
      <view class="empty" v-if="!loading && !list.length">暂无请假记录</view>
      <view class="card" v-for="it in list" :key="it.id" @click="openDetail(it)">
        <view class="row-head">
          <text class="row-title">{{ dictLabel('biz_leave_type', it.leaveType) }} {{ it.days }} 天</text>
          <text class="tag" :style="{ background: dictColor('biz_leave_status', it.status) }">
            {{ dictLabel('biz_leave_status', it.status) }}
          </text>
        </view>
        <view class="row-sub">{{ it.startDate }} ~ {{ it.endDate }}<text v-if="it.reason"> · {{ it.reason }}</text></view>
        <view class="row-foot">
          <text class="row-time">{{ fmt(it.createTime) }}</text>
          <text class="btn-mini btn-mini-gray" v-if="it.status === '1'" @click.stop="cancel(it)">销假</text>
        </view>
      </view>
      <view class="load-more" v-if="list.length" @click="loadMore">{{ hasMore ? '加载更多…' : '— 已加载全部 —' }}</view>
    </block>

    <!-- 表单视图 -->
    <view class="card form-card" v-else>
      <view class="form-item">
        <text class="form-label">请假类型</text>
        <picker :range="typeLabels" @change="(e) => (form.leaveType = dict.biz_leave_type[e.detail.value].value)">
          <view class="picker-box">
            <text :class="{ 'picker-val': !!form.leaveType }">{{ form.leaveType ? dictLabel('biz_leave_type', form.leaveType) : '请选择' }}</text>
            <text>▾</text>
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">开始日期</text>
        <picker mode="date" :value="form.startDate" @change="(e) => (form.startDate = e.detail.value)">
          <view class="picker-box"><text :class="{ 'picker-val': !!form.startDate }">{{ form.startDate || '请选择' }}</text><text>▾</text></view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">结束日期</text>
        <picker mode="date" :value="form.endDate" start="" @change="(e) => (form.endDate = e.detail.value)">
          <view class="picker-box"><text :class="{ 'picker-val': !!form.endDate }">{{ form.endDate || '请选择' }}</text><text>▾</text></view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">请假天数</text>
        <input class="form-input" type="digit" v-model="form.days" placeholder="如 1.5" />
      </view>
      <view class="form-item">
        <text class="form-label">事由</text>
        <textarea class="form-input area" v-model="form.reason" placeholder="请填写请假事由" />
      </view>
      <button class="btn-primary" :disabled="submitting" @click="submit">{{ submitting ? '提交中…' : '提交申请' }}</button>
      <view class="back-link" @click="mode = 'list'">返回列表</view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getLeavePage, submitLeave, cancelLeave } from '../../api'
import { dictOptions, dictLabel, dictColor } from '../../utils/dict'

const mode = ref('list')
const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 15 })
const form = reactive({ leaveType: '', startDate: '', endDate: '', days: '', reason: '' })

const dict = reactive({ biz_leave_type: dictOptions('biz_leave_type'), biz_leave_status: dictOptions('biz_leave_status') })
const typeLabels = computed(() => dict.biz_leave_type.map((d) => d.label))
const hasMore = computed(() => list.value.length < total.value)

const fmt = (t) => (typeof t === 'string' ? t.replace('T', ' ').slice(5, 16) : '')

const load = async (append) => {
  loading.value = true
  try {
    const page = await getLeavePage(queryParams)
    list.value = append ? list.value.concat(page.list || []) : page.list || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}
const loadMore = () => {
  if (!hasMore.value) return
  queryParams.pageNo++
  load(true)
}
const openForm = () => {
  Object.assign(form, { leaveType: '', startDate: '', endDate: '', days: '', reason: '' })
  mode.value = 'form'
}
const openDetail = (it) =>
  uni.showModal({ title: '请假详情', content: `${dictLabel('biz_leave_type', it.leaveType)} ${it.days} 天\n${it.startDate} ~ ${it.endDate}\n事由：${it.reason || '-'}\n状态：${dictLabel('biz_leave_status', it.status)}`, showCancel: false })

const submit = async () => {
  if (!form.leaveType || !form.startDate || !form.endDate || !form.days) {
    return uni.showToast({ title: '请完整填写', icon: 'none' })
  }
  submitting.value = true
  try {
    await submitLeave({ ...form, days: Number(form.days) })
    uni.showToast({ title: '已提交审批', icon: 'success' })
    mode.value = 'list'
    queryParams.pageNo = 1
    setTimeout(() => load(), 600)
  } finally {
    submitting.value = false
  }
}

const cancel = (it) =>
  uni.showModal({
    title: '确认销假',
    content: '提前返岗可销假，确认提交？',
    success: async (m) => {
      if (!m.confirm) return
      await cancelLeave(it.id)
      uni.showToast({ title: '已销假', icon: 'success' })
      load()
    }
  })

onShow(() => {
  queryParams.pageNo = 1
  load()
})
</script>

<style scoped>
.fab {
  margin: 24rpx 24rpx 0;
  background: #1ab394;
  color: #fff;
  text-align: center;
  line-height: 88rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
}
.form-card {
  margin-top: 24rpx;
}
.area {
  height: 160rpx;
}
.back-link {
  text-align: center;
  color: #909399;
  margin-top: 24rpx;
  font-size: 26rpx;
}
.load-more {
  text-align: center;
  color: #c0c4cc;
  font-size: 24rpx;
  padding: 24rpx 0 40rpx;
}
</style>
