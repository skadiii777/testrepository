<template>
  <view class="avatar" :class="{ 'is-square': square }" :style="boxStyle">
    <image v-if="src" class="avatar-img" :src="src" mode="aspectFill" />
    <text v-else class="avatar-text" :style="{ fontSize: fontSize }">{{ initial }}</text>
    <view v-if="online" class="online-dot" />
  </view>
</template>

<script setup>
/**
 * 首字头像：按名称稳定取色（同一人每次颜色一致），可选方形/圆形与在线点。
 *
 * 用法：
 *   <Avatar name="李静" :size="84" square />
 *   <Avatar name="产品研发群" :size="84" square online />
 */
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, default: '' },
  src: { type: String, default: '' },
  size: { type: Number, default: 84 },
  square: { type: Boolean, default: false },
  online: { type: Boolean, default: false }
})

const PALETTE = ['#0E7A63', '#378ADD', '#534AB7', '#D85A30', '#1D9E75', '#993556', '#854F0B']

const seed = computed(() => {
  const s = props.name || '?'
  let h = 0
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) % 100000
  return h
})

const bg = computed(() => PALETTE[seed.value % PALETTE.length])

const initial = computed(() => {
  const s = (props.name || '?').trim()
  return s ? s[0].toUpperCase() : '?'
})

const fontSize = computed(() => `${Math.round(props.size * 0.4)}rpx`)

const boxStyle = computed(() => ({
  width: `${props.size}rpx`,
  height: `${props.size}rpx`,
  background: bg.value,
  borderRadius: props.square ? `${Math.round(props.size * 0.28)}rpx` : '50%'
}))
</script>

<style scoped>
.avatar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: visible;
}
.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: inherit;
}
.avatar-text {
  color: #ffffff;
  font-weight: 500;
  line-height: 1;
}
.online-dot {
  position: absolute;
  right: -2rpx;
  bottom: -2rpx;
  width: 22rpx;
  height: 22rpx;
  border-radius: 50%;
  background: #1d9e75;
  border: 4rpx solid #ffffff;
}
</style>
