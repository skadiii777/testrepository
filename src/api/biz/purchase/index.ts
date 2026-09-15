import request from '@/config/axios'

export interface PurchaseVO {
  id?: number
  purchaseCode?: string
  supplierName?: string
  productId?: number
  warehouseId?: number
  warehouse?: string
  productName?: string
  quantity?: number
  price?: number
  totalAmount?: number
  purchaseDate?: string
  status?: string
  createTime?: Date
}

export const getPurchasePage = async (params: any) => {

  return await request.get({ url: '/biz/purchase/page', params })

}

export const getPurchase = async (id: number) => {

  return await request.get({ url: '/biz/purchase/get', params: { id } })

}

export const createPurchase = async (data: PurchaseVO) => {

  return await request.post({ url: '/biz/purchase/create', data })

}

export const updatePurchase = async (data: PurchaseVO) => {

  return await request.put({ url: '/biz/purchase/update', data })

}

export const deletePurchase = async (id: number) => {

  return await request.delete({ url: '/biz/purchase/delete', params: { id } })

}

export const exportPurchase = async (params: any) => {

  return await request.download({ url: '/biz/purchase/export-excel', params })

}

/** 状态流转：confirm 确认 / void 作废 */
export const transitionPurchase = async (id: number, action: string) => {
  return await request.post({ url: '/biz/purchase/transition', params: { id, action } })
}

/** 完成（采购入库 / 销售出库，联动库存） */
export const completePurchase = async (id: number) => {
  return await request.post({ url: '/biz/purchase/complete', params: { id } })
}

export const createAndCompletePurchase = async (data: PurchaseVO) => {
  return await request.post({ url: '/biz/purchase/create-and-complete', data })
}

// 在途库存（已确认未完成的采购单按产品+仓库汇总）
export const getInTransit = async (warehouseId?: number) => {
  return await request.get({ url: '/biz/purchase/in-transit', params: { warehouseId } })
}
