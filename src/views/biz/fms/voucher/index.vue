<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="凭证号" prop="voucherNo">
        <el-input v-model="queryParams.voucherNo" placeholder="请输入凭证号" clearable
          @keyup.enter="handleQuery" class="!w-200px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-160px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_FMS_VOUCHER_STATUS)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="来源" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="凭证来源" clearable class="!w-160px">
          <el-option label="手工录入" value="manual" />
          <el-option label="收付款" value="payment" />
          <el-option label="采购入库" value="purchase" />
          <el-option label="销售出库" value="sales" />
          <el-option label="退货" value="return" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:fms:voucher:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新建凭证
        </el-button>
        <el-button type="success" plain @click="openBalance" v-hasPermi="['biz:fms:voucher:query']">
          <Icon icon="ep:coin" class="mr-5px" /> 科目余额
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe show-summary :summary-method="getSummary">
      <el-table-column label="凭证号" align="center" prop="voucherNo" min-width="150" />
      <el-table-column label="凭证日期" align="center" prop="voucherDate" width="110" />
      <el-table-column label="摘要" align="left" prop="summary" min-width="180" show-overflow-tooltip />
      <el-table-column label="借方合计" align="right" prop="debitTotal" min-width="120">
        <template #default="scope">¥ {{ formatAmount(scope.row.debitTotal) }}</template>
      </el-table-column>
      <el-table-column label="贷方合计" align="right" prop="creditTotal" min-width="120">
        <template #default="scope">¥ {{ formatAmount(scope.row.creditTotal) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_FMS_VOUCHER_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="来源" align="center" prop="sourceType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.sourceType === 'payment'" type="success">收付款</el-tag>
          <el-tag v-else-if="scope.row.sourceType === 'purchase'" type="primary">采购入库</el-tag>
          <el-tag v-else-if="scope.row.sourceType === 'sales'" type="warning">销售出库</el-tag>
          <el-tag v-else-if="scope.row.sourceType === 'return'" type="danger">退货</el-tag>
          <el-tag v-else-if="scope.row.sourceType" type="info">{{ scope.row.sourceType }}</el-tag>
          <el-tag v-else type="info">手工</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170"
        :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" width="230" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row.id)">详情</el-button>
          <template v-if="scope.row.status === 0">
            <el-button link type="warning" @click="openForm(scope.row.id)"
              v-hasPermi="['biz:fms:voucher:update']">编辑</el-button>
            <el-button link type="success" @click="handlePost(scope.row.id)"
              v-hasPermi="['biz:fms:voucher:post']">记账</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row.id)"
              v-hasPermi="['biz:fms:voucher:delete']">删除</el-button>
          </template>
          <el-button v-else link type="info" @click="handleUnpost(scope.row.id)"
            v-hasPermi="['biz:fms:voucher:post']">取消记账</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗（新建/编辑） -->
  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑凭证' : '新建凭证'" width="880px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
      <el-row :gutter="20">
        <el-col :span="10">
          <el-form-item label="凭证日期" prop="voucherDate">
            <el-date-picker v-model="formData.voucherDate" value-format="YYYY-MM-DD" type="date"
              placeholder="请选择凭证日期" class="!w-1/1" />
          </el-form-item>
        </el-col>
        <el-col :span="14">
          <el-form-item label="摘要" prop="summary">
            <el-input v-model="formData.summary" placeholder="整单摘要，选填" maxlength="500" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <!-- 分录编辑 -->
    <el-table :data="formData.entries" border size="small">
      <el-table-column label="#" type="index" width="45" align="center" />
      <el-table-column label="会计科目" min-width="220">
        <template #default="{ row }">
          <el-select v-model="row.accountId" placeholder="选择科目" filterable size="small" class="!w-1/1">
            <el-option v-for="a in accountOptions" :key="a.id" :value="a.id!"
              :label="`${a.code}｜${a.name}`" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="分录摘要" min-width="160">
        <template #default="{ row }">
          <el-input v-model="row.summary" placeholder="选填" size="small" maxlength="500" />
        </template>
      </el-table-column>
      <el-table-column label="借方金额" width="160">
        <template #default="{ row }">
          <el-input-number v-model="row.debitAmount" :min="0" :precision="2" :step="100"
            :controls="false" size="small" class="!w-1/1" placeholder="0.00"
            @change="row.creditAmount = row.debitAmount ? 0 : row.creditAmount" />
        </template>
      </el-table-column>
      <el-table-column label="贷方金额" width="160">
        <template #default="{ row }">
          <el-input-number v-model="row.creditAmount" :min="0" :precision="2" :step="100"
            :controls="false" size="small" class="!w-1/1" placeholder="0.00"
            @change="row.debitAmount = row.creditAmount ? 0 : row.debitAmount" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="60" align="center">
        <template #default="{ $index }">
          <el-button link type="danger" size="small"
            :disabled="formData.entries!.length <= 2" @click="formData.entries!.splice($index, 1)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-10px flex items-center justify-between">
      <el-button type="primary" plain size="small" @click="addEntry">
        <Icon icon="ep:plus" class="mr-5px" /> 添加分录
      </el-button>
      <div class="text-14px">
        <span class="mr-20px">借方合计：<b>¥ {{ entryDebitSum }}</b></span>
        <span class="mr-20px">贷方合计：<b>¥ {{ entryCreditSum }}</b></span>
        <span :class="balanced ? 'text-green-600' : 'text-red-500'">
          {{ balanced ? '√ 借贷平衡' : '× 借贷不平，差额 ¥ ' + diff }}
        </span>
      </div>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="formLoading" :disabled="!balanced" @click="submitForm">
        确 定
      </el-button>
    </template>
  </el-dialog>

  <!-- 详情弹窗（只读） -->
  <el-dialog v-model="detailVisible" :title="`凭证详情 ${detail.voucherNo || ''}`" width="880px">
    <el-descriptions :column="3" border size="small" class="mb-10px">
      <el-descriptions-item label="凭证日期">{{ detail.voucherDate }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <dict-tag :type="DICT_TYPE.BIZ_FMS_VOUCHER_STATUS" :value="detail.status ?? ''" />
      </el-descriptions-item>
      <el-descriptions-item label="摘要">{{ detail.summary || '—' }}</el-descriptions-item>
    </el-descriptions>
    <el-table :data="detail.entries || []" border size="small" show-summary :summary-method="getDetailSummary">
      <el-table-column label="#" type="index" width="45" align="center" />
      <el-table-column label="科目编码" prop="accountCode" width="110" align="center" />
      <el-table-column label="科目名称" prop="accountName" min-width="150" align="center" />
      <el-table-column label="分录摘要" prop="summary" min-width="150" align="center">
        <template #default="{ row }">{{ row.summary || '—' }}</template>
      </el-table-column>
      <el-table-column label="借方金额" prop="debitAmount" align="right" width="130">
        <template #default="{ row }">{{ formatAmount(row.debitAmount) }}</template>
      </el-table-column>
      <el-table-column label="贷方金额" prop="creditAmount" align="right" width="130">
        <template #default="{ row }">{{ formatAmount(row.creditAmount) }}</template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <!-- 科目余额弹窗 -->
  <el-dialog v-model="balanceVisible" title="科目余额表（仅已记账凭证）" width="720px">
    <el-table v-loading="balanceLoading" :data="balanceList" border size="small"
      show-summary :summary-method="getBalanceSummary">
      <el-table-column label="科目编码" prop="accountCode" width="110" align="center" />
      <el-table-column label="科目名称" prop="accountName" min-width="150" align="center" />
      <el-table-column label="借方发生额" prop="debitTotal" align="right" width="130">
        <template #default="{ row }">{{ formatAmount(row.debitTotal) }}</template>
      </el-table-column>
      <el-table-column label="贷方发生额" prop="creditTotal" align="right" width="130">
        <template #default="{ row }">{{ formatAmount(row.creditTotal) }}</template>
      </el-table-column>
      <el-table-column label="余额（借-贷）" prop="balance" align="right" width="140">
        <template #default="{ row }">
          <b :class="Number(row.balance) >= 0 ? 'text-blue-600' : 'text-orange-600'">
            {{ formatAmount(row.balance) }}
          </b>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="!balanceLoading && balanceList.length === 0" class="text-center text-gray-400 py-20px">
      暂无已记账凭证，记账后即可看到科目余额
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/fms/voucher'
import type { VoucherVO, VoucherEntryVO, AccountBalanceVO } from '@/api/fms/voucher'
import { getSimpleAccountList } from '@/api/fms/account'

defineOptions({ name: 'BizFmsVoucher' })

const message = useMessage()

const loading = ref(true)
const list = ref<VoucherVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  voucherNo: undefined,
  status: undefined,
  sourceType: undefined
})
const queryFormRef = ref()

const formatAmount = (v: number | string | undefined | null) =>
  Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getVoucherPage(queryParams)
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

/** 列表金额合计行 */
const getSummary = (param: any) => {
  const { columns, data } = param
  return columns.map((column: any, index: number) => {
    if (index === 0) return '合计'
    if (column.property === 'debitTotal' || column.property === 'creditTotal') {
      const sum = data.reduce((s: number, row: any) => s + Number(row[column.property] || 0), 0)
      return `¥ ${formatAmount(sum)}`
    }
    return ''
  })
}

/** 表单弹窗逻辑 */
const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const accountOptions = ref<{ id: number; code: string; name: string }[]>([])
const formData = ref<VoucherVO>({} as VoucherVO)
const formRules = reactive({
  voucherDate: [{ required: true, message: '凭证日期不能为空', trigger: 'change' }]
})

const emptyEntry = (): VoucherEntryVO => ({ accountId: undefined, summary: '', debitAmount: 0, creditAmount: 0 })

const addEntry = () => {
  formData.value.entries!.push(emptyEntry())
}

/** 借贷合计与平衡判断 */
const entryDebitSum = computed(() =>
  (formData.value.entries || []).reduce((s, e) => s + Number(e.debitAmount || 0), 0).toFixed(2))
const entryCreditSum = computed(() =>
  (formData.value.entries || []).reduce((s, e) => s + Number(e.creditAmount || 0), 0).toFixed(2))
const balanced = computed(() => {
  const d = Number(entryDebitSum.value)
  const c = Number(entryCreditSum.value)
  return d > 0 && d === c
})
const diff = computed(() => Math.abs(Number(entryDebitSum.value) - Number(entryCreditSum.value)).toFixed(2))

const openForm = async (id?: number) => {
  dialogVisible.value = true
  if (accountOptions.value.length === 0) {
    accountOptions.value = await getSimpleAccountList()
  }
  if (id) {
    formData.value = await Api.getVoucher(id)
  } else {
    formData.value = {
      voucherDate: new Date().toISOString().slice(0, 10),
      entries: [emptyEntry(), emptyEntry()]
    } as VoucherVO
  }
}

/** 校验并提交表单 */
const submitForm = async () => {
  await formRef.value.validate()
  const entries = formData.value.entries || []
  if (entries.some((e) => !e.accountId)) {
    message.error('存在未选择会计科目的分录行')
    return
  }
  if (entries.some((e) => Number(e.debitAmount || 0) > 0 && Number(e.creditAmount || 0) > 0)) {
    message.error('同一分录行不能同时填写借方和贷方金额')
    return
  }
  if (entries.some((e) => Number(e.debitAmount || 0) === 0 && Number(e.creditAmount || 0) === 0)) {
    message.error('存在借贷金额均为零的分录行')
    return
  }
  if (!balanced.value) {
    message.error('借贷方金额不相等，凭证不平')
    return
  }
  formLoading.value = true
  try {
    if (formData.value.id) {
      await Api.updateVoucher(formData.value)
      message.success('更新成功')
    } else {
      await Api.createVoucher(formData.value)
      message.success('创建成功，记账后计入科目余额')
    }
    dialogVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

/** 记账 / 取消记账 */
const handlePost = async (id: number) => {
  try {
    await message.confirm('确认记账？记账后凭证计入科目余额，如需修改请先取消记账')
    await Api.postVoucher(id)
    message.success('记账成功')
    await getList()
  } catch {}
}

const handleUnpost = async (id: number) => {
  try {
    await message.confirm('确认取消记账？凭证回到草稿状态，科目余额随之减少')
    await Api.unpostVoucher(id)
    message.success('已取消记账')
    await getList()
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteVoucher(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

/** 详情弹窗 */
const detailVisible = ref(false)
const detail = ref<VoucherVO>({})
const openDetail = async (id: number) => {
  detail.value = await Api.getVoucher(id)
  detailVisible.value = true
}
const getDetailSummary = (param: any) => {
  const { columns, data } = param
  return columns.map((column: any, index: number) => {
    if (index === 0) return '合计'
    if (column.property === 'debitAmount' || column.property === 'creditAmount') {
      const sum = data.reduce((s: number, row: any) => s + Number(row[column.property] || 0), 0)
      return formatAmount(sum)
    }
    return ''
  })
}

/** 科目余额弹窗 */
const balanceVisible = ref(false)
const balanceLoading = ref(false)
const balanceList = ref<AccountBalanceVO[]>([])
const openBalance = async () => {
  balanceVisible.value = true
  balanceLoading.value = true
  try {
    balanceList.value = await Api.getAccountBalance()
  } finally {
    balanceLoading.value = false
  }
}
const getBalanceSummary = (param: any) => {
  const { columns, data } = param
  return columns.map((column: any, index: number) => {
    if (index === 0) return '合计'
    if (['debitTotal', 'creditTotal'].includes(column.property)) {
      const sum = data.reduce((s: number, row: any) => s + Number(row[column.property] || 0), 0)
      return formatAmount(sum)
    }
    return ''
  })
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
