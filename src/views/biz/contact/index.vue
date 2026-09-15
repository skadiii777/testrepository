<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="客户" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入联系人姓名" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="请输入手机号" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:contact:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新建联系人
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:contact:query']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="姓名" align="center" prop="name" min-width="100" />
      <el-table-column label="客户" align="center" prop="customerName" min-width="150" show-overflow-tooltip />
      <el-table-column label="职位" align="center" prop="position" min-width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.position" type="primary" effect="plain">{{ scope.row.position }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="center" prop="mobile" min-width="120" />
      <el-table-column label="邮箱" align="center" prop="email" min-width="140" show-overflow-tooltip />
      <el-table-column label="微信" align="center" prop="wechat" min-width="100" />
      <el-table-column label="备注" align="center" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="130" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm(scope.row)"
            v-hasPermi="['biz:contact:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['biz:contact:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑联系人' : '新建联系人'" width="560px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="关联客户" prop="customerId">
        <el-select v-model="formData.customerId" placeholder="请选择客户" filterable
          class="!w-1/1" :loading="customerLoading">
          <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入联系人姓名" />
      </el-form-item>
      <el-form-item label="职位" prop="position">
        <el-input v-model="formData.position" placeholder="如：总经理 / 采购助理" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="formData.mobile" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="formData.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="微信" prop="wechat">
        <el-input v-model="formData.wechat" placeholder="请输入微信号" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="如：决策人 / 对接人" />
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
import * as Api from '@/api/biz/contact'
import type { ContactVO } from '@/api/biz/contact'
import { getCustomerPage } from '@/api/biz/customer'

defineOptions({ name: 'BizContact' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.ContactVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  customerName: undefined,
  name: undefined,
  mobile: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getContactPage(queryParams)
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

/** 客户下拉 */
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
const formData = ref<ContactVO>({} as ContactVO)
const formRules = reactive({
  customerId: [{ required: true, message: '关联客户不能为空', trigger: 'change' }],
  name: [{ required: true, message: '联系人姓名不能为空', trigger: 'blur' }],
  mobile: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
})

const openForm = async (row?: ContactVO) => {
  dialogVisible.value = true
  formData.value = row ? { ...row } : ({} as ContactVO)
  if (!customerOptions.value.length) {
    await loadCustomers()
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteContact(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formData.value.id) {
      await Api.updateContact(formData.value as unknown as ContactVO)
      message.success('更新成功')
    } else {
      await Api.createContact(formData.value as unknown as ContactVO)
      message.success('创建成功')
    }
    dialogVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await Api.exportContact(queryParams)
    download.excel(data, '客户联系人.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>
