<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="退货类型" prop="returnType">
        <el-select v-model="queryParams.returnType" placeholder="请选择退货类型" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_RETURN_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_RETURN_STATUS)"
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
      <el-form-item label="退货日期" prop="returnDateRange">
        <el-date-picker v-model="queryParams.returnDateRange" value-format="YYYY-MM-DD"
          type="daterange" start-placeholder="开始日期" end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:return:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新建退货
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:return:query']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe show-summary :summary-method="getSummary">
      <el-table-column label="退货单号" align="center" prop="returnNo" min-width="150" />
      <el-table-column label="退货类型" align="center" prop="returnType" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.returnType === '1' ? 'success' : 'warning'">
            {{ scope.row.returnType === '1' ? '销售退货' : '采购退货' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关联单据" align="center" prop="orderCode" min-width="130" />
      <el-table-column label="对方名称" align="center" prop="partyName" min-width="130" />
      <el-table-column label="产品" align="center" prop="productName" min-width="120" show-overflow-tooltip />
      <el-table-column label="仓库" align="center" prop="warehouse" width="100" />
      <el-table-column label="退货数量" align="center" prop="quantity" width="90" />
      <el-table-column label="总金额" align="center" prop="totalAmount" min-width="110">
        <template #default="scope">
          <span class="font-600">¥ {{ scope.row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退货日期" align="center" prop="returnDate" min-width="110" />
      <el-table-column label="退货原因" align="center" prop="reason" min-width="120" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : scope.row.status === '3' ? 'info' : 'warning'">
            {{ statusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === '0'">
            <el-button link type="primary" @click="handleExecute(scope.row.id)"
              v-hasPermi="['biz:return:update']">执行退货</el-button>
            <el-button link type="warning" @click="openForm(scope.row)"
              v-hasPermi="['biz:return:update']">编辑</el-button>
            <el-button link type="info" @click="handleVoid(scope.row.id)"
              v-hasPermi="['biz:return:update']">作废</el-button>
          </template>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['biz:return:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑退货单' : '新建退货'" width="640px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="退货类型" prop="returnType">
        <el-radio-group v-model="formData.returnType" :disabled="!!formData.id" @change="handleTypeChange">
          <el-radio label="1">销售退货（入库）</el-radio>
          <el-radio label="2">采购退货（出库）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="关联单据" prop="orderId">
        <el-select v-model="formData.orderId" placeholder="请选择已完成单据" filterable class="!w-1/1"
          :disabled="!!formData.id" :loading="orderLoading" @change="handleOrderChange">
          <el-option v-for="o in orderOptions" :key="o.id" :label="o.label" :value="o.id" />
        </el-select>
        <div v-if="!orderLoading && !formData.id && orderOptions.length === 0" class="text-12px text-orange-500">
          暂无已完成的{{ formData.returnType === '1' ? '销售单' : '采购单' }}，请先在采购/销售管理中完成单据流转
        </div>
      </el-form-item>
      <el-form-item label="对方名称" prop="partyName">
        <el-input v-model="formData.partyName" disabled placeholder="选择单据后自动带出" />
      </el-form-item>
      <el-form-item label="产品" prop="productName">
        <el-input v-model="formData.productName" disabled placeholder="选择单据后自动带出" />
      </el-form-item>
      <el-form-item label="退货数量" prop="quantity">
        <el-input-number v-model="formData.quantity" :min="1" :precision="0" class="!w-1/1" />
        <div v-if="orderQuantity != null" class="text-12px text-gray-400">
          原单数量：{{ orderQuantity }}，已退：{{ returnedSum ?? 0 }}，可退：{{ (orderQuantity ?? 0) - (returnedSum ?? 0) }}
        </div>
      </el-form-item>
      <el-form-item label="退货单价" prop="price">
        <el-input-number v-model="formData.price" :min="0" :precision="2" :step="10" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="退货日期" prop="returnDate">
        <el-date-picker v-model="formData.returnDate" value-format="YYYY-MM-DD" type="date"
          placeholder="请选择退货日期" class="!w-1/1" />
      </el-form-item>
      <el-form-item v-if="!formData.id" label="执行方式">
        <el-switch v-model="executeNow" active-text="创建后立即执行（库存联动）"
          inactive-text="仅创建，稍后执行" />
      </el-form-item>
      <el-form-item label="退货原因" prop="reason">
        <el-input v-model="formData.reason" type="textarea" :rows="2" placeholder="请输入退货原因" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="formLoading" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/return'
import type { ReturnVO } from '@/api/biz/return'
import { getSalesPage, getSales } from '@/api/biz/sales'
import { getPurchasePage, getPurchase } from '@/api/biz/purchase'

defineOptions({ name: 'BizReturn' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.ReturnVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  returnType: undefined,
  status: undefined,
  orderCode: undefined,
  partyName: undefined,
  returnDateRange: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

const statusLabel = (status: string) => {
  if (status === '0') return '待退货'
  if (status === '1') return '已退货'
  return '已作废'
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getReturnPage(queryParams)
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
    if (column.property === 'totalAmount') {
      const sum = data.reduce((s: number, row: any) => s + Number(row.totalAmount || 0), 0)
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
const orderQuantity = ref<number | null>(null)
const returnedSum = ref<number | null>(null)
const formData = ref<ReturnVO>({} as ReturnVO)
const formRules = reactive({
  returnType: [{ required: true, message: "退货类型不能为空", trigger: "change" }],
  orderId: [{ required: true, message: "关联单据不能为空", trigger: "change" }],
  quantity: [{ required: true, message: "退货数量不能为空", trigger: "blur" }],
  returnDate: [{ required: true, message: "退货日期不能为空", trigger: "change" }],
  reason: [{ required: true, message: "退货原因不能为空", trigger: "blur" }]
})

const openForm = async (row?: ReturnVO) => {
  dialogVisible.value = true
  orderOptions.value = []
  orderQuantity.value = null
  returnedSum.value = null
  if (row) {
    // 编辑：只允许改数量/单价/日期/原因等
    formData.value = { ...row }
    orderQuantity.value = null
  } else {
    formData.value = { returnType: '1', returnDate: new Date().toISOString().slice(0, 10) } as ReturnVO
    await loadOrderOptions('1')
  }
}

/** 退货类型切换：联动加载对应的已完成单据 */
const handleTypeChange = async (type: string) => {
  formData.value.orderId = undefined
  formData.value.partyName = undefined
  formData.value.productName = undefined
  orderOptions.value = []
  orderQuantity.value = null
  returnedSum.value = null
  await loadOrderOptions(type)
}

const loadOrderOptions = async (returnType: string) => {
  orderLoading.value = true
  try {
    if (returnType === '1') {
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

/** 选择单据：带出对方/产品/原单数量与已退数量 */
const handleOrderChange = async (orderId: number) => {
  const returnType = formData.value.returnType === '1' ? '1' : '2'
  if (returnType === '1') {
    const sales = await getSales(orderId)
    formData.value.partyName = sales.customerName
    formData.value.productName = sales.productName
    formData.value.price = Number(sales.price)
    orderQuantity.value = Number(sales.quantity)
  } else {
    const purchase = await getPurchase(orderId)
    formData.value.partyName = purchase.supplierName
    formData.value.productName = purchase.productName
    formData.value.price = Number(purchase.price)
    orderQuantity.value = Number(purchase.quantity)
  }
  returnedSum.value = Number(await Api.getReturnedSumByOrder(returnType, orderId))
}

/** 执行退货：联动库存 */
const handleExecute = async (id: number) => {
  try {
    await message.confirm('确认执行退货？销售退货将入库、采购退货将出库，库存随之变动')
    await Api.executeReturn(id)
    message.success('退货已执行')
    await getList()
  } catch {}
}

/** 作废退货单 */
const handleVoid = async (id: number) => {
  try {
    await message.confirm('确认作废该退货单？')
    await Api.voidReturn(id)
    message.success('已作废')
    await getList()
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteReturn(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

/** 提交表单 */
const executeNow = ref(true)
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (!formData.value.id && executeNow.value) {
      await Api.createAndExecuteReturn(formData.value as unknown as ReturnVO)
      message.success('已创建并执行退货')
    } else if (formData.value.id) {
      await Api.updateReturn(formData.value as unknown as ReturnVO)
      message.success('更新成功')
    } else {
      await Api.createReturn(formData.value as unknown as ReturnVO)
      message.success('创建成功，请执行退货以联动库存')
    }
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
    const data = await Api.exportReturn(queryParams)
    download.excel(data, '退货单.xls')
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
