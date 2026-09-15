import request from '@/config/axios'

export interface EmployeeVO {
  id?: number
  userId?: number
  empNo?: string
  empName?: string
  deptName?: string
  postName?: string
  phone?: string
  email?: string
  entryDate?: string
  status?: string
  createTime?: Date
}

export const getEmployeePage = async (params: any) => {

  return await request.get({ url: '/biz/employee/page', params })

}

export const getEmployee = async (id: number) => {

  return await request.get({ url: '/biz/employee/get', params: { id } })

}

export const createEmployee = async (data: EmployeeVO) => {

  return await request.post({ url: '/biz/employee/create', data })

}

export const updateEmployee = async (data: EmployeeVO) => {

  return await request.put({ url: '/biz/employee/update', data })

}

export const deleteEmployee = async (id: number) => {

  return await request.delete({ url: '/biz/employee/delete', params: { id } })

}

export const exportEmployee = async (params: any) => {

  return await request.download({ url: '/biz/employee/export-excel', params })

}
