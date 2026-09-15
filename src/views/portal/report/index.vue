<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true" :model="queryParams" label-width="80px">
      <el-form-item label="汇报类型" prop="reportType">
        <el-select v-model="queryParams.reportType" placeholder="全部" clearable class="!w-240px">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_REPORT_TYPE)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button type="primary" plain @click="openForm">
          <Icon icon="ep:plus" class="mr-5px" /> 提交汇报
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="类型" align="center" prop="reportType" width="90">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_REPORT_TYPE" :value="scope.row.reportType" />
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title" min-width="200">
        <template #default="scope">
          <el-button link type="primary" @click="viewReport(scope.row)">{{ scope.row.title }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="汇报日期" align="center" prop="reportDate" width="120" />
      <el-table-column label="提交时间" align="center" prop="createTime" :formatter="dateFormatter" width="180" />
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="viewReport(scope.row)">查看</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>

  <!-- 提交/查看弹窗 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" :disabled="viewMode">
      <el-form-item label="汇报类型" prop="reportType">
        <el-select v-model="formData.reportType" class="!w-1/1">
          <el-option v-for="d in getDictOptions(DICT_TYPE.BIZ_REPORT_TYPE)" :key="d.value"
            :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="汇报日期" prop="reportDate">
        <el-date-picker v-model="formData.reportDate" value-format="YYYY-MM-DD" type="date" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" maxlength="200" />
      </el-form-item>
      <el-form-item label="汇报内容" prop="content">
        <el-input v-model="formData.content" type="textarea" :rows="6"
          placeholder="今日完成工作 / 遇到的问题 / 明日计划" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
      <el-button v-if="!viewMode" type="primary" @click="submitForm">提 交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as PortalApi from '@/api/portal'

defineOptions({ name: 'PortalReport' })

const message = useMessage()
const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, reportType: undefined })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const viewMode = ref(false)
const formRef = ref()
const formData = ref<any>({})
const formRules = {
  reportType: [{ required: true, message: '汇报类型不能为空', trigger: 'blur' }],
  reportDate: [{ required: true, message: '汇报日期不能为空', trigger: 'blur' }],
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '汇报内容不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await PortalApi.getPortalReportPage(queryParams)
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

const openForm = () => {
  viewMode.value = false
  dialogTitle.value = '提交汇报'
  formData.value = {}
  dialogVisible.value = true
}

const viewReport = (row: any) => {
  viewMode.value = true
  dialogTitle.value = '汇报详情'
  formData.value = { ...row }
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value.validate()
  await PortalApi.submitPortalReport(formData.value)
  message.success('提交成功')
  dialogVisible.value = false
  await getList()
}

const handleDelete = async (id: number) => {
  await message.delConfirm()
  await PortalApi.deletePortalReport(id)
  message.success('删除成功')
  await getList()
}

onMounted(() => {
  getList()
})
</script>
