<template>
  <div class="cost-page">
    <div class="cost-page__toolbar">
      <div class="cost-page__toolbar-left">
        <el-button v-if="!showHelpAlert" link type="primary" @click="showHelpAlert = true">显示说明</el-button>
        <el-button v-if="result && !resultPanelVisible" link type="primary" @click="resultPanelVisible = true">显示导入结果</el-button>
      </div>
      <div class="cost-page__toolbar-actions">
        <el-button type="primary" @click="dlg = true">更新</el-button>
        <el-button @click="loadTable">刷新列表</el-button>
      </div>
    </div>
    <el-alert
      v-if="showHelpAlert"
      type="info"
      show-icon
      closable
      title="说明"
      description="上传设备成本 Excel，按「编码」与 device_id 对应，存在则更新、不存在则插入。支持单行扁平表头（PSP成本-1月…），或双行合并表头（首行 PSP成本/标准成本分组，次行为「N月」列）。下方表格为库中当前数据；双击表格行可在线编辑并保存（与 Excel 导入同为按编码 upsert）。"
      @close="showHelpAlert = false"
    />

    <el-dialog v-model="dlg" title="上传成本 Excel" width="520px" align-center destroy-on-close>
      <el-upload drag :auto-upload="false" :limit="1" :on-change="onFile">
        <div>拖拽文件到此处，或点击选择</div>
      </el-upload>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="doUpload">上传</el-button>
      </template>
    </el-dialog>

    <el-card v-if="result && resultPanelVisible" class="cost-page__result">
      <template #header>
        <div class="cost-page__result-head">
          <span>导入结果</span>
          <el-button link type="primary" @click="resultPanelVisible = false">隐藏</el-button>
        </div>
      </template>
      <div>成功行数: {{ result.successCount }}</div>
      <div>失败行数: {{ result.failCount }}</div>
      <div v-if="result.errors?.length" class="cost-page__errors">
        <div v-for="(e, i) in result.errors.slice(0, 30)" :key="i">{{ e }}</div>
      </div>
    </el-card>

    <div class="cost-page__table-wrap">
      <div class="cost-page__table-x">
        <el-table
          :data="items"
          border
          stripe
          class="cost-table"
          v-loading="tableLoading"
          :style="costTableLayoutStyle"
          :header-cell-style="headerCellStyle"
          @row-dblclick="onRowDblClick"
        >
          <el-table-column
            v-for="c in costColumns"
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
      <div class="cost-page__pager">
        <el-pagination
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="page"
          @current-change="loadTable"
        />
      </div>
    </div>

    <el-dialog v-model="editDlg" title="编辑成本" width="900px" top="5vh" align-center destroy-on-close>
      <el-scrollbar max-height="78vh">
        <el-form v-if="edit" label-width="140px" class="cost-edit-form">
          <el-divider content-position="left">基础信息</el-divider>
          <el-form-item label="编码">
            <el-input v-model="edit.deviceId" disabled />
          </el-form-item>
          <el-form-item label="编码描述">
            <el-input v-model="edit.description" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="型号">
            <el-input v-model="edit.deviceType" />
          </el-form-item>
          <el-form-item label="币种">
            <el-input v-model="edit.currency" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="edit.additionInfo" type="textarea" :rows="2" />
          </el-form-item>

          <el-divider content-position="left">PSP 成本（各月）</el-divider>
          <el-row :gutter="12">
            <el-col v-for="(k, i) in MONTH_PSP_KEYS" :key="k" :span="8">
              <el-form-item :label="'PSP-' + (i + 1) + '月'">
                <el-input v-model.number="edit[k]" type="number" step="any" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">标准成本（各月）</el-divider>
          <el-row :gutter="12">
            <el-col v-for="(k, i) in MONTH_STD_KEYS" :key="k" :span="8">
              <el-form-item :label="'标准-' + (i + 1) + '月'">
                <el-input v-model.number="edit[k]" type="number" step="any" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-scrollbar>
      <template #footer>
        <el-button @click="editDlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCost">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'
import type { CSSProperties } from 'vue'
import client from '../api/client'

/** 与后端 CostRow JSON（camelCase）一致 */
interface CostRowItem {
  deviceId?: string | null
  description?: string | null
  deviceType?: string | null
  currency?: string | null
  pspM01?: number | null
  pspM02?: number | null
  pspM03?: number | null
  pspM04?: number | null
  pspM05?: number | null
  pspM06?: number | null
  pspM07?: number | null
  pspM08?: number | null
  pspM09?: number | null
  pspM10?: number | null
  pspM11?: number | null
  pspM12?: number | null
  stdM01?: number | null
  stdM02?: number | null
  stdM03?: number | null
  stdM04?: number | null
  stdM05?: number | null
  stdM06?: number | null
  stdM07?: number | null
  stdM08?: number | null
  stdM09?: number | null
  stdM10?: number | null
  stdM11?: number | null
  stdM12?: number | null
  additionInfo?: string | null
}

type CostColKey = keyof CostRowItem

interface CostColDef {
  prop: CostColKey
  label: string
  width?: number
  minWidth?: number
  fixed?: 'left'
  showOverflow?: boolean
}

const costColumns: CostColDef[] = [
  { prop: 'deviceId', label: '编码', width: 120, fixed: 'left' },
  { prop: 'description', label: '编码描述', minWidth: 140, showOverflow: true },
  { prop: 'deviceType', label: '型号', width: 110 },
  { prop: 'currency', label: '币种', width: 72 },
  { prop: 'pspM01', label: 'PSP-1月', width: 88 },
  { prop: 'pspM02', label: 'PSP-2月', width: 88 },
  { prop: 'pspM03', label: 'PSP-3月', width: 88 },
  { prop: 'pspM04', label: 'PSP-4月', width: 88 },
  { prop: 'pspM05', label: 'PSP-5月', width: 88 },
  { prop: 'pspM06', label: 'PSP-6月', width: 88 },
  { prop: 'pspM07', label: 'PSP-7月', width: 88 },
  { prop: 'pspM08', label: 'PSP-8月', width: 88 },
  { prop: 'pspM09', label: 'PSP-9月', width: 88 },
  { prop: 'pspM10', label: 'PSP-10月', width: 92 },
  { prop: 'pspM11', label: 'PSP-11月', width: 92 },
  { prop: 'pspM12', label: 'PSP-12月', width: 92 },
  { prop: 'stdM01', label: '标准-1月', width: 88 },
  { prop: 'stdM02', label: '标准-2月', width: 88 },
  { prop: 'stdM03', label: '标准-3月', width: 88 },
  { prop: 'stdM04', label: '标准-4月', width: 88 },
  { prop: 'stdM05', label: '标准-5月', width: 88 },
  { prop: 'stdM06', label: '标准-6月', width: 88 },
  { prop: 'stdM07', label: '标准-7月', width: 88 },
  { prop: 'stdM08', label: '标准-8月', width: 88 },
  { prop: 'stdM09', label: '标准-9月', width: 88 },
  { prop: 'stdM10', label: '标准-10月', width: 92 },
  { prop: 'stdM11', label: '标准-11月', width: 92 },
  { prop: 'stdM12', label: '标准-12月', width: 92 },
  { prop: 'additionInfo', label: '备注', minWidth: 120, showOverflow: true },
]

const costTablePixelWidth = computed(() => costColumns.reduce((acc, c) => acc + (c.width ?? c.minWidth ?? 0), 0))

const costTableLayoutStyle = computed(() => {
  const w = costTablePixelWidth.value
  return { width: `${w}px`, minWidth: `${w}px` }
})

const headerCellStyle = (): CSSProperties => ({
  background: '#b3d7f2',
  color: '#0f2744',
  fontWeight: '600',
})

const dlg = ref(false)
const file = ref<File | null>(null)
const uploading = ref(false)
const result = ref<{ successCount: number; failCount: number; errors: string[] } | null>(null)
const showHelpAlert = ref(true)
const resultPanelVisible = ref(false)

const items = ref<CostRowItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const tableLoading = ref(false)

const MONTH_PSP_KEYS = [
  'pspM01',
  'pspM02',
  'pspM03',
  'pspM04',
  'pspM05',
  'pspM06',
  'pspM07',
  'pspM08',
  'pspM09',
  'pspM10',
  'pspM11',
  'pspM12',
] as const satisfies readonly (keyof CostRowItem)[]

const MONTH_STD_KEYS = [
  'stdM01',
  'stdM02',
  'stdM03',
  'stdM04',
  'stdM05',
  'stdM06',
  'stdM07',
  'stdM08',
  'stdM09',
  'stdM10',
  'stdM11',
  'stdM12',
] as const satisfies readonly (keyof CostRowItem)[]

const editDlg = ref(false)
const edit = ref<CostRowItem | null>(null)
const saving = ref(false)

function onFile(u: UploadFile) {
  file.value = u.raw ?? null
}

async function loadTable() {
  tableLoading.value = true
  try {
    const { data } = await client.get<{ total: number; items: CostRowItem[] }>('/product-costs', {
      params: { page: page.value - 1, size: pageSize },
    })
    total.value = data.total
    items.value = data.items ?? []
  } catch {
    ElMessage.error('加载成本列表失败')
  } finally {
    tableLoading.value = false
  }
}

async function doUpload() {
  if (!file.value) {
    ElMessage.warning('请选择文件')
    return
  }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file.value)
    const { data } = await client.post('/product-costs/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    result.value = data
    resultPanelVisible.value = true
    ElMessage.success('导入完成')
    dlg.value = false
    page.value = 1
    await loadTable()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    uploading.value = false
  }
}

function onRowDblClick(row: CostRowItem) {
  edit.value = JSON.parse(JSON.stringify(row)) as CostRowItem
  editDlg.value = true
}

function normalizeCostPayload(row: CostRowItem): CostRowItem {
  const body = JSON.parse(JSON.stringify(row)) as CostRowItem
  const monthKeys = [...MONTH_PSP_KEYS, ...MONTH_STD_KEYS] as (keyof CostRowItem)[]
  for (const k of monthKeys) {
    const v = body[k]
    if (typeof v === 'number' && Number.isNaN(v)) {
      ;(body as Record<string, unknown>)[k as string] = null
    }
  }
  return body
}

async function saveCost() {
  if (!edit.value?.deviceId?.trim()) {
    ElMessage.warning('编码不能为空')
    return
  }
  saving.value = true
  try {
    const body = normalizeCostPayload(edit.value)
    await client.put('/product-costs', body)
    ElMessage.success('已保存')
    editDlg.value = false
    await loadTable()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadTable()
})
</script>

<style scoped>
.cost-page {
  margin: calc(-1 * var(--el-main-padding, 20px));
  padding: var(--el-main-padding, 20px);
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
  background: #f0f2f5;
}

.cost-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.cost-page__toolbar-actions {
  display: flex;
  gap: 8px;
}

.cost-page__result {
  margin-top: 12px;
}

.cost-page__result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.cost-page__errors {
  margin-top: 8px;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
  padding-right: 4px;
  line-height: 1.45;
}

.cost-page__table-wrap {
  margin-top: 16px;
  padding: 12px 12px 8px;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.cost-page__table-x {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
}

.cost-table {
  --el-table-border-color: #dcdfe6;
  --el-table-header-bg-color: #b3d7f2;
}

.cost-table :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.cost-table :deep(.el-table__body-wrapper .el-table__row--striped td) {
  background-color: #fafcff !important;
}

.cost-page__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 4px;
}

.cost-edit-form {
  padding-right: 8px;
}
</style>
