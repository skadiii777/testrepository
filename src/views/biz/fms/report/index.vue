<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true">
      <el-form-item label="凭证日期">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange"
          start-placeholder="开始" end-placeholder="结束" style="width: 260px" @change="loadReport" />
      </el-form-item>
      <el-form-item>
        <el-button @click="loadReport"><Icon icon="ep:search" class="mr-5px" /> 查询</el-button>
        <el-button @click="dateRange = []; loadReport()"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 利润表 -->
  <ContentWrap>
    <div class="text-16px font-600 mb-10px">利润表</div>
    <el-row :gutter="16" class="mb-15px">
      <el-col :span="8">
        <el-card shadow="never"><el-statistic title="营业收入" :value="report?.totalRevenue || 0"
          :precision="2" prefix="¥" /></el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never"><el-statistic title="成本与费用" :value="report?.totalExpense || 0"
          :precision="2" prefix="¥" /></el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <el-statistic title="净利润" :value="report?.netProfit || 0" :precision="2" prefix="¥"
            :value-style="{ color: (report?.netProfit || 0) >= 0 ? '#67c23a' : '#f56c6c' }" />
        </el-card>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="report?.profitItems || []" stripe size="small">
      <el-table-column label="科目编码" prop="code" width="110" align="center" />
      <el-table-column label="科目名称" prop="name" min-width="150" align="center" />
      <el-table-column label="借方发生（成本费用）" prop="debit" align="right" min-width="160">
        <template #default="{ row }">{{ fmt(row.debit) }}</template>
      </el-table-column>
      <el-table-column label="贷方发生（收入）" prop="credit" align="right" min-width="160">
        <template #default="{ row }">{{ fmt(row.credit) }}</template>
      </el-table-column>
      <el-table-column label="余额" prop="balance" align="right" width="140">
        <template #default="{ row }">
          <b :class="Number(row.balance) >= 0 ? 'text-green-600' : 'text-red-500'">{{ fmt(row.balance) }}</b>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <!-- 资产负债表 -->
  <ContentWrap>
    <div class="text-16px font-600 mb-10px">资产负债表</div>
    <el-row :gutter="16" class="mb-15px">
      <el-col :span="6">
        <el-card shadow="never"><el-statistic title="资产合计" :value="report?.totalAssets || 0"
          :precision="2" prefix="¥" /></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><el-statistic title="负债合计" :value="report?.totalLiabilities || 0"
          :precision="2" prefix="¥" /></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><el-statistic title="权益合计" :value="report?.totalEquity || 0"
          :precision="2" prefix="¥" /></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <el-statistic title="资产−负债−权益" :value="balanceGap" :precision="2" prefix="¥"
            :value-style="{ color: Math.abs(balanceGap) < 0.01 ? '#67c23a' : '#e6a23c' }" />
          <div class="text-12px text-gray-400 mt-4px">{{ Math.abs(balanceGap) < 0.01 ? '√ 平衡' : '差额（含未分配利润）' }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="text-14px font-600 mb-6px">资产</div>
        <el-table v-loading="loading" :data="report?.assets || []" stripe size="small">
          <el-table-column label="编码" prop="code" width="90" align="center" />
          <el-table-column label="科目" prop="name" min-width="120" align="center" />
          <el-table-column label="余额（借-贷）" prop="balance" align="right" width="130">
            <template #default="{ row }">{{ fmt(row.balance) }}</template>
          </el-table-column>
        </el-table>
      </el-col>
      <el-col :span="12">
        <div class="text-14px font-600 mb-6px">负债与权益</div>
        <el-table v-loading="loading" :data="[...(report?.liabilities || []), ...(report?.equity || [])]" stripe size="small">
          <el-table-column label="编码" prop="code" width="90" align="center" />
          <el-table-column label="科目" prop="name" min-width="120" align="center" />
          <el-table-column label="余额（贷-借）" prop="balance" align="right" width="130">
            <template #default="{ row }">{{ fmt(row.balance) }}</template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </ContentWrap>
</template>

<script setup lang="ts">
import { getFinancialReport, type FinancialReport } from '@/api/fms/report'

defineOptions({ name: 'BizFmsReport' })

const loading = ref(false)
const report = ref<FinancialReport>()
const dateRange = ref<string[]>([])

const fmt = (v: number | string | undefined) =>
  Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })

const balanceGap = computed(() =>
  Number(report.value?.totalAssets || 0) - Number(report.value?.totalLiabilities || 0)
    - Number(report.value?.totalEquity || 0) - Number(report.value?.netProfit || 0))

const loadReport = async () => {
  loading.value = true
  try {
    report.value = await getFinancialReport({
      beginDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReport()
})
</script>
