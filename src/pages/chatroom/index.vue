<template>
  <view class="room">
    <scroll-view class="msgs" scroll-y :scroll-into-view="anchor" scroll-with-animation>
      <view class="load-prev" v-if="hasMore" @click="loadPrev">加载更早的消息</view>
      <view v-for="m in messages" :key="m.clientMessageId || m.id" :id="'m' + (m.clientMessageId || m.id)" class="msg-line">
        <view class="time-split" v-if="m._showTime">{{ m._showTime }}</view>
        <view class="bubble-row" :class="{ mine: m._mine }">
          <view class="bubble" :class="{ mine: m._mine }">
            <text>{{ m._text }}</text>
          </view>
        </view>
      </view>
      <view id="bottom" style="height: 20rpx"></view>
    </scroll-view>

    <view class="input-bar">
      <input
        class="chat-input"
        v-model="draft"
        placeholder="输入消息…"
        :adjust-position="true"
        confirm-type="send"
        @confirm="doSend"
      />
      <view class="send-btn" :class="{ off: !draft.trim() || sending }" @click="doSend">发送</view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { getFriendList, getPrivateHistory, sendPrivateText, pullPrivateMessage } from '../../api'
import { getUser } from '../../utils/auth'

const messages = ref([])
const draft = ref('')
const sending = ref(false)
const hasMore = ref(false)
const anchor = ref('')
let peerId = 0
let selfId = 0
let oldestId = null
let lastMaxId = 0
let pollTimer = null

const toTime = (v) => {
  if (typeof v === 'number') return new Date(v)
  const s = String(v || '').replace('T', ' ')
  if (!s) return null
  // LocalDateTime 序列化可能带纳秒/空格分隔，交 Date 解析
  const d = new Date(s)
  return isNaN(d.getTime()) ? null : d
}
const parseText = (content, type) => {
  try {
    const c = JSON.parse(content)
    if (c.content) return c.content
    if (c.url) return '[图片]'
    return '[消息]'
  } catch {
    return type === 101 ? (content || '') : '[消息]'
  }
}
const fmtTime = (t) => {
  const d = toTime(t)
  if (!d) return ''
  const p = (n) => (n < 10 ? '0' + n : n)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${p(d.getHours())}:${p(d.getMinutes())}`
}

// 归一化：标记本人/文本/时间分隔（相邻 5 分钟以上显示时间）
const normalize = (list, append) => {
  const mapped = list.map((m) => ({
    ...m,
    _mine: m.senderId === selfId,
    _text: parseText(m.content, m.type)
  }))
  let prev = append && messages.value.length ? messages.value[messages.value.length - 1] : null
  for (const m of mapped) {
    const t = toTime(m.sendTime)?.getTime() || 0
    const pt = prev ? toTime(prev.sendTime)?.getTime() || 0 : 0
    if (!prev || Math.abs(t - pt) > 5 * 60 * 1000) m._showTime = fmtTime(m.sendTime)
    prev = m
  }
  return mapped
}

const scrollBottom = () => {
  nextTick(() => {
    const last = messages.value[messages.value.length - 1]
    anchor.value = ''
    setTimeout(() => (anchor.value = last ? 'm' + (last.clientMessageId || last.id) : 'bottom'), 40)
  })
}

const loadHistory = async () => {
  const list = await getPrivateHistory(peerId, undefined, 30).catch(() => [])
  messages.value = normalize(list, false)
  if (list.length) {
    oldestId = list[list.length - 1].id
    lastMaxId = Math.max(...list.map((m) => m.id))
    hasMore.value = list.length >= 30
  }
  scrollBottom()
}

const loadPrev = async () => {
  if (oldestId == null) return
  const list = await getPrivateHistory(peerId, oldestId, 30).catch(() => [])
  if (!list.length) {
    hasMore.value = false
    return
  }
  // list 接口按 id 倒序返回 maxId 之前的消息：反转为正序后前插
  const asc = list.slice().reverse()
  oldestId = asc[0].id
  const head = asc.map((m) => ({ ...m, _mine: m.senderId === selfId, _text: parseText(m.content, m.type) }))
  const firstOld = messages.value[0]
  if (firstOld && head.length) {
    const ot = toTime(firstOld.sendTime)?.getTime() || 0
    const ht = toTime(head[head.length - 1].sendTime)?.getTime() || 0
    if (!head[head.length - 1]._showTime && ot - ht > 5 * 60 * 1000)
      head[head.length - 1]._showTime = fmtTime(head[head.length - 1].sendTime)
  }
  messages.value = head.concat(messages.value)
  if (list.length < 30) hasMore.value = false
}

const doSend = async () => {
  const text = draft.value.trim()
  if (!text || sending.value) return
  sending.value = true
  try {
    const sent = await sendPrivateText(peerId, text)
    draft.value = ''
    if (sent) {
      messages.value.push({ ...sent, _mine: true, _text: parseText(sent.content, sent.type) })
      lastMaxId = Math.max(lastMaxId, sent.id || lastMaxId)
      scrollBottom()
    }
  } catch (e) {
    uni.showToast({ title: '发送失败', icon: 'none' })
  } finally {
    sending.value = false
  }
}

// 增量轮询（8 秒）
const poll = async () => {
  try {
    const list = await pullPrivateMessage(lastMaxId || 0, 100)
    const mine = (list || []).filter((m) => m.senderId === peerId || m.receiverId === peerId)
    const fresh = mine.filter((m) => m.id > lastMaxId)
    if (fresh.length) {
      messages.value = messages.value.concat(normalize(fresh, true))
      lastMaxId = Math.max(...fresh.map((m) => m.id))
      scrollBottom()
    }
  } catch {}
}

onLoad(async (options) => {
  peerId = Number(options.id)
  selfId = getUser()?.userId
  try {
    const list = await getFriendList()
    const f = (list || []).find((x) => x.friendUserId === peerId)
    if (f) uni.setNavigationBarTitle({ title: f.displayName || f.nickname || '对话' })
  } catch {}
  loadHistory()
})
onShow(() => {
  pollTimer = setInterval(poll, 8000)
})
onHide(() => clearInterval(pollTimer))
onUnload(() => clearInterval(pollTimer))
</script>

<style scoped>
.room {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f6f8;
}
.msgs {
  flex: 1;
  overflow: hidden;
  padding: 16rpx 0;
}
.load-prev {
  text-align: center;
  color: #909399;
  font-size: 24rpx;
  padding: 16rpx 0;
}
.time-split {
  text-align: center;
  color: #c0c4cc;
  font-size: 22rpx;
  margin: 20rpx 0;
}
.msg-line {
  padding: 6rpx 24rpx;
}
.bubble-row {
  display: flex;
}
.bubble-row.mine {
  justify-content: flex-end;
}
.bubble {
  max-width: 70%;
  background: #fff;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  line-height: 1.5;
  word-break: break-all;
}
.bubble.mine {
  background: #95ec69;
}
.input-bar {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 16rpx 20rpx;
  padding-bottom: calc(16rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #eee;
}
.chat-input {
  flex: 1;
  background: #f5f6f8;
  border-radius: 12rpx;
  height: 76rpx;
  line-height: 76rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}
.chat-input .uni-input-wrapper,
.chat-input .uni-input-input {
  height: 76rpx;
  font-size: 28rpx;
}
.send-btn {
  margin-left: 16rpx;
  background: #1ab394;
  color: #fff;
  border-radius: 12rpx;
  height: 76rpx;
  line-height: 76rpx;
  padding: 0 32rpx;
  font-size: 28rpx;
  flex-shrink: 0;
}
.send-btn.off {
  background: #c5c8ce;
}
</style>
