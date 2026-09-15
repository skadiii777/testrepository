<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="合同编号" prop="contractCode">
        <el-input v-model="queryParams.contractCode" placeholder="请输入合同编号" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="签订日期" prop="signDate">
        <el-date-picker v-model="queryParams.signDate" value-format="YYYY-MM-DD" type="date"
          placeholder="请选择签订日期" clearable class="!w-240px" />
      </el-form-item>
      <el-form-item label="合同状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择合同状态" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_CONTRACT_STATUS)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['biz:contract:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:contract:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="合同编号" align="center" prop="contractCode" min-width="110" />
      <el-table-column label="客户名称" align="center" prop="customerName" min-width="110" />
      <el-table-column label="产品名称" align="center" prop="productName" min-width="110" />
      <el-table-column label="合同金额" align="right" prop="amount" min-width="100" />
      <el-table-column label="回款进度" align="center" min-width="130">
        <template #default="scope">
          <div class="text-12px">
            ¥ {{ Number(scope.row.receivedAmount || 0).toLocaleString() }}
            <span class="text-gray-400">/ {{ Number(scope.row.amount || 0).toLocaleString() }}</span>
          </div>
          <el-progress
            :percentage="Number(scope.row.amount) > 0 ? Math.min(100, Math.round(Number(scope.row.receivedAmount || 0) / Number(scope.row.amount) * 100)) : 0"
            :stroke-width="6"
            :status="Number(scope.row.receivedAmount || 0) >= Number(scope.row.amount || 0) && Number(scope.row.amount || 0) > 0 ? 'success' : undefined"
          />
        </template>
      </el-table-column>
      <el-table-column label="签订日期" align="center" prop="signDate" min-width="110" />
      <el-table-column label="开始日期" align="center" prop="startDate" min-width="110" />
      <el-table-column label="结束日期" align="center" prop="endDate" min-width="110" />
      <el-table-column label="负责人" align="center" prop="owner" min-width="110" />
      <el-table-column label="合同状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_CONTRACT_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['biz:contract:update']">修改</el-button>

          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['biz:contract:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="合同编号" prop="contractCode">
          <el-input v-model="formData.contractCode" placeholder="请输入合同编号" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="formData.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="合同金额" prop="amount">
          <el-input-number v-model="formData.amount" :precision="2" :min="0" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="签订日期" prop="signDate">
          <el-date-picker v-model="formData.signDate" value-format="YYYY-MM-DD" type="date"
            placeholder="请选择签订日期" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="formData.startDate" value-format="YYYY-MM-DD" type="date"
            placeholder="请选择开始日期" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="formData.endDate" value-format="YYYY-MM-DD" type="date"
            placeholder="请选择结束日期" class="!w-1/1" />
        </el-form-item>
        <el-form-item label="负责人" prop="owner">
          <el-input v-model="formData.owner" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="合同状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择合同状态" class="!w-1/1">
            <el-option
              v-for="d in getDictOptions(DICT_TYPE.BIZ_CONTRACT_STATUS)"
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
import * as Api from '@/api/biz/contract'
import type { ContractVO } from '@/api/biz/contract'

defineOptions({ name: 'Contract' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.ContractVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  contractCode: undefined,
  customerName: undefined,
  signDate: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getContractPage(queryParams)
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
const formData = ref<Api.ContractVO>({} as Api.ContractVO)
const formRules = reactive({
  contractCode: [{ required: true, message: "合同编号不能为空", trigger: "blur" }],
  customerName: [{ required: true, message: "客户名称不能为空", trigger: "blur" }],
  amount: [{ required: true, message: "合同金额不能为空", trigger: "blur" }],
  signDate: [{ required: true, message: "签订日期不能为空", trigger: "blur" }],
})
const openForm = (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增合同' : '修改合同'
  formType.value = type
  if (id) {
    formLoading.value = true
    Api.getContract(id).then((data) => {
      formData.value = data
    }).finally(() => { formLoading.value = false })
  } else {
    formData.value = {} as Api.ContractVO
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteContract(id)
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
      await Api.createContract(formData.value as unknown as Api.ContractVO)
      message.success('新增成功')
    } else {
      await Api.updateContract(formData.value as unknown as Api.ContractVO)
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
    const data = await Api.exportContract(queryParams)
    download.excel(data, '合同.xls')
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
