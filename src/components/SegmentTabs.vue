<template>
  <view class="seg">
    <view
      v-for="t in tabs"
      :key="t.key"
      class="seg-item"
      :class="{ on: t.key === modelValue }"
      @click="pick(t.key)"
    >
      <text class="seg-label">{{ t.label }}</text>
      <text v-if="t.count" class="seg-count">{{ t.count > 99 ? '99+' : t.count }}</text>
    </view>
  </view>
</template>

<script setup>
/**
 * 分段控件（底部指示条，参考钉钉消息页）
 *
 * 用法：
 *   <SegmentTabs v-model="tab" :tabs="[{key:'chat',label:'聊天',count:2},{key:'notice',label:'通知'}]" />
 */
const props = defineProps({
  tabs: { type: Array, default: () => [] },
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'change'])

function pick(key) {
  if (key === props.modelValue) return
  emit('update:modelValue', key)
  emit('change', key)
}
</script>

<style scoped>
.seg {
  display: flex;
  align-items: center;
  gap: 44rpx;
  padding: 0 28rpx;
  background: #ffffff;
}
.seg-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 22rpx 0 20rpx;
}
.seg-label {
  font-size: 30rpx;
  color: #9ca3af;
  transition: color 0.15s;
}
.seg-item.on .seg-label {
  color: #111827;
  font-weight: 500;
}
.seg-count {
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  padding: 0 8rpx;
  border-radius: 16rpx;
  background: #f1f3f5;
  color: #6b7280;
  font-size: 20rpx;
  text-align: center;
}
.seg-item.on .seg-count {
  background: #fcebeb;
  color: #e24b4a;
}
.seg-item.on::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 48rpx;
  height: 5rpx;
  border-radius: 3rpx;
  background: #0e7a63;
}
</style>
