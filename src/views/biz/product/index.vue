<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="产品编号" prop="productCode">
        <el-input v-model="queryParams.productCode" placeholder="请输入产品编号" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_COMMON_STATUS)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['biz:product:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:product:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="产品编号" align="center" prop="productCode" min-width="110" />
      <el-table-column label="产品名称" align="center" prop="productName" min-width="110" />
      <el-table-column label="产品分类" align="center" prop="category" min-width="110" />
      <el-table-column label="单位" align="center" prop="unit" min-width="110" />
      <el-table-column label="销售单价" align="right" prop="price" min-width="100" />
      <el-table-column label="成本价" align="right" prop="cost" min-width="100" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['biz:product:update']">修改</el-button>

          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['biz:product:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="产品编号" prop="productCode">
          <el-input v-model="formData.productCode" placeholder="请输入产品编号" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="formData.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="产品分类" prop="category">
          <el-input v-model="formData.category" placeholder="请输入产品分类" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="formData.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="销售单价" prop="price">
          <el-input-number v-model="formData.price" :precision="2" :min="0" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="成本价" prop="cost">
          <el-input-number v-model="formData.cost" :precision="2" :min="0" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" class="!w-1/1">
            <el-option
              v-for="d in getDictOptions(DICT_TYPE.BIZ_COMMON_STATUS)"
              :key="d.value"
              :label="d.label"
              :value="d.value"
            />
          </el-select>
        </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/product'
import type { ProductVO } from '@/api/biz/product'

defineOptions({ name: 'Product' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.ProductVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productCode: undefined,
  productName: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getProductPage(queryParams)
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
const formData = ref<Api.ProductVO>({} as Api.ProductVO)
const formRules = reactive({
  productCode: [{ required: true, message: "产品编号不能为空", trigger: "blur" }],
  productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }],
  price: [{ required: true, message: "销售单价不能为空", trigger: "blur" }],
})
const openForm = (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增产品' : '修改产品'
  formType.value = type
  if (id) {
    formLoading.value = true
    Api.getProduct(id).then((data) => {
      formData.value = data
    }).finally(() => { formLoading.value = false })
  } else {
    formData.value = {} as Api.ProductVO
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteProduct(id)
    message.success('删除成功')
    await getList()
  } catch {}
}
/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await Api.createProduct(formData.value as unknown as Api.ProductVO)
      message.success('新增成功')
    } else {
      await Api.updateProduct(formData.value as unknown as Api.ProductVO)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await Api.exportProduct(queryParams)
    download.excel(data, '产品.xls')
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
