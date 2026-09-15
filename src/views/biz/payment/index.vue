<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="收付类型" prop="paymentType">
        <el-select v-model="queryParams.paymentType" placeholder="请选择收付类型" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_PAYMENT_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="单据编号" prop="orderCode">
        <el-input v-model="queryParams.orderCode" placeholder="请输入关联单据编号" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="对方名称" prop="partyName">
        <el-input v-model="queryParams.partyName" placeholder="请输入客户/供应商名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="收付日期" prop="paymentDateRange">
        <el-date-picker v-model="queryParams.paymentDateRange" value-format="YYYY-MM-DD"
          type="daterange" start-placeholder="开始日期" end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:payment:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 登记收付款
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:payment:query']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe show-summary :summary-method="getSummary">
      <el-table-column label="收付单号" align="center" prop="paymentNo" min-width="150" />
      <el-table-column label="收付类型" align="center" prop="paymentType" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.paymentType === '1' ? 'success' : 'warning'">
            {{ scope.row.paymentType === '1' ? '收款' : '付款' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关联单据" align="center" prop="orderCode" min-width="130" />
      <el-table-column label="单据类型" align="center" prop="bizType" width="90">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_PAYMENT_TYPE" :value="scope.row.paymentType" />
          <span class="text-12px text-gray-400">{{ scope.row.bizType === '1' ? '（销售）' : '（采购）' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="对方名称" align="center" prop="partyName" min-width="130" />
      <el-table-column label="金额" align="center" prop="amount" min-width="110">
        <template #default="scope">
          <span class="font-600">¥ {{ scope.row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="收付方式" align="center" prop="paymentMethod" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_PAYMENT_METHOD" :value="scope.row.paymentMethod" />
        </template>
      </el-table-column>
      <el-table-column label="收付日期" align="center" prop="paymentDate" min-width="110" />
      <el-table-column label="原流水ID" prop="reversalOfId" width="110" />
      <el-table-column label="退货单ID" prop="sourceReturnId" width="110" />
      <el-table-column label="备注" align="center" prop="remark" min-width="110" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="100" fixed="right">
        <template #default="scope">
          <el-button link type="danger" v-if="Number(scope.row.amount) > 0 && !scope.row.reversalOfId" @click="handleReverse(scope.row.id)" v-hasPermi="['biz:payment:reverse']">冲销</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" title="登记收付款" width="640px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="收付类型" prop="paymentType">
        <el-radio-group v-model="formData.paymentType" @change="handleTypeChange">
          <el-radio label="1">收款（销售单）</el-radio>
          <el-radio label="2">付款（采购单）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="关联单据" prop="orderId">
        <el-select v-model="formData.orderId" placeholder="请选择已完成单据（挂合同回款时可留空）" filterable clearable class="!w-1/1"
          :loading="orderLoading" @change="handleOrderChange">
          <el-option v-for="o in orderOptions" :key="o.id" :label="o.label" :value="o.id" />
        </el-select>
        <div v-if="!orderLoading && orderOptions.length === 0 && !formData.contractId" class="text-12px text-orange-500">
          暂无已完成的{{ formData.paymentType === '1' ? '销售单' : '采购单' }}，可改为挂合同登记回款
        </div>
      </el-form-item>
      <el-form-item v-if="formData.paymentType === '1'" label="关联合同" prop="contractId">
        <el-select v-model="formData.contractId" placeholder="可选：挂到合同拉通回款进度" filterable clearable
          class="!w-1/1" :loading="contractLoading" @change="handleContractChange">
          <el-option v-for="c in contractOptions" :key="c.id"
            :label="`${c.contractCode}｜${c.customerName}｜¥${c.amount}`" :value="c.id!" />
        </el-select>
        <div class="text-12px text-gray-400">单据与合同二选一；挂合同后可在合同管理查看回款进度</div>
      </el-form-item>
      <el-form-item label="对方名称" prop="partyName">
        <el-input v-model="formData.partyName" disabled placeholder="选择单据后自动带出" />
      </el-form-item>
      <el-form-item label="金额" prop="amount">
        <el-input-number v-model="formData.amount" :min="0.01" :precision="2" :step="100" class="!w-1/1" />
        <div v-if="selectedTotal != null" class="text-12px text-gray-400">
          单据总额：¥{{ selectedTotal }}，已收付：¥{{ paidSum ?? 0 }}
        </div>
      </el-form-item>
      <el-form-item label="收付方式" prop="paymentMethod">
        <el-select v-model="formData.paymentMethod" placeholder="请选择收付方式" class="!w-1/1">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_PAYMENT_METHOD)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="收付日期" prop="paymentDate">
        <el-date-picker v-model="formData.paymentDate" value-format="YYYY-MM-DD" type="date"
          placeholder="请选择收付日期" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="formLoading" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/payment'
import type { PaymentVO } from '@/api/biz/payment'
import { getSalesPage, getSales } from '@/api/biz/sales'
import { getPurchasePage, getPurchase } from '@/api/biz/purchase'

defineOptions({ name: 'BizPayment' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.PaymentVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  paymentType: undefined,
  bizType: undefined,
  orderCode: undefined,
  partyName: undefined,
  paymentDateRange: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getPaymentPage(queryParams)
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

/** 金额合计行 */
const getSummary = (param: any) => {
  const { columns, data } = param
  return columns.map((column: any, index: number) => {
    if (index === 0) return '合计'
    if (column.property === 'amount') {
      const sum = data.reduce((s: number, row: any) => s + Number(row.amount || 0), 0)
      return `¥ ${sum.toFixed(2)}`
    }
    return ''
  })
}

/** 表单弹窗逻辑 */
const dialogVisible = ref(false)
const formLoading = ref(false)
const orderLoading = ref(false)
const formRef = ref()
const orderOptions = ref<{ id: number; label: string }[]>([])
const selectedTotal = ref<number | null>(null)
const paidSum = ref<number | null>(null)
const formData = ref<PaymentVO>({} as PaymentVO)
const formRules = reactive({
  paymentType: [{ required: true, message: "收付类型不能为空", trigger: "change" }],
  orderId: [{
    validator: (_rule: any, value: any, callback: any) => {
      // 单据与合同二选一
      if (!value && !formData.value.contractId) callback(new Error('请选择单据，或改挂合同'))
      else callback()
    },
    trigger: "change"
  }],
  amount: [{ required: true, message: "金额不能为空", trigger: "blur" }],
  paymentDate: [{ required: true, message: "收付日期不能为空", trigger: "change" }]
})

const openForm = async () => {
  dialogVisible.value = true
  formData.value = { requestId: `pay_${Date.now()}_${Math.random().toString(36).slice(2)}`, paymentType: '1', paymentDate: new Date().toISOString().slice(0, 10) } as PaymentVO
  orderOptions.value = []
  selectedTotal.value = null
  paidSum.value = null
  await Promise.all([loadOrderOptions('1'), loadContractOptions()])
}

/** 收付类型切换：联动加载对应的已完成单据 */
const handleTypeChange = async (type: string) => {
  formData.value.orderId = undefined
  formData.value.contractId = undefined
  formData.value.partyName = undefined
  orderOptions.value = []
  selectedTotal.value = null
  paidSum.value = null
  await loadOrderOptions(type)
}

const loadOrderOptions = async (paymentType: string) => {
  orderLoading.value = true
  try {
    if (paymentType === '1') {
      const data = await getSalesPage({ pageNo: 1, pageSize: 100, status: '2' })
      orderOptions.value = data.list.map((o: any) => ({
        id: o.id,
        label: `${o.salesCode}｜${o.customerName}｜¥${o.totalAmount}`
      }))
    } else {
      const data = await getPurchasePage({ pageNo: 1, pageSize: 100, status: '2' })
      orderOptions.value = data.list.map((o: any) => ({
        id: o.id,
        label: `${o.purchaseCode}｜${o.supplierName}｜¥${o.totalAmount}`
      }))
    }
  } finally {
    orderLoading.value = false
  }
}

/** 收款时可选拉通合同 */
const contractOptions = ref<any[]>([])
const contractLoading = ref(false)
const loadContractOptions = async () => {
  contractLoading.value = true
  try {
    const { getContractPage } = await import('@/api/biz/contract')
    const data = await getContractPage({ pageNo: 1, pageSize: 100 })
    contractOptions.value = data.list
  } finally {
    contractLoading.value = false
  }
}

/** 选了合同：清空单据选择，带出合同客户 */
const handleContractChange = async (contractId: number | undefined) => {
  if (!contractId) return
  formData.value.orderId = undefined
  formData.value.partyName = undefined
  selectedTotal.value = null
  paidSum.value = null
  if (!contractOptions.value.length) await loadContractOptions()
  const c = contractOptions.value.find((x) => x.id === contractId)
  if (c) formData.value.partyName = c.customerName
}

/** 收付类型切换：联动加载对应的已完成单据 */

/** 选择单据：带出对方名称与已收付金额 */
const handleOrderChange = async (orderId: number) => {
  const opt = orderOptions.value.find((o) => o.id === orderId)
  if (!opt) return
  formData.value.contractId = undefined
  const bizType = formData.value.paymentType === '1' ? '1' : '2'
  if (bizType === '1') {
    const sales = await getSales(orderId)
    formData.value.partyName = sales.customerName
    selectedTotal.value = Number(sales.totalAmount)
  } else {
    const purchase = await getPurchase(orderId)
    formData.value.partyName = purchase.supplierName
    selectedTotal.value = Number(purchase.totalAmount)
  }
  paidSum.value = Number(await Api.getPaidSumByOrder(bizType, orderId))
}

/** 冲销保留原流水，另记一条反向金额。 */
const handleReverse = async (id: number) => {
  try {
    const { value } = await ElMessageBox.prompt('将保留原流水并生成反向流水，请填写冲销原因。', '冲销收付款', {
      inputValidator: (value: string) => !!value?.trim() && value.trim().length <= 200 || '请输入200字以内的原因'
    })
    await Api.reversePayment(id, value.trim())
    message.success('冲销成功，原流水已保留')
    await getList()
  } catch {}
}

/** 提交表单 */
const submitForm = async () => {
  if (formLoading.value) return
  await formRef.value.validate()
  formLoading.value = true
  try {
    formData.value.bizType = formData.value.paymentType === '1' ? '1' : '2'
    await Api.createPayment(formData.value as unknown as PaymentVO)
    message.success('登记成功')
    dialogVisible.value = false
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
    const data = await Api.exportPayment(queryParams)
    download.excel(data, '收付款流水.xls')
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
