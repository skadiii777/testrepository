import request from '@/config/axios'

export interface SupplierVO {
  id?: number
  supplierName?: string
  contactPerson?: string
  phone?: string
  address?: string
  status?: string
  createTime?: Date
}

export const getSupplierPage = async (params: any) => {

  return await request.get({ url: '/biz/supplier/page', params })

}

export const getSupplier = async (id: number) => {

  return await request.get({ url: '/biz/supplier/get', params: { id } })

}

export const createSupplier = async (data: SupplierVO) => {

  return await request.post({ url: '/biz/supplier/create', data })

}

export const updateSupplier = async (data: SupplierVO) => {

  return await request.put({ url: '/biz/supplier/update', data })

}

export const deleteSupplier = async (id: number) => {

  return await request.delete({ url: '/biz/supplier/delete', params: { id } })

}

export const exportSupplier = async (params: any) => {

  return await request.download({ url: '/biz/supplier/export-excel', params })

}
