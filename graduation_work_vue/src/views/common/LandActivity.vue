<template>
  <div class="planting-management">
    <!-- 标题 -->
    <div class="header">
      <h1>{{ roleTitle }}-农事活动管理</h1>
      <!-- <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        新增农事活动
      </el-button> -->
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
        <el-table-column prop="cropName" label="农作物" align="center" />
        <el-table-column prop="plantingDate" label="种植时间" width="160" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.plantingDate) }}
          </template>
        </el-table-column>
        <!-- <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" effect="dark" size="small">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column> -->

        <!-- 操作 -->
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button type="info" size="small" plain @click="openDetailDialog(scope.row)">
              查看农事活动记录
            </el-button>
            <!-- <el-button type="primary" size="small" plain @click="openEditDialog(scope.row)">
              添加农事活动
            </el-button>
            <el-button type="danger" size="small" plain @click="handleDelete(scope.row)">
              删除
            </el-button> -->
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

    <!-- 农事活动历史记录弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="🌾 农事活动历史记录"
      width="1000px"
      class="activity-dialog"
    >
      <!-- 头部信息卡片 -->
      <div class="activity-header-card">
        <div class="header-title">
          <el-icon><Document /></el-icon>
          <span>种植记录信息</span>
        </div>
        <el-descriptions :column="3" border class="record-info">
          <el-descriptions-item label="记录ID" label-class-name="label-style">
            <span class="value-highlight">{{ detailRow.recordId }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="地块名" label-class-name="label-style">
            <span class="value-highlight">{{ detailRow.landName }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="农作物" label-class-name="label-style">
            <span class="value-highlight">{{ detailRow.cropName }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 操作按钮区 -->
      <div class="activity-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="openAddActivityDialog" class="add-btn">
            <el-icon><Plus /></el-icon>
            新增农事活动
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-tag type="info" effect="plain" size="large"> 共 {{ activityTotal }} 条记录 </el-tag>
        </div>
      </div>

      <el-divider content-position="left">
        <span class="divider-title">📋 农事活动列表</span>
      </el-divider>

      <!-- 农事活动表格 -->
      <el-table
        :data="activityList"
        border
        stripe
        v-loading="activityLoading"
        style="width: 100%"
        max-height="450"
        class="activity-table"
        highlight-current-row
      >
        <el-table-column prop="operationId" label="操作ID" width="90" align="center" fixed="left">
          <template #default="scope">
            <span class="id-badge">#{{ scope.row.operationId }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="operationType"
          label="操作类型"
          width="120"
          align="center"
          fixed="left"
        >
          <template #default="scope">
            <el-tag
              :type="getActivityTypeTag(scope.row.operationType)"
              effect="dark"
              size="small"
              class="activity-tag"
            >
              {{ scope.row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" min-width="100" align="center" />

        <el-table-column prop="materialName" label="农资名" min-width="120" align="center">
          <template #default="scope">
            <span v-if="scope.row.materialName">{{ scope.row.materialName }}</span>
            <el-tag v-else type="info" size="small" effect="plain">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="用量" min-width="100" align="center">
          <template #default="scope">
            <span>{{ scope.row.quantity || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="outputQuantity" label="产量(kg)" min-width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.operationType === '收割'">
              {{ scope.row.outputQuantity || '无' }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130" align="center" />
        <el-table-column prop="operationTime" label="操作时间" width="170" align="center">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(scope.row.operationTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <div class="action-btns">
              <el-button
                type="primary"
                size="small"
                plain
                @click="openEditActivityDialog(scope.row)"
                class="action-btn"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                plain
                @click="handleDeleteActivity(scope.row)"
                class="action-btn"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="activity-pagination">
        <el-pagination
          v-model:current-page="activityPageNum"
          v-model:page-size="activityPageSize"
          :page-sizes="[5]"
          :total="activityTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleActivityPageChange"
          @size-change="handleActivitySizeChange"
          background
        />
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false" size="large">
          <el-icon><Close /></el-icon>
          关闭
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加农事活动弹窗 -->
    <el-dialog v-model="addActivityDialogVisible" title="🌱 新增农事活动" width="600px">
      <el-form
        ref="addActivityFormRef"
        :model="addActivityForm"
        :rules="activityFormRules"
        label-width="100px"
      >
        <el-form-item label="操作类型" prop="operationType">
          <el-select
            v-model="addActivityForm.operationType"
            placeholder="请选择操作类型"
            style="width: 100%"
          >
            <el-option label="播种" value="播种" />
            <el-option label="施肥" value="施肥" />
            <el-option label="浇水" value="浇水" />
            <el-option label="除草" value="除草" />
            <el-option label="除虫" value="除虫" />
            <el-option label="收割" value="收割" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="addActivityForm.operationType === '收割'"
          label="产量(kg)"
          prop="outputQuantity"
        >
          <el-input
            type="number"
            min="0"
            v-model="addActivityForm.outputQuantity"
            placeholder="请输入产量"
          />
        </el-form-item>
        <el-form-item label="操作时间" prop="operationTime">
          <el-date-picker
            v-model="addActivityForm.operationTime"
            type="datetime"
            placeholder="选择操作时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="操作人" prop="operatorName">
          <el-input v-model="addActivityForm.operatorName" placeholder="请输入操作人姓名" />
        </el-form-item>
        <el-form-item
          v-if="addActivityForm.operationType !== '收割'"
          label="农资"
          prop="materialId"
        >
          <el-select
            v-model="addActivityForm.materialId"
            placeholder="请选择农资（可选）"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="item in materialList"
              :key="item.materialId"
              :label="item.materialName"
              :value="item.materialId"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="addActivityForm.operationType !== '收割'" label="用量" prop="quantity">
          <el-input type="number" v-model="addActivityForm.quantity" placeholder="请输入用量" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addActivityForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="addActivityForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addActivityDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddActivity">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑农事活动弹窗 -->
    <el-dialog v-model="editActivityDialogVisible" title="✏️ 编辑农事活动" width="600px">
      <el-form
        ref="editActivityFormRef"
        :model="editActivityForm"
        :rules="activityFormRules"
        label-width="100px"
      >
        <el-form-item label="操作ID">
          <el-input v-model="editActivityForm.operationId" disabled />
        </el-form-item>
        <el-form-item label="操作类型" prop="operationType">
          <el-select
            v-model="editActivityForm.operationType"
            placeholder="请选择操作类型"
            style="width: 100%"
            :disabled="editActivityForm.operationType === '收割'"
          >
            <el-option label="播种" value="播种" />
            <el-option label="施肥" value="施肥" />
            <el-option label="浇水" value="浇水" />
            <el-option label="除草" value="除草" />
            <el-option label="除虫" value="除虫" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="editActivityForm.operationType === '收割'"
          label="产量(kg)"
          prop="outputQuantity"
        >
          <el-input
            type="number"
            min="0"
            v-model="editActivityForm.outputQuantity"
            placeholder="请输入产量"
          />
        </el-form-item>
        <el-form-item label="操作时间" prop="operationTime">
          <el-date-picker
            v-model="editActivityForm.operationTime"
            type="datetime"
            placeholder="选择操作时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="操作人" prop="operatorName">
          <el-input v-model="editActivityForm.operatorName" placeholder="请输入操作人姓名" />
        </el-form-item>
        <el-form-item
          v-if="editActivityForm.operationType !== '收割'"
          label="农资"
          prop="materialId"
        >
          <el-select
            v-model="editActivityForm.materialId"
            placeholder="请选择农资（可选）"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="item in materialList"
              :key="item.materialId"
              :label="item.materialName"
              :value="item.materialId"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="editActivityForm.operationType !== '收割'" label="用量" prop="quantity">
          <el-input type="number" v-model="editActivityForm.quantity" placeholder="请输入用量" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editActivityForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="editActivityForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editActivityDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEditActivity">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Plus, Search, Document, Clock, Edit, Delete, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import $API from '@/api'

const role = ref(localStorage.getItem('role') || '')
const roleTitle = computed(() => {
  const map = {
    user: '承包人',
    enterprise_admin: '企业管理员',
    system_admin: '系统管理员',
  }
  return map[role.value] || '系统管理员'
})

// ==================== 列表相关 ====================
const recordList = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
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
  operationId: '',
  operationType: '',
  operatorName: '',
  materialId: '',
  quantity: 0,
  phone: '',
  description: '',
})

const editFormRules = {
  plantingDate: [{ required: true, message: '请选择种植日期', trigger: 'change' }],
  expectedHarvestDate: [{ required: true, message: '请选择期望收割日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// ==================== 详情弹窗 ====================
const detailDialogVisible = ref(false)
const detailRow = ref({})
const activityList = ref([])
const activityLoading = ref(false)
const activityPageNum = ref(1)
const activityPageSize = ref(5)
const activityTotal = ref(0)

// ==================== 农事活动表单 ====================
const materialList = ref([])

// 添加农事活动
const addActivityDialogVisible = ref(false)
const addActivityFormRef = ref(null)
const addActivityForm = ref({
  recordId: '',
  operationType: '',
  operatorName: '',
  materialId: '',
  quantity: 0,
  phone: '',
  description: '',
  outputQuantity: '',
  operationTime: '',
})

// 编辑农事活动
const editActivityDialogVisible = ref(false)
const editActivityFormRef = ref(null)
const editActivityForm = ref({
  operationId: '',
  recordId: '',
  operationType: '',
  operatorName: '',
  materialId: '',
  quantity: '',
  phone: '',
  description: '',
  operationTime: '',
  outputQuantity: '',
})

const activityFormRules = {
  operationType: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
  operatorName: [{ required: true, message: '请输入操作人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  operationTime: [{ required: true, message: '请选择操作时间', trigger: 'change' }],
}

// ==================== 工具函数 ====================

/* 状态文字 */
const statusText = (status) => {
  const map = { 0: '生长中', 1: '已成熟' }
  return map[status] ?? '未知'
}

/* 状态标签类型 */
const statusTagType = (status) => {
  const map = { 0: 'success', 1: 'warning' }
  return map[status] ?? 'info'
}

/* 农事活动类型标签类型 */
const getActivityTypeTag = (type) => {
  const map = {
    播种: 'primary',
    施肥: 'success',
    浇水: 'info',
    除草: 'warning',
    除虫: 'danger',
    收割: 'success',
  }
  return map[type] ?? 'info'
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

/* 加载农事活动分页列表 */
const loadRecords = async () => {
  loading.value = true
  try {
    let res
    if (userRole.value === 'user') {
      res = await $API.getMyPlantingRecords(currentPage.value, pageSize.value)
    } else {
      res = await $API.getGrowthPlantingRecordByPage(currentPage.value, pageSize.value)
    }
    if (res.data.code === '200') {
      recordList.value = res.data.data.records
      total.value = res.data.data.total
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

  editForm.value = {
    recordId: row.recordId,
    landName: row.landName ?? '',
    planName: row.planName ?? '',
    cropName: row.cropName ?? '',
    plantingDate: row.plantingDate ? formatDate(row.plantingDate) : '',
    expectedHarvestDate: row.expectedHarvestDate ? formatDate(row.expectedHarvestDate) : '',
    actualHarvestDate: row.actualHarvestDate ? formatDate(row.actualHarvestDate) : '',
    status: row.status ?? 0,
    outputQuantity: row.outputQuantity ?? 0,
  }
  editDialogVisible.value = true
  nextTick(() => editFormRef.value?.clearValidate())
}

// ==================== 详情 ====================

const openDetailDialog = async (row) => {
  detailRow.value = { ...row }
  activityPageNum.value = 1
  detailDialogVisible.value = true
  await loadActivityRecords(row.recordId)
}

/* 加载农事活动历史记录 */
const loadActivityRecords = async (recordId) => {
  activityLoading.value = true
  try {
    const res = await $API.getLandActivitiesByRecordId(
      recordId,
      activityPageNum.value,
      activityPageSize.value,
    )
    if (res.data.code === '200') {
      activityList.value = res.data.data.records || []
      activityTotal.value = res.data.data.total || 0
    } else {
      activityList.value = []
      activityTotal.value = 0
    }
  } catch (e) {
    console.error('加载农事活动记录失败:', e)
    activityList.value = []
    activityTotal.value = 0
  } finally {
    activityLoading.value = false
  }
}

/* 农事活动分页改变 */
const handleActivityPageChange = (page) => {
  activityPageNum.value = page
  loadActivityRecords(detailRow.value.recordId)
}

/* 农事活动每页数量改变 */
const handleActivitySizeChange = (size) => {
  activityPageSize.value = size
  activityPageNum.value = 1
  loadActivityRecords(detailRow.value.recordId)
}

// ==================== 农事活动 CRUD ====================

/* 加载农资列表 */
const loadMaterials = async () => {
  try {
    const res = await $API.getMaterialList()
    if (res.data.code === '200') {
      materialList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载农资列表失败:', e)
  }
}

/* 打开添加农事活动对话框 */
const openAddActivityDialog = async () => {
  await loadMaterials()
  addActivityForm.value = {
    recordId: detailRow.value.recordId,
    operationType: '',
    operatorName: '',
    materialId: '',
    quantity: 0,
    phone: '',
    description: '',
    outputQuantity: '',
    operationTime: '',
  }
  addActivityDialogVisible.value = true
  nextTick(() => addActivityFormRef.value?.clearValidate())
}

/* 保存添加农事活动 */
const saveAddActivity = async () => {
  if (!addActivityFormRef.value) return
  await addActivityFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      addActivityForm.value.quantity = Number(addActivityForm.value.quantity)
      const res = await $API.addLandActivity(addActivityForm.value)
      if (res.data.code === '200') {
        ElMessage.success('添加成功')
        addActivityDialogVisible.value = false
        await loadActivityRecords(detailRow.value.recordId)
      } else {
        ElMessage.error(res.data.message || '添加失败')
      }
    } catch (e) {
      console.error('添加农事活动失败:', e)
      ElMessage.error('添加失败')
    }
  })
}

/* 打开编辑农事活动对话框 */
const openEditActivityDialog = async (row) => {
  await loadMaterials()
  editActivityForm.value = {
    operationId: row.operationId,
    recordId: detailRow.value.recordId,
    operationType: row.operationType,
    operatorName: row.operatorName,
    materialId: row.materialId || '',
    quantity: row.quantity || 0,
    phone: row.phone,
    description: row.description || '',
    operationTime: row.operationTime || '',
    outputQuantity: row.outputQuantity || 0,
  }
  editActivityDialogVisible.value = true
  nextTick(() => editActivityFormRef.value?.clearValidate())
}

/* 保存编辑农事活动 */
const saveEditActivity = async () => {
  if (!editActivityFormRef.value) return
  await editActivityFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      // 处理清除农资时的情况
      if (editActivityForm.value.materialId === '') {
        editActivityForm.value.materialId = 0
      }
      editActivityForm.value.quantity = Number(editActivityForm.value.quantity)
      if (editActivityForm.value.operationType !== '收割') {
        editActivityForm.value.outputQuantity = 0
      }
      editActivityForm.value.outputQuantity = Number(editActivityForm.value.outputQuantity)
      const res = await $API.updateLandActivity(editActivityForm.value)
      if (res.data.code === '200') {
        ElMessage.success('修改成功')
        editActivityDialogVisible.value = false
        await loadActivityRecords(detailRow.value.recordId)
      } else {
        ElMessage.error(res.data.message || '修改失败')
      }
    } catch (e) {
      console.error('修改农事活动失败:', e)
      ElMessage.error('修改失败')
    }
  })
}

/* 删除农事活动 */
const handleDeleteActivity = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该农事活动记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await $API.deleteLandActivity(row.operationId)
    if (res.data.code === '200') {
      ElMessage.success('删除成功')
      await loadActivityRecords(detailRow.value.recordId)
    } else {
      ElMessage.error(res.data.messtiage || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除农事活动失败:', e)
      ElMessage.error('删除失败')
    }
  }
}

// ==================== 删除 ====================

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该农事活动吗？', '提示', {
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

/* 农事活动弹窗样式 */
.activity-header-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.header-title .el-icon {
  font-size: 20px;
}

.record-info {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 16px;
  border-radius: 8px;
}

.record-info :deep(.label-style) {
  background-color: #f0f2f5;
  font-weight: 600;
  color: #606266;
}

.value-highlight {
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
}

.activity-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.toolbar-left .add-btn {
  background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
  border: none;
  box-shadow: 0 2px 12px rgba(103, 194, 58, 0.3);
}

.toolbar-left .add-btn:hover {
  background: linear-gradient(135deg, #85ce61 0%, #67c23a 100%);
  transform: translateY(-1px);
}

.divider-title {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
}

.activity-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.activity-table :deep(.el-table__header) {
  background-color: #f5f7fa;
}

.id-badge {
  display: inline-block;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.activity-tag {
  font-weight: 600;
  padding: 4px 12px;
}

.time-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

.time-cell .el-icon {
  color: #409eff;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.3s;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.activity-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  margin-right: 0;
}

:deep(.activity-dialog .el-dialog__title) {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

:deep(.activity-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

:deep(.activity-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
  color: #f0f0f0;
}

.activity-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 8px;
}

.activity-pagination :deep(.el-pagination) {
  display: flex;
  align-items: center;
}

.activity-pagination :deep(.el-pagination.is-background .el-pager li) {
  background-color: white;
  border: 1px solid #dcdfe6;
  color: #606266;
  font-weight: 500;
  transition: all 0.3s;
}

.activity-pagination :deep(.el-pagination.is-background .el-pager li:hover) {
  color: #409eff;
  border-color: #409eff;
}

.activity-pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-color: #409eff;
  color: white;
}

.activity-pagination :deep(.el-pagination.is-background .btn-prev),
.activity-pagination :deep(.el-pagination.is-background .btn-next) {
  background-color: white;
  border: 1px solid #dcdfe6;
  color: #606266;
  transition: all 0.3s;
}

.activity-pagination :deep(.el-pagination.is-background .btn-prev:hover),
.activity-pagination :deep(.el-pagination.is-background .btn-next:hover) {
  color: #409eff;
  border-color: #409eff;
}

.activity-pagination :deep(.el-pagination__total) {
  color: #606266;
  font-weight: 500;
}

.activity-pagination :deep(.el-pagination__sizes) {
  color: #606266;
}

.activity-pagination :deep(.el-pagination__jump) {
  color: #606266;
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
