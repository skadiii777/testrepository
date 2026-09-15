<template>
  <el-row :gutter="16">
    <el-col :xs="24" :md="16" class="mb-16px md:mb-0">
      <el-card shadow="hover" class="text-center">
        <div class="color-#999">{{ nickname }}，今天是 {{ data.today }}（工作时间 {{ data.workStart }} - {{ data.workEnd }}）</div>
        <div class="text-46px font-600 color-#1ab394 my-16px">{{ clock }}</div>
        <div>
          <el-button type="success" size="large" class="w-full sm:w-auto sm:min-w-140px mb-8px sm:mb-0" @click="handlePunch('in')">
            <Icon icon="ep:alarm-clock" class="mr-5px" /> 上班打卡
          </el-button>
          <el-button type="primary" size="large" class="w-full sm:w-auto sm:min-w-140px" @click="handlePunch('out')">
            <Icon icon="ep:moon" class="mr-5px" /> 下班打卡
          </el-button>
        </div>
        <div class="mt-8px color-#909399 text-13px">
          本月加班：<span class="color-#e6a23c font-600">{{ Math.round((data.monthOvertimeMinutes || 0) / 6) / 10 }} 小时</span>
        </div>
        <div v-if="data.checkIn || data.checkOut" class="mt-16px color-#666">
          上班：<span>{{ data.checkIn || '未打卡' }}</span>
          <el-divider vertical />
          下班：<span>{{ data.checkOut || '未打卡' }}</span>
          <dict-tag v-if="data.status" class="ml-8px" :type="DICT_TYPE.BIZ_ATTENDANCE_STATUS" :value="data.status" />
        </div>
        <div v-else class="mt-16px color-#aaa">今日尚未打卡</div>
      </el-card>
    </el-col>
    <el-col :xs="24" :md="8">
      <AnnouncementCard class="mb-16px fade-in-up" />
      <el-card shadow="hover" v-if="quotaList.length" class="mb-16px">
        <template #header>我的假期余额</template>
        <el-tag
          v-for="q in quotaList"
          :key="q.type"
          :type="Number(q.remain) <= 0 ? 'danger' : 'success'"
          class="mr-8px mb-8px"
        >
          {{ q.label }} 剩余 {{ q.remain }} 天
        </el-tag>
      </el-card>
      <el-card shadow="hover">
        <el-button class="!w-full mb-10px !ml-0" @click="$router.push('/portal/leave')">
          <span class="inline-block w-16px text-center"><Icon icon="ep:calendar" /></span>
          <span class="ml-5px">我的请假</span>
        </el-button>
        <el-button class="!w-full mb-10px !ml-0" @click="$router.push('/portal/report')">
          <span class="inline-block w-16px text-center"><Icon icon="ep:document" /></span>
          <span class="ml-5px">业务汇报</span>
        </el-button>
        <el-button class="!w-full mb-10px !ml-0" @click="$router.push('/portal/expense')">
          <span class="inline-block w-16px text-center"><Icon icon="ep:money" /></span>
          <span class="ml-5px">我的报销</span>
        </el-button>
        <el-button class="!w-full !ml-0" @click="$router.push('/portal/correction')">
          <span class="inline-block w-16px text-center"><Icon icon="ep:edit-pen" /></span>
          <span class="ml-5px">我的补卡</span>
        </el-button>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { useUserStore } from '@/store/modules/user'
import * as PortalApi from '@/api/portal'

defineOptions({ name: 'PortalIndex' })

const message = useMessage()
const userStore = useUserStore()

const nickname = computed(() => userStore.user.nickname)
const clock = ref('--:--:--')
const data = ref<any>({})
const quotaList = ref<{ label: string; remain: number }[]>([])

const LEAVE_LABELS: Record<string, string> = { '1': '事假', '2': '病假', '3': '年假', '4': '调休' }

const tick = () => {
  const d = new Date()
  const p = (n: number) => (n < 10 ? '0' + n : n)
  clock.value = `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
let timer: any = null

const getIndexData = async () => {
  data.value = await PortalApi.getPortalIndexData()
  const quotas = (data.value.quotas || {}) as Record<string, number>
  quotaList.value = Object.keys(quotas).map((type) => ({
    type,
    label: LEAVE_LABELS[type] || type,
    remain: Number(quotas[type])
  }))
}

const handlePunch = async (type: string) => {
  await message.confirm(type === 'in' ? '确认上班打卡？' : '确认下班打卡？')
  const msg = await PortalApi.punch(type)
  message.success(msg)
  await getIndexData()
}

onMounted(() => {
  tick()
  timer = setInterval(tick, 1000)
  getIndexData()
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>
