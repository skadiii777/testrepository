import request from '@/config/axios'

export interface LeaveVO {
  id?: number
  employeeId?: number
  empName?: string
  leaveType?: string
  startDate?: string
  endDate?: string
  days?: number
  reason?: string
  status?: string
  createTime?: Date
}

export const getLeavePage = async (params: any) => {

  return await request.get({ url: '/biz/leave/page', params })

}

export const getLeave = async (id: number) => {

  return await request.get({ url: '/biz/leave/get', params: { id } })

}

export const createLeave = async (data: LeaveVO) => {

  return await request.post({ url: '/biz/leave/create', data })

}

export const updateLeave = async (data: LeaveVO) => {

  return await request.put({ url: '/biz/leave/update', data })

}

export const deleteLeave = async (id: number) => {

  return await request.delete({ url: '/biz/leave/delete', params: { id } })

}

export const exportLeave = async (params: any) => {

  return await request.download({ url: '/biz/leave/export-excel', params })

}

// 审批（status: 1=通过 2=驳回）

export const auditLeave = async (id: number, status: string, auditRemark?: string) => {

  return await request.post({ url: '/biz/leave/audit', params: { id, status, auditRemark } })

}
