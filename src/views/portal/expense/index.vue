<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true" :model="queryParams" label-width="80px">
      <el-form-item label="费用类别" prop="category">
        <el-select v-model="queryParams.category" placeholder="全部" clearable class="!w-240px">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_EXPENSE_TYPE)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-240px">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_EXPENSE_STATUS)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button type="primary" plain @click="openForm">
          <Icon icon="ep:plus" class="mr-5px" /> 提交报销
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="类别" align="center" prop="category">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_EXPENSE_TYPE" :value="scope.row.category" />
        </template>
      </el-table-column>
      <el-table-column label="金额" align="right" prop="amount" width="120" />
      <el-table-column label="发票" align="center" prop="invoiceUrl" width="80">
        <template #default="scope">
          <el-link v-if="scope.row.invoiceUrl" :href="scope.row.invoiceUrl" target="_blank" type="primary">查看</el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="费用日期" align="center" prop="expenseDate" width="120" />
      <el-table-column label="说明" align="center" prop="reason" min-width="160" show-overflow-tooltip />
      <el-table-column label="审批状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_EXPENSE_STATUS" :value="scope.row.status" />
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

  <!-- 提交报销弹窗 -->
  <el-dialog v-model="dialogVisible" title="提交报销" width="520px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
      <el-form-item label="费用类别" prop="category">
        <el-select v-model="formData.category" class="!w-1/1">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_EXPENSE_TYPE)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="金额（元）" prop="amount">
        <el-input-number v-model="formData.amount" :precision="2" :min="0.01" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="费用日期" prop="expenseDate">
        <el-date-picker v-model="formData.expenseDate" value-format="YYYY-MM-DD" type="date" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="发票照片" prop="invoiceUrl">
        <UploadImg v-model="formData.invoiceUrl" :limit="1" />
      </el-form-item>
      <el-form-item label="费用说明" prop="reason">
        <el-input v-model="formData.reason" type="textarea" :rows="3"
          placeholder="如：出差至北京的高铁票及住宿" />
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

defineOptions({ name: 'PortalExpense' })

const message = useMessage()
const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, category: undefined, status: undefined })
const queryFormRef = ref()

const dialogVisible = ref(false)
const formRef = ref()
const formData = ref<any>({})
const formRules = {
  category: [{ required: true, message: '费用类别不能为空', trigger: 'blur' }],
  amount: [{ required: true, message: '金额不能为空', trigger: 'blur' }],
  expenseDate: [{ required: true, message: '费用日期不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await PortalApi.getPortalExpensePage(queryParams)
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
  await PortalApi.submitPortalExpense(formData.value)
  message.success('提交成功，等待审批')
  dialogVisible.value = false
  await getList()
}

const handleWithdraw = async (id: number) => {
  await message.confirm('确认撤回该报销申请？')
  await PortalApi.withdrawPortalExpense(id)
  message.success('撤回成功')
  await getList()
}

onMounted(() => {
  getList()
})
</script>
