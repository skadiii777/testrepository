<template>
  <div class="page-enter">
    <!-- 欢迎栏：问候 + 今日打卡概况 -->
    <el-card shadow="never" class="mb-15px hero-card">
      <el-row :gutter="16" justify="space-between" align="middle">
        <el-col :xl="14" :lg="14" :md="14" :sm="24" :xs="24">
          <div class="flex items-center">
            <el-avatar :src="avatar" :size="64" class="mr-16px">
              {{ nickname.slice(0, 1) }}
            </el-avatar>
            <div>
              <div class="text-20px font-600">你好，{{ nickname }}！</div>
              <div class="mt-8px text-14px text-gray-500">{{ todayText }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xl="10" :lg="10" :md="10" :sm="24" :xs="24">
          <div class="flex flex-wrap justify-end gap-12px lt-sm:mt-12px">
            <div class="work-chip">
              <div class="text-12px text-gray-400">上班打卡</div>
              <div class="text-18px font-600" :class="portalData?.checkIn ? 'text-gray-800' : 'text-gray-400'">
                {{ portalData?.checkIn ?? '未打卡' }}
              </div>
            </div>
            <div class="work-chip">
              <div class="text-12px text-gray-400">下班打卡</div>
              <div class="text-18px font-600" :class="portalData?.checkOut ? 'text-gray-800' : 'text-gray-400'">
                {{ portalData?.checkOut ?? '未打卡' }}
              </div>
            </div>
            <div class="work-chip">
              <div class="text-12px text-gray-400">本月加班</div>
              <div class="text-18px font-600 text-gray-800">{{ overtimeHours }} 小时</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 快捷入口 -->
    <el-card shadow="never" class="mb-15px" header="快捷入口">
      <el-row :gutter="12">
        <el-col v-for="(item, i) in quickLinks" :key="item.path" :xl="8" :lg="8" :md="8" :sm="12" :xs="12">
          <div class="quick-item stagger" :style="{ '--d': i * 45 + 'ms' }" @click="router.push(item.path)">
            <Icon :icon="item.icon" :size="26" class="quick-icon" />
            <div>
              <div class="text-15px font-600">{{ item.title }}</div>
              <div class="text-12px text-gray-400">{{ item.desc }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 公司公告 -->
    <el-row :gutter="15" class="mb-15px">
      <el-col :span="24">
        <AnnouncementCard />
      </el-col>
    </el-row>

    <el-row :gutter="15">
      <!-- 我的假期余额 -->
      <el-col :xl="10" :lg="10" :md="24" :sm="24" :xs="24" class="mb-15px">
        <el-card shadow="never" header="我的假期余额（天）">
          <div v-if="quotaList.length" class="flex flex-wrap gap-12px">
            <el-tag v-for="q in quotaList" :key="q.label" size="large" type="info" effect="plain">
              {{ q.label }}：{{ q.days }}
            </el-tag>
          </div>
          <el-empty v-else description="暂无假期额度，请联系人事配置" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 企业概览（有 biz:dashboard:query 权限时展示） -->
      <el-col :xl="14" :lg="14" :md="24" :sm="24" :xs="24" class="mb-15px">
        <el-card v-if="panel" shadow="never" header="企业概览">
          <el-row :gutter="12">
            <el-col v-for="(s, i) in statCards" :key="s.label" :span="6" class="mb-12px">
              <div class="stat-item fade-in-up" :style="{ animationDelay: i * 45 + 'ms' }">
                <div class="text-12px text-gray-400">{{ s.label }}</div>
                <div class="text-22px font-600" :class="s.warn ? 'text-orange-500' : 'text-gray-800'">
                  {{ s.value }}
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { checkPermi } from '@/utils/permission'
import { getPortalIndexData } from '@/api/portal'
import { getDashboardPanel } from '@/api/biz/dashboard'

defineOptions({ name: 'Home' })

const router = useRouter()
const userStore = useUserStore()
const nickname = computed(() => userStore.user.nickname || '')
const avatar = computed(() => userStore.user.avatar || '')

const now = new Date()
const todayText = `${now.getFullYear()} 年 ${now.getMonth() + 1} 月 ${now.getDate()} 日 星期${'日一二三四五六'[now.getDay()]}`

/** 工作台个人数据：今日打卡 + 本月加班 + 假期余额 */
const portalData = ref<Record<string, any> | null>(null)
const overtimeHours = computed(() =>
  (((portalData.value?.monthOvertimeMinutes as number) || 0) / 60).toFixed(1)
)
/** 请假类型标签（biz_leave_type 字典） */
const quotaList = computed(() => {
  const quotas = (portalData.value?.quotas ?? {}) as Record<string, number>
  return Object.entries(quotas).map(([type, days]) => ({ label: leaveTypeLabel(type), days }))
})
const leaveTypeLabel = (type: string) =>
  ({ '1': '事假', '2': '病假', '3': '年假', '4': '调休' })[type] ?? `类型${type}`

/** 快捷入口（按权限过滤：无权限的入口不显示，避免点击后 403；perm 为空 = 登录即可用） */
const quickLinks = [
  { title: '打卡签到', desc: '上下班打卡', icon: 'ep:alarm-clock', path: '/portal/index', perm: 'portal:index:query' },
  { title: '在线聊天', desc: '同事单聊 / 群聊', icon: 'ep:chat-dot-round', path: '/im/home/conversation', perm: '' },
  { title: '我的请假', desc: '请假与销假申请', icon: 'ep:calendar', path: '/portal/leave', perm: 'portal:leave:query' },
  { title: '业务汇报', desc: '日报周报提交', icon: 'ep:edit-pen', path: '/portal/report', perm: 'portal:report:query' },
  { title: '我的报销', desc: '费用报销申请', icon: 'ep:money', path: '/portal/expense', perm: 'portal:expense:query' },
  { title: '我的补卡', desc: '漏打卡补卡申请', icon: 'ep:refresh-left', path: '/portal/correction', perm: 'portal:correction:query' },
  { title: '审批中心', desc: '待办审批处理', icon: 'ep:stamp', path: '/biz/approval', perm: 'biz:approval:query' }
].filter((item) => !item.perm || checkPermi([item.perm]))

/** 企业概览（无权限或接口失败时整块隐藏） */
const panel = ref<Record<string, any> | null>(null)
/** 企业概览数字渐增动画（count-up） */
const shownStats = ref<Record<string, number>>({})
const animNum = (key: string, target: number) => {
  if (!Number.isFinite(target)) target = 0
  const dur = 600
  const t0 = performance.now()
  const step = (t: number) => {
    const pr = Math.min(1, (t - t0) / dur)
    shownStats.value[key] = Math.round(target * (pr * (2 - pr)))
    if (pr < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}
watch(panel, (p) => {
  if (!p) return
  Object.keys(p).forEach((k) => animNum(k, Number(p[k] ?? 0)))
})

const statCards = computed(() => [
  { label: '客户数', value: shownStats.value.customerCount ?? 0 },
  { label: '产品数', value: shownStats.value.productCount ?? 0 },
  { label: '员工数', value: shownStats.value.employeeCount ?? 0 },
  { label: '销售单', value: shownStats.value.salesCount ?? 0 },
  { label: '采购单', value: shownStats.value.purchaseCount ?? 0 },
  { label: '待审请假', value: shownStats.value.leavePending ?? 0, warn: true },
  { label: '待审报销', value: shownStats.value.expensePending ?? 0, warn: true },
  { label: '库存预警', value: shownStats.value.lowStockCount ?? 0, warn: true },
  { label: '合同30天内到期', value: shownStats.value.contractExpiringCount ?? 0, warn: true },
  { label: '超期商机', value: shownStats.value.businessOverdueCount ?? 0, warn: true }
])

onMounted(async () => {
  getPortalIndexData()
    .then((res: any) => (portalData.value = res))
    .catch(() => {})
  // 企业概览仅对有看板权限的用户请求，避免 403 弹窗
  if (checkPermi(['biz:dashboard:query'])) {
    getDashboardPanel()
      .then((res: any) => (panel.value = res))
      .catch(() => (panel.value = null))
  }
})
</script>

<style lang="scss" scoped>
/* 欢迎栏：淡主色渐变底 + 左侧色条，强化"首页"视觉锚点 */
.hero-card {
  position: relative;
  overflow: hidden;
  border-left: 3px solid var(--el-color-primary) !important;
  background: linear-gradient(
    120deg,
    var(--el-color-primary-light-9) 0%,
    var(--el-bg-color) 45%
  ) !important;
}

.work-chip {
  min-width: 96px;
  padding: 10px 14px;
  text-align: center;
  border-radius: 8px;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--el-box-shadow-lighter);
  }
}

.quick-item {
  display: flex;
  gap: 12px;
  align-items: center;
  height: 72px;
  margin-bottom: 12px;
  padding: 0 16px;
  cursor: pointer;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-bg-color);
  transition:
    transform 0.2s var(--ease-out-cubic, ease),
    box-shadow 0.2s ease,
    border-color 0.2s ease;

  &:hover {
    transform: translateY(-3px);
    border-color: var(--el-color-primary-light-5);
    box-shadow: var(--el-box-shadow-light);

    .quick-icon {
      transform: scale(1.12) rotate(-5deg);
    }
  }

  .quick-icon {
    color: var(--el-color-primary);
    transition: transform 0.22s var(--ease-out-cubic, ease);
  }
}

.stat-item {
  padding: 10px 12px;
  border-radius: 8px;
  background-color: var(--el-fill-color-lighter);
  transition: background-color 0.2s ease;

  &:hover {
    background-color: var(--el-fill-color-light);
  }
}
</style>
