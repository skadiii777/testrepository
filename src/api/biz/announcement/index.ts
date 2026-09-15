import request from '@/config/axios'

export interface AnnouncementVO {
  id?: number
  title?: string
  type?: string // biz_announcement_type：1通知 2公告 3制度
  content?: string
  pinned?: string // 0=否 1=是
  status?: string // 0=已发布 1=已下架
  publishDate?: string
  createTime?: Date
}

export const getAnnouncementPage = async (params: any) => {
  return await request.get({ url: '/biz/announcement/page', params })
}

export const getAnnouncement = async (id: number) => {
  return await request.get({ url: '/biz/announcement/get', params: { id } })
}

export const getLatestAnnouncements = async (limit = 5) => {
  return await request.get({ url: '/biz/announcement/list-latest', params: { limit } })
}

export const createAnnouncement = async (data: AnnouncementVO) => {
  return await request.post({ url: '/biz/announcement/create', data })
}

export const updateAnnouncement = async (data: AnnouncementVO) => {
  return await request.put({ url: '/biz/announcement/update', data })
}

export const deleteAnnouncement = async (id: number) => {
  return await request.delete({ url: '/biz/announcement/delete', params: { id } })
}
