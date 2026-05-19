<template>
  <div class="summary-page">
    <div class="summary-page__toolbar">
      <span class="summary-page__toolbar-label">选择任务</span>
      <el-select
        v-model="taskId"
        placeholder="全部任务"
        filterable
        clearable
        class="summary-page__task-select"
        @change="onTaskChange"
      >
        <el-option v-for="t in tasks" :key="t.taskId" :label="`${t.taskName} (${t.taskStatus})`" :value="t.taskId" />
      </el-select>
      <el-button type="primary" :disabled="!taskId" @click="exportXlsx">导出</el-button>
    </div>

    <div class="summary-page__table-wrap">
      <div class="summary-page__table-x">
        <el-table
          :data="items"
          border
          stripe
          class="summary-table"
          v-loading="loading"
          :style="summaryTableLayoutStyle"
          :header-cell-style="headerCellStyle"
          empty-text="暂无评测数据"
        >
          <el-table-column
            v-for="c in summaryColumns"
            :key="c.prop"
            :prop="c.prop"
            :label="c.label"
            :width="c.width"
            :min-width="c.minWidth"
            :fixed="c.fixed"
            :show-overflow-tooltip="c.showOverflow ?? false"
          />
        </el-table>
      </div>
      <div class="summary-page__pager">
        <el-pagination
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="page"
          @current-change="load"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { CSSProperties } from 'vue'
import client from '../api/client'

interface TaskRow {
  taskId: string
  taskName: string
  taskStatus: string
}

/** 与后端 SummaryRow / 表 t_summary_report 及导出列顺序一致；跨任务列表含 taskName */
interface SummaryRowItem {
  taskId?: string | null
  taskName?: string | null
  orderId?: string | null
  area?: string | null
  representativeOffice?: string | null
  country?: string | null
  accounts?: string | null
  project?: string | null
  poId?: string | null
  productDomain?: string | null
  grossProfit?: number | null
  incomeMonth?: string | null
  hwPspGrossProfit?: number | null
  hwStandardGrossProfit?: number | null
  beforeTotalPrice?: number | null
  afterTotalPrice?: number | null
  totalPriceIncrease?: number | null
  priceIncreaseRate?: number | null
  softwareHistoryPrice?: number | null
  softwarePrice?: number | null
  softwarePriceIncreaseRate?: number | null
}

type SummaryColKey = keyof SummaryRowItem

interface SummaryColDef {
  prop: SummaryColKey
  label: string
  width?: number
  minWidth?: number
  fixed?: 'left'
  showOverflow?: boolean
}

/** 单任务视图下列顺序与导出一致；跨任务时在左侧附加任务列 */
const baseSummaryColumns: SummaryColDef[] = [
  { prop: 'orderId', label: '合同号', width: 140, fixed: 'left' },
  { prop: 'area', label: '区域', width: 100 },
  { prop: 'representativeOffice', label: '代表处', width: 110 },
  { prop: 'country', label: '国家', width: 100 },
  { prop: 'accounts', label: '客户群', width: 100 },
  { prop: 'project', label: '项目', minWidth: 120, showOverflow: true },
  { prop: 'poId', label: 'PO号', width: 120 },
  { prop: 'productDomain', label: '产品领域', width: 120 },
  { prop: 'grossProfit', label: '销毛', width: 100 },
  { prop: 'incomeMonth', label: '收入确定时间', width: 140 },
  { prop: 'hwPspGrossProfit', label: '硬件PSP制毛', width: 120 },
  { prop: 'hwStandardGrossProfit', label: '硬件标准口径制毛', width: 150 },
  { prop: 'beforeTotalPrice', label: '提价前总价格', width: 120 },
  { prop: 'afterTotalPrice', label: '提价后总价格', width: 120 },
  { prop: 'totalPriceIncrease', label: '价格上涨', width: 100 },
  { prop: 'priceIncreaseRate', label: '价格涨幅', width: 110 },
  { prop: 'softwareHistoryPrice', label: '软件历史价格', width: 120 },
  { prop: 'softwarePrice', label: '本次软件价格', width: 120 },
  { prop: 'softwarePriceIncreaseRate', label: '软件价格变化', width: 120 },
]

const summaryColumns = computed((): SummaryColDef[] => {
  if (!taskId.value) {
    return [
      { prop: 'taskName', label: '任务名称', minWidth: 160, fixed: 'left', showOverflow: true },
      { prop: 'taskId', label: '任务ID', width: 220, fixed: 'left', showOverflow: true },
      ...baseSummaryColumns.map((c) => (c.prop === 'orderId' ? { ...c, fixed: undefined } : c)),
    ]
  }
  return baseSummaryColumns
})

const summaryTablePixelWidth = computed(() =>
  summaryColumns.value.reduce((acc, c) => acc + (c.width ?? c.minWidth ?? 0), 0),
)

const summaryTableLayoutStyle = computed(() => {
  const w = summaryTablePixelWidth.value
  return { width: '100%', minWidth: `${w}px` }
})

const headerCellStyle = (): CSSProperties => ({
  background: '#b3d7f2',
  color: '#0f2744',
  fontWeight: '600',
})

const tasks = ref<TaskRow[]>([])
const taskId = ref('')
const items = ref<SummaryRowItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const loading = ref(false)

async function loadTasks() {
  const { data } = await client.get<TaskRow[]>('/tasks')
  tasks.value = data ?? []
}

function onTaskChange() {
  page.value = 1
  load()
}

async function load() {
  loading.value = true
  try {
    if (!taskId.value) {
      const { data } = await client.get<{ total: number; items: SummaryRowItem[] }>('/summary', {
        params: { page: page.value - 1, size: pageSize },
      })
      total.value = data.total
      items.value = data.items ?? []
    } else {
      const { data } = await client.get<{ total: number; items: SummaryRowItem[] }>(
        `/tasks/${taskId.value}/summary`,
        {
          params: { page: page.value - 1, size: pageSize },
        },
      )
      total.value = data.total
      items.value = data.items ?? []
    }
  } catch {
    ElMessage.error('加载评测结果失败')
  } finally {
    loading.value = false
  }
}

async function exportXlsx() {
  if (!taskId.value) return
  const res = await client.get(`/tasks/${taskId.value}/summary/export`, { responseType: 'blob' })
  const url = window.URL.createObjectURL(new Blob([res.data]))
  const a = document.createElement('a')
  a.href = url
  a.download = `summary-${taskId.value}.xlsx`
  a.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('已开始下载')
}

onMounted(async () => {
  await loadTasks()
  await load()
})
</script>

<style scoped>
.summary-page {
  margin: calc(-1 * var(--el-main-padding, 20px));
  padding: var(--el-main-padding, 20px);
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
  background: #f0f2f5;
}

.summary-page__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.summary-page__toolbar-label {
  font-size: 14px;
  color: #303133;
}

.summary-page__task-select {
  width: min(360px, 100%);
  flex: 1 1 200px;
}

.summary-page__table-wrap {
  width: 100%;
  box-sizing: border-box;
  padding: 12px 12px 8px;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.summary-page__table-x {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
}

.summary-table {
  --el-table-border-color: #dcdfe6;
  --el-table-header-bg-color: #b3d7f2;
}

.summary-table :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.summary-table :deep(.el-table__body-wrapper .el-table__row--striped td) {
  background-color: #fafcff !important;
}

.summary-page__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 4px;
}
</style>
