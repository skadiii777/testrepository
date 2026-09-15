import request from '@/config/axios'

export interface ProductVO {
  id?: number
  productCode?: string
  productName?: string
  category?: string
  unit?: string
  price?: number
  cost?: number
  status?: string
  createTime?: Date
}

export const getProductPage = async (params: any) => {

  return await request.get({ url: '/biz/product/page', params })

}

export const getProduct = async (id: number) => {

  return await request.get({ url: '/biz/product/get', params: { id } })

}

export const createProduct = async (data: ProductVO) => {

  return await request.post({ url: '/biz/product/create', data })

}

export const updateProduct = async (data: ProductVO) => {

  return await request.put({ url: '/biz/product/update', data })

}

export const deleteProduct = async (id: number) => {

  return await request.delete({ url: '/biz/product/delete', params: { id } })

}

export const exportProduct = async (params: any) => {

  return await request.download({ url: '/biz/product/export-excel', params })

}
