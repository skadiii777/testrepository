import request from '@/config/axios'

export interface LeaveQuotaVO {
  id?: number
  employeeId?: number
  empName?: string
  leaveType?: string
  year?: string
  quotaDays?: number
  usedDays?: number
  createTime?: Date
}

export const getLeaveQuotaPage = async (params: any) => {

  return await request.get({ url: '/biz/quota/page', params })

}

export const getLeaveQuota = async (id: number) => {

  return await request.get({ url: '/biz/quota/get', params: { id } })

}

export const createLeaveQuota = async (data: LeaveQuotaVO) => {

  return await request.post({ url: '/biz/quota/create', data })

}

export const updateLeaveQuota = async (data: LeaveQuotaVO) => {

  return await request.put({ url: '/biz/quota/update', data })

}

export const deleteLeaveQuota = async (id: number) => {

  return await request.delete({ url: '/biz/quota/delete', params: { id } })

}

export const exportLeaveQuota = async (params: any) => {

  return await request.download({ url: '/biz/quota/export-excel', params })

}
