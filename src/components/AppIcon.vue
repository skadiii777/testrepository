<template>
  <svg
    class="app-icon"
    :style="{ width: px, height: px }"
    viewBox="0 0 24 24"
    fill="none"
    :stroke="color"
    :stroke-width="weight"
    stroke-linecap="round"
    stroke-linejoin="round"
    v-html="inner"
  />
</template>

<script setup>
/**
 * 统一图标组件（内联 SVG，线性风格，24x24 网格）
 *
 * 用法：<AppIcon name="calendar" :size="20" color="#0F6E56" />
 *
 * 注意：内联 <svg> 仅 H5 有效。若后续要出微信小程序，
 * 需改走 image + data-uri 或字体图标（小程序不支持内联 svg 标签）。
 */
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 20 },
  color: { type: String, default: '#6B7280' },
  weight: { type: [Number, String], default: 1.7 }
})

const ICONS = {
  // —— 底部导航 ——
  chat: '<path d="M4 5h16v11H9l-4 3v-3H4z"/>',
  work: '<rect x="3.5" y="3.5" width="7" height="7" rx="2"/><rect x="13.5" y="3.5" width="7" height="7" rx="2"/><rect x="3.5" y="13.5" width="7" height="7" rx="2"/><rect x="13.5" y="13.5" width="7" height="7" rx="2"/>',
  check: '<path d="M5 13l4 4L19 7"/>',
  user: '<circle cx="12" cy="8" r="3.5"/><path d="M5 20c0-3.5 3-5.5 7-5.5s7 2 7 5.5"/>',

  // —— 业务 ——
  calendar: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18M8 3v4M16 3v4"/>',
  bill: '<path d="M6 3h12v18l-3-2-3 2-3-2-3 2z"/><path d="M9.5 8.5h5M9.5 12.5h5"/>',
  clock: '<circle cx="12" cy="12" r="8"/><path d="M12 8v4.5l3 1.8"/>',
  doc: '<path d="M6 3h8l4 4v14H6z"/><path d="M14 3v4h4M9 13h6M9 17h4"/>',
  bell: '<path d="M6 16V10a6 6 0 1112 0v6l1.5 2.5h-15z"/><path d="M10 21h4"/>',
  megaphone: '<path d="M3 4h18v12H8l-5 4z"/><path d="M7.5 8h8M7.5 12h5"/>',

  // —— 交互 ——
  search: '<circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  back: '<path d="M15 5l-7 7 7 7"/>',
  forward: '<path d="M9 5l7 7-7 7"/>',
  close: '<path d="M6 6l12 12M18 6L6 18"/>',
  more: '<circle cx="5" cy="12" r="1.6"/><circle cx="12" cy="12" r="1.6"/><circle cx="19" cy="12" r="1.6"/>',
  refresh: '<path d="M20 11a8 8 0 10-2.2 5.4"/><path d="M20 5v6h-6"/>',
  filter: '<path d="M4 6h16M7 12h10M10 18h4"/>',

  // —— 我的 / 设置 ——
  logout: '<path d="M15 4h3a2 2 0 012 2v12a2 2 0 01-2 2h-3"/><path d="M10 8l-4 4 4 4M6 12h10"/>',
  shield: '<path d="M12 3l7 3v6c0 4.2-3 7.2-7 9-4-1.8-7-4.8-7-9V6z"/><path d="M9 12l2 2 4-4"/>',
  info: '<circle cx="12" cy="12" r="8.5"/><path d="M12 16.5v-5M12 8v.6"/>',
  monitor: '<rect x="3" y="4" width="18" height="12.5" rx="2"/><path d="M8.5 20h7M12 16.5V20"/>',
  phone: '<rect x="6" y="2.5" width="12" height="19" rx="2.5"/><path d="M10.5 18.5h3"/>',

  // —— 打卡 / 状态 ——
  location: '<path d="M12 21.5s7-6.5 7-11.2A7 7 0 105 10.3c0 4.7 7 11.2 7 11.2z"/><circle cx="12" cy="10" r="2.5"/>',
  success: '<circle cx="12" cy="12" r="8.5"/><path d="M8.2 12.4l2.6 2.6 5-5.2"/>',
  warn: '<path d="M12 4l8.5 15h-17z"/><path d="M12 10v4M12 16.6v.4"/>',
  trend: '<path d="M4 16.5l5-5 3.5 3.5L20 8"/><path d="M15 8h5v5"/>'
}

const inner = computed(() => ICONS[props.name] || '')
const px = computed(() => (typeof props.size === 'number' ? `${props.size}px` : props.size))
</script>

<style scoped>
.app-icon {
  display: block;
  flex-shrink: 0;
}
</style>
