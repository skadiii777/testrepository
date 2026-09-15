<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="用户账号" prop="username">
        <el-input v-model="queryParams.username" placeholder="请输入用户账号" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option label="待审批" value="0" />
          <el-option label="已通过" value="1" />
          <el-option label="已驳回" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="用户账号" align="center" prop="username" min-width="120" />
      <el-table-column label="用户昵称" align="center" prop="nickname" min-width="110" />
      <el-table-column label="申请部门" align="center" min-width="130">
        <template #default="scope">{{ deptName(scope.row.deptId) }}</template>
      </el-table-column>
      <el-table-column label="申请职位" align="center" min-width="120">
        <template #default="scope">{{ postName(scope.row.postId) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="驳回原因" align="center" prop="rejectReason" min-width="130" show-overflow-tooltip />
      <el-table-column label="申请时间" align="center" prop="createTime" :formatter="dateFormatter" width="170px" />
      <el-table-column label="审批时间" align="center" prop="auditTime" width="110px" />
      <el-table-column label="操作" align="center" width="170" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === '0'">
            <el-button link type="primary" @click="handleApprove(scope.row)"
              v-hasPermi="['biz:register-apply:audit']">通过</el-button>
            <el-button link type="warning" @click="handleReject(scope.row)"
              v-hasPermi="['biz:register-apply:audit']">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 通过弹窗：确认分配角色 -->
  <el-dialog v-model="approveDialogVisible" title="通过注册申请" width="520px">
    <el-form label-width="100px">
      <el-form-item label="账号">
        <el-input :model-value="approveRow?.username" disabled />
      </el-form-item>
      <el-form-item label="部门 / 职位">
        <el-input :model-value="`${deptName(approveRow?.deptId)} / ${postName(approveRow?.postId) || '未指定'}`" disabled />
      </el-form-item>
      <el-form-item label="分配角色">
        <el-select v-model="approveRoleIds" multiple class="!w-1/1" placeholder="默认按部门映射 / 普通角色">
          <el-option v-for="r in roleOptions" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
        <div class="text-12px text-gray-400">留空 = 按部门默认角色映射分配，无映射时分配「普通角色」</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="approveDialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="approveLoading" @click="submitApprove">确认通过</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as Api from '@/api/system/registerapply'
import type { RegisterApplyVO } from '@/api/system/registerapply'
import * as DeptApi from '@/api/system/dept'
import * as PostApi from '@/api/system/post'
import * as RoleApi from '@/api/system/role'

defineOptions({ name: 'RegisterApply' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.RegisterApplyVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  username: undefined,
  status: undefined
})
const queryFormRef = ref()

/** 部门/职位名称映射 */
const deptMap = ref<Map<number, string>>(new Map())
const postMap = ref<Map<number, string>>(new Map())
const deptName = (id?: number) => (id == null ? '' : deptMap.value.get(id) || `部门${id}`)
const postName = (id?: number) => (id == null ? '' : postMap.value.get(id) || `岗位${id}`)

const statusText = (s?: string) => (s === '1' ? '已通过' : s === '2' ? '已驳回' : '待审批')
const statusTag = (s?: string) => (s === '1' ? 'success' : s === '2' ? 'danger' : 'warning')

/** 角色选项（通过弹窗） */
const roleOptions = ref<{ id: number; name: string }[]>([])

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getRegisterApplyPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 通过 */
const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const approveRow = ref<Api.RegisterApplyVO>()
const approveRoleIds = ref<number[]>([])
const handleApprove = async (row: Api.RegisterApplyVO) => {
  approveRow.value = row
  approveRoleIds.value = []
  approveDialogVisible.value = true
}
const submitApprove = async () => {
  approveLoading.value = true
  try {
    await Api.approveRegisterApply(approveRow.value!.id!)
    message.success('已通过，账号已创建并加入部门')
    approveDialogVisible.value = false
    await getList()
  } finally {
    approveLoading.value = false
  }
}

/** 驳回 */
const handleReject = async (row: Api.RegisterApplyVO) => {
  const { value } = await ElMessageBox.prompt('请输入驳回原因（可留空）', `驳回 ${row.username} 的注册申请`, {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputPlaceholder: '驳回原因'
  })
  await Api.rejectRegisterApply(row.id!, value || undefined)
  message.success('已驳回')
  await getList()
}

/** 初始化：加载部门/岗位/角色名称映射 */
onMounted(async () => {
  await getList()
  try {
    const [depts, posts, roles] = await Promise.all([
      DeptApi.getSimpleDeptList(),
      PostApi.getSimplePostList(),
      RoleApi.getSimpleRoleList()
    ])
    deptMap.value = new Map(depts.map((d: any) => [d.id, d.name]))
    postMap.value = new Map(posts.map((p: any) => [p.id, p.name]))
    roleOptions.value = roles
      .filter((r: any) => r.status === 0)
      .map((r: any) => ({ id: r.id, name: r.name }))
  } catch {}
})
</script>
