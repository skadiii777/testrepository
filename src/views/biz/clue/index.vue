<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="线索名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入线索名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="联系人" prop="contactName">
        <el-input v-model="queryParams.contactName" placeholder="请输入联系人" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="来源" prop="source">
        <el-select v-model="queryParams.source" placeholder="请选择来源" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_CLUE_SOURCE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_CLUE_STATUS)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:clue:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新建线索
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:clue:query']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="线索名称" align="center" prop="name" min-width="160" show-overflow-tooltip />
      <el-table-column label="联系人" align="center" prop="contactName" min-width="100" />
      <el-table-column label="联系电话" align="center" prop="contactMobile" min-width="120" />
      <el-table-column label="来源" align="center" prop="source" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_CLUE_SOURCE" :value="scope.row.source" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="负责人" align="center" prop="ownerName" width="100" />
      <el-table-column label="备注" align="center" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === '0' || scope.row.status === '1'">
            <el-button link type="primary" @click="openConvert(scope.row)"
              v-hasPermi="['biz:clue:update']">转商机</el-button>
            <el-button link type="warning" @click="openForm(scope.row)"
              v-hasPermi="['biz:clue:update']">编辑</el-button>
          </template>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['biz:clue:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 新建/编辑弹窗 -->
  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑线索' : '新建线索'" width="560px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="线索名称" prop="name">
        <el-input v-model="formData.name" placeholder="客户公司名称" />
      </el-form-item>
      <el-form-item label="联系人" prop="contactName">
        <el-input v-model="formData.contactName" placeholder="请输入联系人" />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactMobile">
        <el-input v-model="formData.contactMobile" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="来源" prop="source">
        <el-select v-model="formData.source" placeholder="请选择来源" clearable class="!w-1/1">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_CLUE_SOURCE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="formData.id" label="状态" prop="status">
        <el-select v-model="formData.status" class="!w-1/1">
          <el-option label="待跟进" value="0" />
          <el-option label="跟进中" value="1" />
          <el-option label="已无效" value="3" />
        </el-select>
        <div class="text-12px text-gray-400">「已转化」由转商机操作自动标记，不可手工设置</div>
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

  <!-- 转商机弹窗 -->
  <el-dialog v-model="convertDialogVisible" title="线索转商机" width="560px">
    <el-alert type="info" :closable="false" class="mb-15px"
      title="转化将自动创建客户（按线索名称，已存在则复用）并创建商机，线索状态置为「已转化」" />
    <el-form ref="convertFormRef" :model="convertData" :rules="convertRules" label-width="100px">
      <el-form-item label="线索">
        <el-input :model-value="convertClueRow?.name" disabled />
      </el-form-item>
      <el-form-item label="商机名称" prop="businessName">
        <el-input v-model="convertData.businessName" placeholder="默认同线索名称" />
      </el-form-item>
      <el-form-item label="初始阶段" prop="stage">
        <el-select v-model="convertData.stage" class="!w-1/1">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_BUSINESS_STAGE).filter((x) => ['1', '2'].includes(x.value))"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="预期金额" prop="amount">
        <el-input-number v-model="convertData.amount" :min="0" :precision="2" :step="1000" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="预计成交" prop="expectedDate">
        <el-date-picker v-model="convertData.expectedDate" value-format="YYYY-MM-DD" type="date"
          placeholder="请选择预计成交日期" class="!w-1/1" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="convertDialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="convertLoading" @click="submitConvert">确认转化</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/clue'
import type { ClueVO } from '@/api/biz/clue'

defineOptions({ name: 'BizClue' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.ClueVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  contactName: undefined,
  source: undefined,
  status: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)

const statusText = (s?: string) => (s === '1' ? '跟进中' : s === '2' ? '已转化' : s === '3' ? '已无效' : '待跟进')
const statusType = (s?: string) => (s === '1' ? 'primary' : s === '2' ? 'success' : s === '3' ? 'info' : 'warning')

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getCluePage(queryParams)
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

/** 新建/编辑弹窗 */
const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const formData = ref<ClueVO>({} as ClueVO)
const formRules = reactive({
  name: [{ required: true, message: '线索名称不能为空', trigger: 'blur' }],
  contactName: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
  contactMobile: [{ required: true, message: '联系电话不能为空', trigger: 'blur' }]
})

const openForm = (row?: ClueVO) => {
  dialogVisible.value = true
  formData.value = row ? { ...row } : ({ source: '3' } as ClueVO)
}

/** 转商机弹窗 */
const convertDialogVisible = ref(false)
const convertLoading = ref(false)
const convertFormRef = ref()
const convertClueRow = ref<ClueVO>()
const convertData = ref<any>({})
const convertRules = reactive({
  businessName: [{ required: true, message: '商机名称不能为空', trigger: 'blur' }],
  stage: [{ required: true, message: '初始阶段不能为空', trigger: 'change' }]
})

const openConvert = (row: ClueVO) => {
  convertClueRow.value = row
  convertData.value = { businessName: row.name, stage: '1' }
  convertDialogVisible.value = true
}

const submitConvert = async () => {
  await convertFormRef.value.validate()
  convertLoading.value = true
  try {
    await Api.convertClue(convertClueRow.value!.id!, convertData.value)
    message.success('转化成功：已创建客户与商机')
    convertDialogVisible.value = false
    await getList()
  } finally {
    convertLoading.value = false
  }
}

/** 删除 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteClue(id)
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
      await Api.updateClue(formData.value as unknown as ClueVO)
      message.success('更新成功')
    } else {
      await Api.createClue(formData.value as unknown as ClueVO)
      message.success('创建成功')
    }
    dialogVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

/** 导出 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await Api.exportClue(queryParams)
    download.excel(data, '销售线索.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>
