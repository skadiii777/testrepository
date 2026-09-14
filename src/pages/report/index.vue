<template>
  <view>
    <block v-if="mode === 'list'">
      <view class="fab" @click="openForm">＋ 提交汇报</view>
      <view class="empty" v-if="!loading && !list.length">暂无汇报记录</view>
      <view class="card" v-for="it in list" :key="it.id" @click="openDetail(it)">
        <view class="row-head">
          <text class="row-title">{{ dictLabel('biz_report_type', it.reportType) }} · {{ it.title }}</text>
          <text class="row-time">{{ it.reportDate }}</text>
        </view>
        <view class="row-sub excerpt">{{ it.content }}</view>
        <view class="row-foot">
          <text class="row-time">{{ fmt(it.createTime) }}</text>
          <text class="btn-mini btn-mini-gray" @click.stop="remove(it)">删除</text>
        </view>
      </view>
    </block>

    <view class="card form-card" v-else>
      <view class="form-item">
        <text class="form-label">汇报类型</text>
        <picker :range="typeLabels" @change="(e) => (form.reportType = dict.biz_report_type[e.detail.value].value)">
          <view class="picker-box">
            <text :class="{ 'picker-val': !!form.reportType }">{{ form.reportType ? dictLabel('biz_report_type', form.reportType) : '请选择' }}</text>
            <text>▾</text>
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">汇报日期</text>
        <picker mode="date" :value="form.reportDate" @change="(e) => (form.reportDate = e.detail.value)">
          <view class="picker-box"><text :class="{ 'picker-val': !!form.reportDate }">{{ form.reportDate || '请选择' }}</text><text>▾</text></view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">标题</text>
        <input class="form-input" v-model="form.title" maxlength="200" placeholder="一句话概括" />
      </view>
      <view class="form-item">
        <text class="form-label">正文</text>
        <textarea class="form-input area" v-model="form.content" placeholder="工作内容、进展、问题与计划" />
      </view>
      <button class="btn-primary" :disabled="submitting" @click="submit">{{ submitting ? '提交中…' : '提交汇报' }}</button>
      <view class="back-link" @click="mode = 'list'">返回列表</view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getReportPage, submitReport, deleteReport } from '../../api'
import { dictOptions, dictLabel } from '../../utils/dict'

const mode = ref('list')
const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const queryParams = reactive({ pageNo: 1, pageSize: 30 })
const form = reactive({ reportType: '', title: '', content: '', reportDate: '' })

const dict = reactive({ biz_report_type: dictOptions('biz_report_type') })
const typeLabels = computed(() => dict.biz_report_type.map((d) => d.label))
const fmt = (t) => (typeof t === 'string' ? t.replace('T', ' ').slice(5, 16) : '')

const load = async () => {
  loading.value = true
  try {
    const page = await getReportPage(queryParams)
    list.value = page.list || []
  } finally {
    loading.value = false
  }
}

const openForm = () => {
  Object.assign(form, { reportType: '1', title: '', content: '', reportDate: today() })
  mode.value = 'form'
}
const today = () => {
  const d = new Date()
  const p = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const submit = async () => {
  if (!form.reportType || !form.title || !form.content) {
    return uni.showToast({ title: '请完整填写', icon: 'none' })
  }
  submitting.value = true
  try {
    await submitReport({ ...form })
    uni.showToast({ title: '已提交', icon: 'success' })
    mode.value = 'list'
    setTimeout(() => load(), 600)
  } finally {
    submitting.value = false
  }
}

const openDetail = (it) =>
  uni.showModal({ title: it.title, content: it.content || '-', showCancel: false })

const remove = (it) =>
  uni.showModal({
    title: '删除汇报',
    content: `确认删除《${it.title}》？`,
    success: async (m) => {
      if (!m.confirm) return
      await deleteReport(it.id)
      uni.showToast({ title: '已删除', icon: 'success' })
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
  height: 220rpx;
}
.excerpt {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.back-link {
  text-align: center;
  color: #909399;
  margin-top: 24rpx;
  font-size: 26rpx;
}
</style>
