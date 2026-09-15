import request from '@/config/axios'

export interface StockVO {
  id?: number
  productId?: number
  warehouseId?: number
  productName?: string
  warehouse?: string
  quantity?: number
  minQuantity?: number
  createTime?: Date
}

export const getStockPage = async (params: any) => {

  return await request.get({ url: '/biz/stock/page', params })

}

export const getStock = async (id: number) => {

  return await request.get({ url: '/biz/stock/get', params: { id } })

}

export const createStock = async (data: StockVO) => {

  return await request.post({ url: '/biz/stock/create', data })

}

export const updateStock = async (data: StockVO) => {

  return await request.put({ url: '/biz/stock/update', data })

}

export const deleteStock = async (id: number) => {

  return await request.delete({ url: '/biz/stock/delete', params: { id } })

}

export const exportStock = async (params: any) => {

  return await request.download({ url: '/biz/stock/export-excel', params })

}
