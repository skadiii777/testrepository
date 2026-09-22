<script>
export default {
  onLaunch: function () {}
}
</script>

<style lang="scss">
/*
 * 全局基础样式。设计变量见 src/styles/tokens.scss（由 uni.scss 全局注入）。
 *
 * 约束：本文件中的 .card / .form-* / .btn-* / .tag / .empty / .row-* 类
 * 被现有页面（leave、expense、correction、report、approval、chatroom）依赖，
 * 只允许升级外观，不允许删除或改名。
 */

page {
  background: #f5f6f8;
  font-size: 28rpx;
  color: #111827;
  -webkit-font-smoothing: antialiased;
}

/* ---------- 页面容器 ---------- */
.page {
  min-height: 100vh;
  padding-bottom: 40rpx;
}

/* ---------- 卡片 ---------- */
.card {
  background: #ffffff;
  border-radius: 24rpx;
  margin: 24rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 8rpx rgba(17, 24, 39, 0.04);
}
.card-title {
  font-size: 30rpx;
  font-weight: 500;
  color: #111827;
  margin-bottom: 20rpx;
}

/* ---------- 分组列表（卡片内多行） ---------- */
.group {
  background: #ffffff;
  border-radius: 24rpx;
  margin: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(17, 24, 39, 0.04);
}
.group-title {
  font-size: 24rpx;
  color: #9ca3af;
  padding: 32rpx 28rpx 12rpx;
}

/* ---------- 行内字段（label 左 / 值右） ---------- */
.field {
  display: flex;
  align-items: flex-start;
  padding: 26rpx 28rpx;
  position: relative;
}
.field::after {
  content: '';
  position: absolute;
  left: 28rpx;
  right: 0;
  bottom: 0;
  height: 1rpx;
  background: #f1f3f5;
}
.field:last-child::after {
  display: none;
}
.field-label {
  width: 176rpx;
  flex-shrink: 0;
  font-size: 28rpx;
  color: #6b7280;
}
.field-value {
  flex: 1;
  font-size: 28rpx;
  color: #111827;
  min-width: 0;
}
.field-value.placeholder {
  color: #c4c9d0;
}

/* ---------- 表单（沿用原结构，仅升级外观） ---------- */
.form-item {
  margin-bottom: 28rpx;
}
.form-label {
  font-size: 26rpx;
  color: #6b7280;
  margin-bottom: 12rpx;
  display: block;
}
.form-input {
  background: #f1f3f5;
  border-radius: 16rpx;
  font-size: 28rpx;
  width: 100%;
  box-sizing: border-box;
  height: 88rpx;
  line-height: 88rpx;
}
/* uni-input 内层真实 input 需显式撑高，否则高度塌陷为 0 无法点击输入 */
.form-input .uni-input-wrapper {
  height: 88rpx;
  line-height: 88rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
}
.form-input .uni-input-input {
  height: 88rpx;
  font-size: 28rpx;
}
.form-input .uni-input-placeholder {
  line-height: 88rpx;
}
/* 多行文本域：uni-textarea 内层同样要撑满 */
.form-input.area {
  height: 180rpx;
  line-height: 1.6;
  padding: 20rpx 24rpx;
}
.form-input.area .uni-textarea-wrapper,
.form-input.area .uni-textarea-textarea {
  height: 100%;
  line-height: 1.6;
}
.picker-box {
  background: #f1f3f5;
  border-radius: 16rpx;
  padding: 0 24rpx;
  height: 88rpx;
  color: #c4c9d0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;
}
.picker-val {
  color: #111827;
}

/* ---------- 按钮 ---------- */
.btn-primary {
  background: #0e7a63;
  color: #ffffff;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 500;
  line-height: 92rpx;
  height: 92rpx;
  text-align: center;
  border: none;
}
.btn-primary::after {
  border: none;
}
.btn-primary:active {
  background: #0c6b57;
}
.btn-primary[disabled] {
  background: #9ed4c6;
  color: #ffffff;
}
.btn-mini {
  border: 1rpx solid #0e7a63;
  color: #0e7a63;
  border-radius: 999rpx;
  font-size: 24rpx;
  padding: 6rpx 26rpx;
  display: inline-block;
  line-height: 1.5;
}
.btn-mini-gray {
  border-color: #e5e7eb;
  color: #6b7280;
}
.btn-mini:active {
  background: #f2fbf8;
}

/* ---------- 状态标签 ---------- */
.tag {
  font-size: 22rpx;
  padding: 5rpx 16rpx;
  border-radius: 8rpx;
  color: #ffffff;
  flex-shrink: 0;
  line-height: 1.4;
}

/* ---------- 徽标 ---------- */
.badge {
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  border-radius: 16rpx;
  background: #e24b4a;
  color: #ffffff;
  font-size: 20rpx;
  line-height: 32rpx;
  text-align: center;
}

/* ---------- 列表行（旧页面沿用） ---------- */
.row-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.row-title {
  font-size: 29rpx;
  font-weight: 500;
  flex: 1;
  margin-right: 16rpx;
}
.row-sub {
  color: #6b7280;
  font-size: 24rpx;
  margin-top: 10rpx;
  line-height: 1.6;
}
.row-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f1f3f5;
}
.row-time {
  color: #9ca3af;
  font-size: 22rpx;
}

/* ---------- 空态（旧页面沿用 .empty，新页面用 EmptyState 组件） ---------- */
.empty {
  text-align: center;
  color: #9ca3af;
  padding: 120rpx 0;
  font-size: 26rpx;
}

/* ---------- 骨架屏 ---------- */
.skeleton {
  background: linear-gradient(90deg, #f1f3f5 25%, #e9ecef 37%, #f1f3f5 63%);
  background-size: 400% 100%;
  animation: sk 1.4s ease infinite;
  border-radius: 12rpx;
}
@keyframes sk {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}

/* ---------- 动画体系（进入 / 微交互，克制使用） ---------- */
@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes scale-in {
  from {
    opacity: 0;
    transform: scale(0.94) translateY(8rpx);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
@keyframes ring-breathe {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.4);
  }
  70% {
    box-shadow: 0 0 0 24rpx rgba(255, 255, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0);
  }
}
@keyframes pulse-soft {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.06);
    opacity: 0.8;
  }
}

/* 页面根容器进入 */
.page-enter {
  animation: fade-in-up 0.3s cubic-bezier(0.33, 1, 0.68, 1) both;
}
/* 卡片 / 区块进入 */
.card-enter {
  animation: fade-in-up 0.34s cubic-bezier(0.33, 1, 0.68, 1) both;
}
/* 错落进入：配 --d（ms）延迟。
   不用基础 opacity:0 + both —— tabBar 页面 keep-alive 切回时动画不重放，
   基础样式 opacity:0 会让列表永久不可见（生产实测复现）。
   backwards 在 delay 期间填充 from 态，动画结束后回到自然态（可见）。 */
.stagger {
  animation: fade-in-up 0.32s cubic-bezier(0.33, 1, 0.68, 1) backwards;
  animation-delay: var(--d, 0ms);
}
/* 按钮点击态 */
.tap-scale {
  transition: transform 0.15s ease;
}
.tap-scale:active {
  transform: scale(0.96);
}
/* 呼吸光环（打卡 / 重点 CTA） */
.ring-breathe {
  animation: ring-breathe 2s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}
/* 柔和脉冲（徽标 / 预警） */
.pulse-soft {
  animation: pulse-soft 1.6s ease-in-out infinite;
}

/* ---------- 通用工具 ---------- */
.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.safe-bottom {
  height: env(safe-area-inset-bottom);
}
</style>
