<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_CHECK_STATUS)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点日期" prop="checkDateRange">
        <el-date-picker v-model="queryParams.checkDateRange" value-format="YYYY-MM-DD"
          type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:stockcheck:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 发起盘点
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:stockcheck:query']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="盘点单号" align="center" prop="checkNo" min-width="150" />
      <el-table-column label="产品名称" align="center" prop="productName" min-width="130" />
      <el-table-column label="仓库" align="center" prop="warehouse" min-width="90" />
      <el-table-column label="账面数量" align="center" prop="bookQuantity" min-width="90" />
      <el-table-column label="实盘数量" align="center" prop="actualQuantity" min-width="90" />
      <el-table-column label="差异" align="center" prop="diffQuantity" min-width="90">
        <template #default="scope">
          <span v-if="scope.row.status !== '1'">—</span>
          <el-tag v-else :type="(scope.row.diffQuantity || 0) === 0 ? 'info' : ((scope.row.diffQuantity || 0) > 0 ? 'success' : 'danger')">
            {{ scope.row.diffQuantity > 0 ? '+' : '' }}{{ scope.row.diffQuantity }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_CHECK_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="盘点日期" align="center" prop="checkDate" min-width="110" />
      <el-table-column label="备注" align="center" prop="remark" min-width="110" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" v-if="scope.row.status === '0'"
            @click="handleConfirm(scope.row)" v-hasPermi="['biz:stockcheck:confirm']">确认</el-button>
          <el-button link type="danger" v-if="scope.row.status === '0'"
            @click="handleDelete(scope.row.id)" v-hasPermi="['biz:stockcheck:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" title="发起盘点" width="560px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="产品" prop="productName">
        <el-select v-model="selectedStockId" placeholder="请选择库存产品" filterable class="!w-1/1"
          :loading="stockLoading" @change="handleProductChange">
          <el-option v-for="s in stockOptions" :key="s.id"
            :label="`${s.productName}｜${s.warehouse}｜#${s.productId}｜当前库存 ${s.quantity}`" :value="s.id" />
        </el-select>
        <div v-if="!stockLoading && stockOptions.length === 0" class="text-12px text-orange-500">
          暂无库存记录，请先通过采购入库建立库存
        </div>
      </el-form-item>
      <el-form-item label="当前库存" prop="warehouse">
        <el-input :model-value="bookQuantity == null ? '—' : String(bookQuantity)" disabled>
          <template #append>账面参考（确认时以届时库存为准）</template>
        </el-input>
      </el-form-item>
      <el-form-item label="实盘数量" prop="actualQuantity">
        <el-input-number v-model="formData.actualQuantity" :min="0" :step="1" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="盘点日期" prop="checkDate">
        <el-date-picker v-model="formData.checkDate" value-format="YYYY-MM-DD" type="date"
          placeholder="请选择盘点日期" class="!w-1/1" />
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
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/stockcheck'
import type { StockCheckVO } from '@/api/biz/stockcheck'
import { getStockPage } from '@/api/biz/stock'

defineOptions({ name: 'BizStockCheck' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.StockCheckVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productName: undefined,
  status: undefined,
  checkDateRange: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getStockCheckPage(queryParams)
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

/** 确认盘点：按实盘调整库存 */
const handleConfirm = async (row: Api.StockCheckVO) => {
  await message.confirm(
    `确认按实盘 ${row.actualQuantity} 调整「${row.productName}」库存吗？将按差异写库存流水，且不可撤销。`
  )
  await Api.confirmStockCheck(row.id!)
  message.success('盘点已确认，库存已按实盘调整')
  await getList()
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteStockCheck(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

/** 表单弹窗逻辑 */
const dialogVisible = ref(false)
const formLoading = ref(false)
const stockLoading = ref(false)
const formRef = ref()
const selectedStockId = ref<number>()
const stockOptions = ref<{ id: number; productId: number; warehouseId: number; warehouse: string; productName: string; quantity: number }[]>([])
const bookQuantity = ref<number | null>(null)
const formData = ref<StockCheckVO>({} as StockCheckVO)
const formRules = reactive({
  productName: [{ required: true, message: "产品不能为空", trigger: "change" }],
  actualQuantity: [{ required: true, message: "实盘数量不能为空", trigger: "blur" }],
  checkDate: [{ required: true, message: "盘点日期不能为空", trigger: "change" }]
})

const openForm = async () => {
  selectedStockId.value = undefined
  dialogVisible.value = true
  formData.value = { checkDate: new Date().toISOString().slice(0, 10) } as StockCheckVO
  bookQuantity.value = null
  await loadStockOptions()
}

const loadStockOptions = async () => {
  stockLoading.value = true
  try {
    const data = await getStockPage({ pageNo: 1, pageSize: 100 })
    stockOptions.value = data.list
  } finally {
    stockLoading.value = false
  }
}

const handleProductChange = (stockId: number) => {
  const stock = stockOptions.value.find((s) => s.id === stockId)
  if (stock) {
    formData.value.productId = stock.productId
    formData.value.warehouseId = stock.warehouseId
    formData.value.productName = stock.productName
    formData.value.warehouse = stock.warehouse
  }
  bookQuantity.value = stock ? Number(stock.quantity) : null
}

/** 提交表单 */
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    await Api.createStockCheck(formData.value as unknown as StockCheckVO)
    message.success('盘点单已创建，确认后库存将按实盘调整')
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
    const data = await Api.exportStockCheck(queryParams)
    download.excel(data, '库存盘点.xls')
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
