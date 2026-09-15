<template>
  <!-- 漏斗统计条 -->
  <ContentWrap>
    <el-row :gutter="12" v-if="funnel.length">
      <el-col :span="8">
        <Echart :options="funnelOptions" :height="230" />
      </el-col>
      <el-col :span="16">
        <el-row :gutter="10">
          <el-col :span="8" v-for="s in funnel" :key="s.stage">
            <div class="text-center py-6px mb-10px" :class="stageCardClass(s.stage)">
              <div class="text-13px text-gray-500">{{ s.stageName }}</div>
              <div class="text-18px font-700 mt-2px">{{ s.count }} 单</div>
              <div class="text-12px text-gray-400">¥ {{ Number(s.totalAmount || 0).toLocaleString() }}</div>
            </div>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </ContentWrap>

  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="商机名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入商机名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="客户" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="阶段" prop="stage">
        <el-select v-model="queryParams.stage" placeholder="请选择阶段" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_BUSINESS_STAGE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:business:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新建商机
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:business:query']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe show-summary :summary-method="getSummary">
      <el-table-column label="商机名称" align="center" prop="name" min-width="160" show-overflow-tooltip />
      <el-table-column label="客户" align="center" prop="customerName" min-width="140" show-overflow-tooltip />
      <el-table-column label="阶段" align="center" prop="stage" width="100">
        <template #default="scope">
          <el-tag :type="stageType(scope.row.stage)">{{ stageText(scope.row.stage) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预期金额" align="center" prop="amount" min-width="120">
        <template #default="scope">
          <span class="font-600">¥ {{ scope.row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="预计成交" align="center" prop="expectedDate" min-width="110" />
      <el-table-column label="负责人" align="center" prop="ownerName" width="100" />
      <el-table-column label="备注" align="center" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.stage !== '5' && scope.row.stage !== '6'">
            <el-button link type="primary" @click="openForm(scope.row)"
              v-hasPermi="['biz:business:update']">编辑</el-button>
            <el-button link type="success" @click="quickWin(scope.row)"
              v-hasPermi="['biz:business:update']">标记赢单</el-button>
          </template>
          <template v-else-if="scope.row.stage === '5'">
            <el-button link type="primary" @click="openConvert(scope.row)"
              v-hasPermi="['biz:business:update']">转合同</el-button>
          </template>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['biz:business:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 转合同弹窗 -->
  <el-dialog v-model="convertDialogVisible" title="赢单商机转合同" width="520px">
    <el-alert type="info" :closable="false" class="mb-15px" show-icon
      title="自动带入客户、金额与负责人，合同编号自动生成，状态为「执行中」" />
    <el-form ref="convertFormRef" :model="convertData" :rules="convertRules" label-width="100px">
      <el-form-item label="商机">
        <el-input :model-value="`${convertRow?.name}｜${convertRow?.customerName}｜¥${convertRow?.amount}`" disabled />
      </el-form-item>
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="convertData.productName" placeholder="合同对应的产品/服务" />
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker v-model="convertData.startDate" value-format="YYYY-MM-DD" type="date" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker v-model="convertData.endDate" value-format="YYYY-MM-DD" type="date" class="!w-1/1" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="convertDialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="convertLoading" @click="submitConvert">生成合同</el-button>
    </template>
  </el-dialog>

  <!-- 新建/编辑弹窗 -->
  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑商机' : '新建商机'" width="560px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="商机名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入商机名称" />
      </el-form-item>
      <el-form-item label="关联客户" prop="customerName">
        <el-select v-model="formData.customerName" placeholder="请选择客户" filterable
          class="!w-1/1" :disabled="!!formData.id" :loading="customerLoading">
          <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.customerName" />
        </el-select>
        <div v-if="!formData.id && formData.customerName" class="text-12px text-orange-500">
          客户不在列表？先到 客户合同产品 或 线索转商机 中创建
        </div>
      </el-form-item>
      <el-form-item label="阶段" prop="stage">
        <el-select v-model="formData.stage" class="!w-1/1">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_BUSINESS_STAGE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
        <div class="text-12px text-gray-400">赢单/输单为终局，置入后不能再修改</div>
      </el-form-item>
      <el-form-item label="预期金额" prop="amount">
        <el-input-number v-model="formData.amount" :min="0" :precision="2" :step="1000" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="预计成交" prop="expectedDate">
        <el-date-picker v-model="formData.expectedDate" value-format="YYYY-MM-DD" type="date"
          placeholder="请选择预计成交日期" class="!w-1/1" />
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
import * as Api from '@/api/biz/business'
import type { BusinessVO, FunnelStatVO } from '@/api/biz/business'
import { getCustomerPage } from '@/api/biz/customer'
import { Echart } from '@/components/Echart'

defineOptions({ name: 'BizBusiness' })

const message = useMessage()

/** 漏斗统计 */
const funnel = ref<FunnelStatVO[]>([])
const loadFunnel = async () => {
  funnel.value = await Api.getFunnelStats()
}

/** 漏斗图（保持阶段顺序，初步接触在最上） */
const funnelOptions = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: (p: any) => {
      const s = funnel.value.find((f) => f.stageName === p.name)
      return s ? `${s.stageName}：${s.count} 单 / ¥${Number(s.totalAmount || 0).toLocaleString()}` : p.name
    }
  },
  series: [
    {
      name: '销售漏斗',
      type: 'funnel',
      left: '12%',
      width: '76%',
      top: 10,
      bottom: 10,
      sort: 'none',
      gap: 2,
      minSize: '18%',
      label: { show: true, position: 'inside', formatter: '{b}  {c} 单' },
      itemStyle: { borderWidth: 0 },
      data: funnel.value.map((f) => ({ name: f.stageName, value: f.count }))
    }
  ]
}))

const stageText = (s?: string) =>
  funnel.value.find((f) => f.stage === s)?.stageName ||
  ({ '1': '初步接触', '2': '需求确认', '3': '方案报价', '4': '谈判协商', '5': '赢单', '6': '输单' } as any)[s || ''] || s
const stageType = (s?: string) =>
  (({ '1': 'info', '2': 'primary', '3': 'primary', '4': 'warning', '5': 'success', '6': 'danger' }) as any)[s || ''] || 'info'
const stageCardClass = (s: string) => (s === '5' ? 'bg-green-50 rounded' : s === '6' ? 'bg-gray-50 rounded' : 'bg-blue-50 rounded')

const loading = ref(true)
const list = ref<Api.BusinessVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  customerName: undefined,
  stage: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getBusinessPage(queryParams)
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

/** 客户下拉（新建时选） */
const customerOptions = ref<any[]>([])
const customerLoading = ref(false)
const loadCustomers = async () => {
  customerLoading.value = true
  try {
    const data = await getCustomerPage({ pageNo: 1, pageSize: 100 })
    customerOptions.value = data.list
  } finally {
    customerLoading.value = false
  }
}

/** 新建/编辑弹窗 */
const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const formData = ref<BusinessVO>({} as BusinessVO)
const formRules = reactive({
  name: [{ required: true, message: '商机名称不能为空', trigger: 'blur' }],
  customerName: [{ required: true, message: '关联客户不能为空', trigger: 'change' }],
  stage: [{ required: true, message: '商机阶段不能为空', trigger: 'change' }]
})

const openForm = async (row?: BusinessVO) => {
  dialogVisible.value = true
  formData.value = row ? { ...row } : ({ stage: '1' } as BusinessVO)
  if (!customerOptions.value.length) {
    await loadCustomers()
  }
}

/** 一键标记赢单 */
const quickWin = async (row: BusinessVO) => {
  try {
    await message.confirm(`确认将商机「${row.name}」标记为赢单？赢单后不可再修改`)
    await Api.updateBusiness({ ...row, stage: '5' } as BusinessVO)
    message.success('已标记赢单 🎉 可点击「转合同」快速生成合同')
    await Promise.all([getList(), loadFunnel()])
  } catch {}
}

/** 赢单转合同 */
const convertDialogVisible = ref(false)
const convertLoading = ref(false)
const convertFormRef = ref()
const convertRow = ref<BusinessVO>()
const convertData = ref<any>({})
const convertRules = reactive({
  productName: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }]
})

const openConvert = (row: BusinessVO) => {
  convertRow.value = row
  const today = new Date().toISOString().slice(0, 10)
  const nextYear = new Date(Date.now() + 365 * 86400000).toISOString().slice(0, 10)
  convertData.value = { productName: row.productName || '', startDate: today, endDate: nextYear }
  convertDialogVisible.value = true
}

const submitConvert = async () => {
  await convertFormRef.value.validate()
  convertLoading.value = true
  try {
    await Api.convertToContract(convertRow.value!.id!, convertData.value)
    message.success('合同已生成，可在 合同管理 中查看')
    convertDialogVisible.value = false
  } finally {
    convertLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteBusiness(id)
    message.success('删除成功')
    await Promise.all([getList(), loadFunnel()])
  } catch {}
}

const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formData.value.id) {
      await Api.updateBusiness(formData.value as unknown as BusinessVO)
      message.success('更新成功')
    } else {
      await Api.createBusiness(formData.value as unknown as BusinessVO)
      message.success('创建成功')
    }
    dialogVisible.value = false
    await Promise.all([getList(), loadFunnel()])
  } finally {
    formLoading.value = false
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await Api.exportBusiness(queryParams)
    download.excel(data, '商机.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([getList(), loadFunnel()])
})
</script>
