<template>
  <view>
    <block v-if="mode === 'list'">
      <view class="fab" @click="openForm">＋ 报销申请</view>
      <view class="empty" v-if="!loading && !list.length">暂无报销记录</view>
      <view class="card" v-for="it in list" :key="it.id">
        <view class="row-head">
          <text class="row-title">{{ dictLabel('biz_expense_type', it.category) }} ￥{{ it.amount }}</text>
          <text class="tag" :style="{ background: dictColor('biz_expense_status', it.status) }">
            {{ dictLabel('biz_expense_status', it.status) }}
          </text>
        </view>
        <view class="row-sub">{{ it.expenseDate }}<text v-if="it.reason"> · {{ it.reason }}</text></view>
        <view class="row-sub" v-if="it.status === '2' && it.auditRemark">驳回原因：{{ it.auditRemark }}</view>
        <view class="row-foot">
          <text class="row-time">{{ fmt(it.createTime) }}</text>
          <text class="btn-mini btn-mini-gray" v-if="it.status === '0'" @click="withdraw(it)">撤回</text>
        </view>
      </view>
      <view class="load-more" v-if="list.length" @click="loadMore">{{ hasMore ? '加载更多…' : '— 已加载全部 —' }}</view>
    </block>

    <view class="card form-card" v-else>
      <view class="form-item">
        <text class="form-label">费用类别</text>
        <picker :range="typeLabels" @change="(e) => (form.category = dict.biz_expense_type[e.detail.value].value)">
          <view class="picker-box">
            <text :class="{ 'picker-val': !!form.category }">{{ form.category ? dictLabel('biz_expense_type', form.category) : '请选择' }}</text>
            <text>▾</text>
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">金额（元）</text>
        <input class="form-input" type="digit" v-model="form.amount" placeholder="0.00" />
      </view>
      <view class="form-item">
        <text class="form-label">费用日期</text>
        <picker mode="date" :value="form.expenseDate" @change="(e) => (form.expenseDate = e.detail.value)">
          <view class="picker-box"><text :class="{ 'picker-val': !!form.expenseDate }">{{ form.expenseDate || '请选择' }}</text><text>▾</text></view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">报销事由</text>
        <textarea class="form-input area" v-model="form.reason" placeholder="费用说明" />
      </view>
      <button class="btn-primary" :disabled="submitting" @click="submit">{{ submitting ? '提交中…' : '提交申请' }}</button>
      <view class="back-link" @click="mode = 'list'">返回列表</view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getExpensePage, submitExpense, withdrawExpense } from '../../api'
import { dictOptions, dictLabel, dictColor } from '../../utils/dict'

const mode = ref('list')
const submitting = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 15 })
const form = reactive({ category: '', amount: '', expenseDate: '', reason: '' })

const dict = reactive({ biz_expense_type: dictOptions('biz_expense_type'), biz_expense_status: dictOptions('biz_expense_status') })
const typeLabels = computed(() => dict.biz_expense_type.map((d) => d.label))
const hasMore = computed(() => list.value.length < total.value)
const fmt = (t) => (typeof t === 'string' ? t.replace('T', ' ').slice(5, 16) : '')

const load = async (append) => {
  loading.value = true
  try {
    const page = await getExpensePage(queryParams)
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
  Object.assign(form, { category: '', amount: '', expenseDate: today(), reason: '' })
  mode.value = 'form'
}
const today = () => {
  const d = new Date()
  const p = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const submit = async () => {
  if (!form.category || !form.amount || !form.expenseDate) {
    return uni.showToast({ title: '请完整填写', icon: 'none' })
  }
  submitting.value = true
  try {
    await submitExpense({ ...form, amount: Number(form.amount) })
    uni.showToast({ title: '已提交审批', icon: 'success' })
    mode.value = 'list'
    queryParams.pageNo = 1
    setTimeout(() => load(), 600)
  } finally {
    submitting.value = false
  }
}

const withdraw = (it) =>
  uni.showModal({
    title: '撤回报销',
    content: '确认撤回该报销申请？',
    success: async (m) => {
      if (!m.confirm) return
      await withdrawExpense(it.id)
      uni.showToast({ title: '已撤回', icon: 'success' })
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
