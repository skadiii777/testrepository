<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="科目编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入科目编码" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="科目名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入科目名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="科目类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择科目类型" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_FMS_ACCOUNT_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:fms:account:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增科目
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="科目编码" align="center" prop="code" width="120" />
      <el-table-column label="科目名称" align="left" prop="name" min-width="160" />
      <el-table-column label="科目类型" align="center" prop="type" width="110">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_FMS_ACCOUNT_TYPE" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="余额方向" align="center" prop="direction" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_FMS_DIRECTION" :value="scope.row.direction" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">
            {{ scope.row.status === 0 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="140" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" width="170"
        :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm(scope.row)"
            v-hasPermi="['biz:fms:account:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['biz:fms:account:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑科目' : '新增科目'" width="520px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="科目编码" prop="code">
        <el-input v-model="formData.code" placeholder="如 1001" maxlength="32" />
      </el-form-item>
      <el-form-item label="科目名称" prop="name">
        <el-input v-model="formData.name" placeholder="如 库存现金" maxlength="128" />
      </el-form-item>
      <el-form-item label="科目类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择科目类型" class="!w-1/1">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_FMS_ACCOUNT_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="Number(d.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="余额方向" prop="direction">
        <el-radio-group v-model="formData.direction">
          <el-radio :label="1">借方</el-radio>
          <el-radio :label="2">贷方</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="0">启用</el-radio>
          <el-radio :label="1">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="选填" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="formLoading" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/fms/account'
import type { AccountVO } from '@/api/fms/account'

defineOptions({ name: 'BizFmsAccount' })

const message = useMessage()

const loading = ref(true)
const list = ref<AccountVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  code: undefined,
  name: undefined,
  type: undefined
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getAccountPage(queryParams)
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
const formLoading = ref(false)
const formRef = ref()
const formData = ref<AccountVO>({} as AccountVO)
const formRules = reactive({
  code: [{ required: true, message: '科目编码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '科目名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '科目类型不能为空', trigger: 'change' }]
})

const openForm = (row?: AccountVO) => {
  dialogVisible.value = true
  if (row) {
    formData.value = { ...row }
  } else {
    formData.value = { direction: 1, status: 0, parentId: 0 } as AccountVO
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteAccount(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

/** 提交表单 */
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formData.value.id) {
      await Api.updateAccount(formData.value)
      message.success('更新成功')
    } else {
      await Api.createAccount(formData.value)
      message.success('新增成功')
    }
    dialogVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
