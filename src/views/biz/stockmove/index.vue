<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="类型" prop="moveType">
        <el-select v-model="queryParams.moveType" placeholder="请选择类型" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_STOCK_MOVE_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="来源单号" prop="sourceCode">
        <el-input v-model="queryParams.sourceCode" placeholder="请输入来源单号" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>

        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:stockmove:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="类型" align="center" prop="moveType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_STOCK_MOVE_TYPE" :value="scope.row.moveType" />
        </template>
      </el-table-column>
      <el-table-column label="产品名称" align="center" prop="productName" min-width="110" />
      <el-table-column label="仓库" align="center" prop="warehouse" min-width="110" />
      <el-table-column label="数量" align="right" prop="quantity" min-width="100" />
      <el-table-column label="结余" align="right" prop="balanceAfter" min-width="100" />
      <el-table-column label="来源类型" align="center" prop="sourceType" min-width="110" />
      <el-table-column label="来源单号" align="center" prop="sourceCode" min-width="110" />
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/stockmove'

defineOptions({ name: 'StockMove' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.StockMoveVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  moveType: undefined,
  productName: undefined,
  sourceCode: undefined,
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getStockMovePage(queryParams)
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




/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await Api.exportStockMove(queryParams)
    download.excel(data, '库存流水.xls')
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
