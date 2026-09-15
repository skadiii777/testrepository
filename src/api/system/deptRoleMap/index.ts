import request from '@/config/axios'

export interface DeptRoleMapVO {
  id?: number
  deptId?: number
  roleId?: number
}

/** 获得部门的默认角色映射列表 */
export const getDeptRoleMapListByDept = async (deptId: number) => {
  return await request.get({ url: '/system/dept-role-map/list-by-dept', params: { deptId } })
}

/** 创建部门默认角色映射（重复时后端幂等返回已有 id） */
export const createDeptRoleMap = async (deptId: number, roleId: number) => {
  return await request.post({ url: '/system/dept-role-map/create', params: { deptId, roleId } })
}

/** 删除部门默认角色映射 */
export const deleteDeptRoleMap = async (id: number) => {
  return await request.delete({ url: '/system/dept-role-map/delete', params: { id } })
}
