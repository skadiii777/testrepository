import request from '@/config/axios'

export interface WmsLocationStockVO {
  id?: number
  warehouseId?: number
  warehouseName?: string
  locationId?: number
  locationCode?: string
  productId?: number
  productName?: string
  quantity?: number
}

export interface WmsUnassignedVO {
  warehouseId: number
  warehouseName: string
  productId: number
  productName: string
  mainQuantity: number
  allocated: number
  unassigned: number
}

export interface WmsMoveVO {
  id?: number
  moveType?: string // putaway/remove/move
  warehouseId?: number
  productId?: number
  productName?: string
  quantity?: number
  fromLocationCode?: string
  toLocationCode?: string
  operatorName?: string
  remark?: string
  createTime?: Date
}

// 库位库存分页
export const getStockPage = async (params: any) => {
  return await request.get({ url: '/biz/wms/stock/page', params })
}

// 未分配库存（仓库库存 - 库位合计）
export const getUnassigned = async (warehouseId?: number): Promise<WmsUnassignedVO[]> => {
  return await request.get({ url: '/biz/wms/stock/unassigned', params: { warehouseId } })
}

// 上架（未分配 → 库位）
export const putaway = async (data: { productId: number; warehouseId?: number; locationId: number; quantity: number; remark?: string }) => {
  return await request.put({ url: '/biz/wms/stock/putaway', data })
}

// 下架（库位 → 未分配）
export const remove = async (data: { productId: number; fromLocationId: number; quantity: number; remark?: string }) => {
  return await request.put({ url: '/biz/wms/stock/remove', data })
}

// 移库（同仓库库位间）
export const move = async (data: { productId: number; fromLocationId: number; locationId: number; quantity: number; remark?: string }) => {
  return await request.put({ url: '/biz/wms/stock/move', data })
}

// 库位流水分页
export const getMovePage = async (params: any) => {
  return await request.get({ url: '/biz/wms/stock/move-page', params })
}

export interface WmsTaskVO {
  id?: number
  type?: string // putaway上架 pick拣货
  sourceCode?: string
  productId?: number
  productName?: string
  warehouseId?: number
  warehouseName?: string
  quantity?: number
  doneQuantity?: number
  status?: number // 0进行中 1已完成
  createTime?: Date
}

// 作业任务分页（上架/拣货统一视图）
export const getTaskPage = async (type: string, params: any): Promise<any> => {
  return await request.get({ url: '/biz/wms/stock/task-page', params: { type, ...params } })
}
