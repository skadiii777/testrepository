<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :inline="true" label-width="80px">
      <el-form-item label="仓库">
        <BizReferenceSelect v-model="warehouseId" kind="warehouses" style="width: 200px" clearable @change="reloadAll" />
      </el-form-item>
      <el-form-item label="产品" label-width="50px">
        <el-input v-model="productName" placeholder="产品名称" clearable style="width: 200px"
          @keyup.enter="reloadAll" />
      </el-form-item>
      <el-form-item>
        <el-button @click="reloadAll"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-tabs v-model="activeTab" @tab-change="reloadAll">
      <!-- Tab1 库位库存 -->
      <el-tab-pane label="库位库存" name="stock">
        <el-table v-loading="loading" :data="stockList" stripe>
          <el-table-column label="仓库" align="center" prop="warehouseName" min-width="120" />
          <el-table-column label="库位" align="center" prop="locationCode" width="130" />
          <el-table-column label="产品" align="center" prop="productName" min-width="160" show-overflow-tooltip />
          <el-table-column label="库位数量" align="center" prop="quantity" width="110">
            <template #default="scope"><b>{{ scope.row.quantity }}</b></template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="160" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="openMove(scope.row)"
                v-hasPermi="['biz:wms:stock:operate']">移库</el-button>
              <el-button link type="warning" @click="openRemove(scope.row)"
                v-hasPermi="['biz:wms:stock:operate']">下架</el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination :total="stockTotal" v-model:page="stockQuery.pageNo"
          v-model:limit="stockQuery.pageSize" @pagination="getStockList" />
      </el-tab-pane>

      <!-- Tab2 未分配 -->
      <el-tab-pane label="未分配库存" name="unassigned">
        <el-alert type="info" :closable="false" class="mb-10px"
          title="未分配量 = 仓库库存 − 已上架到库位的合计；上架不改变仓库总库存，只是把货定位到库位" />
        <el-table v-loading="unassignedLoading" :data="filteredUnassigned" stripe>
          <el-table-column label="仓库" align="center" prop="warehouseName" min-width="120" />
          <el-table-column label="产品" align="center" prop="productName" min-width="160" show-overflow-tooltip />
          <el-table-column label="仓库库存" align="center" prop="mainQuantity" width="110" />
          <el-table-column label="已分配" align="center" prop="allocated" width="100" />
          <el-table-column label="未分配（可上架）" align="center" width="140">
            <template #default="scope">
              <b :class="scope.row.unassigned > 0 ? 'text-green-600' : 'text-gray-400'">{{ scope.row.unassigned }}</b>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="120" fixed="right">
            <template #default="scope">
              <el-button link type="success" :disabled="scope.row.unassigned <= 0" @click="openPutaway(scope.row)"
                v-hasPermi="['biz:wms:stock:operate']">上架</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab3 作业任务 -->
      <el-tab-pane label="作业任务" name="tasks">
        <div class="mb-10px flex items-center">
          <el-radio-group v-model="taskType" @change="getTaskList">
            <el-radio-button label="putaway">上架任务（采购入库）</el-radio-button>
            <el-radio-button label="pick">拣货任务（销售出库）</el-radio-button>
          </el-radio-group>
        </div>
        <el-table v-loading="tasksLoading" :data="taskList" stripe>
          <el-table-column label="来源单号" align="center" prop="sourceCode" min-width="150" />
          <el-table-column label="产品" align="center" prop="productName" min-width="150" show-overflow-tooltip />
          <el-table-column label="仓库" align="center" prop="warehouseName" min-width="110" />
          <el-table-column label="任务量" align="center" prop="quantity" width="90" />
          <el-table-column label="已完成" align="center" width="180">
            <template #default="scope">
              <el-progress :percentage="taskPercent(scope.row)" :stroke-width="12"
                :color="scope.row.status === 1 ? '#67c23a' : '#409eff'" />
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                {{ scope.row.status === 1 ? '已完成' : '进行中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" width="170" :formatter="dateFormatter" />
          <el-table-column label="操作" align="center" width="110" fixed="right">
            <template #default="scope">
              <el-button v-if="scope.row.status === 0" link type="primary" @click="openFromTask(scope.row)"
                v-hasPermi="['biz:wms:stock:operate']">
                {{ scope.row.type === 'putaway' ? '去上架' : '去拣货' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination :total="taskTotal" v-model:page="taskQuery.pageNo"
          v-model:limit="taskQuery.pageSize" @pagination="getTaskList" />
      </el-tab-pane>

      <!-- Tab4 流水 -->
      <el-tab-pane label="库位流水" name="moves">
        <el-table v-loading="movesLoading" :data="moveList" stripe>
          <el-table-column label="时间" align="center" prop="createTime" width="170" :formatter="dateFormatter" />
          <el-table-column label="动作" align="center" prop="moveType" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.moveType === 'putaway' ? 'success' : scope.row.moveType === 'remove' ? 'warning' : 'primary'">
                {{ moveTypeLabel(scope.row.moveType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="产品" align="center" prop="productName" min-width="150" show-overflow-tooltip />
          <el-table-column label="数量" align="center" prop="quantity" width="90" />
          <el-table-column label="源库位" align="center" width="120">
            <template #default="scope">{{ scope.row.fromLocationCode || '未分配区' }}</template>
          </el-table-column>
          <el-table-column label="目标库位" align="center" width="120">
            <template #default="scope">{{ scope.row.toLocationCode || '未分配区' }}</template>
          </el-table-column>
          <el-table-column label="操作人" align="center" prop="operatorName" width="100" />
          <el-table-column label="备注" align="center" prop="remark" min-width="130" show-overflow-tooltip />
        </el-table>
        <Pagination :total="moveTotal" v-model:page="moveQuery.pageNo"
          v-model:limit="moveQuery.pageSize" @pagination="getMoveList" />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <!-- 上架弹窗 -->
  <el-dialog v-model="putawayVisible" title="上架（未分配 → 库位）" width="480px">
    <el-form ref="putawayFormRef" :model="putawayForm" :rules="operateRules" label-width="100px">
      <el-form-item label="产品">
        <el-input :model-value="putawayRow?.productName" disabled />
      </el-form-item>
      <el-form-item label="目标库位" prop="locationId">
        <el-select v-model="putawayForm.locationId" placeholder="选择库位" filterable class="!w-1/1">
          <el-option v-for="l in locationOptions" :key="l.id" :value="l.id!"
            :label="`${l.code}｜${l.warehouseName}`" />
        </el-select>
        <div class="text-12px text-gray-400">可上架量：{{ putawayRow?.unassigned }}</div>
      </el-form-item>
      <el-form-item label="数量" prop="quantity">
        <el-input-number v-model="putawayForm.quantity" :min="1" :precision="0" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="putawayForm.remark" type="textarea" :rows="2" placeholder="选填" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="putawayVisible = false">取 消</el-button>
      <el-button type="primary" :loading="operateLoading" @click="submitPutaway">确 定</el-button>
    </template>
  </el-dialog>

  <!-- 下架/拣货弹窗（库位行发起=库位固定；任务发起=选库位） -->
  <el-dialog v-model="removeVisible" :title="pickFromTask ? '拣货（按任务从库位下架）' : '下架（库位 → 未分配）'" width="480px">
    <el-form ref="removeFormRef" :model="removeForm" :rules="operateRules" label-width="100px">
      <el-form-item v-if="removeRow" label="库位">
        <el-input :model-value="`${removeRow.locationCode}｜${removeRow.productName}`" disabled />
      </el-form-item>
      <template v-else>
        <el-form-item label="产品">
          <el-input :model-value="pickFromTask?.productName" disabled />
        </el-form-item>
        <el-form-item label="拣货库位" prop="locationId">
          <el-select v-model="removeForm.locationId" placeholder="选择有库存的库位" filterable class="!w-1/1">
            <el-option v-for="s in pickLocations" :key="s.locationId" :value="s.locationId!"
              :label="`${s.locationCode}｜库存 ${s.quantity}`" />
          </el-select>
          <div class="text-12px text-gray-400">任务剩余待拣：{{ (pickFromTask?.quantity ?? 0) - (pickFromTask?.doneQuantity ?? 0) }}</div>
        </el-form-item>
      </template>
      <el-form-item label="数量" prop="quantity">
        <el-input-number v-model="removeForm.quantity" :min="1" :max="removeRow?.quantity" :precision="0" class="!w-1/1" />
        <div v-if="removeRow" class="text-12px text-gray-400">库位现有：{{ removeRow?.quantity }}</div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="removeForm.remark" type="textarea" :rows="2" placeholder="选填" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="removeVisible = false">取 消</el-button>
      <el-button type="primary" :loading="operateLoading" @click="submitRemove">确 定</el-button>
    </template>
  </el-dialog>

  <!-- 移库弹窗 -->
  <el-dialog v-model="moveVisible" title="移库（同仓库库位间移动）" width="480px">
    <el-form ref="moveFormRef" :model="moveForm" :rules="operateRules" label-width="100px">
      <el-form-item label="源库位">
        <el-input :model-value="`${moveRow?.locationCode}｜${moveRow?.productName}`" disabled />
      </el-form-item>
      <el-form-item label="目标库位" prop="locationId">
        <el-select v-model="moveForm.locationId" placeholder="选择同仓库的其他库位" filterable class="!w-1/1">
          <el-option v-for="l in sameWarehouseLocations" :key="l.id" :value="l.id!"
            :label="`${l.code}｜${l.warehouseName}`" />
        </el-select>
      </el-form-item>
      <el-form-item label="数量" prop="quantity">
        <el-input-number v-model="moveForm.quantity" :min="1" :max="moveRow?.quantity" :precision="0" class="!w-1/1" />
        <div class="text-12px text-gray-400">库位现有：{{ moveRow?.quantity }}</div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="moveForm.remark" type="textarea" :rows="2" placeholder="选填" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="moveVisible = false">取 消</el-button>
      <el-button type="primary" :loading="operateLoading" @click="submitMove">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as StockApi from '@/api/wms/stock'
import * as LocationApi from '@/api/wms/location'
import type { WmsLocationStockVO, WmsUnassignedVO, WmsMoveVO } from '@/api/wms/stock'
import type { WmsLocationVO } from '@/api/wms/location'

defineOptions({ name: 'BizWmsStock' })

const message = useMessage()

const warehouseId = ref<number>()
const productName = ref('')
const activeTab = ref('stock')

const operateRules = reactive({
  locationId: [{ required: true, message: '库位不能为空', trigger: 'change' }],
  quantity: [{ required: true, message: '数量不能为空', trigger: 'blur' }]
})

/** Tab1 库位库存 */
const loading = ref(false)
const stockList = ref<WmsLocationStockVO[]>([])
const stockTotal = ref(0)
const stockQuery = reactive({ pageNo: 1, pageSize: 10 })
const getStockList = async () => {
  loading.value = true
  try {
    const data = await StockApi.getStockPage({
      ...stockQuery,
      warehouseId: warehouseId.value,
      productName: productName.value || undefined
    })
    stockList.value = data.list
    stockTotal.value = data.total
  } finally {
    loading.value = false
  }
}

/** Tab2 未分配 */
const unassignedLoading = ref(false)
const unassignedList = ref<WmsUnassignedVO[]>([])
const filteredUnassigned = computed(() =>
  unassignedList.value.filter(
    (u) =>
      (!warehouseId.value || u.warehouseId === warehouseId.value) &&
      (!productName.value || (u.productName || '').includes(productName.value))
  )
)
const getUnassignedList = async () => {
  unassignedLoading.value = true
  try {
    unassignedList.value = await StockApi.getUnassigned()
  } finally {
    unassignedLoading.value = false
  }
}

/** Tab3 流水 */
const movesLoading = ref(false)
const moveList = ref<WmsMoveVO[]>([])
const moveTotal = ref(0)
const moveQuery = reactive({ pageNo: 1, pageSize: 10 })
const getMoveList = async () => {
  movesLoading.value = true
  try {
    const data = await StockApi.getMovePage({
      ...moveQuery,
      warehouseId: warehouseId.value,
      productName: productName.value || undefined
    })
    moveList.value = data.list
    moveTotal.value = data.total
  } finally {
    movesLoading.value = false
  }
}

const moveTypeLabel = (t?: string) =>
  t === 'putaway' ? '上架' : t === 'remove' ? '下架' : t === 'move' ? '移库' : t || ''

const reloadAll = () => {
  if (activeTab.value === 'stock') {
    stockQuery.pageNo = 1
    getStockList()
  } else if (activeTab.value === 'unassigned') {
    getUnassignedList()
  } else if (activeTab.value === 'tasks') {
    taskQuery.pageNo = 1
    getTaskList()
  } else {
    moveQuery.pageNo = 1
    getMoveList()
  }
}

/** Tab3 作业任务 */
const tasksLoading = ref(false)
const taskList = ref<StockApi.WmsTaskVO[]>([])
const taskTotal = ref(0)
const taskQuery = reactive({ pageNo: 1, pageSize: 10 })
const taskType = ref<'putaway' | 'pick'>('putaway')
const getTaskList = async () => {
  tasksLoading.value = true
  try {
    const data = await StockApi.getTaskPage(taskType.value, {
      ...taskQuery,
      warehouseId: warehouseId.value,
      productName: productName.value || undefined
    })
    taskList.value = data.list
    taskTotal.value = data.total
  } finally {
    tasksLoading.value = false
  }
}
const taskPercent = (row: StockApi.WmsTaskVO) =>
  row.quantity ? Math.min(100, Math.round(((row.doneQuantity || 0) / row.quantity) * 100)) : 0

/** 从任务发起：上架（预填产品/仓库/数量=任务剩余） */
const openFromTask = async (task: StockApi.WmsTaskVO) => {
  const remain = (task.quantity || 0) - (task.doneQuantity || 0)
  if (task.type === 'putaway') {
    putawayRow.value = {
      productId: task.productId,
      warehouseId: task.warehouseId,
      productName: task.productName,
      warehouseName: task.warehouseName,
      mainQuantity: 0,
      allocated: 0,
      unassigned: remain
    } as WmsUnassignedVO
    putawayForm.locationId = undefined
    putawayForm.quantity = remain
    putawayForm.remark = `任务 ${task.sourceCode}`
    await loadLocations(task.warehouseId)
    putawayVisible.value = true
  } else {
    // 拣货：查该仓库该产品有库存的库位，弹窗里选库位
    const data = await StockApi.getStockPage({
      pageNo: 1, pageSize: 100,
      warehouseId: task.warehouseId, productName: task.productName
    })
    pickLocations.value = data.list
    pickFromTask.value = task
    removeForm.locationId = undefined
    removeForm.quantity = remain
    removeForm.remark = `任务 ${task.sourceCode}`
    removeVisible.value = true
  }
}

const resetQuery = () => {
  warehouseId.value = undefined
  productName.value = ''
  reloadAll()
}

/** 库位下拉 */
const locationOptions = ref<WmsLocationVO[]>([])
const loadLocations = async (warehouseId?: number) => {
  locationOptions.value = await LocationApi.getSimpleLocationList(warehouseId)
}

/** 上架 */
const putawayVisible = ref(false)
const operateLoading = ref(false)
const putawayFormRef = ref()
const putawayRow = ref<WmsUnassignedVO>()
const putawayForm = reactive({ locationId: undefined, quantity: 1, remark: '' })
const openPutaway = async (row: WmsUnassignedVO) => {
  putawayRow.value = row
  putawayForm.locationId = undefined
  putawayForm.quantity = row.unassigned
  putawayForm.remark = ''
  await loadLocations(row.warehouseId)
  putawayVisible.value = true
}
const submitPutaway = async () => {
  await putawayFormRef.value.validate()
  operateLoading.value = true
  try {
    await StockApi.putaway({
      productId: putawayRow.value!.productId,
      warehouseId: putawayRow.value!.warehouseId,
      locationId: putawayForm.locationId!,
      quantity: putawayForm.quantity,
      remark: putawayForm.remark || undefined
    })
    message.success('上架成功')
    putawayVisible.value = false
    await Promise.all([getUnassignedList(), getStockList()])
  } finally {
    operateLoading.value = false
  }
}

/** 下架（库位行发起） */
const removeVisible = ref(false)
const removeFormRef = ref()
const removeRow = ref<WmsLocationStockVO>()
const pickFromTask = ref<StockApi.WmsTaskVO>()
const pickLocations = ref<WmsLocationStockVO[]>([])
const removeForm = reactive<{ locationId: number | undefined; quantity: number; remark: string }>({
  locationId: undefined, quantity: 1, remark: ''
})
const openRemove = (row: WmsLocationStockVO) => {
  removeRow.value = row
  pickFromTask.value = undefined
  removeForm.locationId = undefined
  removeForm.quantity = row.quantity || 1
  removeForm.remark = ''
  removeVisible.value = true
}
const submitRemove = async () => {
  await removeFormRef.value.validate()
  operateLoading.value = true
  try {
    if (pickFromTask.value) {
      await StockApi.remove({
        productId: pickFromTask.value.productId!,
        fromLocationId: removeForm.locationId!,
        quantity: removeForm.quantity,
        remark: removeForm.remark || undefined
      })
      message.success('拣货成功')
    } else {
      await StockApi.remove({
        productId: removeRow.value!.productId!,
        fromLocationId: removeRow.value!.locationId!,
        quantity: removeForm.quantity,
        remark: removeForm.remark || undefined
      })
      message.success('下架成功')
    }
    removeVisible.value = false
    await Promise.all([getStockList(), getUnassignedList()])
  } finally {
    operateLoading.value = false
  }
}

/** 移库 */
const moveVisible = ref(false)
const moveFormRef = ref()
const moveRow = ref<WmsLocationStockVO>()
const moveForm = reactive({ locationId: undefined, quantity: 1, remark: '' })
const sameWarehouseLocations = computed(() =>
  locationOptions.value.filter((l) => l.id !== moveRow.value?.locationId)
)
const openMove = async (row: WmsLocationStockVO) => {
  moveRow.value = row
  moveForm.locationId = undefined
  moveForm.quantity = row.quantity || 1
  moveForm.remark = ''
  await loadLocations(row.warehouseId)
  moveVisible.value = true
}
const submitMove = async () => {
  await moveFormRef.value.validate()
  operateLoading.value = true
  try {
    await StockApi.move({
      productId: moveRow.value!.productId!,
      fromLocationId: moveRow.value!.locationId!,
      locationId: moveForm.locationId!,
      quantity: moveForm.quantity,
      remark: moveForm.remark || undefined
    })
    message.success('移库成功')
    moveVisible.value = false
    await getStockList()
  } finally {
    operateLoading.value = false
  }
}

onMounted(() => {
  getStockList()
  getUnassignedList()
})
</script>
