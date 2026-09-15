import request from '@/config/axios'

export interface SalesTargetVO {
  id?: number
  empName?: string
  targetMonth?: string // yyyy-MM
  targetAmount?: number
  actualAmount?: number
  percent?: number
  remark?: string
}

// 目标进度分页（实际完成与达成率由服务端计算）
export const getTargetPage = async (params: any) => {
  return await request.get({ url: '/biz/target/page', params })
}

export const createTarget = async (data: SalesTargetVO) => {
  return await request.post({ url: '/biz/target/create', data })
}

export const updateTarget = async (data: SalesTargetVO) => {
  return await request.put({ url: '/biz/target/update', data })
}

export const deleteTarget = async (id: number) => {
  return await request.delete({ url: '/biz/target/delete', params: { id } })
}
