<template>
  <view class="page">
    <view class="header">
      <Avatar :name="user.nickname || ''" :size="124" />
      <view class="hinfo">
        <view class="hname">{{ user.nickname || '未登录' }}</view>
        <view class="hsub">企业平台 · 移动工作台</view>
      </view>
    </view>

    <view class="group-title">我的申请</view>
    <view class="group card-enter">
      <Cell
        v-for="(m, i) in menus"
        :key="m.title"
        :title="m.title"
        :icon="m.icon"
        :icon-bg="m.bg"
        :icon-fg="m.fg"
        :border="i < menus.length - 1"
        arrow
        @click="uni.navigateTo({ url: m.url })"
      />
    </view>

    <view class="group-title">其他</view>
    <view class="group card-enter" style="animation-delay: 0.08s">
      <Cell
        title="电脑端完整版"
        desc="功能更全面，适合批量操作"
        icon="monitor"
        icon-bg="#F1EFE8"
        icon-fg="#5F5E5A"
        arrow
        @click="openPc"
      />
      <Cell
        title="版本"
        icon="info"
        icon-bg="#F1EFE8"
        icon-fg="#5F5E5A"
        value="v1.0.0 移动工作台"
        :border="false"
      />
    </view>

    <view class="logout" @click="doLogout">退出登录</view>
    <view class="safe-bottom" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import Avatar from '../../components/Avatar.vue'
import Cell from '../../components/Cell.vue'
import { getUser, clearAuth } from '../../utils/auth'
import { logout } from '../../api'

const user = ref(getUser() || {})

const menus = [
  { title: '我的请假', icon: 'calendar', bg: '#E1F5EE', fg: '#0F6E56', url: '/pages/leave/index' },
  { title: '我的报销', icon: 'bill', bg: '#E6F1FB', fg: '#185FA5', url: '/pages/expense/index' },
  { title: '我的补卡', icon: 'clock', bg: '#FAEEDA', fg: '#854F0B', url: '/pages/correction/index' },
  { title: '业务汇报', icon: 'doc', bg: '#EEEDFE', fg: '#534AB7', url: '/pages/report/index' }
]

const openPc = () => {
  // #ifdef H5
  window.open(location.origin + '/', '_blank')
  // #endif
  // #ifndef H5
  uni.setClipboardData({ data: 'http://8.155.128.225' })
  // #endif
}

const doLogout = () =>
  uni.showModal({
    title: '退出登录',
    content: '确认退出当前账号？',
    success: async (m) => {
      if (!m.confirm) return
      try {
        await logout()
      } catch {}
      clearAuth()
      uni.reLaunch({ url: '/pages/login/index' })
    }
  })

onShow(() => (user.value = getUser() || {}))
</script>

<style lang="scss" scoped>
.header {
  background: $c-primary;
  padding: 56rpx 36rpx 48rpx;
  display: flex;
  align-items: center;
  gap: 26rpx;
}
.hname {
  font-size: 38rpx;
  font-weight: 500;
  color: #ffffff;
}
.hsub {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.78);
  margin-top: 10rpx;
}

.group-title {
  font-size: 24rpx;
  color: $c-text-3;
  padding: 32rpx 40rpx 12rpx;
}

.logout {
  margin: 32rpx 28rpx 0;
  background: #ffffff;
  color: $c-danger;
  text-align: center;
  line-height: 100rpx;
  border-radius: 24rpx;
  font-size: 30rpx;
  box-shadow: $shadow-card;
}
.logout:active {
  background: #fdf5f5;
}
</style>
