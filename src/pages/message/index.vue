<template>
  <view class="page">
    <view class="topbar">
      <view class="search">
        <AppIcon name="search" :size="16" color="#9CA3AF" />
        <input
          class="search-ipt"
          v-model="keyword"
          :placeholder="tab === 'chat' ? '搜索同事' : '搜索通知内容'"
          placeholder-class="ph"
        />
        <view v-if="keyword" class="search-clear" @click="keyword = ''">
          <AppIcon name="close" :size="13" color="#C4C9D0" />
        </view>
      </view>
      <view v-if="tab === 'notice' && hasUnread" class="read-all" @click="markAll">全部已读</view>
    </view>

    <SegmentTabs v-model="tab" :tabs="segTabs" @change="onTabChange" />

    <!-- ---------- 聊天 ---------- -->
    <block v-if="tab === 'chat'">
      <EmptyState
        v-if="!chatLoading && !filteredFriends.length"
        icon="chat"
        :text="keyword ? '没有匹配的同事' : '暂无同事好友'"
        :hint="keyword ? '换个关键词试试' : '可在电脑端添加好友后同步'"
      />
      <view
        v-for="f in filteredFriends"
        :key="f.friendUserId"
        class="conv"
        @click="openChat(f)"
      >
        <Avatar :name="f.displayName || f.nickname || ''" :size="88" square />
        <view class="conv-body">
          <view class="conv-name">{{ f.displayName || f.nickname || '用户 ' + f.friendUserId }}</view>
          <view class="conv-msg">{{ last[f.friendUserId] || '发起会话' }}</view>
        </view>
        <view class="conv-time">{{ lastTime[f.friendUserId] || '' }}</view>
      </view>
    </block>

    <!-- ---------- 通知 ---------- -->
    <block v-else>
      <EmptyState
        v-if="!noticeLoading && !filteredNotices.length"
        icon="bell"
        :text="keyword ? '没有匹配的通知' : '暂无通知'"
        :hint="keyword ? '换个关键词试试' : '审批、提醒等消息会出现在这里'"
      />
      <view
        v-for="it in filteredNotices"
        :key="it.id"
        class="notice"
        :class="{ unread: !it.readStatus }"
        @click="openNotice(it)"
      >
        <view class="notice-head">
          <text class="notice-from">{{ it.templateNickname || '系统' }}</text>
          <text class="notice-time">{{ fmt(it.createTime) }}</text>
        </view>
        <view class="notice-body">{{ render(it) }}</view>
        <view v-if="!it.readStatus" class="notice-dot" />
      </view>
      <view v-if="hasMore" class="load-more" @click="loadMore">加载更多…</view>
    </block>

    <view class="safe-bottom" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import AppIcon from '../../components/AppIcon.vue'
import Avatar from '../../components/Avatar.vue'
import SegmentTabs from '../../components/SegmentTabs.vue'
import EmptyState from '../../components/EmptyState.vue'
import { getFriendList, getPrivateHistory, getMyMessagePage, readMessages, readAllMessages } from '../../api'
import { isLoggedIn } from '../../utils/auth'

const tab = ref('chat')
const keyword = ref('')

const friends = ref([])
const chatLoading = ref(true)
const last = ref({})
const lastTime = ref({})

const notices = ref([])
const noticeLoading = ref(false)
const total = ref(0)
const queryParams = ref({ pageNo: 1, pageSize: 20 })

const hasUnread = computed(() => notices.value.some((m) => !m.readStatus))
const hasMore = computed(() => notices.value.length < total.value)

const segTabs = computed(() => [
  { key: 'chat', label: '聊天' },
  { key: 'notice', label: '通知', count: unreadCount() }
])

function unreadCount() {
  return notices.value.filter((m) => !m.readStatus).length
}

const fmt = (t) => (typeof t === 'string' ? t.replace('T', ' ').slice(5, 16) : '')

const match = (text) => !keyword.value || String(text || '').toLowerCase().includes(keyword.value.toLowerCase())

const filteredFriends = computed(() =>
  friends.value.filter((f) => match(f.displayName || f.nickname))
)

const filteredNotices = computed(() =>
  notices.value.filter((it) => match(render(it)) || match(it.templateNickname))
)

/** 模板内容里的 {param} 用 templateParams 填充 */
const render = (it) => {
  let c = it.templateContent || ''
  const p = it.templateParams || {}
  for (const k of Object.keys(p)) c = c.split('{' + k + '}').join(String(p[k]))
  return c
}

// sendTime 序列化兼容：epoch 毫秒数字/字符串 或 LocalDateTime 字符串
const toTime = (v) => {
  if (typeof v === 'number') return new Date(v)
  const s = String(v || '').replace('T', ' ')
  if (!s) return null
  if (/^\d+$/.test(s)) return new Date(Number(s))
  const d = new Date(s)
  return isNaN(d.getTime()) ? null : d
}

const relTime = (t) => {
  if (!t) return ''
  const p = (n) => (n < 10 ? '0' + n : n)
  const now = new Date()
  const sameDay =
    t.getFullYear() === now.getFullYear() && t.getMonth() === now.getMonth() && t.getDate() === now.getDate()
  if (sameDay) return `${p(t.getHours())}:${p(t.getMinutes())}`
  const yesterday = new Date(now.getTime() - 86400000)
  const isYesterday =
    t.getFullYear() === yesterday.getFullYear() &&
    t.getMonth() === yesterday.getMonth() &&
    t.getDate() === yesterday.getDate()
  if (isYesterday) return '昨天'
  return `${p(t.getMonth() + 1)}-${p(t.getDate())}`
}

const loadChat = async () => {
  chatLoading.value = true
  try {
    const list = (await getFriendList()) || []
    friends.value = list.filter((f) => !f.blocked)
    // 会话摘要：逐个好友取最近一条（限制前 20，避免请求发散）
    for (const f of friends.value.slice(0, 20)) {
      try {
        const msgs = await getPrivateHistory(f.friendUserId, undefined, 1)
        if (msgs && msgs.length) {
          let text = ''
          try {
            text = JSON.parse(msgs[0].content).content || ''
          } catch {
            text = ''
          }
          last.value[f.friendUserId] = text
          const t = toTime(msgs[0].sendTime)
          if (t) lastTime.value[f.friendUserId] = relTime(t)
        }
      } catch {}
    }
  } finally {
    chatLoading.value = false
  }
}

const loadNotices = async (append) => {
  noticeLoading.value = true
  try {
    const page = await getMyMessagePage(queryParams.value)
    notices.value = append ? notices.value.concat(page.list || []) : page.list || []
    total.value = page.total || 0
  } finally {
    noticeLoading.value = false
  }
}

const loadMore = () => {
  if (!hasMore.value) return
  queryParams.value.pageNo++
  loadNotices(true)
}

const onTabChange = () => {
  keyword.value = ''
  if (tab.value === 'notice' && !notices.value.length) {
    queryParams.value.pageNo = 1
    loadNotices()
  }
}

const openChat = (f) => {
  // 只传 id：中文经 uni H5 路由会二次编码，标题在 chatroom 内按好友列表解析
  uni.navigateTo({ url: `/pages/chatroom/index?id=${f.friendUserId}` })
}

const openNotice = async (it) => {
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
  loadNotices()
}

onShow(() => {
  if (!isLoggedIn()) return uni.reLaunch({ url: '/pages/login/index' })
  if (tab.value === 'chat') loadChat()
  else loadNotices()
})
</script>

<style lang="scss" scoped>
.topbar {
  background: #ffffff;
  padding: 20rpx 28rpx 0;
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.search {
  flex: 1;
  height: 68rpx;
  border-radius: 20rpx;
  background: #f1f3f5;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 0 22rpx;
}
.search-ipt {
  flex: 1;
  height: 68rpx;
  font-size: 26rpx;
  color: $c-text-1;
  background: transparent;
}
.ph {
  color: $c-text-3;
  font-size: 26rpx;
}
.read-all {
  font-size: 26rpx;
  color: $c-primary;
  flex-shrink: 0;
}

/* ---------- 会话 ---------- */
.conv {
  display: flex;
  align-items: center;
  gap: 22rpx;
  padding: 24rpx 28rpx;
  background: #ffffff;
  position: relative;
}
.conv:active {
  background: #f7f8fa;
}
.conv::after {
  content: '';
  position: absolute;
  left: 130rpx;
  right: 0;
  bottom: 0;
  height: 1rpx;
  background: $c-divider;
}
.conv-body {
  flex: 1;
  min-width: 0;
}
.conv-name {
  font-size: 30rpx;
  font-weight: 500;
  color: $c-text-1;
}
.conv-msg {
  font-size: 24rpx;
  color: $c-text-3;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conv-time {
  font-size: 22rpx;
  color: $c-text-4;
  flex-shrink: 0;
  align-self: flex-start;
  margin-top: 6rpx;
}

/* ---------- 通知 ---------- */
.notice {
  background: #ffffff;
  padding: 26rpx 28rpx;
  position: relative;
}
.notice.unread {
  background: #f2fbf8;
}
.notice-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.notice-from {
  font-size: 27rpx;
  font-weight: 500;
  color: $c-text-1;
}
.notice-time {
  font-size: 22rpx;
  color: $c-text-4;
}
.notice-body {
  font-size: 26rpx;
  color: $c-text-2;
  line-height: 1.6;
  margin-top: 10rpx;
}
.notice-dot {
  position: absolute;
  left: 12rpx;
  top: 36rpx;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: $c-danger;
}

.load-more {
  text-align: center;
  color: $c-text-4;
  font-size: 24rpx;
  padding: 28rpx 0 40rpx;
}
</style>
