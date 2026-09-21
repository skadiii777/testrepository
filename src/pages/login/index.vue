<template>
  <view class="login">
    <view class="brand">
      <view class="logo">企</view>
      <view class="name">企业管理系统</view>
      <view class="sub">移动工作台</view>
    </view>

    <view class="form">
      <view class="row">
        <AppIcon name="user" :size="20" color="#9CA3AF" />
        <input
          class="ipt"
          v-model="form.username"
          placeholder="请输入用户名"
          placeholder-class="ph"
        />
      </view>
      <view class="row">
        <AppIcon name="shield" :size="20" color="#9CA3AF" />
        <input
          class="ipt"
          v-model="form.password"
          password
          placeholder="请输入密码"
          placeholder-class="ph"
          @confirm="doLogin"
        />
      </view>
    </view>

    <button class="btn-primary submit" :disabled="loading" @click="doLogin">
      {{ loading ? '登录中…' : '登 录' }}
    </button>

    <view class="tip">默认租户：企业平台</view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import AppIcon from '../../components/AppIcon.vue'
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
      userId: info.user?.id || data.userId,
      nickname: info.user?.nickname || form.username,
      avatar: info.user?.avatar || '',
      roles: info.roles || [],
      permissions: info.permissions || []
    })
    refreshDict()
    // 登录后落地页 = 消息（tabBar 首位）
    uni.reLaunch({ url: '/pages/message/index' })
  } catch (e) {
    const msg = e?.message || String(e)
    uni.showModal({ title: '登录失败', content: msg.includes('账号密码') ? '账号或密码错误' : msg, showCancel: false })
    clearAuth()
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login {
  min-height: 100vh;
  background: #ffffff;
  padding: 0 56rpx;
  box-sizing: border-box;
}

.brand {
  padding: 160rpx 0 80rpx;
  text-align: center;
}
.logo {
  width: 128rpx;
  height: 128rpx;
  line-height: 128rpx;
  border-radius: 36rpx;
  background: $c-primary;
  color: #ffffff;
  font-size: 60rpx;
  font-weight: 500;
  margin: 0 auto 32rpx;
}
.name {
  font-size: 42rpx;
  font-weight: 500;
  color: $c-text-1;
  letter-spacing: 1rpx;
}
.sub {
  font-size: 26rpx;
  color: $c-text-3;
  margin-top: 12rpx;
}

.form {
  margin-top: 24rpx;
}
.row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  height: 108rpx;
  border-bottom: 1rpx solid $c-divider;
}
.row:last-child {
  border-bottom: none;
}
.ipt {
  flex: 1;
  height: 108rpx;
  font-size: 30rpx;
  color: $c-text-1;
  background: transparent;
}
.ph {
  color: $c-text-4;
  font-size: 30rpx;
}

.submit {
  margin-top: 72rpx;
}

.tip {
  text-align: center;
  color: $c-text-4;
  font-size: 22rpx;
  margin-top: 32rpx;
}
</style>
