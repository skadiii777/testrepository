import request from '@/config/axios'

export interface RegisterApplyVO {
  id?: number
  username?: string
  nickname?: string
  deptId?: number
  postId?: number
  status?: string // 0=待审批 1=已通过 2=已驳回
  rejectReason?: string
  auditTime?: string
  createTime?: string
}

export const getRegisterApplyPage = async (params: any) => {

  return await request.get({ url: '/system/register-apply/page', params })

}

export const approveRegisterApply = async (id: number) => {

  return await request.post({ url: '/system/register-apply/approve', params: { id } })

}

export const rejectRegisterApply = async (id: number, reason?: string) => {

  return await request.post({ url: '/system/register-apply/reject', params: { id, reason } })

}

export const exportRegisterApply = async (params: any) => {

  return await request.download({ url: '/system/register-apply/export-excel', params })

}
