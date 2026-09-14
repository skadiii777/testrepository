import { getAccessToken, getTenantId, redirectToLogin } from './auth'

// H5 同源部署走相对路径 /admin-api；本地 dev 直连生产
export const BASE_URL = import.meta.env.VITE_API_BASE || '/admin-api'

export interface ApiResult<T = any> {
  code: number
  data: T
  msg: string
}

interface RequestOptions {
  /** 无需 token 的接口（登录等） */
  auth?: boolean
  /** 静默失败：不弹 toast，由调用方自行处理 */
  silent?: boolean
}

const toast = (msg: string) => uni.showToast({ title: msg, icon: 'none' })

/** 通用请求：自动带 tenant-id / Bearer token，401 统一跳登录 */
export const request = <T = any>(
  method: 'GET' | 'POST' | 'PUT' | 'DELETE',
  url: string,
  data?: any,
  options: RequestOptions = {}
): Promise<T> => {
  const header: Record<string, string> = {
    'tenant-id': getTenantId(),
    'Content-Type': 'application/json'
  }
  const token = getAccessToken()
  if (token) header.Authorization = 'Bearer ' + token

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header,
      timeout: 15000,
      success: (res) => {
        const body = res.data as ApiResult<T>
        if (!body || typeof body.code === 'undefined') {
          if (!options.silent) toast('服务异常，请稍后重试')
          reject(new Error('bad response'))
          return
        }
        if (body.code === 401) {
          redirectToLogin()
          reject(new Error(body.msg || '登录已失效'))
          return
        }
        if (body.code !== 0) {
          if (!options.silent) toast(body.msg || '请求失败')
          reject(new Error(body.msg || '请求失败'))
          return
        }
        resolve(body.data)
      },
      fail: (err) => {
        if (!options.silent) toast('网络异常，请检查网络')
        reject(err)
      }
    })
  })
}

export const get = <T = any>(url: string, params?: any, options?: RequestOptions) => {
  if (params) {
    const qs = Object.entries(params)
      .filter(([, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
      .join('&')
    if (qs) url += (url.includes('?') ? '&' : '?') + qs
  }
  return request<T>('GET', url, undefined, options)
}

export const post = <T = any>(url: string, data?: any, options?: RequestOptions) =>
  request<T>('POST', url, data, options)

export const put = <T = any>(url: string, data?: any, options?: RequestOptions) =>
  request<T>('PUT', url, data, options)

export const del = <T = any>(url: string, options?: RequestOptions) =>
  request<T>('DELETE', url, undefined, options)
