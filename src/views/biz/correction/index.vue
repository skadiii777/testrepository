<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="员工姓名" prop="empName">
        <el-input v-model="queryParams.empName" placeholder="请输入员工姓名" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="补卡日期" prop="workDate">
        <el-date-picker v-model="queryParams.workDate" value-format="YYYY-MM-DD" type="date"
          placeholder="请选择补卡日期" clearable class="!w-240px" />
      </el-form-item>
      <el-form-item label="补卡类型" prop="correctType">
        <el-select v-model="queryParams.correctType" placeholder="请选择补卡类型" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_CORRECTION_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="审批状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择审批状态" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_CORRECTION_STATUS)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['biz:correction:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:correction:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="员工姓名" align="center" prop="empName" min-width="110" />
      <el-table-column label="补卡日期" align="center" prop="workDate" min-width="110" />
      <el-table-column label="补卡类型" align="center" prop="correctType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_CORRECTION_TYPE" :value="scope.row.correctType" />
        </template>
      </el-table-column>
      <el-table-column label="补卡时间" align="center" prop="correctTime" min-width="110" />
      <el-table-column label="补卡原因" align="center" prop="reason" min-width="110" />
      <el-table-column label="审批状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_CORRECTION_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['biz:correction:update']">修改</el-button>
          <el-button link type="primary" @click="handleAudit(scope.row.id, '1')" v-hasPermi="['biz:correction:audit']">通过</el-button>
          <el-button link type="warning" @click="handleAudit(scope.row.id, '2')" v-hasPermi="['biz:correction:audit']">驳回</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['biz:correction:delete']">删除</el-button>
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
          <el-input v-model="formData.empName" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="补卡日期" prop="workDate">
          <el-date-picker v-model="formData.workDate" value-format="YYYY-MM-DD" type="date"
            placeholder="请选择补卡日期" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="补卡类型" prop="correctType">
          <el-select v-model="formData.correctType" placeholder="请选择补卡类型" class="!w-1/1">
            <el-option
              v-for="d in getDictOptions(DICT_TYPE.BIZ_CORRECTION_TYPE)"
              :key="d.value"
              :label="d.label"
              :value="d.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="补卡时间" prop="correctTime">
          <el-input v-model="formData.correctTime" placeholder="请输入补卡时间" />
        </el-form-item>
        <el-form-item label="补卡原因" prop="reason">
          <el-input v-model="formData.reason" type="textarea" :rows="4" placeholder="请输入补卡原因" />
        </el-form-item>
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择审批状态" class="!w-1/1">
            <el-option
              v-for="d in getDictOptions(DICT_TYPE.BIZ_CORRECTION_STATUS)"
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
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/correction'
import type { AttendanceCorrectionVO } from '@/api/biz/correction'

defineOptions({ name: 'AttendanceCorrection' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.AttendanceCorrectionVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  empName: undefined,
  workDate: undefined,
  correctType: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getAttendanceCorrectionPage(queryParams)
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
  const tip = status === '1' ? '确认通过该补卡申请申请吗？' : '确认驳回该补卡申请申请吗？'
  await message.confirm(tip)
  await Api.auditAttendanceCorrection(id, status, status === '1' ? '审批通过' : '审批驳回')
  message.success('审批成功')
  await getList()
}

/** 表单弹窗逻辑 */
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formType = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Api.AttendanceCorrectionVO>({} as Api.AttendanceCorrectionVO)
const formRules = reactive({
  workDate: [{ required: true, message: "补卡日期不能为空", trigger: "blur" }],
  correctType: [{ required: true, message: "补卡类型不能为空", trigger: "blur" }],
  correctTime: [{ required: true, message: "补卡时间不能为空", trigger: "blur" }],
  reason: [{ required: true, message: "补卡原因不能为空", trigger: "blur" }],
})
const openForm = (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增补卡申请' : '修改补卡申请'
  formType.value = type
  if (id) {
    formLoading.value = true
    Api.getAttendanceCorrection(id).then((data) => {
      formData.value = data
    }).finally(() => { formLoading.value = false })
  } else {
    formData.value = {} as Api.AttendanceCorrectionVO
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteAttendanceCorrection(id)
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
      await Api.createAttendanceCorrection(formData.value as unknown as Api.AttendanceCorrectionVO)
      message.success('新增成功')
    } else {
      await Api.updateAttendanceCorrection(formData.value as unknown as Api.AttendanceCorrectionVO)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await Api.exportAttendanceCorrection(queryParams)
    download.excel(data, '补卡申请.xls')
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
