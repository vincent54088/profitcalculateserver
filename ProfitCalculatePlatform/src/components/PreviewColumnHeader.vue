<template>
  <div class="p-col-head">
    <span class="p-col-head__text">{{ label }}</span>
    <el-popover
      v-model:visible="panelOpen"
      placement="bottom-start"
      :width="320"
      trigger="click"
      @show="onPanelShow"
    >
      <div v-loading="loadingOptions" class="p-col-head__panel">
        <div v-if="!loadingOptions && options.length === 0" class="p-col-head__empty">暂无可选值</div>
        <template v-else-if="!loadingOptions">
          <div class="p-col-head__toolbar">
            <el-button size="small" text type="primary" @click="selectAll">全选</el-button>
            <el-button size="small" text @click="selectNone">全不选</el-button>
          </div>
          <el-scrollbar max-height="280">
            <el-checkbox-group v-model="draftSelected" class="p-col-head__checks">
              <div v-for="(opt, idx) in options" :key="idx" class="p-col-head__row">
                <el-checkbox :label="opt">
                  <span class="p-col-head__opt-label">{{ formatOption(opt) }}</span>
                </el-checkbox>
              </div>
            </el-checkbox-group>
          </el-scrollbar>
        </template>
        <div class="p-col-head__actions">
          <el-button size="small" text type="danger" @click="clearAndApply">清除筛选</el-button>
          <el-button size="small" type="primary" @click="confirmApply">确定</el-button>
        </div>
      </div>
      <template #reference>
        <button
          type="button"
          class="p-col-head__caret"
          :class="{ 'p-col-head__caret--active': hasFilter }"
          :aria-label="`筛选 ${label}`"
          @click.stop
        >
          <span aria-hidden="true" class="p-col-head__caret-icon">&#9660;</span>
        </button>
      </template>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

const props = defineProps<{
  label: string
  field: string
  /** 已生效的多选值（与后端精确匹配） */
  selectedValues: string[]
  /** 当前其它筛选条件下，本列去重后的可选值 */
  options: string[]
  loadingOptions?: boolean
}>()

const emit = defineEmits<{
  (e: 'request-options'): void
  (e: 'apply', payload: { field: string; values: string[] }): void
}>()

const panelOpen = ref(false)
const draftSelected = ref<string[]>([])

const hasFilter = computed(() => props.selectedValues.length > 0)

watch(
  () => props.selectedValues,
  (v) => {
    if (!panelOpen.value) draftSelected.value = [...v]
  },
  { deep: true },
)

function formatOption(v: string): string {
  return v === '' ? '(空)' : v
}

function onPanelShow() {
  draftSelected.value = [...props.selectedValues]
  emit('request-options')
}

function selectAll() {
  draftSelected.value = [...props.options]
}

function selectNone() {
  draftSelected.value = []
}

function clearAndApply() {
  draftSelected.value = []
  emit('apply', { field: props.field, values: [] })
  panelOpen.value = false
}

function confirmApply() {
  emit('apply', { field: props.field, values: [...draftSelected.value] })
  panelOpen.value = false
}
</script>

<style scoped>
.p-col-head {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 100%;
  vertical-align: middle;
}

.p-col-head__text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.p-col-head__caret {
  flex-shrink: 0;
  margin: 0;
  padding: 0 2px;
  border: none;
  background: transparent;
  cursor: pointer;
  line-height: 1;
  color: #409eff;
  opacity: 0.75;
  border-radius: 2px;
}

.p-col-head__caret:hover {
  opacity: 1;
  background: rgba(64, 158, 255, 0.12);
}

.p-col-head__caret--active {
  opacity: 1;
  color: #337ecc;
  font-weight: 700;
}

.p-col-head__caret-icon {
  font-size: 9px;
  display: inline-block;
  transform: scaleY(0.85);
}

.p-col-head__panel {
  min-height: 48px;
  padding: 2px 0;
}

.p-col-head__empty {
  font-size: 12px;
  color: #909399;
  padding: 8px 4px 12px;
}

.p-col-head__toolbar {
  display: flex;
  gap: 4px;
  margin-bottom: 6px;
}

.p-col-head__checks {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  width: 100%;
}

.p-col-head__row {
  width: 100%;
  padding: 2px 0;
}

.p-col-head__row :deep(.el-checkbox) {
  width: 100%;
  margin-right: 0;
  align-items: flex-start;
}

.p-col-head__row :deep(.el-checkbox__label) {
  white-space: normal;
  word-break: break-all;
  line-height: 1.35;
}

.p-col-head__opt-label {
  font-size: 12px;
}

.p-col-head__actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}
</style>
