<template>
  <view class="empty-state">
    <view class="es-icon">
      <AppIcon :name="icon" :size="32" color="#c4c9d0" />
    </view>
    <text class="es-text">{{ text }}</text>
    <text v-if="hint" class="es-hint">{{ hint }}</text>
    <view v-if="actionText" class="es-action" @click="$emit('action')">{{ actionText }}</view>
  </view>
</template>

<script setup>
/**
 * 空态：图形 + 主文案 + 可选引导行动
 *
 * 用法：<EmptyState text="暂无待审批单据" hint="有新的申请会出现在这里" />
 */
import AppIcon from './AppIcon.vue'

defineProps({
  icon: { type: String, default: 'info' },
  text: { type: String, default: '暂无数据' },
  hint: { type: String, default: '' },
  actionText: { type: String, default: '' }
})

defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 60rpx 100rpx;
}
.es-icon {
  width: 132rpx;
  height: 132rpx;
  border-radius: 50%;
  background: #f1f3f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
}
.es-text {
  font-size: 28rpx;
  color: #6b7280;
}
.es-hint {
  font-size: 24rpx;
  color: #c4c9d0;
  margin-top: 12rpx;
  text-align: center;
  line-height: 1.6;
}
.es-action {
  margin-top: 36rpx;
  padding: 0 44rpx;
  height: 68rpx;
  line-height: 68rpx;
  border-radius: 999rpx;
  border: 1rpx solid #0e7a63;
  color: #0e7a63;
  font-size: 26rpx;
}
.es-action:active {
  background: #f2fbf8;
}
</style>
