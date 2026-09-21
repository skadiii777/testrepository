<template>
  <view class="cell" :class="{ 'has-border': border, 'is-tap': tappable }" @click="onTap">
    <view v-if="icon" class="cell-icon" :style="{ background: iconBg }">
      <AppIcon :name="icon" :size="20" :color="iconFg" />
    </view>

    <view class="cell-body">
      <text class="cell-title">{{ title }}</text>
      <text v-if="desc" class="cell-desc">{{ desc }}</text>
    </view>

    <text v-if="value" class="cell-value">{{ value }}</text>
    <slot name="right" />
    <AppIcon v-if="arrow" name="forward" :size="15" color="#c4c9d0" />
  </view>
</template>

<script setup>
/**
 * 列表单元格（设置项 / 功能入口 / 详情字段行）
 *
 * 用法：
 *   <Cell title="我的请假" icon="calendar" icon-bg="#E1F5EE" icon-fg="#0F6E56" arrow />
 *   <Cell title="请假类型" value="年假" />
 */
import { computed } from 'vue'
import AppIcon from './AppIcon.vue'

const props = defineProps({
  title: { type: String, default: '' },
  desc: { type: String, default: '' },
  icon: { type: String, default: '' },
  iconBg: { type: String, default: '#F1F3F5' },
  iconFg: { type: String, default: '#5F5E5A' },
  value: { type: String, default: '' },
  arrow: { type: Boolean, default: false },
  border: { type: Boolean, default: true }
})

const emit = defineEmits(['click'])

const tappable = computed(() => props.arrow)

function onTap() {
  emit('click')
}
</script>

<style scoped>
.cell {
  display: flex;
  align-items: center;
  gap: 20rpx;
  min-height: 104rpx;
  padding: 0 28rpx;
  background: var(--c-card, #ffffff);
  position: relative;
}
.cell.has-border::after {
  content: '';
  position: absolute;
  left: 28rpx;
  right: 0;
  bottom: 0;
  height: 1rpx;
  background: #f1f3f5;
}
.cell.is-tap:active {
  background: #f7f8fa;
}
.cell-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cell-body {
  flex: 1;
  min-width: 0;
  padding: 22rpx 0;
}
.cell-title {
  font-size: 28rpx;
  color: #111827;
  display: block;
}
.cell-desc {
  font-size: 22rpx;
  color: #9ca3af;
  margin-top: 6rpx;
  display: block;
}
.cell-value {
  font-size: 26rpx;
  color: #6b7280;
  flex-shrink: 0;
}
</style>
