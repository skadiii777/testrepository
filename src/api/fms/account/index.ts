import request from '@/config/axios'

export interface AccountVO {
  id?: number
  code?: string
  name?: string
  type?: number // 1资产 2负债 3权益 4成本 5损益
  direction?: number // 1借方 2贷方
  parentId?: number
  status?: number // 0启用 1停用
  remark?: string
  createTime?: Date
}

// 查询会计科目分页
export const getAccountPage = async (params: any) => {
  return await request.get({ url: '/biz/fms/account/page', params })
}

// 查询会计科目详情
export const getAccount = async (id: number) => {
  return await request.get({ url: '/biz/fms/account/get', params: { id } })
}

// 新增会计科目
export const createAccount = async (data: AccountVO) => {
  return await request.post({ url: '/biz/fms/account/create', data })
}

// 修改会计科目
export const updateAccount = async (data: AccountVO) => {
  return await request.put({ url: '/biz/fms/account/update', data })
}

// 删除会计科目（已被分录引用或有子科目时拒绝）
export const deleteAccount = async (id: number) => {
  return await request.delete({ url: '/biz/fms/account/delete', params: { id } })
}

// 科目精简列表（凭证分录下拉用，仅启用）
export const getSimpleAccountList = async () => {
  return await request.get({ url: '/biz/fms/account/simple-list' })
}
