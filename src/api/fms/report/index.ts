import request from '@/config/axios'

export interface ReportRow {
  accountId: number
  code: string
  name: string
  debit: number
  credit: number
  balance: number
}

export interface FinancialReport {
  assets: ReportRow[]
  liabilities: ReportRow[]
  equity: ReportRow[]
  profitItems: ReportRow[]
  totalRevenue: number
  totalExpense: number
  netProfit: number
  totalAssets: number
  totalLiabilities: number
  totalEquity: number
}

// 财务报表（利润表+资产负债表，按凭证日期过滤）
export const getFinancialReport = async (params: { beginDate?: string; endDate?: string }) => {
  return await request.get({ url: '/biz/fms/voucher/report', params })
}
