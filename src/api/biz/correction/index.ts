import request from '@/config/axios'

export interface AttendanceCorrectionVO {
  id?: number
  empName?: string
  workDate?: string
  correctType?: string
  correctTime?: string
  reason?: string
  status?: string
  createTime?: Date
}

export const getAttendanceCorrectionPage = async (params: any) => {

  return await request.get({ url: '/biz/correction/page', params })

}

export const getAttendanceCorrection = async (id: number) => {

  return await request.get({ url: '/biz/correction/get', params: { id } })

}

export const createAttendanceCorrection = async (data: AttendanceCorrectionVO) => {

  return await request.post({ url: '/biz/correction/create', data })

}

export const updateAttendanceCorrection = async (data: AttendanceCorrectionVO) => {

  return await request.put({ url: '/biz/correction/update', data })

}

export const deleteAttendanceCorrection = async (id: number) => {

  return await request.delete({ url: '/biz/correction/delete', params: { id } })

}

export const exportAttendanceCorrection = async (params: any) => {

  return await request.download({ url: '/biz/correction/export-excel', params })

}

// 审批（status: 1=通过 2=驳回）

export const auditAttendanceCorrection = async (id: number, status: string, auditRemark?: string) => {

  return await request.post({ url: '/biz/correction/audit', params: { id, status, auditRemark } })

}
