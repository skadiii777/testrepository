<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
__SEARCH__
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
__CREATE__
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['biz:__PERM__:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
__COLS__
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="scope">
__UPDATE__
__AUDIT__
__DEL__
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
__FORM__
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/__API__'
import type { __VO__VO } from '@/api/__API__'

defineOptions({ name: '__VNAME__' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.__VO__VO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
__QFIELDS__
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await Api.get__VNAME__Page(queryParams)
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
__AUDITFN__
__FORMSCRIPT__
__CRUD__
__EXPORT__
/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
