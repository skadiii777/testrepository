import request from '@/config/axios'

export interface ContactVO {
  id?: number
  customerId?: number
  customerName?: string
  name?: string
  position?: string
  mobile?: string
  email?: string
  wechat?: string
  remark?: string
}

export const getContactPage = async (params: any) => {
  return await request.get({ url: '/biz/contact/page', params })
}

export const getContact = async (id: number) => {
  return await request.get({ url: '/biz/contact/get', params: { id } })
}

export const getContactListByCustomer = async (customerId: number) => {
  return await request.get({ url: '/biz/contact/list-by-customer', params: { customerId } })
}

export const createContact = async (data: ContactVO) => {
  return await request.post({ url: '/biz/contact/create', data })
}

export const updateContact = async (data: ContactVO) => {
  return await request.put({ url: '/biz/contact/update', data })
}

export const deleteContact = async (id: number) => {
  return await request.delete({ url: '/biz/contact/delete', params: { id } })
}

export const exportContact = async (params: any) => {
  return await request.download({ url: '/biz/contact/export-excel', params })
}
