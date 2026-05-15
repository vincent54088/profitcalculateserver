<template>
  <div class="preview-page">
    <div class="preview-page__header">
      <el-page-header class="preview-page__page-header" @back="$router.push('/tasks')" content="预览与编辑" />
      <div class="preview-page__meta">任务: {{ taskId }} &nbsp;|&nbsp; 状态: {{ status }}</div>
    </div>

    <div class="preview-page__table-wrap">
      <div class="preview-page__table-x">
        <el-table
          :data="items"
          border
          stripe
          class="preview-table"
          :style="previewTableLayoutStyle"
          :header-cell-style="headerCellStyle"
          @row-dblclick="onDblClick"
        >
          <el-table-column
            v-for="c in previewColumns"
            :key="c.prop"
            :prop="c.prop"
            :width="c.width"
            :min-width="c.minWidth"
            :fixed="c.fixed"
            :show-overflow-tooltip="c.showOverflow ?? false"
          >
            <template #header>
              <PreviewColumnHeader
                :label="c.label"
                :field="c.prop"
                :selected-values="columnFilters[c.prop] ?? []"
                :options="columnOptionsByField[c.prop] ?? []"
                :loading-options="!!columnOptionsLoading[c.prop]"
                @request-options="() => loadColumnOptions(c.prop)"
                @apply="onColumnFilterApply"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="preview-page__pager">
        <el-pagination
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="page"
          @current-change="load"
        />
      </div>
    </div>

    <el-dialog v-model="editDlg" title="编辑" width="900px" top="5vh">
      <el-scrollbar max-height="78vh">
        <el-form v-if="edit" label-width="160px">
          <el-divider content-position="left">订单信息</el-divider>
          <el-form-item label="合同号"><el-input v-model="edit.orderId" disabled /></el-form-item>
          <el-form-item label="区域"><el-input v-model="edit.area" /></el-form-item>
          <el-form-item label="代表处"><el-input v-model="edit.representativeOffice" /></el-form-item>
          <el-form-item label="国家"><el-input v-model="edit.country" /></el-form-item>
          <el-form-item label="客户群"><el-input v-model="edit.accounts" /></el-form-item>
          <el-form-item label="项目"><el-input v-model="edit.project" /></el-form-item>
          <el-form-item label="PO号"><el-input v-model="edit.poId" /></el-form-item>
          <el-form-item label="产品领域"><el-input v-model="edit.productDomain" /></el-form-item>

          <el-divider content-position="left">订单明细</el-divider>
          <el-form-item label="销毛"><el-input v-model.number="edit.grossProfit" type="number" step="any" /></el-form-item>
          <el-form-item label="型号"><el-input v-model="edit.deviceType" /></el-form-item>
          <el-form-item label="编码"><el-input v-model="edit.deviceId" /></el-form-item>
          <el-form-item label="编码描述"><el-input v-model="edit.description" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="软硬件分类"><el-input v-model="edit.categoryType" /></el-form-item>
          <el-form-item label="硬件类型"><el-input v-model="edit.hardwareType" /></el-form-item>
          <el-form-item label="使用场景"><el-input v-model="edit.scene" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="数量"><el-input-number v-model="edit.deviceCount" :min="0" controls-position="right" /></el-form-item>
          <el-form-item label="预计收入触发时间"><el-input v-model="edit.incomeMonth" /></el-form-item>
          <el-form-item label="币种"><el-input v-model="edit.currency" /></el-form-item>
          <el-form-item label="目录价"><el-input v-model.number="edit.listPrice" type="number" step="any" /></el-form-item>
          <el-form-item label="提价前-价位"><el-input v-model.number="edit.beforeTier" type="number" step="any" /></el-form-item>
          <el-form-item label="提价前-价格"><el-input v-model.number="edit.beforePrice" type="number" step="any" /></el-form-item>
          <el-form-item label="优惠说明(前)"><el-input v-model="edit.discountDescBefore" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="提价后-价位"><el-input v-model.number="edit.afterTier" type="number" step="any" /></el-form-item>
          <el-form-item label="提价后-价格"><el-input v-model.number="edit.afterPrice" type="number" step="any" /></el-form-item>
          <el-form-item label="优惠说明(后)"><el-input v-model="edit.discountDescAfter" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="价位提价"><el-input v-model.number="edit.tierIncrease" type="number" step="any" /></el-form-item>
          <el-form-item label="价格提价"><el-input v-model.number="edit.priceIncrease" type="number" step="any" /></el-form-item>
          <el-form-item label="提价后-价位折扣率"><el-input v-model.number="edit.afterTierDiscountRate" type="number" step="any" /></el-form-item>
          <el-form-item label="提价后-价格折扣率"><el-input v-model.number="edit.afterPriceDiscountRate" type="number" step="any" /></el-form-item>
          <el-form-item label="提价前-总价"><el-input v-model.number="edit.beforeTotalPrice" type="number" step="any" /></el-form-item>
          <el-form-item label="提价后-总价"><el-input v-model.number="edit.afterTotalPrice" type="number" step="any" /></el-form-item>
          <el-form-item label="总价上涨"><el-input v-model.number="edit.totalPriceIncrease" type="number" step="any" /></el-form-item>
          <el-form-item label="涨价%"><el-input v-model.number="edit.totalIncreaseRate" type="number" step="any" /></el-form-item>
          <el-form-item label="备注"><el-input v-model="edit.additionInfo" type="textarea" :rows="2" /></el-form-item>
        </el-form>
      </el-scrollbar>
      <template #footer>
        <el-button @click="editDlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存并重算</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { CSSProperties } from 'vue'
import client from '../api/client'
import PreviewColumnHeader from '../components/PreviewColumnHeader.vue'

const route = useRoute()
const taskId = computed(() => String(route.params.taskId))

/** 与导入 Excel 列一致：订单头 + 明细（字段名与后端 JSON camelCase 一致） */
interface DetailRowItem {
  id: string
  taskId?: string
  orderId: string
  area?: string | null
  representativeOffice?: string | null
  country?: string | null
  accounts?: string | null
  project?: string | null
  poId?: string | null
  productDomain?: string | null
  grossProfit?: number | null
  deviceType?: string | null
  deviceId?: string | null
  description?: string | null
  categoryType?: string | null
  hardwareType?: string | null
  scene?: string | null
  deviceCount?: number | null
  incomeMonth?: string | null
  currency?: string | null
  listPrice?: number | null
  beforeTier?: number | null
  beforePrice?: number | null
  discountDescBefore?: string | null
  afterTier?: number | null
  afterPrice?: number | null
  discountDescAfter?: string | null
  tierIncrease?: number | null
  priceIncrease?: number | null
  afterTierDiscountRate?: number | null
  afterPriceDiscountRate?: number | null
  beforeTotalPrice?: number | null
  afterTotalPrice?: number | null
  totalPriceIncrease?: number | null
  totalIncreaseRate?: number | null
  additionInfo?: string | null
}

type PreviewColKey = keyof Pick<
  DetailRowItem,
  | 'orderId'
  | 'area'
  | 'representativeOffice'
  | 'country'
  | 'accounts'
  | 'project'
  | 'poId'
  | 'productDomain'
  | 'grossProfit'
  | 'deviceType'
  | 'deviceId'
  | 'description'
  | 'categoryType'
  | 'hardwareType'
  | 'scene'
  | 'deviceCount'
  | 'incomeMonth'
  | 'currency'
  | 'listPrice'
  | 'beforeTier'
  | 'beforePrice'
  | 'discountDescBefore'
  | 'afterTier'
  | 'afterPrice'
  | 'discountDescAfter'
  | 'tierIncrease'
  | 'priceIncrease'
  | 'afterTierDiscountRate'
  | 'afterPriceDiscountRate'
  | 'beforeTotalPrice'
  | 'afterTotalPrice'
  | 'totalPriceIncrease'
  | 'totalIncreaseRate'
  | 'additionInfo'
>

interface PreviewColDef {
  prop: PreviewColKey
  label: string
  width?: number
  minWidth?: number
  fixed?: 'left'
  showOverflow?: boolean
}

/** 列宽之和须不小于实际列总宽，否则表体横向 scrollWidth 偏小，滑到最右仍会裁切末列 */
const previewColumns: PreviewColDef[] = [
  { prop: 'orderId', label: '合同号', width: 140, fixed: 'left' },
  { prop: 'area', label: '区域', width: 100 },
  { prop: 'representativeOffice', label: '代表处', width: 110 },
  { prop: 'country', label: '国家', width: 100 },
  { prop: 'accounts', label: '客户群', width: 100 },
  { prop: 'project', label: '项目', width: 120 },
  { prop: 'poId', label: 'PO号', width: 120 },
  { prop: 'productDomain', label: '产品领域', width: 120 },
  { prop: 'grossProfit', label: '销毛', width: 100 },
  { prop: 'deviceType', label: '型号', width: 120 },
  { prop: 'deviceId', label: '编码', width: 120 },
  { prop: 'description', label: '编码描述', minWidth: 140, showOverflow: true },
  { prop: 'categoryType', label: '软硬件分类', width: 120 },
  { prop: 'hardwareType', label: '硬件类型', width: 110 },
  { prop: 'scene', label: '使用场景', minWidth: 120, showOverflow: true },
  { prop: 'deviceCount', label: '数量', width: 80 },
  { prop: 'incomeMonth', label: '预计收入触发时间', width: 150 },
  { prop: 'currency', label: '币种', width: 80 },
  { prop: 'listPrice', label: '目录价', width: 100 },
  { prop: 'beforeTier', label: '提价前-价位', width: 120 },
  { prop: 'beforePrice', label: '提价前-价格', width: 120 },
  { prop: 'discountDescBefore', label: '优惠说明(前)', minWidth: 120, showOverflow: true },
  { prop: 'afterTier', label: '提价后-价位', width: 120 },
  { prop: 'afterPrice', label: '提价后-价格', width: 120 },
  { prop: 'discountDescAfter', label: '优惠说明(后)', minWidth: 120, showOverflow: true },
  { prop: 'tierIncrease', label: '价位提价', width: 100 },
  { prop: 'priceIncrease', label: '价格提价', width: 100 },
  { prop: 'afterTierDiscountRate', label: '提价后-价位折扣率', width: 150 },
  { prop: 'afterPriceDiscountRate', label: '提价后-价格折扣率', width: 150 },
  { prop: 'beforeTotalPrice', label: '提价前-总价', width: 120 },
  { prop: 'afterTotalPrice', label: '提价后-总价', width: 120 },
  { prop: 'totalPriceIncrease', label: '总价上涨', width: 100 },
  { prop: 'totalIncreaseRate', label: '涨价%', width: 90 },
  { prop: 'additionInfo', label: '备注', minWidth: 120, showOverflow: true },
]

const previewTablePixelWidth = computed(() =>
  previewColumns.reduce((acc, c) => acc + (c.width ?? c.minWidth ?? 0), 0),
)

const previewTableLayoutStyle = computed(() => {
  const w = previewTablePixelWidth.value
  return { width: `${w}px`, minWidth: `${w}px` }
})

const headerCellStyle = (): CSSProperties => ({
  background: '#b3d7f2',
  color: '#0f2744',
  fontWeight: '600',
})

const items = ref<DetailRowItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const status = ref('')
const columnFilters = ref<Record<string, string[]>>({})
const columnOptionsByField = ref<Record<string, string[]>>({})
const columnOptionsLoading = ref<Record<string, boolean>>({})
const editDlg = ref(false)
const edit = ref<DetailRowItem | null>(null)
const saving = ref(false)
let timer: number | undefined

async function loadStatus() {
  const { data } = await client.get<{ taskStatus: string }>(`/tasks/${taskId.value}/status`)
  status.value = data.taskStatus
}

function columnFiltersPayload(): Record<string, string[]> | undefined {
  const raw = columnFilters.value
  const out: Record<string, string[]> = {}
  for (const k of Object.keys(raw)) {
    const arr = raw[k]
    if (arr != null && arr.length > 0) {
      out[k] = [...arr]
    }
  }
  return Object.keys(out).length ? out : undefined
}

async function load() {
  const cf = columnFiltersPayload()
  const { data } = await client.get<{ total: number; items: DetailRowItem[] }>(`/tasks/${taskId.value}/details`, {
    params: {
      page: page.value - 1,
      size: pageSize,
      ...(cf ? { columnFilters: JSON.stringify(cf) } : {}),
    },
  })
  total.value = data.total
  items.value = data.items
}

async function loadColumnOptions(field: string) {
  columnOptionsLoading.value = { ...columnOptionsLoading.value, [field]: true }
  try {
    const other: Record<string, string[]> = { ...columnFilters.value }
    delete other[field]
    const payload = columnFiltersPayloadFrom(other)
    const { data } = await client.get<string[]>(`/tasks/${taskId.value}/details/column-options`, {
      params: {
        field,
        ...(payload ? { columnFilters: JSON.stringify(payload) } : {}),
      },
    })
    columnOptionsByField.value = { ...columnOptionsByField.value, [field]: data ?? [] }
  } catch {
    ElMessage.error('加载筛选项失败')
    columnOptionsByField.value = { ...columnOptionsByField.value, [field]: [] }
  } finally {
    columnOptionsLoading.value = { ...columnOptionsLoading.value, [field]: false }
  }
}

function columnFiltersPayloadFrom(map: Record<string, string[]>): Record<string, string[]> | undefined {
  const out: Record<string, string[]> = {}
  for (const k of Object.keys(map)) {
    const arr = map[k]
    if (arr != null && arr.length > 0) {
      out[k] = [...arr]
    }
  }
  return Object.keys(out).length ? out : undefined
}

function onColumnFilterApply(payload: { field: string; values: string[] }) {
  const next = { ...columnFilters.value }
  if (payload.values.length === 0) {
    delete next[payload.field]
  } else {
    next[payload.field] = [...payload.values]
  }
  columnFilters.value = next
  columnOptionsByField.value = {}
  page.value = 1
  load()
}

watch(taskId, () => {
  columnFilters.value = {}
  columnOptionsByField.value = {}
  page.value = 1
  load()
  loadStatus()
})

function onDblClick(row: DetailRowItem) {
  edit.value = JSON.parse(JSON.stringify(row))
  editDlg.value = true
}

async function save() {
  if (!edit.value) return
  saving.value = true
  try {
    await client.put(`/tasks/${taskId.value}/details/${edit.value.id}`, edit.value)
    ElMessage.success('已保存，后台重算中')
    editDlg.value = false
    columnOptionsByField.value = {}
    await load()
    await loadStatus()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  load()
  loadStatus()
  timer = window.setInterval(async () => {
    await loadStatus()
  }, 2000)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.preview-page {
  margin: calc(-1 * var(--el-main-padding, 20px));
  padding: 0 0 var(--el-main-padding, 20px);
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
  background: #f0f2f5;
}

.preview-page__header {
  padding: 14px 20px 16px;
  background: linear-gradient(180deg, #d9e6f5 0%, #c8d9ec 100%);
  border-bottom: 1px solid #a8bdd4;
  margin-bottom: 16px;
}

.preview-page__page-header {
  margin-bottom: 8px;
}

.preview-page__page-header :deep(.el-page-header__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1a2f45;
}

.preview-page__page-header :deep(.el-page-header__content) {
  color: #1a2f45;
}

.preview-page__meta {
  font-size: 12px;
  color: #2c3e50;
}

.preview-page__table-wrap {
  margin: 0 20px 20px;
  padding: 12px 12px 8px;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.preview-page__table-x {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
}

.preview-table {
  --el-table-border-color: #dcdfe6;
  --el-table-header-bg-color: #b3d7f2;
}

.preview-table :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.preview-table :deep(.el-table__body-wrapper .el-table__row--striped td) {
  background-color: #fafcff !important;
}

.preview-table :deep(.el-table__header .cell) {
  overflow: visible;
}

.preview-page__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 4px;
}
</style>
