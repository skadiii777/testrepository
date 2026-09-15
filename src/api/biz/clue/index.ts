import request from '@/config/axios'

export interface ClueVO {
  id?: number
  name?: string
  contactName?: string
  contactMobile?: string
  source?: string // biz_clue_source：1广告投放 2客户推荐 3官网咨询 4电话营销 5其他渠道
  status?: string // biz_clue_status：0待跟进 1跟进中 2已转化 3已无效
  ownerName?: string
  customerId?: number
  remark?: string
  createTime?: Date
}

export const getCluePage = async (params: any) => {
  return await request.get({ url: '/biz/clue/page', params })
}

export const getClue = async (id: number) => {
  return await request.get({ url: '/biz/clue/get', params: { id } })
}

export const createClue = async (data: ClueVO) => {
  return await request.post({ url: '/biz/clue/create', data })
}

export const updateClue = async (data: ClueVO) => {
  return await request.put({ url: '/biz/clue/update', data })
}

export const deleteClue = async (id: number) => {
  return await request.delete({ url: '/biz/clue/delete', params: { id } })
}

export const convertClue = async (id: number, data: any) => {
  return await request.put({ url: '/biz/clue/convert', params: { id }, data })
}

export const exportClue = async (params: any) => {
  return await request.download({ url: '/biz/clue/export-excel', params })
}
