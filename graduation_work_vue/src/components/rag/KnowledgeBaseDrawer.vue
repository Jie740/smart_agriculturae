<template>
  <el-drawer
    v-model="drawerVisible"
    title="知识库配置"
    :size="drawerSize"
    destroy-on-close
    @open="handleOpen"
    @closed="handleClosed"
  >
    <div class="drawer-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="知识库与文档" name="knowledge">
          <div class="toolbar">
            <el-input
              v-model="keyword"
              clearable
              placeholder="按名称筛选"
              class="keyword-input"
              @keyup.enter="loadKnowledgeBases"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select
              v-model="typeFilter"
              clearable
              placeholder="全部类型"
              class="type-filter"
              @change="loadKnowledgeBases"
            >
              <el-option v-for="type in types" :key="type.id" :label="typeLabel(type)" :value="String(type.id)" />
            </el-select>
            <el-button :loading="loading" title="刷新" @click="loadAll">
              <el-icon><Refresh /></el-icon>
            </el-button>
            <el-button type="primary" @click="openKnowledgeBaseDialog()">
              <el-icon><Plus /></el-icon>
              新建知识库
            </el-button>
          </div>

          <div class="selection-summary">
            <span>已选择 {{ selectedKnowledgeBaseIds.length }} 个知识库，{{ selectedDocumentIds.length }} 篇文档</span>
            <el-button text type="primary" @click="clearSelection">清空勾选</el-button>
          </div>

          <el-tree
            ref="treeRef"
            v-loading="loading"
            class="knowledge-tree"
            node-key="key"
            show-checkbox
            check-strictly
            default-expand-all
            :data="treeData"
            :props="treeProps"
            empty-text="暂无知识库"
            @check="handleTreeCheck"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span class="tree-node-main">
                  <el-icon><Collection /></el-icon>
                  <span class="tree-node-label" :title="node.label">{{ node.label }}</span>
                  <el-tag v-if="data.kind === 'knowledge-base'" size="small" effect="plain">
                    {{ data.documentCount || 0 }} 篇
                  </el-tag>
                  <el-tag
                    v-else-if="data.processStatus"
                    size="small"
                    :type="processStatusType(data.processStatus)"
                    effect="plain"
                  >
                    {{ processStatusLabel(data.processStatus) }}
                  </el-tag>
                </span>
                <span v-if="data.kind === 'knowledge-base'" class="tree-actions" @click.stop>
                  <el-button text size="small" title="编辑知识库" @click="openKnowledgeBaseDialog(data.raw)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button text size="small" title="删除知识库" @click="removeKnowledgeBase(data.raw)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </span>
              </div>
            </template>
          </el-tree>

          <section v-if="activeKnowledgeBase" class="document-section">
            <div class="section-heading">
              <div>
                <strong>{{ activeKnowledgeBase.name }}</strong>
                <span class="muted">文档管理</span>
              </div>
              <el-upload
                :show-file-list="false"
                :multiple="true"
                :accept="ragDocumentAccept"
                :before-upload="validateRagDocument"
                :http-request="uploadRagDocument"
              >
                <el-button type="primary" plain :loading="uploadingDocument">
                  <el-icon><Upload /></el-icon>
                  上传文档
                </el-button>
              </el-upload>
            </div>
            <el-table :data="activeDocuments" size="small" border>
              <el-table-column prop="name" label="文档名称" min-width="180" show-overflow-tooltip />
              <el-table-column prop="fileType" label="类型" width="90" />
              <el-table-column prop="chunkCount" label="Chunk" width="82" />
              <el-table-column label="处理状态" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="processStatusType(row.processStatus)">
                    {{ processStatusLabel(row.processStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="132" fixed="right">
                <template #default="{ row }">
                  <el-button text size="small" title="重建索引" @click="reindexDocument(row)">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                  <el-button text size="small" title="重命名" @click="renameDocument(row)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button text type="danger" size="small" title="删除文档" @click="removeDocument(row)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!activeDocuments.length" description="暂无文档" :image-size="56" />
          </section>
        </el-tab-pane>

        <el-tab-pane label="类型管理" name="types">
          <div class="toolbar type-toolbar">
            <span class="toolbar-title">知识库类型</span>
            <el-button type="primary" @click="openTypeDialog()">
              <el-icon><Plus /></el-icon>
              新建类型
            </el-button>
          </div>
          <el-table v-loading="typeLoading" :data="types" size="small" border>
            <el-table-column prop="typeName" label="类型名称" min-width="120" />
            <el-table-column prop="typeCode" label="编码" min-width="120" />
            <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
            <el-table-column prop="sortNo" label="排序" width="68" />
            <el-table-column label="状态" width="76">
              <template #default="{ row }">
                <el-tag size="small" :type="Number(row.status) === 1 ? 'success' : 'info'">
                  {{ Number(row.status) === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="112" fixed="right">
              <template #default="{ row }">
                <el-button text size="small" @click="openTypeDialog(row)">编辑</el-button>
                <el-button text type="danger" size="small" @click="removeType(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="knowledgeBaseDialogVisible" :title="knowledgeBaseForm.id ? '编辑知识库' : '新建知识库'" width="480px">
      <el-form ref="knowledgeBaseFormRef" :model="knowledgeBaseForm" :rules="knowledgeBaseRules" label-width="88px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="knowledgeBaseForm.name" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="typeId">
          <el-select v-model="knowledgeBaseForm.typeId" clearable placeholder="选择类型" style="width: 100%">
            <el-option v-for="type in types" :key="type.id" :label="typeLabel(type)" :value="type.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="knowledgeBaseForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="knowledgeBaseForm.description" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="knowledgeBaseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveKnowledgeBase">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="typeDialogVisible" :title="typeForm.id ? '编辑类型' : '新建类型'" width="480px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="88px">
        <el-form-item label="名称" prop="typeName">
          <el-input v-model="typeForm.typeName" maxlength="40" />
        </el-form-item>
        <el-form-item label="编码" prop="typeCode">
          <el-input v-model="typeForm.typeCode" maxlength="40" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="typeForm.sortNo" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="typeForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="typeForm.description" type="textarea" :rows="3" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveType">保存</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import {
  Collection,
  Delete,
  Edit,
  Plus,
  Refresh,
  Search,
  Upload,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import $API from '@/api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  selectedKnowledgeBaseIds: { type: Array, default: () => [] },
  selectedDocumentIds: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:modelValue', 'selection-change'])

const drawerVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})
const drawerSize = 'min(820px, 100vw)'
// 与后端 AiRagDocumentServiceImpl.SUPPORTED_FILE_TYPES 保持一致
const RAG_DOCUMENT_TYPES = ['pdf', 'doc', 'docx', 'txt', 'md', 'xlsx', 'xls', 'ppt', 'pptx', 'csv']
const ragDocumentAccept = RAG_DOCUMENT_TYPES.map((type) => `.${type}`).join(',')
const activeTab = ref('knowledge')
const keyword = ref('')
const typeFilter = ref('')
const loading = ref(false)
const typeLoading = ref(false)
const saving = ref(false)
const uploadingDocument = ref(false)
const treeRef = ref(null)
const treeProps = { children: 'children', label: 'label' }
const knowledgeBases = ref([])
const types = ref([])
const documentsByBaseId = ref({})
const activeKnowledgeBaseId = ref(null)
const knowledgeBaseDialogVisible = ref(false)
const typeDialogVisible = ref(false)
const knowledgeBaseFormRef = ref(null)
const typeFormRef = ref(null)

const knowledgeBaseForm = reactive({
  id: null,
  name: '',
  typeId: null,
  description: '',
  status: 1,
})
const typeForm = reactive({
  id: null,
  typeName: '',
  typeCode: '',
  description: '',
  sortNo: 0,
  status: 1,
})
const knowledgeBaseRules = {
  name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }],
}
const typeRules = {
  typeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  typeCode: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
}

const unwrap = (response) => response?.data?.data ?? response?.data
const extractList = (response) => {
  const value = unwrap(response)
  if (Array.isArray(value)) return value
  if (Array.isArray(value?.records)) return value.records
  if (Array.isArray(value?.list)) return value.list
  if (Array.isArray(value?.data)) return value.data
  return []
}
/**
 * 主键为后端雪花 ID（19 位），超出 JS 安全整数范围。
 * 一旦经过 Number() 转换就会丢失精度（...601 → ...600），
 * 导致上传/删除命中不存在的 ID、树勾选后因 key 不匹配而自动取消，
 * 因此 ID 全程按字符串保存与比较。
 */
const toId = (value) => (value === null || value === undefined || value === '' ? null : String(value))
const toCount = (value) => {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : 0
}

const normalizeType = (item, index) => ({
  ...item,
  id: toId(item.id ?? item.typeId) ?? `local-${index}`,
  typeName: item.typeName ?? item.name ?? '未命名类型',
  typeCode: item.typeCode ?? item.code ?? '',
  sortNo: item.sortNo ?? item.sort ?? index,
  status: item.status ?? 1,
})
const normalizeBase = (item) => ({
  ...item,
  id: toId(item.id ?? item.knowledgeBaseId),
  name: item.name ?? item.knowledgeBaseName ?? '未命名知识库',
  typeId: toId(item.typeId ?? item.knowledgeBaseTypeId),
  typeName: item.typeName ?? item.knowledgeBaseTypeName ?? '',
  status: item.status ?? 1,
  documentCount: toCount(item.documentCount ?? item.docCount),
})
const persistedTypeId = (value) => {
  const id = toId(value)
  return id !== null && /^\d+$/.test(id) ? id : null
}
const normalizeDocument = (item) => ({
  ...item,
  id: toId(item.id ?? item.documentId),
  knowledgeBaseId: toId(item.knowledgeBaseId),
  name: item.name ?? item.documentName ?? item.fileName ?? '未命名文档',
  fileName: item.fileName ?? item.name ?? item.documentName ?? '',
  fileType: item.fileType ?? item.type ?? '',
  fileSize: toCount(item.fileSize),
  chunkCount: toCount(item.chunkCount),
  processStatus: item.processStatus ?? item.status ?? 'PENDING',
})

const typeLabel = (type) => type.typeName + (type.typeCode ? ` (${type.typeCode})` : '')
const getTypeName = (typeId) => types.value.find((item) => String(item.id) === String(typeId))?.typeName || ''
const filteredBases = computed(() =>
  knowledgeBases.value.filter((base) => {
    const matchesKeyword = !keyword.value.trim() || base.name.toLowerCase().includes(keyword.value.trim().toLowerCase())
    const matchesType = !typeFilter.value || String(base.typeId) === String(typeFilter.value)
    return matchesKeyword && matchesType
  }),
)
const treeData = computed(() =>
  filteredBases.value
    .filter((base) => base.id !== null)
    .map((base) => ({
      key: `kb-${base.id}`,
      id: base.id,
      kind: 'knowledge-base',
      label: base.name,
      documentCount: base.documentCount,
      raw: base,
      children: (documentsByBaseId.value[base.id] || [])
        .filter((document) => document.id !== null)
        .map((document) => ({
          key: `doc-${document.id}`,
          id: document.id,
          kind: 'document',
          label: document.name,
          raw: document,
        })),
    })),
)
const selectedKnowledgeBaseIds = ref([])
const selectedDocumentIds = ref([])
const activeKnowledgeBase = computed(() =>
  knowledgeBases.value.find((item) => String(item.id) === String(activeKnowledgeBaseId.value)),
)
const activeDocuments = computed(() => documentsByBaseId.value[activeKnowledgeBaseId.value] || [])

const normalizeSelection = (baseIds = [], documentIds = []) => ({
  knowledgeBaseIds: [...new Set((Array.isArray(baseIds) ? baseIds : []).map(toId).filter(Boolean))],
  documentIds: [...new Set((Array.isArray(documentIds) ? documentIds : []).map(toId).filter(Boolean))],
})
const setSelection = (baseIds = props.selectedKnowledgeBaseIds, documentIds = props.selectedDocumentIds, notify = false) => {
  const selection = normalizeSelection(baseIds, documentIds)
  selectedKnowledgeBaseIds.value = selection.knowledgeBaseIds
  selectedDocumentIds.value = selection.documentIds
  if (notify) emit('selection-change', selection)
}

const syncTreeSelection = async () => {
  await nextTick()
  if (!treeRef.value) return
  treeRef.value.setCheckedKeys([
    ...selectedKnowledgeBaseIds.value.map((id) => `kb-${id}`),
    ...selectedDocumentIds.value.map((id) => `doc-${id}`),
  ])
}

const handleTreeCheck = (_data, details) => {
  const checkedKeys = details?.checkedKeys || treeRef.value?.getCheckedKeys() || []
  setSelection(
    checkedKeys
      .filter((key) => String(key).startsWith('kb-'))
      .map((key) => String(key).slice(3)),
    checkedKeys
      .filter((key) => String(key).startsWith('doc-'))
      .map((key) => String(key).slice(4)),
    true,
  )
}
const clearSelection = () => {
  setSelection([], [], true)
  treeRef.value?.setCheckedKeys([])
}
const handleNodeClick = (data) => {
  activeKnowledgeBaseId.value = data.kind === 'knowledge-base' ? data.id : data.raw.knowledgeBaseId
}
const processStatusLabel = (status) => {
  const map = {
    0: '待处理',
    1: '解析中',
    2: '向量化中',
    3: '已完成',
    4: '失败',
    PENDING: '待处理',
    PROCESSING: '处理中',
    SUCCESS: '已完成',
    FAILED: '失败',
  }
  return map[status] || '未知'
}
const processStatusType = (status) => {
  if (status === 3 || status === 'SUCCESS') return 'success'
  if (status === 4 || status === 'FAILED') return 'danger'
  if (status === 0 || status === 'PENDING') return 'info'
  return 'warning'
}

const getErrorMessage = (error, fallback) =>
  error?.response?.data?.message || error?.message || fallback

const loadTypes = async () => {
  typeLoading.value = true
  try {
    const response = await $API.listKnowledgeBaseTypes()
    const result = extractList(response).map(normalizeType)
    types.value = result
  } catch (error) {
    types.value = []
    ElMessage.error(getErrorMessage(error, '加载知识库类型失败'))
  } finally {
    typeLoading.value = false
  }
}

const loadDocuments = async (base) => {
  const baseId = toId(base?.id)
  if (!baseId) return []
  try {
    const response = await $API.listRagDocuments(baseId, { pageNum: 1, pageSize: 200 })
    const result = extractList(response).map((item) =>
      normalizeDocument({ ...item, knowledgeBaseId: item.knowledgeBaseId ?? baseId }),
    )
    documentsByBaseId.value = { ...documentsByBaseId.value, [baseId]: result }
    return result
  } catch (error) {
    // 单个知识库的文档加载失败不应连带影响其它知识库的展示与勾选回显
    documentsByBaseId.value = { ...documentsByBaseId.value, [baseId]: [] }
    ElMessage.warning(getErrorMessage(error, `知识库「${base.name}」的文档加载失败`))
    return []
  }
}

const loadKnowledgeBases = async () => {
  loading.value = true
  try {
    const response = await $API.listKnowledgeBases({
      keyword: keyword.value.trim(),
      pageSize: 200,
      typeId: typeFilter.value || undefined,
    })
    knowledgeBases.value = extractList(response).map(normalizeBase)
    await Promise.all(knowledgeBases.value.map((base) => loadDocuments(base)))
    const activeExists = knowledgeBases.value.some(
      (base) => String(base.id) === String(activeKnowledgeBaseId.value),
    )
    if (!activeExists) activeKnowledgeBaseId.value = knowledgeBases.value[0]?.id ?? null
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '加载知识库失败'))
  } finally {
    loading.value = false
    await syncTreeSelection()
  }
}
const loadAll = async () => {
  await Promise.all([loadTypes(), loadKnowledgeBases()])
}

const resetKnowledgeBaseForm = (base = null) => {
  Object.assign(knowledgeBaseForm, {
    id: base?.id ?? null,
    name: base?.name ?? '',
    typeId: base?.typeId ?? types.value[0]?.id ?? null,
    description: base?.description ?? '',
    status: base?.status ?? 1,
  })
}
const openKnowledgeBaseDialog = (base = null) => {
  resetKnowledgeBaseForm(base)
  knowledgeBaseDialogVisible.value = true
}
const saveKnowledgeBase = async () => {
  const valid = await knowledgeBaseFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      id: knowledgeBaseForm.id,
      name: knowledgeBaseForm.name.trim(),
      description: knowledgeBaseForm.description.trim(),
      status: knowledgeBaseForm.status,
      typeId: persistedTypeId(knowledgeBaseForm.typeId),
      typeName: getTypeName(knowledgeBaseForm.typeId),
    }
    if (knowledgeBaseForm.id) await $API.updateKnowledgeBase(payload)
    else await $API.addKnowledgeBase(payload)
    knowledgeBaseDialogVisible.value = false
    await loadKnowledgeBases()
    ElMessage.success('知识库已保存')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '保存知识库失败'))
  } finally {
    saving.value = false
  }
}
const removeKnowledgeBase = async (base) => {
  try {
    await ElMessageBox.confirm(`确定删除知识库“${base.name}”吗？`, '删除知识库', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await $API.deleteKnowledgeBase(base.id)
    if (String(activeKnowledgeBaseId.value) === String(base.id)) activeKnowledgeBaseId.value = null
    await loadKnowledgeBases()
    ElMessage.success('知识库已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(getErrorMessage(error, '删除知识库失败'))
  }
}

const resetTypeForm = (type = null) => {
  Object.assign(typeForm, {
    id: type?.id ?? null,
    typeName: type?.typeName ?? '',
    typeCode: type?.typeCode ?? '',
    description: type?.description ?? '',
    sortNo: type?.sortNo ?? 0,
    status: type?.status ?? 1,
  })
}
const openTypeDialog = (type = null) => {
  resetTypeForm(type)
  typeDialogVisible.value = true
}
const saveType = async () => {
  const valid = await typeFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { ...typeForm, typeName: typeForm.typeName.trim(), typeCode: typeForm.typeCode.trim() }
    if (typeForm.id) {
      await $API.updateKnowledgeBaseType(payload)
    } else {
      await $API.addKnowledgeBaseType(payload)
    }
    await loadTypes()
    typeDialogVisible.value = false
    ElMessage.success('类型已保存')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '保存类型失败'))
  } finally {
    saving.value = false
  }
}
const removeType = async (type) => {
  try {
    await ElMessageBox.confirm(`确定删除类型“${type.typeName}”吗？`, '删除类型', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await $API.deleteKnowledgeBaseType(type.id)
    await loadTypes()
    ElMessage.success('类型已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(getErrorMessage(error, '删除类型失败'))
  }
}

const validateRagDocument = (file) => {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error(`${file.name} 超过 10MB 限制`)
    return false
  }
  if (!activeKnowledgeBaseId.value) {
    ElMessage.warning('请先选择一个知识库')
    return false
  }
  const extension = String(file.name).split('.').pop()?.toLowerCase() ?? ''
  if (!RAG_DOCUMENT_TYPES.includes(extension)) {
    ElMessage.error(`不支持的文档类型：.${extension}，支持 ${RAG_DOCUMENT_TYPES.join(' / ')}`)
    return false
  }
  return true
}
const uploadRagDocument = async ({ file, onSuccess, onError }) => {
  const base = activeKnowledgeBase.value
  if (!base?.id) {
    onError?.(new Error('未选择知识库'))
    ElMessage.warning('请先选择一个知识库')
    return
  }
  uploadingDocument.value = true
  try {
    const response = await $API.uploadRagDocument(file, base.id)
    onSuccess?.(response)
    ElMessage.success(`${file.name} 上传成功`)
    // 以服务端返回为准重新拉取，保证新文档带着真实 ID 出现在树中且可勾选
    await loadKnowledgeBases()
  } catch (error) {
    onError?.(error)
    ElMessage.error(getErrorMessage(error, `${file.name} 上传失败`))
  } finally {
    uploadingDocument.value = false
  }
}
const removeDocument = async (document) => {
  try {
    await ElMessageBox.confirm(`确定删除文档“${document.name}”吗？`, '删除文档', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await $API.deleteRagDocument(document.id)
    documentsByBaseId.value = {
      ...documentsByBaseId.value,
      [activeKnowledgeBaseId.value]: activeDocuments.value.filter((item) => item.id !== document.id),
    }
    await loadKnowledgeBases()
    ElMessage.success('文档已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(getErrorMessage(error, '删除文档失败'))
  }
}
const reindexDocument = async (document) => {
  try {
    await $API.reindexRagDocument(document.id)
    ElMessage.success('已提交重建索引')
    await loadDocuments(activeKnowledgeBase.value)
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '重建索引失败'))
  }
}
const renameDocument = async (document) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的文档名称', '重命名文档', {
      inputValue: document.name,
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValidator: (value) => value.trim() ? true : '文档名称不能为空',
    })
    await $API.updateRagDocument({ id: document.id, name: value.trim() })
    document.name = value.trim()
    ElMessage.success('文档名称已更新')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(getErrorMessage(error, '重命名失败'))
  }
}

const handleOpen = () => {
  setSelection()
  loadAll()
}

const handleClosed = () => {
  activeTab.value = 'knowledge'
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      setSelection()
    }
  },
)
watch(
  () => [props.selectedKnowledgeBaseIds, props.selectedDocumentIds],
  () => {
    setSelection()
    syncTreeSelection()
  },
  { deep: true },
)
onMounted(() => {
  if (props.modelValue) loadAll()
})
</script>

<style scoped>
.drawer-content { min-height: 100%; }
.toolbar { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.keyword-input { width: 220px; }
.type-filter { width: 150px; }
.type-toolbar { justify-content: space-between; }
.toolbar-title { color: #1f2937; font-weight: 600; }
.selection-summary { display: flex; justify-content: space-between; align-items: center; min-height: 34px; padding: 0 10px; margin-bottom: 8px; color: #6b7280; background: #f8fafc; font-size: 12px; }
.knowledge-tree { min-height: 180px; max-height: 360px; overflow: auto; padding: 6px 0; border: 1px solid #ebeef5; }
.tree-node { display: flex; align-items: center; justify-content: space-between; width: 100%; min-width: 0; padding-right: 8px; }
.tree-node-main { display: flex; align-items: center; gap: 6px; min-width: 0; }
.tree-node-label { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tree-actions { display: none; }
.tree-node:hover .tree-actions { display: inline-flex; }
.document-section { margin-top: 16px; }
.section-heading { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.section-heading strong { margin-right: 8px; color: #1f2937; }
.muted { color: #9ca3af; font-size: 12px; }
.el-table { width: 100%; }
@media (max-width: 640px) {
  .toolbar { flex-wrap: wrap; }
  .keyword-input { flex: 1 1 180px; width: auto; }
  .type-filter { flex: 1 1 130px; width: auto; }
}
</style>
