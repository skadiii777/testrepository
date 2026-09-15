import request from '@/config/axios'

export interface ReturnVO {
  id?: number
  returnNo?: string
  returnType?: string // 1=销售退货 2=采购退货
  orderId?: number
  orderCode?: string
  partyName?: string
  productName?: string
  warehouse?: string
  quantity?: number
  price?: number
  totalAmount?: number
  returnDate?: string
  reason?: string
  status?: string // 0=待退货 1=已退货 3=已作废
  remark?: string
  createTime?: Date
}

export const getReturnPage = async (params: any) => {
  return await request.get({ url: '/biz/return/page', params })
}

export const getReturn = async (id: number) => {
  return await request.get({ url: '/biz/return/get', params: { id } })
}

export const createReturn = async (data: ReturnVO) => {
  return await request.post({ url: '/biz/return/create', data })
}

export const updateReturn = async (data: ReturnVO) => {
  return await request.put({ url: '/biz/return/update', data })
}

export const deleteReturn = async (id: number) => {
  return await request.delete({ url: '/biz/return/delete', params: { id } })
}

export const executeReturn = async (id: number) => {
  return await request.put({ url: '/biz/return/execute', params: { id } })
}

export const voidReturn = async (id: number) => {
  return await request.put({ url: '/biz/return/void', params: { id } })
}

export const getReturnedSumByOrder = async (returnType: string, orderId: number) => {
  return await request.get({ url: '/biz/return/returned-sum', params: { returnType, orderId } })
}

export const exportReturn = async (params: any) => {
  return await request.download({ url: '/biz/return/export-excel', params })
}

export const createAndExecuteReturn = async (data: ReturnVO) => {
  return await request.post({ url: '/biz/return/create-and-execute', data })
}
