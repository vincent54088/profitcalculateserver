<template>
  <div class="task-manage-page">
    <div class="task-list-panel">
      <div class="task-list-panel__header">
        <h2 class="task-list-panel__title">任务列表</h2>
        <el-button type="primary" @click="openUpload">添加</el-button>
      </div>
      <div class="task-card-grid">
        <div v-for="t in tasks" :key="t.taskId" class="task-card-grid__cell">
          <el-card class="task-card" shadow="hover">
            <div class="task-card__inner">
              <span class="task-card__status-pill" :class="statusPillClass(t.taskStatus)">{{ t.taskStatus }}</span>
              <div class="task-card__title">{{ t.taskName }}</div>
              <div class="task-card__row task-card__meta">文件: {{ t.dataFile }}</div>
              <div class="task-card__row task-card__meta task-card__meta--time">上传 {{ formatToMinute(t.createTime) }}</div>
              <div class="task-card__row task-card__meta task-card__meta--time">完成 {{ formatToMinute(t.finishTime) }}</div>
              <div class="task-card__actions">
                <el-button size="small" class="task-card__btn-preview" @click="$router.push(`/tasks/${t.taskId}/preview`)">
                  预览
                </el-button>
                <el-button size="small" type="danger" @click="remove(t.taskId)">删除</el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="dlg"
      title="新建测算任务"
      width="560px"
      class="task-new-dialog"
      align-center
      destroy-on-close
    >
      <div class="task-new-dialog__inner">
        <div class="task-new-dialog__label">上传评测集：</div>
        <el-upload drag :auto-upload="false" :limit="1" :on-change="onFile" class="task-new-dialog__upload">
          <div class="task-new-dialog__drop-hint">拖拉或点击上传</div>
          <template #tip>
            <div class="el-upload__tip task-new-dialog__tip">
              支持：订单单 Sheet（订单头+明细列同表；固定双行表头：第 1 行分组、第 2 行列名；续行可省略合同号沿用上一行；Sheet 名见 excel.order.order-data-sheet-names）
            </div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="doUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'
import client from '../api/client'

interface TaskRow {
  taskId: string
  taskName: string
  taskStatus: string
  dataFile: string
  createTime: string
  finishTime?: string | null
}

const tasks = ref<TaskRow[]>([])
const dlg = ref(false)
const file = ref<File | null>(null)
const uploading = ref(false)
let timer: number | undefined

/** 右上角药丸状态底色 */
function statusPillClass(s: string): string {
  if (s === '成功' || s === 'Success') return 'task-card__status-pill--ok'
  if (s === '失败' || s === 'Failed') return 'task-card__status-pill--fail'
  if (s === '解析中' || s === '汇算中') return 'task-card__status-pill--busy'
  return 'task-card__status-pill--muted'
}

function formatToMinute(s: string | null | undefined): string {
  if (s == null || String(s).trim() === '') return '-'
  const d = new Date(s)
  if (Number.isNaN(d.getTime())) {
    const m = String(s).match(/^(\d{4}-\d{2}-\d{2})[T ](\d{2}):(\d{2})/)
    if (m) return `${m[1]} ${m[2]}:${m[3]}`
    return String(s).replace(/\.\d+/, '').replace(/\+[\d:]+$/, '').replace(/Z$/, '').slice(0, 16)
  }
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function load() {
  const { data } = await client.get<TaskRow[]>('/tasks')
  tasks.value = data
}

function openUpload() {
  file.value = null
  dlg.value = true
}

function onFile(u: UploadFile) {
  file.value = u.raw ?? null
}

async function doUpload() {
  if (!file.value) {
    ElMessage.warning('请选择文件')
    return
  }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file.value, file.value.name)
    await client.post('/tasks/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    ElMessage.success('已提交上传，任务进入解析/汇算队列')
    dlg.value = false
    await load()
  } catch (e: unknown) {
    const msg = e && typeof e === 'object' && 'response' in e ? String((e as { response?: { data?: { message?: string } } }).response?.data?.message || '上传失败') : '上传失败'
    ElMessage.error(msg)
  } finally {
    uploading.value = false
  }
}

async function remove(taskId: string) {
  await client.delete(`/tasks/${taskId}`)
  ElMessage.success('已删除')
  await load()
}

onMounted(() => {
  load()
  timer = window.setInterval(load, 3000)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.task-manage-page {
  /* 抵消 el-main 内边距，使浅灰铺满内容区 */
  margin: calc(-1 * var(--el-main-padding, 20px));
  padding: var(--el-main-padding, 20px);
  min-height: calc(100vh - 60px);
  background: #f0f2f5;
  box-sizing: border-box;
}

.task-list-panel {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px 20px 8px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  /* 无任务时仍保留固定可视高度，避免白底区域塌成一条 */
  min-height: max(480px, calc(100vh - 140px));
  box-sizing: border-box;
}

.task-list-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.task-list-panel__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.task-card-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  align-items: stretch;
}

.task-card-grid__cell {
  min-width: 0;
}

.task-card-grid__cell .task-card {
  height: 100%;
}

@media (max-width: 1440px) {
  .task-card-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .task-card-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .task-card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .task-card-grid {
    grid-template-columns: 1fr;
  }
}

.task-card {
  border-radius: 8px;
  border: 1px solid #c8e6c9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  background: #ecfdf3;
}

.task-card :deep(.el-card__body) {
  padding: 12px 14px 12px;
}

.task-card__inner {
  position: relative;
  padding-right: 76px;
  min-height: 22px;
}

.task-card__status-pill {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 1;
  display: inline-block;
  max-width: 72px;
  padding: 3px 10px;
  border-radius: 999px;
  border: 1px solid #1f2937;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.3;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-sizing: border-box;
  user-select: none;
  pointer-events: none;
}

.task-card__status-pill--ok {
  background: #d1fae5;
  color: #065f46;
  border-color: #047857;
}

.task-card__status-pill--fail {
  background: #fee2e2;
  color: #991b1b;
  border-color: #b91c1c;
}

.task-card__status-pill--busy {
  background: #fef3c7;
  color: #92400e;
  border-color: #b45309;
}

.task-card__status-pill--muted {
  background: #e5e7eb;
  color: #374151;
  border-color: #4b5563;
}

.task-card__title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-card__row {
  margin-top: 4px;
}

.task-card__meta {
  font-size: 11px;
  color: #606266;
  line-height: 1.45;
}

.task-card__meta--time {
  display: block;
  white-space: nowrap;
}

.task-card__actions {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}

.task-card__btn-preview {
  background: #d6ebff !important;
  border-color: #b3d8ff !important;
  color: #1d3a52 !important;
}

.task-card__btn-preview:hover {
  background: #c4e0ff !important;
  border-color: #99ccf5 !important;
  color: #102a3d !important;
}

/* 新建任务弹窗：白底 + 浅黄拖拽区（与线稿 #fff2ac 一致） */
.task-new-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding-bottom: 8px;
}

.task-new-dialog :deep(.el-dialog__body) {
  padding-top: 4px;
}

.task-new-dialog__inner {
  width: 100%;
}

.task-new-dialog__label {
  margin: 0 0 10px;
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
}

.task-new-dialog__upload {
  width: 100%;
}

.task-new-dialog__upload :deep(.el-upload),
.task-new-dialog__upload :deep(.el-upload-dragger) {
  width: 100%;
}

.task-new-dialog__upload :deep(.el-upload-dragger) {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 160px;
  padding: 32px 20px;
  background-color: #fff2ac;
  border: 1px dashed #c4b896;
  border-radius: 12px;
  box-sizing: border-box;
}

.task-new-dialog__upload :deep(.el-upload-dragger:hover) {
  border-color: #8c8570;
}

.task-new-dialog__drop-hint {
  font-size: 15px;
  color: #303133;
  text-align: center;
  user-select: none;
}

.task-new-dialog__tip {
  margin-top: 12px;
  line-height: 1.5;
}
</style>
