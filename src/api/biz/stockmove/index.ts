import request from '@/config/axios'

export interface StockMoveVO {
  id?: number
  moveType?: string
  productId?: number
  warehouseId?: number
  productName?: string
  warehouse?: string
  quantity?: number
  balanceAfter?: number
  sourceType?: string
  sourceCode?: string
  createTime?: Date
}

export const getStockMovePage = async (params: any) => {

  return await request.get({ url: '/biz/stockmove/page', params })

}

export const getStockMove = async (id: number) => {

  return await request.get({ url: '/biz/stockmove/get', params: { id } })

}

export const exportStockMove = async (params: any) => {

  return await request.download({ url: '/biz/stockmove/export-excel', params })

}
