<template>
  <ContentWrap>
    <el-button v-hasPermi="['biz:stock:create']" class="mb-3" @click="createWarehouse">新增仓库</el-button>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['biz:stock:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:stock:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button type="warning" plain @click="openInTransit">
          <Icon icon="ep:van" class="mr-5px" /> 在途库存
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="产品名称" align="center" prop="productName" min-width="110" />
      <el-table-column label="仓库" align="center" prop="warehouse" min-width="110" />
      <el-table-column label="库存数量" align="right" prop="quantity" min-width="100" />
      <el-table-column label="预警下限" align="right" prop="minQuantity" min-width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['biz:stock:update']">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 在途库存弹窗 -->
  <el-dialog v-model="transitVisible" title="在途库存（已确认未入库的采购单）" width="640px">
    <el-table v-loading="transitLoading" :data="transitList" stripe size="small">
      <el-table-column label="产品" align="center" prop="productName" min-width="150" show-overflow-tooltip />
      <el-table-column label="仓库" align="center" prop="warehouse" min-width="110" />
      <el-table-column label="在途数量" align="right" prop="inTransitQuantity" min-width="100">
        <template #default="scope"><b class="text-orange-600">{{ scope.row.inTransitQuantity }}</b></template>
      </el-table-column>
    </el-table>
    <div v-if="!transitLoading && transitList.length === 0" class="text-center text-gray-400 py-20px">
      暂无在途采购，确认采购单后自动出现在这里
    </div>
  </el-dialog>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="产品名称" prop="productName">
          <BizReferenceSelect v-model="formData.productId" v-model:name="formData.productName" kind="products" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="仓库" prop="warehouse">
          <BizReferenceSelect v-model="formData.warehouseId" v-model:name="formData.warehouse" kind="warehouses" :disabled="!!formData.id" />
        </el-form-item>
        <div class="text-gray-500 mb-3">已有库存数量通过盘点调整；新增仓库请先在仓库资料中登记。</div>
        <el-form-item label="库存数量" prop="quantity">
          <el-input-number v-model="formData.quantity" :disabled="!!formData.id" :precision="0" :min="0" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="预警下限" prop="minQuantity">
          <el-input-number v-model="formData.minQuantity" :precision="0" :min="0" class="!w-1/1" />
        </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import request from '@/config/axios'
import BizReferenceSelect from '@/components/BizReferenceSelect/index.vue'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import * as Api from '@/api/biz/stock'
import { getInTransit } from '@/api/biz/purchase'

defineOptions({ name: 'Stock' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.StockVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productName: undefined,
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getStockPage(queryParams)
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


/** 表单弹窗逻辑 */
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formType = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<Api.StockVO>({} as Api.StockVO)
const formRules = reactive({
  productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }],
})
const openForm = (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增库存' : '修改库存'
  formType.value = type
  if (id) {
    formLoading.value = true
    Api.getStock(id).then((data) => {
      formData.value = data
    }).finally(() => { formLoading.value = false })
  } else {
    formData.value = {} as Api.StockVO
  }
}

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await Api.createStock(formData.value as unknown as Api.StockVO)
      message.success('新增成功')
    } else {
      await Api.updateStock(formData.value as unknown as Api.StockVO)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
    await getList()
  } finally {
    formLoading.value = false
  }
}

/** 导出按钮操作 */
const transitVisible = ref(false)
const transitLoading = ref(false)
const transitList = ref<any[]>([])
const openInTransit = async () => {
  transitVisible.value = true
  transitLoading.value = true
  try {
    transitList.value = await getInTransit()
  } finally {
    transitLoading.value = false
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await Api.exportStock(queryParams)
    download.excel(data, '库存.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}
const createWarehouse = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入仓库名称', '新增仓库', {
      inputValidator: (value: string) => !!value?.trim() && value.trim().length <= 50 || '请输入1至50字的名称'
    })
    await request.post({ url: '/biz/reference/warehouses', params: { name: value.trim() } })
    message.success('仓库已创建，重新打开表单即可选择')
  } catch {}
}
/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
