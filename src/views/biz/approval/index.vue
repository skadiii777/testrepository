<template>
  <ContentWrap>
    <el-row :gutter="16" style="margin-bottom: 12px">
      <el-col :span="8">
        <el-button type="warning" class="!w-full !h-48px" @click="tab = 'leave'; getList()">
          <Icon icon="ep:bell" class="mr-5px" /> 请假待审批
          <el-badge :value="pending.leaveCount" type="danger" class="ml-8px" />
        </el-button>
      </el-col>
      <el-col :span="8">
        <el-button type="info" class="!w-full !h-48px" @click="tab = 'expense'; getExpenseList()">
          <Icon icon="fa:money" class="mr-5px" /> 报销待审批
          <el-badge :value="pending.expenseCount" type="danger" class="ml-8px" />
        </el-button>
      </el-col>
      <el-col :span="8">
        <el-button type="primary" class="!w-full !h-48px" @click="tab = 'correction'; getCorrectionList()">
          <Icon icon="ep:edit-pen" class="mr-5px" /> 补卡待审批
          <el-badge :value="pending.correctionCount" type="danger" class="ml-8px" />
        </el-button>
      </el-col>
    </el-row>

    <!-- 请假待办 -->
    <el-table v-if="tab === 'leave'" v-loading="loading" :data="leaveList" stripe>
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="申请人" align="center" prop="empName" min-width="100" />
      <el-table-column label="类型" align="center" prop="leaveType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_LEAVE_TYPE" :value="scope.row.leaveType" />
        </template>
      </el-table-column>
      <el-table-column label="开始" align="center" prop="startDate" width="110" />
      <el-table-column label="结束" align="center" prop="endDate" width="110" />
      <el-table-column label="天数" align="center" prop="days" width="70" />
      <el-table-column label="事由" align="center" prop="reason" min-width="160" show-overflow-tooltip />
      <el-table-column label="提交时间" align="center" prop="createTime" :formatter="dateFormatter" width="180" />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleAuditLeave(scope.row.id, '1')"
            v-hasPermi="['biz:approval:audit']">通过</el-button>
          <el-button link type="danger" @click="handleAuditLeave(scope.row.id, '2')"
            v-hasPermi="['biz:approval:audit']">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 补卡待办 -->
    <el-table v-if="tab === 'correction'" v-loading="loading" :data="correctionList" stripe>
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="申请人" align="center" prop="empName" min-width="100" />
      <el-table-column label="补卡日期" align="center" prop="workDate" width="110" />
      <el-table-column label="类型" align="center" prop="correctType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_CORRECTION_TYPE" :value="scope.row.correctType" />
        </template>
      </el-table-column>
      <el-table-column label="补卡时间" align="center" prop="correctTime" width="100" />
      <el-table-column label="原因" align="center" prop="reason" min-width="160" show-overflow-tooltip />
      <el-table-column label="提交时间" align="center" prop="createTime" :formatter="dateFormatter" width="180" />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleAuditCorrection(scope.row.id, '1')"
            v-hasPermi="['biz:approval:audit']">通过</el-button>
          <el-button link type="danger" @click="handleAuditCorrection(scope.row.id, '2')"
            v-hasPermi="['biz:approval:audit']">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 报销待办 -->
    <el-table v-if="tab === 'expense'" v-loading="loading" :data="expenseList" stripe>
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="报销人" align="center" prop="empName" min-width="100" />
      <el-table-column label="类别" align="center" prop="category">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_EXPENSE_TYPE" :value="scope.row.category" />
        </template>
      </el-table-column>
      <el-table-column label="金额" align="right" prop="amount" width="110" />
      <el-table-column label="费用日期" align="center" prop="expenseDate" width="110" />
      <el-table-column label="说明" align="center" prop="reason" min-width="160" show-overflow-tooltip />
      <el-table-column label="提交时间" align="center" prop="createTime" :formatter="dateFormatter" width="180" />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleAuditExpense(scope.row.id, '1')"
            v-hasPermi="['biz:approval:audit']">通过</el-button>
          <el-button link type="danger" @click="handleAuditExpense(scope.row.id, '2')"
            v-hasPermi="['biz:approval:audit']">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination v-if="tab === 'leave'" :total="leaveTotal" v-model:page="leaveQuery.pageNo"
      v-model:limit="leaveQuery.pageSize" @pagination="getList" />
    <Pagination v-if="tab === 'correction'" :total="correctionTotal" v-model:page="correctionQuery.pageNo"
      v-model:limit="correctionQuery.pageSize" @pagination="getCorrectionList" />
    <Pagination v-if="tab === 'expense'" :total="expenseTotal" v-model:page="expenseQuery.pageNo"
      v-model:limit="expenseQuery.pageSize" @pagination="getExpenseList" />
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE } from '@/utils/dict'
import * as DashboardApi from '@/api/biz/dashboard'

defineOptions({ name: 'BizApproval' })

const message = useMessage()
const tab = ref<'leave' | 'expense'>('leave')
const pending = ref<any>({})
const loading = ref(false)

const leaveList = ref<any[]>([])
const leaveTotal = ref(0)
const leaveQuery = reactive({ pageNo: 1, pageSize: 10, status: '0' })

const expenseList = ref<any[]>([])
const expenseTotal = ref(0)
const expenseQuery = reactive({ pageNo: 1, pageSize: 10, status: '0' })

const correctionList = ref<any[]>([])
const correctionTotal = ref(0)
const correctionQuery = reactive({ pageNo: 1, pageSize: 10, status: '0' })

const getPending = async () => {
  pending.value = await DashboardApi.getApprovalPending()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await DashboardApi.getApprovalLeavePage(leaveQuery)
    leaveList.value = data.list
    leaveTotal.value = data.total
    await getPending()
  } finally {
    loading.value = false
  }
}

const getExpenseList = async () => {
  loading.value = true
  try {
    const data = await DashboardApi.getApprovalExpensePage(expenseQuery)
    expenseList.value = data.list
    expenseTotal.value = data.total
    await getPending()
  } finally {
    loading.value = false
  }
}

const getCorrectionList = async () => {
  loading.value = true
  try {
    const data = await DashboardApi.getApprovalCorrectionPage(correctionQuery)
    correctionList.value = data.list
    correctionTotal.value = data.total
    await getPending()
  } finally {
    loading.value = false
  }
}

const handleAuditCorrection = async (id: number, status: string) => {
  const tip = status === '1' ? '确认通过该补卡申请吗？通过后将自动回写考勤记录' : '确认驳回该补卡申请吗？'
  await message.confirm(tip)
  await DashboardApi.auditApprovalCorrection(id, status, status === '1' ? '审批通过' : '审批驳回')
  message.success('审批成功')
  await getCorrectionList()
}

const handleAuditLeave = async (id: number, status: string) => {
  const tip = status === '1' ? '确认通过该请假申请吗？' : '确认驳回该请假申请吗？'
  await message.confirm(tip)
  await DashboardApi.auditApprovalLeave(id, status, status === '1' ? '审批通过' : '审批驳回')
  message.success('审批成功')
  await getList()
}

const handleAuditExpense = async (id: number, status: string) => {
  const tip = status === '1' ? '确认通过该报销申请吗？' : '确认驳回该报销申请吗？'
  await message.confirm(tip)
  await DashboardApi.auditApprovalExpense(id, status, status === '1' ? '审批通过' : '审批驳回')
  message.success('审批成功')
  await getExpenseList()
}

onMounted(() => {
  getList()
})
</script>
