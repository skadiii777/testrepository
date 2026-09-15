import request from '@/config/axios'

// 核心指标卡
export const getDashboardPanel = async () => {
  return await request.post({ url: '/biz/dashboard/panel' })
}

// 近7日销售/采购金额趋势
export const getDashboardTrend = async () => {
  return await request.post({ url: '/biz/dashboard/trend' })
}

// 产品销售Top5
export const getDashboardProductTop = async () => {
  return await request.post({ url: '/biz/dashboard/productTop' })
}

// 状态分布（合同/请假）
export const getDashboardStatus = async () => {
  return await request.post({ url: '/biz/dashboard/status' })
}

// 审批中心：待办角标
export const getApprovalPending = async () => {
  return await request.post({ url: '/biz/approval/pending' })
}

// 审批中心：待审批请假分页
export const getApprovalLeavePage = async (params: any) => {
  return await request.get({ url: '/biz/approval/leave-page', params })
}

// 审批中心：待审批报销分页
export const getApprovalExpensePage = async (params: any) => {
  return await request.get({ url: '/biz/approval/expense-page', params })
}

// 审批中心：审批请假
export const auditApprovalLeave = async (id: number, status: string, auditRemark?: string) => {
  return await request.post({ url: '/biz/approval/leave-audit', params: { id, status, auditRemark } })
}

// 审批中心：审批报销
export const auditApprovalExpense = async (id: number, status: string, auditRemark?: string) => {
  return await request.post({ url: '/biz/approval/expense-audit', params: { id, status, auditRemark } })
}

// 审批中心：待审批补卡分页
export const getApprovalCorrectionPage = async (params: any) => {
  return await request.get({ url: '/biz/approval/correction-page', params })
}

// 审批中心：审批补卡（通过后自动回写考勤）
export const auditApprovalCorrection = async (id: number, status: string, auditRemark?: string) => {
  return await request.post({ url: '/biz/approval/correction-audit', params: { id, status, auditRemark } })
}
