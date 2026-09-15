<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="仓库" prop="warehouseId">
        <BizReferenceSelect v-model="queryParams.warehouseId" kind="warehouses" style="width: 200px" clearable />
      </el-form-item>
      <el-form-item label="库位编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入库位编码" clearable
          @keyup.enter="handleQuery" class="!w-200px" />
      </el-form-item>
      <el-form-item label="库位类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable class="!w-160px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_WMS_LOCATION_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:wms:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增库位
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="仓库" align="center" prop="warehouseName" min-width="130" />
      <el-table-column label="库位编码" align="center" prop="code" width="130" />
      <el-table-column label="库位名称" align="center" prop="name" min-width="130" />
      <el-table-column label="库位类型" align="center" prop="type" width="110">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_WMS_LOCATION_TYPE" :value="scope.row.type" />
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
            v-hasPermi="['biz:wms:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['biz:wms:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑库位' : '新增库位'" width="520px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="仓库" prop="warehouseId">
        <BizReferenceSelect v-model="formData.warehouseId" kind="warehouses" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="库位编码" prop="code">
        <el-input v-model="formData.code" placeholder="如 A-01-01" maxlength="64" />
      </el-form-item>
      <el-form-item label="库位名称" prop="name">
        <el-input v-model="formData.name" placeholder="选填" maxlength="128" />
      </el-form-item>
      <el-form-item label="库位类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择类型" class="!w-1/1">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_WMS_LOCATION_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="Number(d.value)"
          />
        </el-select>
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
import * as Api from '@/api/wms/location'
import type { WmsLocationVO } from '@/api/wms/location'

defineOptions({ name: 'BizWmsLocation' })

const message = useMessage()

const loading = ref(true)
const list = ref<WmsLocationVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  warehouseId: undefined,
  code: undefined,
  name: undefined,
  type: undefined,
  status: undefined
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getLocationPage(queryParams)
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

/** 表单弹窗逻辑 */
const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const formData = ref<WmsLocationVO>({} as WmsLocationVO)
const formRules = reactive({
  warehouseId: [{ required: true, message: '仓库不能为空', trigger: 'change' }],
  code: [{ required: true, message: '库位编码不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '库位类型不能为空', trigger: 'change' }]
})

const openForm = (row?: WmsLocationVO) => {
  dialogVisible.value = true
  if (row) {
    formData.value = { ...row }
  } else {
    formData.value = { status: 0 } as WmsLocationVO
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteLocation(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formData.value.id) {
      await Api.updateLocation(formData.value)
      message.success('更新成功')
    } else {
      await Api.createLocation(formData.value)
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
