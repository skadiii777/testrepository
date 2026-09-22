<template>
  <div class="page-enter">
    <ContentWrap>
      <el-row :gutter="16">
        <el-col :span="6" v-for="(c, idx) in cards" :key="c.key">
          <div class="stat-card card-hover stagger" :style="{ '--d': idx * 55 + 'ms' }">
            <div class="stat-icon" :style="{ background: c.bg, color: c.fg }">
              <Icon :icon="c.icon" :size="22" />
            </div>
            <div class="stat-body">
              <div class="stat-label">{{ c.label }}</div>
              <div class="stat-value num-tab" :style="{ color: c.fg }">{{ shown[c.key] ?? 0 }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </ContentWrap>

    <el-row :gutter="16">
      <el-col :span="16">
        <ContentWrap>
          <Echart :options="trendOptions" :height="320" />
        </ContentWrap>
      </el-col>
      <el-col :span="8">
        <ContentWrap>
          <Echart :options="topOptions" :height="320" />
        </ContentWrap>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12">
        <ContentWrap>
          <Echart :options="contractOptions" :height="300" />
        </ContentWrap>
      </el-col>
      <el-col :span="12">
        <ContentWrap>
          <Echart :options="leaveOptions" :height="300" />
        </ContentWrap>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import * as DashboardApi from '@/api/biz/dashboard'
import { Echart } from '@/components/Echart'

defineOptions({ name: 'BizDashboard' })

const CONTRACT_LABELS: Record<string, string> = { '0': '草稿', '1': '执行中', '2': '已完成', '3': '已终止' }
const LEAVE_LABELS: Record<string, string> = { '0': '待审批', '1': '已通过', '2': '已驳回', '3': '已销假' }

/** 统一图表色板（业务主绿为锚点，青/蓝/紫/橙/红依次展开） */
const PALETTE = ['#1ab394', '#23c6c8', '#409eff', '#9a6ae3', '#e6a23c', '#f56c6c']

const cards = ref([
  { key: 'customerCount', label: '客户总数', icon: 'ep:user', fg: '#1ab394', bg: 'rgba(26,179,148,.12)' },
  { key: 'productCount', label: '产品总数', icon: 'ep:goods', fg: '#23c6c8', bg: 'rgba(35,198,200,.12)' },
  { key: 'employeeCount', label: '员工总数', icon: 'ep:avatar', fg: '#409eff', bg: 'rgba(64,158,255,.12)' },
  { key: 'salesCount', label: '销售单数', icon: 'ep:sell', fg: '#1ab394', bg: 'rgba(26,179,148,.12)' },
  { key: 'purchaseCount', label: '采购单数', icon: 'ep:shopping-bag', fg: '#23c6c8', bg: 'rgba(35,198,200,.12)' },
  { key: 'leavePending', label: '待审批请假', icon: 'ep:calendar', fg: '#e6a23c', bg: 'rgba(230,162,60,.12)' },
  { key: 'expensePending', label: '待审批报销', icon: 'ep:money', fg: '#e6a23c', bg: 'rgba(230,162,60,.12)' },
  { key: 'lowStockCount', label: '库存预警项', icon: 'ep:warning', fg: '#f56c6c', bg: 'rgba(245,108,108,.12)' }
])

/** 数字滚动（count-up，600ms 缓出） */
const shown = ref<Record<string, number>>({})
const animNum = (key: string, target: number) => {
  if (!Number.isFinite(target)) target = 0
  const dur = 600
  const t0 = performance.now()
  const step = (t: number) => {
    const pr = Math.min(1, (t - t0) / dur)
    shown.value[key] = Math.round(target * (pr * (2 - pr)))
    if (pr < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

const trendOptions = ref<EChartsOption>({})
const topOptions = ref<EChartsOption>({})
const contractOptions = ref<EChartsOption>({})
const leaveOptions = ref<EChartsOption>({})

/** 公共：纵向渐变（对象形式，无需 import graphic） */
const linear = (rgb: string) => ({
  type: 'linear' as const,
  x: 0,
  y: 0,
  x2: 0,
  y2: 1,
  colorStops: [
    { offset: 0, color: `rgba(${rgb},0.32)` },
    { offset: 1, color: `rgba(${rgb},0.02)` }
  ]
})

const chartTitle = (text: string): any => ({
  text,
  left: 'center',
  textStyle: { fontSize: 14, fontWeight: 500 }
})

/** 获取指标卡 */
const getPanel = async () => {
  const data = await DashboardApi.getDashboardPanel()
  const d = data as Record<string, number>
  cards.value.forEach((c) => animNum(c.key, Number(d[c.key] ?? 0)))
}

/** 获取趋势图 */
const getTrend = async () => {
  const data = await DashboardApi.getDashboardTrend()
  trendOptions.value = {
    title: chartTitle('近7日销售/采购金额趋势'),
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售金额', '采购金额'], top: 30, icon: 'roundRect' },
    grid: { top: 70, left: 55, right: 30, bottom: 30 },
    color: PALETTE,
    animationDuration: 800,
    animationEasing: 'cubicOut' as any,
    xAxis: { type: 'category', data: (data as any).dates, axisLine: { lineStyle: { color: '#e5e7eb' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f2f3f5' } } },
    series: [
      {
        name: '销售金额',
        type: 'line',
        smooth: true,
        symbolSize: 7,
        data: (data as any).sales,
        areaStyle: { color: linear('26,179,148') },
        itemStyle: { color: '#1ab394' },
        lineStyle: { width: 3 }
      },
      {
        name: '采购金额',
        type: 'line',
        smooth: true,
        symbolSize: 7,
        data: (data as any).purchase,
        areaStyle: { color: linear('35,198,200') },
        itemStyle: { color: '#23c6c8' },
        lineStyle: { width: 3 }
      }
    ]
  }
}

/** 获取产品Top5 */
const getTop = async () => {
  const rows = (await DashboardApi.getDashboardProductTop()) as any[]
  topOptions.value = {
    title: chartTitle('产品销售Top5（数量）'),
    tooltip: { trigger: 'axis' },
    grid: { top: 55, left: 95, right: 30, bottom: 30 },
    animationDuration: 800,
    animationEasing: 'cubicOut' as any,
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f2f3f5' } } },
    yAxis: { type: 'category', data: rows.map((r) => r.productName).reverse() },
    series: [
      {
        type: 'bar',
        barWidth: 14,
        data: rows.map((r) => r.totalQty).reverse(),
        itemStyle: { color: '#1ab394', borderRadius: [0, 7, 7, 0] },
        emphasis: { itemStyle: { color: '#23c6c8' } }
      }
    ]
  }
}

/** 获取状态分布 */
const getStatus = async () => {
  const data = (await DashboardApi.getDashboardStatus()) as any
  const pieBase = {
    type: 'pie' as const,
    radius: ['40%', '64%'],
    center: ['50%', '58%'],
    avoidLabelOverlap: true,
    itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 4 },
    label: { show: false },
    emphasis: { label: { show: true, fontSize: 14, fontWeight: 500 } }
  }
  contractOptions.value = {
    title: chartTitle('合同状态分布'),
    tooltip: { trigger: 'item' },
    color: PALETTE,
    animationDuration: 800,
    series: [{ ...pieBase, data: (data.contract || []).map((r: any) => ({ name: CONTRACT_LABELS[r.status] || r.status, value: Number(r.cnt) })) }]
  }
  leaveOptions.value = {
    title: chartTitle('请假审批状态分布'),
    tooltip: { trigger: 'item' },
    color: [PALETTE[4], PALETTE[0], PALETTE[5], PALETTE[2]],
    animationDuration: 800,
    series: [{ ...pieBase, data: (data.leave || []).map((r: any) => ({ name: LEAVE_LABELS[r.status] || r.status, value: Number(r.cnt) })) }]
  }
}

onMounted(() => {
  getPanel()
  getTrend()
  getTop()
  getStatus()
})
</script>

<style lang="scss" scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  margin-bottom: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.2s var(--ease-out-cubic, ease);

  .stat-card:hover & {
    transform: scale(1.08) rotate(-4deg);
  }
}

.stat-body {
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  line-height: 1.1;
  margin-top: 3px;
}
</style>
