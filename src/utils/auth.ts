import { ref } from 'vue'

export interface AuthInfo {
  userId: number
  accessToken: string
  refreshToken: string
  expireTime: number
}

const AUTH_KEY = 'APP_AUTH'
const TENANT_ID = '1' // 企业平台（生产已停用其他租户）
const USER_KEY = 'APP_USER'

export const getTenantId = () => TENANT_ID

export const getAuth = (): AuthInfo | null => {
  try {
    return JSON.parse(uni.getStorageSync(AUTH_KEY) || 'null')
  } catch {
    return null
  }
}

export const setAuth = (info: AuthInfo) => {
  uni.setStorageSync(AUTH_KEY, JSON.stringify(info))
}

export const getAccessToken = () => getAuth()?.accessToken || ''

// uni.setStorageSync 对对象直接可存，但为跨端一致统一走 JSON
export const getUser = (): any => {
  try {
    return JSON.parse(uni.getStorageSync(USER_KEY) || 'null')
  } catch {
    return null
  }
}

export const setUser = (u: any) => uni.setStorageSync(USER_KEY, JSON.stringify(u))

export const clearAuth = () => {
  uni.removeStorageSync(AUTH_KEY)
  uni.removeStorageSync(USER_KEY)
}

export const isLoggedIn = () => !!getAccessToken()

/** 登录失效时跳转登录页（防重复跳转） */
const navigating = ref(false)
export const redirectToLogin = () => {
  if (navigating.value) return
  navigating.value = true
  clearAuth()
  uni.reLaunch({ url: '/pages/login/index' })
  setTimeout(() => (navigating.value = false), 1500)
}
