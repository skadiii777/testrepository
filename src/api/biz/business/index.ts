import request from '@/config/axios'

export interface BusinessVO {
  id?: number
  name?: string
  customerName?: string
  stage?: string // biz_business_stage：1初步接触 2需求确认 3方案报价 4谈判协商 5赢单 6输单
  amount?: number
  expectedDate?: string
  ownerName?: string
  remark?: string
  createTime?: Date
}

export interface FunnelStatVO {
  stage: string
  stageName: string
  count: number
  totalAmount: number
}

export const getBusinessPage = async (params: any) => {
  return await request.get({ url: '/biz/business/page', params })
}

export const getBusiness = async (id: number) => {
  return await request.get({ url: '/biz/business/get', params: { id } })
}

export const createBusiness = async (data: BusinessVO) => {
  return await request.post({ url: '/biz/business/create', data })
}

export const updateBusiness = async (data: BusinessVO) => {
  return await request.put({ url: '/biz/business/update', data })
}

export const deleteBusiness = async (id: number) => {
  return await request.delete({ url: '/biz/business/delete', params: { id } })
}

export const getFunnelStats = async () => {
  return await request.get({ url: '/biz/business/funnel-stats' })
}

/** 赢单商机一键转合同 */
export const convertToContract = async (id: number, data: { productName: string; startDate?: string; endDate?: string }) => {
  return await request.post({ url: '/biz/business/convert-to-contract', params: { id }, data })
}

export const getSimpleBusinessList = async () => {
  return await request.get({ url: '/biz/business/simple-list' })
}

export const exportBusiness = async (params: any) => {
  return await request.download({ url: '/biz/business/export-excel', params })
}
