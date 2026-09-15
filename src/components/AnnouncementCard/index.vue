<template>
  <el-card shadow="never" header="公司公告">
    <div v-if="list.length">
      <div
        v-for="a in list"
        :key="a.id"
        class="flex items-center justify-between py-8px cursor-pointer border-bottom-1 border-color-#f0f0f0 border-solid"
        @click="openDetail(a)"
      >
        <div class="flex items-center overflow-hidden">
          <el-tag v-if="a.pinned === '1'" type="danger" size="small" class="mr-6px">置顶</el-tag>
          <dict-tag v-if="a.type" :type="DICT_TYPE.BIZ_ANNOUNCEMENT_TYPE" :value="a.type" class="mr-6px" />
          <span class="truncate">{{ a.title }}</span>
        </div>
        <span class="text-12px text-gray-400 shrink-0 ml-8px">{{ a.publishDate }}</span>
      </div>
    </div>
    <el-empty v-else description="暂无公告" :image-size="60" />

    <el-dialog v-model="detailVisible" :title="detail?.title" width="560px">
      <div class="mb-8px text-12px text-gray-400">
        {{ typeLabel(detail?.type) }} · 发布于 {{ detail?.publishDate }}
      </div>
      <div class="whitespace-pre-wrap leading-6">{{ detail?.content }}</div>
      <template #footer>
        <el-button @click="detailVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { getLatestAnnouncements } from '@/api/biz/announcement'
import type { AnnouncementVO } from '@/api/biz/announcement'

defineOptions({ name: 'AnnouncementCard' })

const list = ref<AnnouncementVO[]>([])
const detailVisible = ref(false)
const detail = ref<AnnouncementVO | null>(null)

const typeLabel = (t?: string) =>
  ({ '1': '通知', '2': '公告', '3': '制度' })[t ?? ''] ?? '公告'

const openDetail = (a: AnnouncementVO) => {
  detail.value = a
  detailVisible.value = true
}

onMounted(async () => {
  try {
    list.value = await getLatestAnnouncements(5)
  } catch {
    list.value = []
  }
})
</script>
