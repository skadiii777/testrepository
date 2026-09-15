import request from '@/config/axios'

export interface ExpenseVO {
  id?: number
  empName?: string
  category?: string
  amount?: number
  expenseDate?: string
  reason?: string
  invoiceUrl?: string
  status?: string
  createTime?: Date
}

export const getExpensePage = async (params: any) => {

  return await request.get({ url: '/biz/expense/page', params })

}

export const getExpense = async (id: number) => {

  return await request.get({ url: '/biz/expense/get', params: { id } })

}

export const createExpense = async (data: ExpenseVO) => {

  return await request.post({ url: '/biz/expense/create', data })

}

export const updateExpense = async (data: ExpenseVO) => {

  return await request.put({ url: '/biz/expense/update', data })

}

export const deleteExpense = async (id: number) => {

  return await request.delete({ url: '/biz/expense/delete', params: { id } })

}

export const exportExpense = async (params: any) => {

  return await request.download({ url: '/biz/expense/export-excel', params })

}

// 审批（status: 1=通过 2=驳回）

export const auditExpense = async (id: number, status: string, auditRemark?: string) => {

  return await request.post({ url: '/biz/expense/audit', params: { id, status, auditRemark } })

}
