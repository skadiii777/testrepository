<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入公告标题" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_ANNOUNCEMENT_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option label="已发布" value="0" />
          <el-option label="已下架" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['biz:announcement:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 发布公告
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="标题" align="center" prop="title" min-width="220" show-overflow-tooltip>
        <template #default="scope">
          <el-link type="primary" @click="openDetail(scope.row)">{{ scope.row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="type" width="90">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BIZ_ANNOUNCEMENT_TYPE" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="置顶" align="center" prop="pinned" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.pinned === '1'" type="danger" size="small">置顶</el-tag>
          <span v-else class="text-gray-300">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
            {{ scope.row.status === '0' ? '已发布' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布日期" align="center" prop="publishDate" min-width="110" />
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="scope">
          <el-button link type="warning" @click="togglePin(scope.row)"
            v-hasPermi="['biz:announcement:update']">{{ scope.row.pinned === '1' ? '取消置顶' : '置顶' }}</el-button>
          <el-button link :type="scope.row.status === '0' ? 'info' : 'success'"
            @click="toggleStatus(scope.row)" v-hasPermi="['biz:announcement:update']">
            {{ scope.row.status === '0' ? '下架' : '重新发布' }}
          </el-button>
          <el-button link type="primary" @click="openForm(scope.row)"
            v-hasPermi="['biz:announcement:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['biz:announcement:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 新建/编辑弹窗 -->
  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑公告' : '发布公告'" width="640px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入公告标题" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择类型" clearable class="!w-1/1">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.BIZ_ANNOUNCEMENT_TYPE)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="置顶" prop="pinned">
        <el-switch v-model="formData.pinned" active-value="1" inactive-value="0" />
      </el-form-item>
      <el-form-item label="正文" prop="content">
        <el-input v-model="formData.content" type="textarea" :rows="6" placeholder="请输入公告正文" />
      </el-form-item>
      <el-form-item v-if="formData.id" label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio label="0">已发布</el-radio>
          <el-radio label="1">已下架</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="formLoading" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>

  <!-- 公告详情弹窗 -->
  <el-dialog v-model="detailVisible" :title="detail?.title" width="640px">
    <div class="mb-8px text-12px text-gray-400">
      {{ typeLabel(detail?.type) }} · 发布于 {{ detail?.publishDate }}
    </div>
    <div class="whitespace-pre-wrap leading-6">{{ detail?.content }}</div>
    <template #footer>
      <el-button @click="detailVisible = false">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as Api from '@/api/biz/announcement'
import type { AnnouncementVO } from '@/api/biz/announcement'

defineOptions({ name: 'BizAnnouncement' })

const message = useMessage()

const typeLabel = (t?: string) =>
  ({ '1': '通知', '2': '公告', '3': '制度' })[t ?? ''] ?? '公告'

const loading = ref(true)
const list = ref<Api.AnnouncementVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  title: undefined,
  type: undefined,
  status: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await Api.getAnnouncementPage(queryParams)
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

/** 详情弹窗 */
const detailVisible = ref(false)
const detail = ref<AnnouncementVO | null>(null)
const openDetail = (row: AnnouncementVO) => {
  detail.value = row
  detailVisible.value = true
}

/** 新建/编辑弹窗 */
const dialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const formData = ref<AnnouncementVO>({} as AnnouncementVO)
const formRules = reactive({
  title: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '公告正文不能为空', trigger: 'blur' }]
})

const openForm = (row?: AnnouncementVO) => {
  dialogVisible.value = true
  formData.value = row ? { ...row } : ({ type: '1', pinned: '0' } as AnnouncementVO)
}

/** 置顶/取消置顶 */
const togglePin = async (row: AnnouncementVO) => {
  const pinned = row.pinned === '1' ? '0' : '1'
  await Api.updateAnnouncement({ ...row, pinned } as AnnouncementVO)
  message.success(pinned === '1' ? '已置顶' : '已取消置顶')
  await getList()
}

/** 下架/重新发布 */
const toggleStatus = async (row: AnnouncementVO) => {
  const status = row.status === '0' ? '1' : '0'
  await Api.updateAnnouncement({ ...row, status } as AnnouncementVO)
  message.success(status === '1' ? '已下架' : '已重新发布')
  await getList()
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await Api.deleteAnnouncement(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formData.value.id) {
      await Api.updateAnnouncement(formData.value as unknown as AnnouncementVO)
      message.success('更新成功')
    } else {
      await Api.createAnnouncement(formData.value as unknown as AnnouncementVO)
      message.success('发布成功，员工工作台即刻可见')
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
