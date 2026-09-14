import { get } from './request'

export interface DictItem {
  value: string
  label: string
  color?: string
}

// 内置兜底字典（与生产 system_dict_data 保持一致；启动后会被远端数据覆盖）
const fallback: Record<string, DictItem[]> = {
  biz_leave_type: [
    { value: '1', label: '事假' },
    { value: '2', label: '病假' },
    { value: '3', label: '年假' },
    { value: '4', label: '调休' }
  ],
  biz_expense_type: [
    { value: '1', label: '差旅' },
    { value: '2', label: '餐费' },
    { value: '3', label: '办公' },
    { value: '4', label: '其他' }
  ],
  biz_report_type: [
    { value: '1', label: '日报' },
    { value: '2', label: '周报' },
    { value: '3', label: '月报' }
  ],
  biz_correction_type: [
    { value: '1', label: '补上班卡' },
    { value: '2', label: '补下班卡' }
  ],
  biz_leave_status: [
    { value: '0', label: '待审批', color: '#e6a23c' },
    { value: '1', label: '已通过', color: '#1ab394' },
    { value: '2', label: '已驳回', color: '#f56c6c' },
    { value: '3', label: '已销假', color: '#909399' }
  ],
  biz_expense_status: [
    { value: '0', label: '待审批', color: '#e6a23c' },
    { value: '1', label: '已通过', color: '#1ab394' },
    { value: '2', label: '已驳回', color: '#f56c6c' }
  ],
  biz_correction_status: [
    { value: '0', label: '待审批', color: '#e6a23c' },
    { value: '1', label: '已通过', color: '#1ab394' },
    { value: '2', label: '已驳回', color: '#f56c6c' }
  ],
  biz_attendance_status: [
    { value: '0', label: '正常', color: '#1ab394' },
    { value: '1', label: '迟到', color: '#e6a23c' },
    { value: '2', label: '早退', color: '#e6a23c' },
    { value: '3', label: '缺勤', color: '#f56c6c' }
  ]
}

const cacheKey = 'APP_DICT'

const loadCache = (): Record<string, DictItem[]> => {
  try {
    return JSON.parse(uni.getStorageSync(cacheKey) || 'null') || fallback
  } catch {
    return fallback
  }
}

let dict = loadCache()

/** 登录后可调用：拉取全量字典刷新缓存（失败保留缓存/兜底） */
export const refreshDict = async () => {
  try {
    const list = await get<any[]>('/system/dict-data/simple-list', undefined, { silent: true })
    const merged: Record<string, DictItem[]> = { ...fallback }
    for (const item of list || []) {
      if (item.status !== 0) continue
      const type = item.dictType
      if (!type) continue
      const color =
        item.colorType === 'success' ? '#1ab394'
        : item.colorType === 'warning' ? '#e6a23c'
        : item.colorType === 'danger' ? '#f56c6c'
        : item.colorType === 'info' ? '#909399' : undefined
      ;(merged[type] ||= []).push({ value: String(item.value), label: item.label, color })
    }
    dict = merged
    uni.setStorageSync(cacheKey, JSON.stringify(merged))
  } catch {
    /* 保留缓存 */
  }
}

export const dictOptions = (type: string): DictItem[] => dict[type] || []

export const dictLabel = (type: string, value: any): string =>
  dictOptions(type).find((d) => d.value === String(value ?? ''))?.label || String(value ?? '-')

export const dictColor = (type: string, value: any): string =>
  dictOptions(type).find((d) => d.value === String(value ?? ''))?.color || '#909399'
