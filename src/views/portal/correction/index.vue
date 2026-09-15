<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true" :model="queryParams" label-width="80px">
      <el-form-item label="补卡类型" prop="correctType">
        <el-select v-model="queryParams.correctType" placeholder="全部" clearable class="!w-240px">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_CORRECTION_TYPE)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-240px">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_CORRECTION_STATUS)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button type="primary" plain @click="openForm">
          <Icon icon="ep:plus" class="mr-5px" /> 提交补卡
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="补卡日期" align="center" prop="workDate" width="120" />
      <el-table-column label="类型" align="center" prop="correctType" width="110">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_CORRECTION_TYPE" :value="scope.row.correctType" />
        </template>
      </el-table-column>
      <el-table-column label="补卡时间" align="center" prop="correctTime" width="100" />
      <el-table-column label="原因" align="center" prop="reason" min-width="160" show-overflow-tooltip />
      <el-table-column label="审批状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_CORRECTION_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="审批意见" align="center" prop="auditRemark" min-width="120" show-overflow-tooltip />
      <el-table-column label="提交时间" align="center" prop="createTime" :formatter="dateFormatter" width="180" />
      <el-table-column label="操作" align="center" width="110" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.status === '0'" link type="warning" @click="handleWithdraw(scope.row.id)">
            撤回
          </el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>

  <!-- 提交补卡弹窗 -->
  <el-dialog v-model="dialogVisible" title="提交补卡申请" width="520px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
      <el-form-item label="补卡日期" prop="workDate">
        <el-date-picker v-model="formData.workDate" value-format="YYYY-MM-DD" type="date" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="补卡类型" prop="correctType">
        <el-select v-model="formData.correctType" class="!w-1/1" placeholder="漏打哪张卡？">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_CORRECTION_TYPE)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="补卡时间" prop="correctTime">
        <el-time-picker v-model="correctTimeObj" format="HH:mm" value-format="HH:mm"
          placeholder="应打卡的时间" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="补卡原因" prop="reason">
        <el-input v-model="formData.reason" type="textarea" :rows="3"
          placeholder="如：早上外出办事忘打卡" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">提 交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as PortalApi from '@/api/portal'

defineOptions({ name: 'PortalCorrection' })

const message = useMessage()
const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, correctType: undefined, status: undefined })
const queryFormRef = ref()

const dialogVisible = ref(false)
const formRef = ref()
const formData = ref<any>({})
const correctTimeObj = ref<any>(null)
const formRules = {
  workDate: [{ required: true, message: '补卡日期不能为空', trigger: 'blur' }],
  correctType: [{ required: true, message: '补卡类型不能为空', trigger: 'blur' }],
  correctTime: [{ required: true, message: '补卡时间不能为空', trigger: 'blur' }],
  reason: [{ required: true, message: '补卡原因不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await PortalApi.getPortalCorrectionPage(queryParams)
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
  correctTimeObj.value = null
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value.validate()
  if (correctTimeObj.value) {
    formData.value.correctTime = String(correctTimeObj.value).slice(0, 5)
  }
  await PortalApi.submitPortalCorrection(formData.value)
  message.success('提交成功，等待审批')
  dialogVisible.value = false
  await getList()
}

const handleWithdraw = async (id: number) => {
  await message.confirm('确认撤回该补卡申请？')
  await PortalApi.withdrawPortalCorrection(id)
  message.success('撤回成功')
  await getList()
}

onMounted(() => {
  getList()
})
</script>
