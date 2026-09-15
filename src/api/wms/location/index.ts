import request from '@/config/axios'

export interface WmsLocationVO {
  id?: number
  warehouseId?: number
  warehouseName?: string
  code?: string
  name?: string
  type?: number // 1存储区 2拣货区 3收货区 4退货区
  status?: number // 0启用 1停用
  remark?: string
  createTime?: Date
}

// 查询库位分页
export const getLocationPage = async (params: any) => {
  return await request.get({ url: '/biz/wms/location/page', params })
}

// 查询库位详情
export const getLocation = async (id: number) => {
  return await request.get({ url: '/biz/wms/location/get', params: { id } })
}

// 新增库位（同仓库编码唯一）
export const createLocation = async (data: WmsLocationVO) => {
  return await request.post({ url: '/biz/wms/location/create', data })
}

// 修改库位
export const updateLocation = async (data: WmsLocationVO) => {
  return await request.put({ url: '/biz/wms/location/update', data })
}

// 删除库位（有库存时拒绝）
export const deleteLocation = async (id: number) => {
  return await request.delete({ url: '/biz/wms/location/delete', params: { id } })
}

// 启用库位精简列表（库存动作下拉用）
export const getSimpleLocationList = async (warehouseId?: number) => {
  return await request.get({ url: '/biz/wms/location/simple-list', params: { warehouseId } })
}
