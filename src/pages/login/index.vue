<template>
  <view class="login-page">
    <view class="brand">
      <view class="brand-logo">企</view>
      <view class="brand-name">企业管理系统</view>
      <view class="brand-sub">移动工作台</view>
    </view>

    <view class="login-card">
      <view class="form-item">
        <text class="form-label">用户名</text>
        <input class="form-input" v-model="form.username" placeholder="请输入用户名" />
      </view>
      <view class="form-item">
        <text class="form-label">密码</text>
        <input class="form-input" v-model="form.password" password placeholder="请输入密码" @confirm="doLogin" />
      </view>
      <button class="btn-primary login-btn" :disabled="loading" @click="doLogin">
        {{ loading ? '登录中…' : '登 录' }}
      </button>
      <view class="tip">默认租户：企业平台</view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { login, getPermissionInfo } from '../../api'
import { setAuth, setUser, clearAuth } from '../../utils/auth'
import { refreshDict } from '../../utils/dict'

const loading = ref(false)
const form = reactive({ username: '', password: '' })

const doLogin = async () => {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const data = await login({ username: form.username, password: form.password })
    setAuth({
      userId: data.userId,
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      expireTime: typeof data.expiresTime === 'number' ? data.expiresTime : Date.now() + 7200_000
    })
    const info = await getPermissionInfo()
    setUser({
      nickname: info.user?.nickname || form.username,
      avatar: info.user?.avatar || '',
      roles: info.roles || [],
      permissions: info.permissions || []
    })
    refreshDict()
    uni.reLaunch({ url: '/pages/index/index' })
  } catch (e) {
    const msg = e?.message || String(e)
    uni.showModal({ title: '登录失败', content: msg.includes('账号密码') ? '账号或密码错误' : msg, showCancel: false })
    clearAuth()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #1ab394 0%, #16987e 45%, #f5f6f8 45.1%);
  padding: 0 40rpx;
}
.brand {
  padding: 120rpx 0 60rpx;
  text-align: center;
  color: #fff;
}
.brand-logo {
  width: 120rpx;
  height: 120rpx;
  line-height: 120rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.2);
  font-size: 60rpx;
  font-weight: 700;
  margin: 0 auto 24rpx;
}
.brand-name {
  font-size: 40rpx;
  font-weight: 700;
}
.brand-sub {
  font-size: 24rpx;
  opacity: 0.85;
  margin-top: 8rpx;
}
.login-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 40rpx;
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.08);
}
.login-btn {
  margin-top: 16rpx;
}
.tip {
  text-align: center;
  color: #c0c4cc;
  font-size: 22rpx;
  margin-top: 24rpx;
}
</style>
