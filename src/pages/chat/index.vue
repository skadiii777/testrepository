<template>
  <view>
    <view class="empty" v-if="!loading && !friends.length">暂无同事好友，可在电脑端添加后同步。</view>
    <view class="conv" v-for="f in friends" :key="f.friendUserId" @click="open(f)">
      <view class="avatar">{{ (f.displayName || f.nickname || String(f.friendUserId))[0] }}</view>
      <view class="cinfo">
        <view class="cname">{{ f.displayName || f.nickname || '用户 ' + f.friendUserId }}</view>
        <view class="cmsg">{{ last[f.friendUserId] || '发起会话' }}</view>
      </view>
      <view class="ctime">{{ lastTime[f.friendUserId] || '' }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getFriendList, getPrivateHistory } from '../../api'
import { isLoggedIn } from '../../utils/auth'

const loading = ref(true)
const friends = ref([])
const last = ref({})
const lastTime = ref({})

const open = (f) =>
  // 只传 id：中文经 uni H5 路由会二次编码，标题在 chatroom 内按好友列表解析
  uni.navigateTo({ url: `/pages/chatroom/index?id=${f.friendUserId}` })

// sendTime 序列化兼容：epoch 毫秒数字/字符串 或 LocalDateTime 字符串
const toTime = (v) => {
  if (typeof v === 'number') return new Date(v)
  const s = String(v || '').replace('T', ' ')
  if (!s) return null
  if (/^\d+$/.test(s)) return new Date(Number(s))
  const d = new Date(s)
  return isNaN(d.getTime()) ? null : d
}

const load = async () => {
  loading.value = true
  try {
    const list = (await getFriendList()) || []
    friends.value = list.filter((f) => !f.blocked)
    // 会话摘要：取最近一条
    for (const f of friends.value.slice(0, 20)) {
      try {
        const msgs = await getPrivateHistory(f.friendUserId, undefined, 1)
        if (msgs && msgs.length) {
          let text = ''
          try { text = JSON.parse(msgs[0].content).content || '' } catch { text = '' }
          last.value[f.friendUserId] = text.slice(0, 20)
          const t = toTime(msgs[0].sendTime)
          if (t) {
            const p = (n) => (n < 10 ? '0' + n : n)
            lastTime.value[f.friendUserId] = `${p(t.getMonth() + 1)}-${p(t.getDate())} ${p(t.getHours())}:${p(t.getMinutes())}`
          }
        }
      } catch {}
    }
  } finally {
    loading.value = false
  }
}

onShow(() => {
  if (!isLoggedIn()) return uni.reLaunch({ url: '/pages/login/index' })
  load()
})
</script>

<style scoped>
.conv {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 24rpx 28rpx;
  margin-bottom: 2rpx;
}
.avatar {
  width: 88rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 20rpx;
  background: #1ab394;
  color: #fff;
  text-align: center;
  font-size: 36rpx;
  font-weight: 600;
  margin-right: 22rpx;
  flex-shrink: 0;
}
.cinfo {
  flex: 1;
  min-width: 0;
}
.cname {
  font-size: 30rpx;
  font-weight: 600;
}
.cmsg {
  font-size: 24rpx;
  color: #909399;
  margin-top: 6rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ctime {
  font-size: 22rpx;
  color: #c0c4cc;
  margin-left: 16rpx;
}
</style>
