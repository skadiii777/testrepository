import request from '@/config/axios'

export interface SalesVO {
  id?: number
  salesCode?: string
  customerName?: string
  empName?: string
  productId?: number
  warehouseId?: number
  warehouse?: string
  productName?: string
  quantity?: number
  price?: number
  totalAmount?: number
  salesDate?: string
  status?: string
  createTime?: Date
}

export const getSalesPage = async (params: any) => {

  return await request.get({ url: '/biz/sales/page', params })

}

export const getSales = async (id: number) => {

  return await request.get({ url: '/biz/sales/get', params: { id } })

}

export const createSales = async (data: SalesVO) => {

  return await request.post({ url: '/biz/sales/create', data })

}

export const updateSales = async (data: SalesVO) => {

  return await request.put({ url: '/biz/sales/update', data })

}

export const deleteSales = async (id: number) => {

  return await request.delete({ url: '/biz/sales/delete', params: { id } })

}

export const exportSales = async (params: any) => {

  return await request.download({ url: '/biz/sales/export-excel', params })

}

/** 状态流转：confirm 确认 / void 作废 */
export const transitionSales = async (id: number, action: string) => {
  return await request.post({ url: '/biz/sales/transition', params: { id, action } })
}

/** 完成（采购入库 / 销售出库，联动库存） */
export const completeSales = async (id: number) => {
  return await request.post({ url: '/biz/sales/complete', params: { id } })
}

export const createAndCompleteSales = async (data: SalesVO) => {
  return await request.post({ url: '/biz/sales/create-and-complete', data })
}
