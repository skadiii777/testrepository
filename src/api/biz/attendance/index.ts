import request from '@/config/axios'

export interface AttendanceVO {
  id?: number
  empName?: string
  workDate?: string
  checkIn?: string
  checkOut?: string
  status?: string
  createTime?: Date
}

export const getAttendancePage = async (params: any) => {

  return await request.get({ url: '/biz/attendance/page', params })

}

export const getAttendance = async (id: number) => {

  return await request.get({ url: '/biz/attendance/get', params: { id } })

}

export const createAttendance = async (data: AttendanceVO) => {

  return await request.post({ url: '/biz/attendance/create', data })

}

export const updateAttendance = async (data: AttendanceVO) => {

  return await request.put({ url: '/biz/attendance/update', data })

}

export const deleteAttendance = async (id: number) => {

  return await request.delete({ url: '/biz/attendance/delete', params: { id } })

}

export const exportAttendance = async (params: any) => {

  return await request.download({ url: '/biz/attendance/export-excel', params })

}

export const getAttendanceSummary = async (month: string) => {

  return await request.get({ url: '/biz/attendance/summary', params: { month } })

}
