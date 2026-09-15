import request from '@/config/axios'

export interface CustomerVO {
  id?: number
  customerName?: string
  contactPerson?: string
  phone?: string
  email?: string
  industry?: string
  source?: string
  address?: string
  creditLimit?: number
  status?: string
  createTime?: Date
}

export const getCustomerPage = async (params: any) => {

  return await request.get({ url: '/biz/customer/page', params })

}

export const getCustomer = async (id: number) => {

  return await request.get({ url: '/biz/customer/get', params: { id } })

}

export const createCustomer = async (data: CustomerVO) => {

  return await request.post({ url: '/biz/customer/create', data })

}

export const updateCustomer = async (data: CustomerVO) => {

  return await request.put({ url: '/biz/customer/update', data })

}

export const deleteCustomer = async (id: number) => {

  return await request.delete({ url: '/biz/customer/delete', params: { id } })

}

export const exportCustomer = async (params: any) => {

  return await request.download({ url: '/biz/customer/export-excel', params })

}

// 客户信用视图（额度/应收/剩余可用）
export const getCreditView = async (customerName: string) => {
  return await request.get({ url: '/biz/customer/credit-view', params: { customerName } })
}
