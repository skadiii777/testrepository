import request from '@/config/axios'

export interface StockCheckVO {
  id?: number
  checkNo?: string
  productId?: number
  warehouseId?: number
  productName?: string
  warehouse?: string
  bookQuantity?: number
  actualQuantity?: number
  diffQuantity?: number
  status?: string // 0=待确认 1=已确认
  checkDate?: string
  remark?: string
  createTime?: Date
}

export const getStockCheckPage = async (params: any) => {

  return await request.get({ url: '/biz/stockcheck/page', params })

}

export const createStockCheck = async (data: StockCheckVO) => {

  return await request.post({ url: '/biz/stockcheck/create', data })

}

export const confirmStockCheck = async (id: number) => {

  return await request.post({ url: '/biz/stockcheck/confirm', params: { id } })

}

export const deleteStockCheck = async (id: number) => {

  return await request.delete({ url: '/biz/stockcheck/delete', params: { id } })

}

export const exportStockCheck = async (params: any) => {

  return await request.download({ url: '/biz/stockcheck/export-excel', params })

}
