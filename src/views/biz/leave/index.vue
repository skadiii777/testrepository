<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="员工姓名" prop="empName">
        <el-input v-model="queryParams.empName" placeholder="请输入员工姓名" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="请假类型" prop="leaveType">
        <el-select v-model="queryParams.leaveType" placeholder="请选择请假类型" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_LEAVE_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="审批状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择审批状态" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_LEAVE_STATUS)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['biz:leave:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:leave:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="员工姓名" align="center" prop="empName" min-width="110" />
      <el-table-column label="请假类型" align="center" prop="leaveType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_LEAVE_TYPE" :value="scope.row.leaveType" />
        </template>
      </el-table-column>
      <el-table-column label="开始日期" align="center" prop="startDate" min-width="110" />
      <el-table-column label="结束日期" align="center" prop="endDate" min-width="110" />
      <el-table-column label="请假天数" align="right" prop="days" min-width="100" />
      <el-table-column label="请假事由" align="center" prop="reason" min-width="110" />
      <el-table-column label="审批状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_LEAVE_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.status === '0'" link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['biz:leave:update']">修改</el-button>
          <el-button v-if="scope.row.status === '0'" link type="primary" @click="handleAudit(scope.row.id, '1')" v-hasPermi="['biz:leave:audit']">通过</el-button>
          <el-button v-if="scope.row.status === '0'" link type="warning" @click="handleAudit(scope.row.id, '2')" v-hasPermi="['biz:leave:audit']">驳回</el-button>
          <el-button v-if="scope.row.status === '0'" link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['biz:leave:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="员工姓名" prop="empName">
          <BizReferenceSelect v-model="formData.employeeId" v-model:name="formData.empName" kind="employees" />
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="formData.leaveType" placeholder="请选择请假类型" class="!w-1/1">
            <el-option
              v-for="d in getDictOptions(DICT_TYPE.BIZ_LEAVE_TYPE)"
              :key="d.value"
              :label="d.label"
              :value="d.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="formData.startDate" value-format="YYYY-MM-DD" type="date"
            placeholder="请选择开始日期" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="formData.endDate" value-format="YYYY-MM-DD" type="date"
            placeholder="请选择结束日期" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="请假天数" prop="days">
          <el-input-number v-model="formData.days" :precision="1" :min="0" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="formData.reason" type="textarea" :rows="4" placeholder="请输入请假事由" />
        </el-form-item>
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择审批状态" class="!w-1/1">
            <el-option
              v-for="d in getDictOptions(DICT_TYPE.BIZ_LEAVE_STATUS)"
              :key="d.value"
              :label="d.label"
              :value="d.value"
            />
          </el-select>
        </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import BizReferenceSelect from '@/components/BizReferenceSelect/index.vue'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/leave'

defineOptions({ name: 'Leave' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.LeaveVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  empName: undefined,
  leaveType: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getLeavePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 审批操作 */
const handleAudit = async (id: number, status: string) => {
  const tip = status === '1' ? '确认通过该请假申请吗？' : '确认驳回该请假申请吗？'
  await message.confirm(tip)
  await Api.auditLeave(id, status, status === '1' ? '审批通过' : '审批驳回')
  message.success('审批成功')
  await getList()
}

/** 表单弹窗逻辑 */
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formType = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Api.LeaveVO>({} as Api.LeaveVO)
const formRules = reactive({
  leaveType: [{ required: true, message: "请假类型不能为空", trigger: "blur" }],
  startDate: [{ required: true, message: "开始日期不能为空", trigger: "blur" }],
  endDate: [{ required: true, message: "结束日期不能为空", trigger: "blur" }],
  days: [{ required: true, message: "请假天数不能为空", trigger: "blur" }],
})
const openForm = (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增请假' : '修改请假'
  formType.value = type
  if (id) {
    formLoading.value = true
    Api.getLeave(id).then((data) => {
      formData.value = data
    }).finally(() => { formLoading.value = false })
  } else {
    formData.value = {} as Api.LeaveVO
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteLeave(id)
    message.success('删除成功')
    await getList()
  } catch {}
}
/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await Api.createLeave(formData.value as unknown as Api.LeaveVO)
      message.success('新增成功')
    } else {
      await Api.updateLeave(formData.value as unknown as Api.LeaveVO)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
    await getList()
  } finally {
    formLoading.value = false
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await Api.exportLeave(queryParams)
    download.excel(data, '请假.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}
/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
