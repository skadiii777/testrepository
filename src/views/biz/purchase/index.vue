<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="采购单号" prop="purchaseCode">
        <el-input v-model="queryParams.purchaseCode" placeholder="请输入采购单号" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="采购日期" prop="purchaseDate">
        <el-date-picker v-model="queryParams.purchaseDate" value-format="YYYY-MM-DD" type="date"
          placeholder="请选择采购日期" clearable class="!w-240px" />
      </el-form-item>
      <el-form-item label="入库状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择入库状态" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_INOUT_STATUS)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['biz:purchase:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:purchase:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="采购单号" align="center" prop="purchaseCode" min-width="110" />
      <el-table-column label="供应商" align="center" prop="supplierName" min-width="110" />
      <el-table-column label="产品名称" align="center" prop="productName" min-width="110" />
      <el-table-column label="采购数量" align="right" prop="quantity" min-width="100" />
      <el-table-column label="采购单价" align="right" prop="price" min-width="100" />
      <el-table-column label="总金额" align="right" prop="totalAmount" min-width="100" />
      <el-table-column label="采购日期" align="center" prop="purchaseDate" min-width="110" />
      <el-table-column label="入库状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_INOUT_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === '0'">
            <el-button link type="success" @click="handleTransition(scope.row, 'confirm')"
              v-hasPermi="['biz:purchase:update']">确认</el-button>
            <el-button link type="warning" @click="handleVoid(scope.row.id)" v-hasPermi="['biz:purchase:update']">作废</el-button>
          </template>
          <el-button v-else-if="scope.row.status === '1'" link type="primary" @click="handleComplete(scope.row)"
            v-hasPermi="['biz:purchase:update']">完成入库</el-button>
          <el-button link type="primary" @click="openForm('update', scope.row.id)"
            v-if="scope.row.status === '0'" v-hasPermi="['biz:purchase:update']">修改</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-if="scope.row.status !== '2'" v-hasPermi="['biz:purchase:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="采购单号" prop="purchaseCode">
          <el-input v-model="formData.purchaseCode" placeholder="留空自动生成" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierName">
          <el-input v-model="formData.supplierName" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <BizReferenceSelect v-model="formData.warehouseId" v-model:name="formData.warehouse" kind="warehouses" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <BizReferenceSelect v-model="formData.productId" v-model:name="formData.productName" kind="products" />
        </el-form-item>
        <el-form-item label="采购数量" prop="quantity">
          <el-input-number v-model="formData.quantity" :precision="0" :min="0" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="采购单价" prop="price">
          <el-input-number v-model="formData.price" :precision="2" :min="0" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="总金额" prop="totalAmount">
          <el-input-number v-model="formData.totalAmount" :precision="2" :min="0" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="采购日期" prop="purchaseDate">
          <el-date-picker v-model="formData.purchaseDate" value-format="YYYY-MM-DD" type="date"
            placeholder="请选择采购日期" class="!w-1/1" />
        </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm()">确 定</el-button>
      <el-button v-if="!formData.id" type="success" :loading="completeLoading"
        @click="submitForm('complete')">保存并完成</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import BizReferenceSelect from '@/components/BizReferenceSelect/index.vue'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/purchase'

defineOptions({ name: 'Purchase' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.PurchaseVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  purchaseCode: undefined,
  productName: undefined,
  purchaseDate: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getPurchasePage(queryParams)
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
const formData = ref<Api.PurchaseVO>({} as Api.PurchaseVO)
const formRules = reactive({
  purchaseCode: [{ required: true, message: "采购单号不能为空", trigger: "blur" }],
  productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }],
  quantity: [{ required: true, message: "采购数量不能为空", trigger: "blur" }],
  price: [{ required: true, message: "采购单价不能为空", trigger: "blur" }],
  purchaseDate: [{ required: true, message: "采购日期不能为空", trigger: "blur" }],
})
const openForm = (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增采购单' : '修改采购单'
  formType.value = type
  if (id) {
    formLoading.value = true
    Api.getPurchase(id).then((data) => {
      formData.value = data
    }).finally(() => { formLoading.value = false })
  } else {
    formData.value = {} as Api.PurchaseVO
  }
}

/** 状态流转：确认 / 作废 */
const handleTransition = async (row: any, action: string) => {
  try {
    await message.confirm(action === 'confirm' ? '确认该单据？确认后可执行完成' : '作废后不可恢复，确认？')
    await Api.transitionPurchase(row.id, action)
    message.success(action === 'confirm' ? '已确认' : '已作废')
    await getList()
  } catch {}
}

/** 完成：联动库存（采购入库） */
/** 作废 */
const handleVoid = async (id: number) => {
  try {
    await message.confirm('作废后不可恢复，确认作废该单据？')
    await Api.transitionPurchase(id, 'void')
    message.success('已作废')
    await getList()
  } catch {}
}

const handleComplete = async (row: any) => {
  try {
    await message.confirm('确认完成？完成入库将联动库存变动')
    await Api.completePurchase(row.id)
    message.success('已完成，库存已更新')
    await getList()
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deletePurchase(id)
    message.success('删除成功')
    await getList()
  } catch {}
}
/** 提交表单 */
const emit = defineEmits(['success'])
const completeLoading = ref(false)
const submitForm = async (mode?: string) => {
  await formRef.value.validate()
  const andComplete = mode === 'complete'
  if (andComplete) completeLoading.value = true
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      if (andComplete) {
        await Api.createAndCompletePurchase(formData.value as unknown as Api.PurchaseVO)
        message.success('已创建并完成入库')
      } else {
        await Api.createPurchase(formData.value as unknown as Api.PurchaseVO)
        message.success('新增成功')
      }
    } else {
      await Api.updatePurchase(formData.value as unknown as Api.PurchaseVO)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
    await getList()
  } finally {
    completeLoading.value = false
    formLoading.value = false
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await Api.exportPurchase(queryParams)
    download.excel(data, '采购单.xls')
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
