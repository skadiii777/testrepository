<template>
  <ContentWrap>
    <el-row :gutter="16">
      <el-col :span="6" v-for="(item, idx) in cards" :key="idx">
        <el-card shadow="hover">
          <div class="text-14px color-#999">{{ item.label }}</div>
          <div class="text-28px font-600" :class="item.color">{{ item.value }}</div>
        </el-card>
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
</template>

<script setup lang="ts">
import * as DashboardApi from '@/api/biz/dashboard'
import { Echart } from '@/components/Echart'

defineOptions({ name: 'BizDashboard' })

const CONTRACT_LABELS: Record<string, string> = { '0': '草稿', '1': '执行中', '2': '已完成', '3': '已终止' }
const LEAVE_LABELS: Record<string, string> = { '0': '待审批', '1': '已通过', '2': '已驳回', '3': '已销假' }

const cards = ref([
  { label: '客户总数', value: 0, color: 'color-#1ab394' },
  { label: '产品总数', value: 0, color: 'color-#1ab394' },
  { label: '员工总数', value: 0, color: 'color-#409eff' },
  { label: '销售单数', value: 0, color: 'color-#409eff' },
  { label: '采购单数', value: 0, color: 'color-#909399' },
  { label: '待审批请假', value: 0, color: 'color-#e6a23c' },
  { label: '待审批报销', value: 0, color: 'color-#e6a23c' },
  { label: '库存预警项', value: 0, color: 'color-#f56c6c' }
])

const trendOptions = ref<EChartsOption>({})
const topOptions = ref<EChartsOption>({})
const contractOptions = ref<EChartsOption>({})
const leaveOptions = ref<EChartsOption>({})

/** 获取指标卡 */
const getPanel = async () => {
  const data = await DashboardApi.getDashboardPanel()
  const d = data as Record<string, number>
  cards.value[0].value = d.customerCount ?? 0
  cards.value[1].value = d.productCount ?? 0
  cards.value[2].value = d.employeeCount ?? 0
  cards.value[3].value = d.salesCount ?? 0
  cards.value[4].value = d.purchaseCount ?? 0
  cards.value[5].value = d.leavePending ?? 0
  cards.value[6].value = d.expensePending ?? 0
  cards.value[7].value = d.lowStockCount ?? 0
}

/** 获取趋势图 */
const getTrend = async () => {
  const data = await DashboardApi.getDashboardTrend()
  trendOptions.value = {
    title: { text: '近7日销售/采购金额趋势', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售金额', '采购金额'], top: 28 },
    grid: { top: 60, left: 50, right: 30, bottom: 30 },
    xAxis: { type: 'category', data: (data as any).dates },
    yAxis: { type: 'value' },
    series: [
      { name: '销售金额', type: 'line', smooth: true, data: (data as any).sales, areaStyle: { opacity: 0.12 }, itemStyle: { color: '#1ab394' } },
      { name: '采购金额', type: 'line', smooth: true, data: (data as any).purchase, areaStyle: { opacity: 0.12 }, itemStyle: { color: '#23c6c8' } }
    ]
  }
}

/** 获取产品Top5 */
const getTop = async () => {
  const rows = (await DashboardApi.getDashboardProductTop()) as any[]
  topOptions.value = {
    title: { text: '产品销售Top5（数量）', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { top: 50, left: 90, right: 30, bottom: 30 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: rows.map((r) => r.productName).reverse() },
    series: [{ type: 'bar', barWidth: 16, data: rows.map((r) => r.totalQty).reverse(), itemStyle: { color: '#1ab394' } }]
  }
}

/** 获取状态分布 */
const getStatus = async () => {
  const data = (await DashboardApi.getDashboardStatus()) as any
  contractOptions.value = {
    title: { text: '合同状态分布', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['35%', '60%'], center: ['50%', '56%'],
      data: (data.contract || []).map((r: any) => ({ name: CONTRACT_LABELS[r.status] || r.status, value: Number(r.cnt) }))
    }]
  }
  leaveOptions.value = {
    title: { text: '请假审批状态分布', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['35%', '60%'], center: ['50%', '56%'],
      data: (data.leave || []).map((r: any) => ({ name: LEAVE_LABELS[r.status] || r.status, value: Number(r.cnt) }))
    }]
  }
}

onMounted(() => {
  getPanel()
  getTrend()
  getTop()
  getStatus()
})
</script>
