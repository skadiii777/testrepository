import { get, post, put, del } from '../utils/request'

// ===== 认证 =====
// 租户通过 tenant-id 请求头传递（见 utils/request），body 只带账号密码
export const login = (data: {
  username: string
  password: string
  captchaVerification?: string
}) => post<any>('/system/auth/login', data, { silent: true })

export const logout = () => post('/system/auth/logout')

export const getPermissionInfo = () => get<any>('/system/auth/get-permission-info')

// ===== 员工工作台（portal）=====
export interface PortalIndexData {
  today: string
  workStart: string
  workEnd: string
  checkIn?: string
  checkOut?: string
  status?: string
  monthOvertimeMinutes?: number
  quotas?: Record<string, number>
}

export const getPortalIndexData = () => get<PortalIndexData>('/portal/index-data')

// web 端是 post('/portal/punch', { params: { type } }) → 参数走 query
export const punch = (type: 'in' | 'out') => post<string>(`/portal/punch?type=${type}`)

// ===== 请假 =====
export const getLeavePage = (params: any) => get<any>('/portal/leave-page', params)
export const submitLeave = (data: any) => post('/portal/leave-submit', data)
export const cancelLeave = (id: number) => post(`/portal/leave-cancel?id=${id}`)

// ===== 报销 =====
export const getExpensePage = (params: any) => get<any>('/portal/expense-page', params)
export const submitExpense = (data: any) => post('/portal/expense-submit', data)
export const withdrawExpense = (id: number) => del(`/portal/expense-withdraw?id=${id}`)

// ===== 汇报 =====
export const getReportPage = (params: any) => get<any>('/portal/report-page', params)
export const submitReport = (data: any) => post('/portal/report-submit', data)
export const deleteReport = (id: number) => del(`/portal/report-delete?id=${id}`)

// ===== 补卡 =====
export const getCorrectionPage = (params: any) => get<any>('/portal/correction-page', params)
export const submitCorrection = (data: any) => post('/portal/correction-submit', data)
export const withdrawCorrection = (id: number) => del(`/portal/correction-withdraw?id=${id}`)

// ===== 审批中心 =====
export interface ApprovalPending {
  leaveCount: number
  expenseCount: number
  correctionCount: number
}
export const getApprovalPending = () => post<ApprovalPending>('/biz/approval/pending')
export const getApprovalLeavePage = (params: any) => get<any>('/biz/approval/leave-page', params)
export const getApprovalExpensePage = (params: any) => get<any>('/biz/approval/expense-page', params)
export const getApprovalCorrectionPage = (params: any) => get<any>('/biz/approval/correction-page', params)
// web 端是 post(url, { params }) → query 参数
export const auditLeave = (id: number, status: string, auditRemark = '') =>
  post(`/biz/approval/leave-audit?id=${id}&status=${status}&auditRemark=${encodeURIComponent(auditRemark)}`)
export const auditExpense = (id: number, status: string, auditRemark = '') =>
  post(`/biz/approval/expense-audit?id=${id}&status=${status}&auditRemark=${encodeURIComponent(auditRemark)}`)
export const auditCorrection = (id: number, status: string, auditRemark = '') =>
  post(`/biz/approval/correction-audit?id=${id}&status=${status}&auditRemark=${encodeURIComponent(auditRemark)}`)

// ===== 站内信 =====
export const getUnreadCount = () =>
  get<number>('/system/notify-message/get-unread-count', undefined, { silent: true })
export const getMyMessagePage = (params: any) => get<any>('/system/notify-message/my-page', params)
export const readMessages = (ids: number[]) =>
  put('/system/notify-message/update-read?' + ids.map((i) => `ids=${i}`).join('&'))
export const readAllMessages = () => put('/system/notify-message/update-all-read')
