import { useCache, CACHE_KEY } from '@/hooks/web/useCache'
import { TokenType } from '@/api/login/types'
import { decrypt, encrypt } from '@/utils/jsencrypt'

const { wsCache } = useCache()
// 令牌使用 sessionStorage：关闭网页（标签页）后自动失效，重新进入必须回到登录界面
const { wsCache: sessionCache } = useCache('sessionStorage')

const AccessTokenKey = 'ACCESS_TOKEN'
const RefreshTokenKey = 'REFRESH_TOKEN'

// 获取token
export const getAccessToken = () => {
  return sessionCache.get(AccessTokenKey)
}

// 刷新token
export const getRefreshToken = () => {
  return sessionCache.get(RefreshTokenKey)
}

// 设置token
export const setToken = (token: TokenType) => {
  sessionCache.set(RefreshTokenKey, token.refreshToken)
  sessionCache.set(AccessTokenKey, token.accessToken)
}

// 删除token
export const removeToken = () => {
  sessionCache.delete(AccessTokenKey)
  sessionCache.delete(RefreshTokenKey)
  // 清理历史版本残留在 localStorage 的令牌，避免旧会话绕过"关页即失效"
  wsCache.delete(AccessTokenKey)
  wsCache.delete(RefreshTokenKey)
}

/** 格式化token（jwt格式） */
export const formatToken = (token: string): string => {
  return 'Bearer ' + token
}
// ========== 账号相关 ==========

/** 获取当前登录用户编号 */
export const getCurrentUserId = (): number => {
  const user = wsCache.get(CACHE_KEY.USER)?.user
  return Number(user?.id) || 0
}

export type LoginFormType = {
  tenantName: string
  username: string
  password: string
  rememberMe: boolean
}

export const getLoginForm = () => {
  const loginForm: LoginFormType = wsCache.get(CACHE_KEY.LoginForm)
  if (loginForm) {
    loginForm.password = decrypt(loginForm.password) as string
  }
  return loginForm
}

export const setLoginForm = (loginForm: LoginFormType) => {
  loginForm.password = encrypt(loginForm.password) as string
  wsCache.set(CACHE_KEY.LoginForm, loginForm, { exp: 30 * 24 * 60 * 60 })
}

export const removeLoginForm = () => {
  wsCache.delete(CACHE_KEY.LoginForm)
}

// ========== 租户相关 ==========

export const getTenantId = () => {
  return wsCache.get(CACHE_KEY.TenantId)
}

export const setTenantId = (tenantId: number) => {
  wsCache.set(CACHE_KEY.TenantId, tenantId)
}

export const getVisitTenantId = () => {
  return wsCache.get(CACHE_KEY.VisitTenantId)
}

export const setVisitTenantId = (visitTenantId: number) => {
  wsCache.set(CACHE_KEY.VisitTenantId, visitTenantId)
}
