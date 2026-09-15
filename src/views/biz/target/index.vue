<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true">
      <el-form-item label="目标月份">
        <el-date-picker v-model="month" type="month" value-format="YYYY-MM"
          placeholder="全部月份" clearable style="width: 160px" @change="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:target:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增目标
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="员工" align="center" prop="empName" min-width="110" />
      <el-table-column label="目标月份" align="center" prop="targetMonth" width="100" />
      <el-table-column label="目标金额" align="right" prop="targetAmount" min-width="120">
        <template #default="scope">¥ {{ fmt(scope.row.targetAmount) }}</template>
      </el-table-column>
      <el-table-column label="实际完成" align="right" prop="actualAmount" min-width="120">
        <template #default="scope">¥ {{ fmt(scope.row.actualAmount) }}</template>
      </el-table-column>
      <el-table-column label="达成率" align="center" min-width="200">
        <template #default="scope">
          <el-progress :percentage="Math.min(100, Number(scope.row.percent || 0))"
            :stroke-width="12"
            :color="scope.row.percent >= 100 ? '#67c23a' : scope.row.percent >= 60 ? '#409eff' : '#e6a23c'" />
          <span class="text-12px">{{ scope.row.percent }}%</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="130" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm(scope.row)"
            v-hasPermi="['biz:target:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['biz:target:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="pageNo" v-model:limit="pageSize" @pagination="getList" />
  </ContentWrap>

  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑目标' : '新增目标'" width="480px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="员工姓名" prop="empName">
        <el-input v-model="formData.empName" placeholder="销售归属人" maxlength="64" />
      </el-form-item>
      <el-form-item label="目标月份" prop="targetMonth">
        <el-date-picker v-model="formData.targetMonth" type="month" value-format="YYYY-MM"
          placeholder="选择月份" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="目标金额" prop="targetAmount">
        <el-input-number v-model="formData.targetAmount" :min="0" :precision="2" :step="10000" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="备注">
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
import * as Api from '@/api/biz/target'
import type { SalesTargetVO } from '@/api/biz/target'

defineOptions({ name: 'BizTarget' })

const message = useMessage()
const fmt = (v: number | string | undefined) =>
  Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })

const loading = ref(true)
const list = ref<SalesTargetVO[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(10)
const month = ref<string>()
const currentMonth = new Date().toISOString().slice(0, 7)

const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getTargetPage({ pageNo: pageNo.value, pageSize: pageSize.value, month: month.value })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
const handleQuery = () => {
  pageNo.value = 1
  getList()
}

const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const formData = ref<SalesTargetVO>({} as SalesTargetVO)
const formRules = reactive({
  empName: [{ required: true, message: '员工姓名不能为空', trigger: 'blur' }],
  targetMonth: [{ required: true, message: '目标月份不能为空', trigger: 'change' }],
  targetAmount: [{ required: true, message: '目标金额不能为空', trigger: 'blur' }]
})

const openForm = (row?: SalesTargetVO) => {
  dialogVisible.value = true
  if (row) {
    formData.value = { ...row }
  } else {
    formData.value = { targetMonth: currentMonth } as SalesTargetVO
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteTarget(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formData.value.id) {
      await Api.updateTarget(formData.value)
      message.success('更新成功')
    } else {
      await Api.createTarget(formData.value)
      message.success('新增成功')
    }
    dialogVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>
