<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true" label-width="80px">
      <el-form-item label="月份">
        <el-date-picker
          v-model="month"
          type="month"
          value-format="YYYY-MM"
          :clearable="false"
          class="!w-200px"
          @change="getList"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="getList"><Icon icon="ep:search" class="mr-5px" /> 查询</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe show-summary>
      <el-table-column label="员工姓名" align="center" prop="empName" min-width="120" />
      <el-table-column label="出勤天数" align="center" prop="attendDays" width="100" />
      <el-table-column label="迟到" align="center" prop="lateCount" width="80">
        <template #default="scope">
          <span :class="scope.row.lateCount > 0 ? 'color-#e6a23c' : ''">{{ scope.row.lateCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="早退" align="center" prop="earlyCount" width="80">
        <template #default="scope">
          <span :class="scope.row.earlyCount > 0 ? 'color-#e6a23c' : ''">{{ scope.row.earlyCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="缺勤" align="center" prop="absentCount" width="80">
        <template #default="scope">
          <span :class="scope.row.absentCount > 0 ? 'color-#f56c6c' : ''">{{ scope.row.absentCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="加班时长（小时）" align="right" prop="overtimeMinutes" width="140">
        <template #default="scope">
          {{ (Number(scope.row.overtimeMinutes || 0) / 60).toFixed(1) }}
        </template>
      </el-table-column>
    </el-table>
    <div class="color-#999 text-13px mt-10px">
      统计口径：出勤天数 = 当月打卡记录数；加班 = 下班打卡/补下班卡晚于 18:00 的累计分钟。
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import * as AttendanceApi from '@/api/biz/attendance'

defineOptions({ name: 'AttendanceSummary' })

const loading = ref(true)
const list = ref<any[]>([])
const month = ref(new Date().toISOString().slice(0, 7))

const getList = async () => {
  loading.value = true
  try {
    list.value = (await AttendanceApi.getAttendanceSummary(month.value)) as any[]
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>
