import request from '@/config/axios'

export interface ReportVO {
  id?: number
  reportType?: string
  title?: string
  content?: string
  reportDate?: string
  createTime?: Date
}

export const getReportPage = async (params: any) => {

  return await request.get({ url: '/biz/report/page', params })

}

export const getReport = async (id: number) => {

  return await request.get({ url: '/biz/report/get', params: { id } })

}

export const createReport = async (data: ReportVO) => {

  return await request.post({ url: '/biz/report/create', data })

}

export const updateReport = async (data: ReportVO) => {

  return await request.put({ url: '/biz/report/update', data })

}

export const deleteReport = async (id: number) => {

  return await request.delete({ url: '/biz/report/delete', params: { id } })

}

export const exportReport = async (params: any) => {

  return await request.download({ url: '/biz/report/export-excel', params })

}
