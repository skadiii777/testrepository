import request from '@/config/axios'

export interface PaymentVO {
  id?: number
  contractId?: number
  reversalOfId?: number
  sourceReturnId?: number
  requestId?: string
  paymentNo?: string
  paymentType?: string // 1=收款 2=付款
  bizType?: string // 1=销售单 2=采购单
  orderId?: number
  orderCode?: string
  partyName?: string
  amount?: number
  paymentMethod?: string
  paymentDate?: string
  remark?: string
  createTime?: Date
}

export const getPaymentPage = async (params: any) => {

  return await request.get({ url: '/biz/payment/page', params })

}

export const createPayment = async (data: PaymentVO) => {

  return await request.post({ url: '/biz/payment/create', data })

}

export const reversePayment = async (id: number, reason: string) => {

  return await request.post({ url: '/biz/payment/reverse', data: { id, reason } })

}

export const getPaidSumByOrder = async (bizType: string, orderId: number) => {

  return await request.get({ url: '/biz/payment/paid-sum', params: { bizType, orderId } })

}

export const exportPayment = async (params: any) => {

  return await request.download({ url: '/biz/payment/export-excel', params })

}
