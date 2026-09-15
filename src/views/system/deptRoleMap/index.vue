<template>
  <ContentWrap>
    <el-alert type="info" :closable="false" class="mb-15px" show-icon title="部门默认角色映射：注册审批通过时，优先按申请部门在这里配置的默认角色分配权限（可配多个）；未配置的部门回退分配「普通角色」。未来按部门/职位开放新模块权限，也在这里维护。" />
    <el-form :inline="true" label-width="80px">
      <el-form-item label="选择部门">
        <el-select v-model="currentDeptId" placeholder="请选择部门" filterable class="!w-280px"
          :loading="deptLoading" @change="loadMaps">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="d.id!" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="currentDeptId">
        <el-select v-model="selectedRoleId" placeholder="添加默认角色" filterable clearable
          class="!w-240px" :loading="roleLoading">
          <el-option v-for="r in roleOptions" :key="r.id" :label="r.name" :value="r.id!" />
        </el-select>
        <el-button type="primary" plain class="ml-8px" :disabled="!selectedRoleId"
          :loading="addLoading" @click="handleAdd">
          <Icon icon="ep:plus" class="mr-5px" /> 添加
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="mapRows" stripe>
      <el-table-column label="部门" align="center" min-width="160">
        <template #default>{{ currentDeptName }}</template>
      </el-table-column>
      <el-table-column label="默认角色" align="center" prop="roleId" min-width="160">
        <template #default="scope">
          <el-tag>{{ roleName(scope.row.roleId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100">
        <template #default="scope">
          <el-button link type="danger" @click="handleDelete(scope.row.id)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="currentDeptId && mapRows.length === 0 && !loading"
      description="该部门暂无默认角色映射，注册审批通过时将回退分配「普通角色」" />
    <el-empty v-if="!currentDeptId" description="请先在上方选择部门" />
  </ContentWrap>
</template>

<script setup lang="ts">
import { getSimpleDeptList } from '@/api/system/dept'
import { getSimpleRoleList } from '@/api/system/role'
import * as Api from '@/api/system/deptRoleMap'
import type { DeptRoleMapVO } from '@/api/system/deptRoleMap'

defineOptions({ name: 'SystemDeptRoleMap' })

const message = useMessage()

const deptOptions = ref<any[]>([])
const deptLoading = ref(false)
const roleOptions = ref<any[]>([])
const roleLoading = ref(false)
const currentDeptId = ref<number>()
const selectedRoleId = ref<number>()
const addLoading = ref(false)
const loading = ref(false)
const mapRows = ref<DeptRoleMapVO[]>([])

const currentDeptName = computed(() =>
  deptOptions.value.find((d) => d.id === currentDeptId.value)?.name || '')

const roleName = (roleId?: number) =>
  roleOptions.value.find((r) => r.id === roleId)?.name || `角色${roleId}`

const loadMaps = async () => {
  if (!currentDeptId.value) return
  loading.value = true
  try {
    mapRows.value = await Api.getDeptRoleMapListByDept(currentDeptId.value)
  } finally {
    loading.value = false
  }
}

const handleAdd = async () => {
  if (!currentDeptId.value || !selectedRoleId.value) return
  addLoading.value = true
  try {
    await Api.createDeptRoleMap(currentDeptId.value, selectedRoleId.value)
    message.success('已添加默认角色')
    selectedRoleId.value = undefined
    await loadMaps()
  } finally {
    addLoading.value = false
  }
}

const handleDelete = async (id?: number) => {
  if (!id) return
  try {
    await message.delConfirm()
    await Api.deleteDeptRoleMap(id)
    message.success('已移除')
    await loadMaps()
  } catch {}
}

onMounted(async () => {
  deptLoading.value = true
  roleLoading.value = true
  try {
    const [depts, roles] = await Promise.all([getSimpleDeptList(), getSimpleRoleList()])
    deptOptions.value = depts || []
    roleOptions.value = roles || []
  } finally {
    deptLoading.value = false
    roleLoading.value = false
  }
})
</script>
