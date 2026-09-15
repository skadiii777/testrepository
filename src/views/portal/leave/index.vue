<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true" :model="queryParams" ref="queryFormRef" label-width="80px">
      <el-form-item label="请假类型" prop="leaveType">
        <el-select v-model="queryParams.leaveType" placeholder="全部" clearable class="!w-240px">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_LEAVE_TYPE)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button type="primary" plain @click="openForm">
          <Icon icon="ep:plus" class="mr-5px" /> 提交请假
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="请假类型" align="center" prop="leaveType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_LEAVE_TYPE" :value="scope.row.leaveType" />
        </template>
      </el-table-column>
      <el-table-column label="开始" align="center" prop="startDate" width="110" />
      <el-table-column label="结束" align="center" prop="endDate" width="110" />
      <el-table-column label="天数" align="center" prop="days" width="80" />
      <el-table-column label="事由" align="center" prop="reason" min-width="140" show-overflow-tooltip />
      <el-table-column label="审批状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_LEAVE_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="审批意见" align="center" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="120" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.status === '1'" link type="warning" @click="handleCancel(scope.row.id)">
            销假
          </el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>

  <!-- 提交请假弹窗 -->
  <el-dialog v-model="dialogVisible" title="提交请假" width="520px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
      <el-form-item label="请假类型" prop="leaveType">
        <el-select v-model="formData.leaveType" class="!w-1/1">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_LEAVE_TYPE)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker v-model="formData.startDate" value-format="YYYY-MM-DD" type="date" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker v-model="formData.endDate" value-format="YYYY-MM-DD" type="date" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="请假天数" prop="days">
        <el-input-number v-model="formData.days" :precision="1" :min="0.5" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="事由" prop="reason">
        <el-input v-model="formData.reason" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">提 交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as PortalApi from '@/api/portal'

defineOptions({ name: 'PortalLeave' })

const message = useMessage()
const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, leaveType: undefined })
const queryFormRef = ref()

const dialogVisible = ref(false)
const formRef = ref()
const formData = ref<any>({})
const formRules = {
  leaveType: [{ required: true, message: '请假类型不能为空', trigger: 'blur' }],
  startDate: [{ required: true, message: '开始日期不能为空', trigger: 'blur' }],
  endDate: [{ required: true, message: '结束日期不能为空', trigger: 'blur' }],
  days: [{ required: true, message: '请假天数不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await PortalApi.getPortalLeavePage(queryParams)
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

const openForm = () => {
  formData.value = {}
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value.validate()
  await PortalApi.submitPortalLeave(formData.value)
  message.success('提交成功，等待审批')
  dialogVisible.value = false
  await getList()
}

const handleCancel = async (id: number) => {
  await message.confirm('确认销假？销假后本次请假将标记为已销假，余额自动返还')
  await PortalApi.cancelPortalLeave(id)
  message.success('销假成功')
  await getList()
}

onMounted(() => {
  getList()
})
</script>
