import request from '@/config/axios'

export interface VoucherEntryVO {
  id?: number
  accountId?: number
  accountCode?: string
  accountName?: string
  summary?: string
  debitAmount?: number | string
  creditAmount?: number | string
}

export interface VoucherVO {
  id?: number
  voucherNo?: string
  voucherDate?: string
  summary?: string
  status?: string // 0草稿 1已记账
  debitTotal?: number | string
  creditTotal?: number | string
  sourceType?: string // payment=收付款 return=退货 undefined=手工
  sourceId?: number
  createTime?: Date
  entries?: VoucherEntryVO[]
}

export interface AccountBalanceVO {
  accountId: number
  accountCode: string
  accountName: string
  debitTotal: number
  creditTotal: number
  balance: number
}

// 查询凭证分页
export const getVoucherPage = async (params: any) => {
  return await request.get({ url: '/biz/fms/voucher/page', params })
}

// 查询凭证详情（含分录）
export const getVoucher = async (id: number) => {
  return await request.get({ url: '/biz/fms/voucher/get', params: { id } })
}

// 新增凭证（借方合计必须等于贷方合计）
export const createVoucher = async (data: VoucherVO) => {
  return await request.post({ url: '/biz/fms/voucher/create', data })
}

// 修改凭证（仅草稿）
export const updateVoucher = async (data: VoucherVO) => {
  return await request.put({ url: '/biz/fms/voucher/update', data })
}

// 删除凭证（仅草稿）
export const deleteVoucher = async (id: number) => {
  return await request.delete({ url: '/biz/fms/voucher/delete', params: { id } })
}

// 凭证记账（草稿 → 已记账）
export const postVoucher = async (id: number) => {
  return await request.put({ url: '/biz/fms/voucher/post', params: { id } })
}

// 取消记账（已记账 → 草稿）
export const unpostVoucher = async (id: number) => {
  return await request.put({ url: '/biz/fms/voucher/unpost', params: { id } })
}

// 科目余额表（仅统计已记账凭证）
export const getAccountBalance = async (): Promise<AccountBalanceVO[]> => {
  return await request.get({ url: '/biz/fms/voucher/balance' })
}
