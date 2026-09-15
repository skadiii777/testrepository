import request from '@/config/axios'

export interface CustomerFollowupVO {
  id?: number
  customerName?: string
  followTime?: string
  method?: string
  content?: string
  nextDate?: string
  createTime?: Date
}

export const getCustomerFollowupPage = async (params: any) => {

  return await request.get({ url: '/biz/followup/page', params })

}

export const getCustomerFollowup = async (id: number) => {

  return await request.get({ url: '/biz/followup/get', params: { id } })

}

export const createCustomerFollowup = async (data: CustomerFollowupVO) => {

  return await request.post({ url: '/biz/followup/create', data })

}

export const updateCustomerFollowup = async (data: CustomerFollowupVO) => {

  return await request.put({ url: '/biz/followup/update', data })

}

export const deleteCustomerFollowup = async (id: number) => {

  return await request.delete({ url: '/biz/followup/delete', params: { id } })

}

export const exportCustomerFollowup = async (params: any) => {

  return await request.download({ url: '/biz/followup/export-excel', params })

}
