<template>
  <view>
    <view class="profile">
      <view class="avatar">{{ (user.nickname || '?')[0] }}</view>
      <view class="pinfo">
        <view class="pname">{{ user.nickname || '未登录' }}</view>
        <view class="psub">租户：企业平台</view>
      </view>
    </view>

    <view class="card menu">
      <view class="mi" @click="uni.navigateTo({ url: '/pages/leave/index' })">
        <text class="mi-label">我的请假</text><text class="mi-arrow">›</text>
      </view>
      <view class="mi" @click="uni.navigateTo({ url: '/pages/expense/index' })">
        <text class="mi-label">我的报销</text><text class="mi-arrow">›</text>
      </view>
      <view class="mi" @click="uni.navigateTo({ url: '/pages/correction/index' })">
        <text class="mi-label">我的补卡</text><text class="mi-arrow">›</text>
      </view>
      <view class="mi" @click="uni.navigateTo({ url: '/pages/report/index' })">
        <text class="mi-label">业务汇报</text><text class="mi-arrow">›</text>
      </view>
    </view>

    <view class="card menu">
      <view class="mi" @click="openPc">
        <text class="mi-label">电脑端完整版</text><text class="mi-arrow">›</text>
      </view>
      <view class="mi">
        <text class="mi-label">版本</text><text class="mi-val">v1.0.0 移动工作台</text>
      </view>
    </view>

    <view class="logout" @click="doLogout">退出登录</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUser, clearAuth } from '../../utils/auth'
import { logout } from '../../api'

const user = ref(getUser() || {})

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

<style scoped>
.profile {
  background: linear-gradient(135deg, #1ab394, #149c80);
  color: #fff;
  display: flex;
  align-items: center;
  padding: 60rpx 40rpx 48rpx;
}
.avatar {
  width: 120rpx;
  height: 120rpx;
  line-height: 120rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  text-align: center;
  font-size: 52rpx;
  font-weight: 700;
  margin-right: 28rpx;
}
.pname {
  font-size: 36rpx;
  font-weight: 700;
}
.psub {
  font-size: 24rpx;
  opacity: 0.85;
  margin-top: 8rpx;
}
.menu {
  padding: 8rpx 28rpx;
}
.mi {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f5f6f8;
  font-size: 28rpx;
}
.mi:last-child {
  border-bottom: none;
}
.mi-label {
  color: #303133;
}
.mi-val {
  color: #909399;
  font-size: 24rpx;
}
.mi-arrow {
  color: #c0c4cc;
  font-size: 34rpx;
}
.logout {
  margin: 40rpx 24rpx;
  background: #fff;
  color: #f56c6c;
  text-align: center;
  line-height: 92rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
}
</style>
