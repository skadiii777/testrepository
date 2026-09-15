import request from '@/config/axios'

export interface ContractVO {
  id?: number
  contractCode?: string
  customerName?: string
  productName?: string
  amount?: number
  signDate?: string
  startDate?: string
  endDate?: string
  owner?: string
  status?: string
  createTime?: Date
}

export const getContractPage = async (params: any) => {

  return await request.get({ url: '/biz/contract/page', params })

}

export const getContract = async (id: number) => {

  return await request.get({ url: '/biz/contract/get', params: { id } })

}

export const createContract = async (data: ContractVO) => {

  return await request.post({ url: '/biz/contract/create', data })

}

export const updateContract = async (data: ContractVO) => {

  return await request.put({ url: '/biz/contract/update', data })

}

export const deleteContract = async (id: number) => {

  return await request.delete({ url: '/biz/contract/delete', params: { id } })

}

export const exportContract = async (params: any) => {

  return await request.download({ url: '/biz/contract/export-excel', params })

}
