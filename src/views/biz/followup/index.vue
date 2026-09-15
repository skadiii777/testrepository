<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="跟进方式" prop="method">
        <el-select v-model="queryParams.method" placeholder="请选择跟进方式" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_FOLLOWUP_METHOD)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['biz:followup:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:followup:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="客户名称" align="center" prop="customerName" min-width="110" />
      <el-table-column label="跟进时间" align="center" prop="followTime" min-width="110" />
      <el-table-column label="跟进方式" align="center" prop="method">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_FOLLOWUP_METHOD" :value="scope.row.method" />
        </template>
      </el-table-column>
      <el-table-column label="跟进内容" align="center" prop="content" min-width="110" />
      <el-table-column label="下次跟进日期" align="center" prop="nextDate" min-width="110" />
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['biz:followup:update']">修改</el-button>

          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['biz:followup:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="跟进时间" prop="followTime">
          <el-date-picker v-model="formData.followTime" value-format="YYYY-MM-DD HH:mm:ss" type="datetime"
            placeholder="请选择跟进时间" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="跟进方式" prop="method">
          <el-select v-model="formData.method" placeholder="请选择跟进方式" class="!w-1/1">
            <el-option
              v-for="d in getDictOptions(DICT_TYPE.BIZ_FOLLOWUP_METHOD)"
              :key="d.value"
              :label="d.label"
              :value="d.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="4" placeholder="请输入跟进内容" />
        </el-form-item>
        <el-form-item label="下次跟进日期" prop="nextDate">
          <el-date-picker v-model="formData.nextDate" value-format="YYYY-MM-DD" type="date"
            placeholder="请选择下次跟进日期" class="!w-1/1" />
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
import * as Api from '@/api/biz/followup'
import type { CustomerFollowupVO } from '@/api/biz/followup'

defineOptions({ name: 'CustomerFollowup' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.CustomerFollowupVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  customerName: undefined,
  method: undefined,
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getCustomerFollowupPage(queryParams)
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
const formData = ref<Api.CustomerFollowupVO>({} as Api.CustomerFollowupVO)
const formRules = reactive({
  customerName: [{ required: true, message: "客户名称不能为空", trigger: "blur" }],
  followTime: [{ required: true, message: "跟进时间不能为空", trigger: "blur" }],
  method: [{ required: true, message: "跟进方式不能为空", trigger: "blur" }],
  content: [{ required: true, message: "跟进内容不能为空", trigger: "blur" }],
})
const openForm = (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增客户跟进' : '修改客户跟进'
  formType.value = type
  if (id) {
    formLoading.value = true
    Api.getCustomerFollowup(id).then((data) => {
      formData.value = data
    }).finally(() => { formLoading.value = false })
  } else {
    formData.value = {} as Api.CustomerFollowupVO
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteCustomerFollowup(id)
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
      await Api.createCustomerFollowup(formData.value as unknown as Api.CustomerFollowupVO)
      message.success('新增成功')
    } else {
      await Api.updateCustomerFollowup(formData.value as unknown as Api.CustomerFollowupVO)
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
    const data = await Api.exportCustomerFollowup(queryParams)
    download.excel(data, '客户跟进.xls')
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
