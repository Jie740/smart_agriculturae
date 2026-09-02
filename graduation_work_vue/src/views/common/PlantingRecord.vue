<template>
  <div class="planting-management">
    <!-- 标题 -->
    <div class="header">
      <h1>{{ roleTitle }}-种植记录管理</h1>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        新增种植记录
      </el-button>
    </div>

    <!-- 搜索 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="输入地块名/地块位置"
        style="width: 300px"
        clearable
        @clear="handleReset"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button type="warning" @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table
        :data="recordList"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
        height="100%"
      >
        <el-table-column prop="recordId" label="记录ID" width="90" align="center" />
        <el-table-column prop="landName" label="地块名" align="center" />
        <el-table-column prop="location" label="地块位置" align="center" />
        <el-table-column prop="area" label="地块面积(亩)" width="130" align="center" />
        <el-table-column prop="planName" label="关联计划名" align="center" />
        <el-table-column prop="cropName" label="农作物" align="center" />
        <el-table-column prop="plantingDate" label="种植时间" width="160" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.plantingDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" effect="dark" size="small">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button type="info" size="small" plain @click="openDetailDialog(scope.row)">
              详情
            </el-button>
            <el-button type="primary" size="small" plain @click="openEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" plain @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]"
        :total="total"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 添加种植记录弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加种植记录" width="600px" @close="resetAddForm">
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="110px">
        <!-- 选择地块 -->
        <el-form-item label="选择地块" prop="landId">
          <el-select
            v-model="addForm.landId"
            placeholder="请选择地块"
            style="width: 100%"
            @change="handleLandChange"
          >
            <el-option
              v-for="item in landList"
              :key="item.landId"
              :label="item.landName"
              :value="item.landId"
            />
          </el-select>
        </el-form-item>

        <!-- 无种植计划提示 -->
        <el-form-item v-if="noPlanTip" label=" ">
          <el-alert title="当前地块无种植计划" type="warning" :closable="false" show-icon />
        </el-form-item>

        <!-- 计划名 -->
        <el-form-item label="计划名" prop="planId" v-if="!noPlanTip || addForm.landId === ''">
          <el-select
            v-model="addForm.planId"
            placeholder="请选择计划"
            style="width: 100%"
            :disabled="true"
          >
            <el-option
              v-for="item in planList"
              :key="item.planId"
              :label="item.planName"
              :value="item.planId"
            />
          </el-select>
        </el-form-item>

        <!-- 农作物名 -->
        <el-form-item label="农作物名" prop="cropId" v-if="!noPlanTip || addForm.landId === ''">
          <el-select
            v-model="addForm.cropId"
            placeholder="请选择农作物"
            style="width: 100%"
            :disabled="true"
          >
            <el-option
              v-for="item in cropList"
              :key="item.cropId"
              :label="item.cropName"
              :value="item.cropId"
            />
          </el-select>
        </el-form-item>

        <!-- 种植日期 -->
        <el-form-item label="种植日期" prop="plantingDate">
          <el-date-picker
            v-model="addForm.plantingDate"
            type="date"
            placeholder="选择种植日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <!-- 期望收割日期 -->
        <el-form-item label="期望收割日期" prop="expectedHarvestDate">
          <el-date-picker
            v-model="addForm.expectedHarvestDate"
            type="date"
            placeholder="选择期望收割日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAdd">添加</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑种植记录弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑种植记录" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="110px">
        <!-- 选择地块 disabled -->
        <el-form-item label="选择地块">
          <el-input v-model="editForm.landName" placeholder="地块名" style="width: 100%" disabled />
        </el-form-item>

        <!-- 计划名：使用el-select显示计划名，值为planId -->
        <el-form-item label="计划名" prop="planId">
          <el-select v-model="editForm.planId" placeholder="计划名" style="width: 100%" disabled>
            <el-option :label="editForm.planName" :value="editForm.planId" />
          </el-select>
        </el-form-item>

        <!-- 农作物名 disabled -->
        <el-form-item label="农作物名">
          <el-input
            v-model="editForm.cropName"
            placeholder="农作物名"
            style="width: 100%"
            disabled
          />
        </el-form-item>

        <!-- 种植日期 -->
        <el-form-item label="种植日期" prop="plantingDate">
          <el-date-picker
            v-model="editForm.plantingDate"
            type="date"
            placeholder="选择种植日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <!-- 期望收割日期 -->
        <el-form-item label="期望收割日期" prop="expectedHarvestDate">
          <el-date-picker
            v-model="editForm.expectedHarvestDate"
            type="date"
            placeholder="选择期望收割日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <!-- 状态 -->
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="editForm.status"
            placeholder="请选择状态"
            style="width: 100%"
            :disabled="userRole === 'user' && editForm.isHarvested"
          >
            <el-option label="生长中" :value="0" />
            <el-option label="已收割" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEdit">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="种植记录详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录ID">{{ detailRow.recordId }}</el-descriptions-item>
        <el-descriptions-item label="地块名">{{ detailRow.landName }}</el-descriptions-item>
        <el-descriptions-item label="地块位置">{{ detailRow.location }}</el-descriptions-item>
        <el-descriptions-item label="地块面积(亩)">{{ detailRow.area }}</el-descriptions-item>
        <el-descriptions-item label="农作物">{{ detailRow.cropName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailRow.status)" effect="dark" size="small">
            {{ statusText(detailRow.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="种植时间" :span="2">
          {{ formatDate(detailRow.plantingDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="期望收割日期" :span="2">
          {{ formatDate(detailRow.expectedHarvestDate) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实际收割日期" :span="2">
          {{ formatDate(detailRow.actualHarvestDate) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDate(detailRow.createTime) || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import $API from '@/api'

// ==================== 列表相关 ====================
const recordList = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const role = ref(localStorage.getItem('role') || '')
const roleTitle = computed(() => {
  const map = {
    user: '承包人',
    enterprise_admin: '企业管理员',
    system_admin: '系统管理员',
  }
  return map[role.value] || '系统管理员'
})
const userRole = ref(localStorage.getItem('role') || '')
const userId = ref(null)

// ==================== 下拉数据 ====================
const landList = ref([])
const planList = ref([])
const cropList = ref([])

// ==================== 添加表单 ====================
const addDialogVisible = ref(false)
const addFormRef = ref(null)
const noPlanTip = ref(false)

const addForm = ref({
  landId: '',
  planId: '',
  cropId: '',
  plantingDate: '',
  expectedHarvestDate: '',
  actualHarvestDate: '',
})

const addFormRules = {
  landId: [{ required: true, message: '请选择地块', trigger: 'change' }],
  planId: [{ required: true, message: '请选择种植计划', trigger: 'change' }],
  cropId: [{ required: true, message: '请选择农作物', trigger: 'change' }],
  plantingDate: [{ required: true, message: '请选择种植日期', trigger: 'change' }],
  expectedHarvestDate: [{ required: true, message: '请选择期望收割日期', trigger: 'change' }],
}

// ==================== 编辑表单 ====================
const editDialogVisible = ref(false)
const editFormRef = ref(null)

const editForm = ref({
  recordId: '',
  landId: '',
  planId: '',
  cropId: '',
  plantingDate: '',
  expectedHarvestDate: '',
  actualHarvestDate: '',
  status: 0,
  outputQuantity: 0,
})

const editFormRules = {
  plantingDate: [{ required: true, message: '请选择种植日期', trigger: 'change' }],
  expectedHarvestDate: [{ required: true, message: '请选择期望收割日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  outputQuantity: [{ required: true, message: '请输入产出数量', trigger: 'blur' }],
  actualHarvestDate: [{ required: true, message: '请选择实际收割日期', trigger: 'change' }],
}

// ==================== 详情弹窗 ====================
const detailDialogVisible = ref(false)
const detailRow = ref({})

// ==================== 工具函数 ====================

/* 状态文字 */
const statusText = (status) => {
  const map = { 0: '生长中', 1: '已收割' }
  return map[status] ?? '未知'
}

/* 状态标签类型 */
const statusTagType = (status) => {
  const map = { 0: 'success', 1: 'warning' }
  return map[status] ?? 'info'
}

/* 时间格式化 */
const formatDate = (val) => {
  if (!val) return ''
  const d = new Date(val)
  if (isNaN(d.getTime())) return ''
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// ==================== 数据加载 ====================

/* 加载种植记录分页列表 */
const loadRecords = async () => {
  loading.value = true
  try {
    if (userRole.value === 'user') {
      const res = await $API.getPlantingRecordByUserIdPage(currentPage.value, pageSize.value)
      if (res.data.code === '200') {
        recordList.value = res.data.data.records
        total.value = res.data.data.total
      }
    } else {
      const res = await $API.getPlantingRecordByPage(currentPage.value, pageSize.value)
      if (res.data.code === '200') {
        recordList.value = res.data.data.records
        total.value = res.data.data.total
      }
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

/* 加载地块列表 */
const loadLand = async () => {
  try {
    if (userRole.value === 'user' && userId.value) {
      const res = await $API.getLandByUserId(userId.value)
      if (res.data.code === '200') {
        landList.value = res.data.data || []
      }
    } else {
      const res = await $API.getLandAll()
      if (res.data.code === '200') {
        landList.value = res.data.data || []
      }
    }
  } catch (e) {
    console.error('加载地块失败:', e)
  }
}

/* 加载计划列表 */
const loadPlan = async () => {
  try {
    const res = await $API.getPlantingPlanAll()
    if (res.data.code === '200') {
      planList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载计划失败:', e)
  }
}

/* 加载农作物列表 */
const loadCrop = async () => {
  try {
    const res = await $API.getCropAll()
    if (res.data.code === '200') {
      cropList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载农作物失败:', e)
  }
}

// ==================== 地块变更联动 ====================

/* 添加弹窗中地块切换：自动填充关联计划和农作物 */
const handleLandChange = async (landId) => {
  addForm.value.planId = ''
  addForm.value.cropId = ''
  noPlanTip.value = false

  if (!landId) return

  try {
    const res = await $API.getPlantingPlanByLandId(landId)
    const data = res.data.data

    if (!data || data.planId == null) {
      // 当前地块无种植计划
      noPlanTip.value = true
    } else {
      noPlanTip.value = false
      addForm.value.planId = data.planId
      addForm.value.cropId = data.cropId
    }
  } catch (e) {
    console.error('查询关联计划失败:', e)
    noPlanTip.value = true
  }
}

// ==================== 搜索/分页 ====================

const handleSearch = () => {
  currentPage.value = 1
  loadRecords()
}

const handleReset = () => {
  searchQuery.value = ''
  currentPage.value = 1
  loadRecords()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadRecords()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadRecords()
}

// ==================== 添加 ====================

const openAddDialog = () => {
  addForm.value = {
    landId: '',
    planId: '',
    cropId: '',
    plantingDate: '',
    expectedHarvestDate: '',
    actualHarvestDate: '',
  }
  noPlanTip.value = false
  addDialogVisible.value = true
  nextTick(() => addFormRef.value?.clearValidate())
}

const resetAddForm = () => {
  noPlanTip.value = false
}

const saveAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const formatDate = (date) => {
        if (!date) return null
        const d = new Date(date)
        const year = d.getFullYear()
        const month = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        const hours = String(d.getHours()).padStart(2, '0')
        const minutes = String(d.getMinutes()).padStart(2, '0')
        const seconds = String(d.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
      const payload = {
        recordId: null,
        landId: addForm.value.landId,
        planId: addForm.value.planId,
        cropId: addForm.value.cropId,
        plantingDate: formatDate(addForm.value.plantingDate),
        expectedHarvestDate: formatDate(addForm.value.expectedHarvestDate),
        actualHarvestDate: formatDate(addForm.value.actualHarvestDate) || null,
        status: 0,
      }
      const res = await $API.addPlantingRecord(payload)
      if (res.data.code === '200') {
        ElMessage.success('添加成功')
        addDialogVisible.value = false
        loadRecords()
      } else {
        ElMessage.error(res.data.message || '添加失败')
      }
    } catch (e) {
      console.error('添加失败:', e)
      ElMessage.error('添加失败')
    }
  })
}

// ==================== 编辑 ====================

const openEditDialog = async (row) => {
  // 确保下拉数据已加载
  await Promise.all([
    landList.value.length === 0 ? loadLand() : Promise.resolve(),
    planList.value.length === 0 ? loadPlan() : Promise.resolve(),
    cropList.value.length === 0 ? loadCrop() : Promise.resolve(),
  ])

  // 获取产出数量
  let outputQuantity = 0
  try {
    const res = await $API.getOutputQuantity(row.recordId)
    if (res.data?.code === '200' && res.data?.data) {
      outputQuantity = res.data.data.outputQuantity ?? 0
    }
  } catch (e) {
    console.error('获取产出数量失败:', e)
  }

  editForm.value = {
    recordId: row.recordId,
    landId: row.landId ?? '',
    landName: row.landName ?? '',
    planId: row.planId ?? '',
    planName: row.planName ?? '',
    cropId: row.cropId ?? '',
    cropName: row.cropName ?? '',
    plantingDate: row.plantingDate ? formatDate(row.plantingDate) : '',
    expectedHarvestDate: row.expectedHarvestDate ? formatDate(row.expectedHarvestDate) : '',
    actualHarvestDate: row.actualHarvestDate ? formatDate(row.actualHarvestDate) : '',
    status: row.status ?? 0,
    outputQuantity: outputQuantity,
    isHarvested: row.status === 1,
  }
  editDialogVisible.value = true
  nextTick(() => editFormRef.value?.clearValidate())
}

const saveEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const formatDate = (date) => {
        if (!date) return null
        const d = new Date(date)
        const year = d.getFullYear()
        const month = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        const hours = String(d.getHours()).padStart(2, '0')
        const minutes = String(d.getMinutes()).padStart(2, '0')
        const seconds = String(d.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
      const payload = {
        recordId: editForm.value.recordId,
        landId: editForm.value.landId,
        planId: editForm.value.planId,
        cropId: editForm.value.cropId,
        plantingDate: formatDate(editForm.value.plantingDate),
        expectedHarvestDate: formatDate(editForm.value.expectedHarvestDate),
        actualHarvestDate: formatDate(editForm.value.actualHarvestDate) || null,
        status: editForm.value.status,
        outputQuantity: editForm.value.outputQuantity,
      }
      const res = await $API.updatePlantingRecord(payload)
      if (res.data.code === '200') {
        ElMessage.success('修改成功')
        editDialogVisible.value = false
        loadRecords()
      } else {
        ElMessage.error(res.data.message || '修改失败')
      }
    } catch (e) {
      console.error('修改失败:', e)
      ElMessage.error('修改失败')
    }
  })
}

// ==================== 详情 ====================

const openDetailDialog = (row) => {
  detailRow.value = { ...row }
  detailDialogVisible.value = true
}
// ==================== 删除 ====================

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该种植记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await $API.deletePlantingRecord(row.recordId)
    if (res.data.code === '200') {
      ElMessage.success('删除成功')
      loadRecords()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// ==================== 初始化 ====================
onMounted(async () => {
  if (userRole.value === 'user') {
    try {
      const userInfoRes = await $API.getUserInfo()
      if (userInfoRes.data?.data?.userId) {
        userId.value = userInfoRes.data.data.userId
      }
    } catch (e) {
      console.error('获取用户信息失败:', e)
    }
  }
  loadRecords()
  loadLand()
  loadPlan()
  loadCrop()
})
</script>

<style scoped>
.planting-management {
  padding: 20px;
  background-color: #f5f7fa;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.table-container {
  height: calc(100vh - 280px);
  overflow: auto;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.table-container::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.table-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #4caf50 0%, #2e7d32 100%);
  border-radius: 10px;
  border: 2px solid #fff;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #2e7d32 0%, #4caf50 100%);
}

.table-container::-webkit-scrollbar-corner {
  background: #f1f1f1;
}

.table-container {
  scrollbar-width: thin;
  scrollbar-color: #4caf50 #f1f1f1;
}
</style>
