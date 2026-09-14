<template>
  <view>
    <view class="bar">
      <text class="bar-title">站内消息</text>
      <text class="bar-op" v-if="unread" @click="markAll">全部已读</text>
    </view>

    <view class="empty" v-if="!loading && !list.length">暂无消息</view>
    <view class="msg" v-for="it in list" :key="it.id" :class="{ unread: !it.readStatus }" @click="open(it)">
      <view class="msg-head">
        <text class="msg-from">{{ it.templateNickname || '系统' }}</text>
        <text class="msg-time">{{ fmt(it.createTime) }}</text>
      </view>
      <view class="msg-body">{{ render(it) }}</view>
      <view class="msg-dot" v-if="!it.readStatus"></view>
    </view>
    <view class="load-more" v-if="list.length" @click="loadMore">{{ hasMore ? '加载更多…' : '— 已加载全部 —' }}</view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyMessagePage, readMessages, readAllMessages } from '../../api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = ref({ pageNo: 1, pageSize: 20 })

const unread = computed(() => list.value.some((m) => !m.readStatus))
const hasMore = computed(() => list.value.length < total.value)
const fmt = (t) => (typeof t === 'string' ? t.replace('T', ' ').slice(5, 16) : '')

/** 模板内容里的 {param} 用 templateParams 填充 */
const render = (it) => {
  let c = it.templateContent || ''
  const p = it.templateParams || {}
  for (const k of Object.keys(p)) c = c.split('{' + k + '}').join(String(p[k]))
  return c
}

const load = async (append) => {
  loading.value = true
  try {
    const page = await getMyMessagePage(queryParams.value)
    list.value = append ? list.value.concat(page.list || []) : page.list || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}
const loadMore = () => {
  if (!hasMore.value) return
  queryParams.value.pageNo++
  load(true)
}

const open = async (it) => {
  uni.showModal({ title: it.templateNickname || '消息', content: render(it), showCancel: false })
  if (!it.readStatus) {
    it.readStatus = true
    try {
      await readMessages([it.id])
    } catch {}
  }
}

const markAll = async () => {
  await readAllMessages()
  uni.showToast({ title: '已全部已读', icon: 'success' })
  queryParams.value.pageNo = 1
  load()
}

onShow(() => {
  queryParams.value.pageNo = 1
  load()
})
</script>

<style scoped>
.bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 24rpx 32rpx;
}
.bar-title {
  font-size: 30rpx;
  font-weight: 600;
}
.bar-op {
  font-size: 24rpx;
  color: #1ab394;
}
.msg {
  background: #fff;
  padding: 24rpx 32rpx;
  margin-bottom: 2rpx;
  position: relative;
}
.msg.unread {
  background: #fffdf5;
}
.msg-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}
.msg-from {
  font-size: 26rpx;
  font-weight: 600;
  color: #303133;
}
.msg-time {
  font-size: 22rpx;
  color: #c0c4cc;
}
.msg-body {
  font-size: 26rpx;
  color: #606266;
  line-height: 1.6;
}
.msg-dot {
  position: absolute;
  top: 32rpx;
  left: 16rpx;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #f56c6c;
}
.load-more {
  text-align: center;
  color: #c0c4cc;
  font-size: 24rpx;
  padding: 24rpx 0 40rpx;
}
</style>
