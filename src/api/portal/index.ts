import request from '@/config/axios'

// 工作台首页数据（今日打卡 + 假期余额）
export const getPortalIndexData = async () => {
  return await request.get({ url: '/portal/index-data' })
}

// 打卡（type: in 上班 / out 下班）
export const punch = async (type: string) => {
  return await request.post({ url: '/portal/punch', params: { type } })
}

// 我的请假分页
export const getPortalLeavePage = async (params: any) => {
  return await request.get({ url: '/portal/leave-page', params })
}

// 提交请假
export const submitPortalLeave = async (data: any) => {
  return await request.post({ url: '/portal/leave-submit', data })
}

// 销假
export const cancelPortalLeave = async (id: number) => {
  return await request.post({ url: '/portal/leave-cancel', params: { id } })
}

// 我的汇报分页
export const getPortalReportPage = async (params: any) => {
  return await request.get({ url: '/portal/report-page', params })
}

// 提交汇报
export const submitPortalReport = async (data: any) => {
  return await request.post({ url: '/portal/report-submit', data })
}

// 删除本人汇报
export const deletePortalReport = async (id: number) => {
  return await request.delete({ url: '/portal/report-delete', params: { id } })
}

// 我的报销分页
export const getPortalExpensePage = async (params: any) => {
  return await request.get({ url: '/portal/expense-page', params })
}

// 提交报销
export const submitPortalExpense = async (data: any) => {
  return await request.post({ url: '/portal/expense-submit', data })
}

// 撤回待审批报销
export const withdrawPortalExpense = async (id: number) => {
  return await request.delete({ url: '/portal/expense-withdraw', params: { id } })
}

// 我的补卡分页
export const getPortalCorrectionPage = async (params: any) => {
  return await request.get({ url: '/portal/correction-page', params })
}

// 提交补卡申请
export const submitPortalCorrection = async (data: any) => {
  return await request.post({ url: '/portal/correction-submit', data })
}

// 撤回待审批补卡
export const withdrawPortalCorrection = async (id: number) => {
  return await request.delete({ url: '/portal/correction-withdraw', params: { id } })
}
