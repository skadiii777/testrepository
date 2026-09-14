<template>
  <view>
    <block v-if="mode === 'list'">
      <view class="fab" @click="openForm">＋ 补卡申请</view>
      <view class="empty" v-if="!loading && !list.length">暂无补卡记录</view>
      <view class="card" v-for="it in list" :key="it.id">
        <view class="row-head">
          <text class="row-title">{{ it.workDate }} {{ dictLabel('biz_correction_type', it.correctType) }}</text>
          <text class="tag" :style="{ background: dictColor('biz_correction_status', it.status) }">
            {{ dictLabel('biz_correction_status', it.status) }}
          </text>
        </view>
        <view class="row-sub">{{ it.correctTime }}<text v-if="it.reason"> · {{ it.reason }}</text></view>
        <view class="row-foot">
          <text class="row-time">{{ fmt(it.createTime) }}</text>
          <text class="btn-mini btn-mini-gray" v-if="it.status === '0'" @click="withdraw(it)">撤回</text>
        </view>
      </view>
    </block>

    <view class="card form-card" v-else>
      <view class="form-item">
        <text class="form-label">补卡日期</text>
        <picker mode="date" :value="form.workDate" @change="(e) => (form.workDate = e.detail.value)">
          <view class="picker-box"><text :class="{ 'picker-val': !!form.workDate }">{{ form.workDate || '请选择' }}</text><text>▾</text></view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">补卡类型</text>
        <picker :range="typeLabels" @change="(e) => (form.correctType = dict.biz_correction_type[e.detail.value].value)">
          <view class="picker-box">
            <text :class="{ 'picker-val': !!form.correctType }">{{ form.correctType ? dictLabel('biz_correction_type', form.correctType) : '漏打哪张卡？' }}</text>
            <text>▾</text>
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">补卡时间</text>
        <picker mode="time" :value="form.correctTime" @change="(e) => (form.correctTime = e.detail.value)">
          <view class="picker-box"><text :class="{ 'picker-val': !!form.correctTime }">{{ form.correctTime || '请选择' }}</text><text>▾</text></view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">补卡原因</text>
        <textarea class="form-input area" v-model="form.reason" placeholder="如：外出拜访客户未及时打卡" />
      </view>
      <button class="btn-primary" :disabled="submitting" @click="submit">{{ submitting ? '提交中…' : '提交申请' }}</button>
      <view class="back-link" @click="mode = 'list'">返回列表</view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCorrectionPage, submitCorrection, withdrawCorrection } from '../../api'
import { dictOptions, dictLabel, dictColor } from '../../utils/dict'

const mode = ref('list')
const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const queryParams = reactive({ pageNo: 1, pageSize: 30 })
const form = reactive({ workDate: '', correctType: '', correctTime: '', reason: '' })

const dict = reactive({ biz_correction_type: dictOptions('biz_correction_type') })
const typeLabels = computed(() => dict.biz_correction_type.map((d) => d.label))
const fmt = (t) => (typeof t === 'string' ? t.replace('T', ' ').slice(5, 16) : '')

const load = async () => {
  loading.value = true
  try {
    const page = await getCorrectionPage(queryParams)
    list.value = page.list || []
  } finally {
    loading.value = false
  }
}

const openForm = () => {
  Object.assign(form, { workDate: today(), correctType: '', correctTime: '', reason: '' })
  mode.value = 'form'
}
const today = () => {
  const d = new Date()
  const p = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const submit = async () => {
  if (!form.workDate || !form.correctType || !form.correctTime) {
    return uni.showToast({ title: '请完整填写', icon: 'none' })
  }
  submitting.value = true
  try {
    await submitCorrection({ ...form })
    uni.showToast({ title: '已提交审批', icon: 'success' })
    mode.value = 'list'
    setTimeout(() => load(), 600)
  } finally {
    submitting.value = false
  }
}

const withdraw = (it) =>
  uni.showModal({
    title: '撤回补卡',
    content: '确认撤回该补卡申请？',
    success: async (m) => {
      if (!m.confirm) return
      await withdrawCorrection(it.id)
      uni.showToast({ title: '已撤回', icon: 'success' })
      load()
    }
  })

onShow(() => load())
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
</style>
