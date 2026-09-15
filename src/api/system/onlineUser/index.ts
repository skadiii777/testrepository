import request from '@/config/axios'

export interface OnlineUserVO {
  userId?: number
  username?: string
  nickname?: string
  deptName?: string
  userType?: number
  clientId?: string
  accessTokenMask?: string
  createTime?: Date
  expiresTime?: Date
}

/** 在线用户列表（有效期内令牌 = 在线会话） */
export const getOnlineUserList = async () => {
  return await request.get({ url: '/system/online-user/list' })
}

/** 强制下线（按用户踢出其会话） */
export const kickOnlineUser = async (userId: number, userType: number) => {
  return await request.delete({ url: '/system/online-user/kick', params: { userId, userType } })
}
