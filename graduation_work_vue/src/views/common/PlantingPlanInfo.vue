<template>
  <div class="plan-management">
    <!-- 标题 -->
    <div class="header">
      <h1>{{ roleTitle }}-种植计划管理</h1>

      <el-button v-if="userRole !== 'user'" type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        制定计划
      </el-button>
    </div>

    <!-- 搜索 -->
    <div class="search-bar" v-if="userRole !== 'user'">
      <el-input
        v-model="searchQuery"
        placeholder="输入计划名"
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
        :data="planList"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
        height="100%"
      >
        <el-table-column prop="planId" label="计划ID" align="center" />

        <el-table-column prop="planName" label="计划名" align="center" />

        <el-table-column prop="landId" label="地块ID" align="center" v-if="false" />

        <el-table-column prop="landName" label="地块名" align="center" />

        <el-table-column
          prop="landLocation"
          label="地块位置"
          align="center"
          show-overflow-tooltip
        />

        <el-table-column prop="cropId" label="农作物ID" align="center" v-if="false" />

        <el-table-column prop="cropName" label="农作物" align="center" />

        <el-table-column prop="status" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" effect="dark" size="small">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="openDetailDialog(scope.row)">
              详情
            </el-button>

            <el-button
              v-if="userRole !== 'user'"
              :type="scope.row.status === 0 ? 'success' : 'warning'"
              size="small"
              plain
              @click="toggleStatus(scope.row)"
              :disabled="scope.row.status === 3 || scope.row.status === 4"
            >
              {{ scope.row.status === 0 ? '发布' : '终止' }}
            </el-button>

            <el-button
              v-if="userRole !== 'user'"
              type="danger"
              size="small"
              plain
              @click="confirmDelete(scope.row)"
            >
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

    <!-- 详情/编辑弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="种植计划详情" width="600px">
      <el-form :model="detailForm" label-width="100px">
        <el-form-item label="计划ID">
          <el-input v-model="detailForm.planId" disabled />
        </el-form-item>

        <el-form-item label="计划名">
          <el-input
            v-model="detailForm.planName"
            placeholder="请输入计划名"
            :disabled="userRole === 'user'"
          />
        </el-form-item>

        <el-form-item label="地块">
          <el-select
            v-model="detailForm.landId"
            placeholder="请选择地块"
            style="width: 100%"
            @change="handleDetailLandChange"
            :disabled="userRole === 'user'"
          >
            <el-option
              v-for="item in landList"
              :key="item.landId"
              :label="item.landName"
              :value="item.landId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="地块位置">
          <el-input v-model="detailForm.landLocation" disabled placeholder="选择地块后自动填充" />
        </el-form-item>

        <el-form-item label="地块面积">
          <el-input v-model="detailForm.landArea" placeholder="请输入地块面积" disabled>
            <template #append>亩</template>
          </el-input>
        </el-form-item>

        <el-form-item label="农作物">
          <el-select
            v-model="detailForm.cropId"
            placeholder="请选择农作物"
            style="width: 100%"
            @change="handleDetailCropChange"
            :disabled="userRole === 'user'"
          >
            <el-option
              v-for="item in cropList"
              :key="item.cropId"
              :label="item.cropName"
              :value="item.cropId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="创建人">
          <el-input v-model="detailForm.creator" disabled />
        </el-form-item>

        <el-form-item label="期望产出">
          <el-input
            v-model="detailForm.expectedOutput"
            placeholder="请输入期望产出"
            :disabled="userRole === 'user'"
          >
            <template #append>kg</template>
          </el-input>
        </el-form-item>

        <el-form-item label="开始时间">
          <el-date-picker
            v-model="detailForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            :disabled="userRole === 'user'"
          />
        </el-form-item>

        <el-form-item label="结束时间">
          <el-date-picker
            v-model="detailForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            :disabled="userRole === 'user'"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="detailForm.status"
            placeholder="请选择状态"
            style="width: 100%"
            :disabled="userRole === 'user'"
          >
            <el-option label="未发布" :value="0" />
            <el-option label="执行中" :value="1" />
            <el-option label="调整中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已终止" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="创建时间">
          <el-input :value="formatDate(detailForm.createTime)" disabled />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">取消</el-button>
          <el-button v-if="userRole !== 'user'" type="primary" @click="saveDetail">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加计划弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加种植计划" width="600px">
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="100px">
        <el-form-item label="计划名" prop="planName">
          <el-input v-model="addForm.planName" placeholder="请输入计划名" />
        </el-form-item>

        <el-form-item label="地块" prop="landId">
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

        <el-form-item label="地块位置">
          <el-input v-model="addForm.landLocation" disabled placeholder="选择地块后自动填充" />
        </el-form-item>

        <el-form-item label="农作物" prop="cropId">
          <el-select
            v-model="addForm.cropId"
            placeholder="请选择农作物"
            style="width: 100%"
            @change="handleCropChange"
          >
            <el-option
              v-for="item in cropList"
              :key="item.cropId"
              :label="item.cropName"
              :value="item.cropId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="期望产出" prop="expectedOutput">
          <el-input v-model="addForm.expectedOutput" placeholder="请输入期望产出">
            <template #append>kg</template>
          </el-input>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="addForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="addForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAdd">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import $API from '@/api'

const planList = ref([])
const loading = ref(false)
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

const searchQuery = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailDialogVisible = ref(false)
const detailForm = ref({})

// 添加计划相关
const addDialogVisible = ref(false)
const addFormRef = ref(null)
const addForm = ref({
  planName: '',
  landId: '',
  landName: '',
  landLocation: '',
  cropId: '',
  cropName: '',
  expectedOutput: '',
  startTime: '',
  endTime: '',
})

const addFormRules = {
  planName: [{ required: true, message: '请输入计划名', trigger: 'blur' }],
  landId: [{ required: true, message: '请选择地块', trigger: 'change' }],
  cropId: [{ required: true, message: '请选择农作物', trigger: 'change' }],
  expectedOutput: [{ required: true, message: '请输入期望产出', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

const landList = ref([])
const cropList = ref([])

/* 状态文字 */
const statusText = (status) => {
  const map = {
    0: '未发布',
    1: '执行中',
    2: '调整中',
    3: '已完成',
    4: '已终止',
  }
  return map[status] || '未知'
}

/* 状态标签类型 */
const statusTagType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'primary',
    4: 'danger',
  }
  return map[status] || 'info'
}

/* 时间格式 */
const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString()
}

/* 加载计划 */
const loadPlans = async () => {
  loading.value = true
  try {
    let res

    if (userRole.value === 'user') {
      res = await $API.getMyPlans()
      if (res.data.code === '200') {
        planList.value = res.data.data || []
        total.value = res.data.data.length || 0
      }
    } else if (searchQuery.value) {
      res = await $API.searchPlantingPlansByPage(
        searchQuery.value,
        currentPage.value,
        pageSize.value,
      )
      if (res.data.code === '200') {
        planList.value = res.data.data.records
        total.value = res.data.data.total
      }
    } else {
      res = await $API.getPlantingPlansByPage(currentPage.value, pageSize.value)
      if (res.data.code === '200') {
        planList.value = res.data.data.records
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
const loadLands = async () => {
  try {
    const res = await $API.getLandsByPage(1, 1000)
    if (res.data.code === '200') {
      landList.value = res.data.data.records || []
    }
  } catch (e) {
    console.error('加载地块失败:', e)
  }
}

/* 加载农作物列表 */
const loadCrops = async () => {
  try {
    const res = await $API.getCropsByPage(1, 1000)
    if (res.data.code === '200') {
      cropList.value = res.data.data.records || []
    }
  } catch (e) {
    console.error('加载农作物失败:', e)
  }
}

/* 详情地块选择变化 */
const handleDetailLandChange = (landId) => {
  const land = landList.value.find((item) => {
    return parseInt(item.landId) === parseInt(landId) || item.landId === landId
  })
  if (land) {
    detailForm.value.landName = land.landName
    detailForm.value.landLocation = land.location
    detailForm.value.landArea = land.area
  }
}

/* 详情农作物选择变化 */
const handleDetailCropChange = (cropId) => {
  const crop = cropList.value.find((item) => {
    return parseInt(item.cropId) === parseInt(cropId) || item.cropId === cropId
  })
  if (crop) {
    detailForm.value.cropName = crop.cropName
  }
}

/* 搜索 */
const handleSearch = () => {
  currentPage.value = 1
  loadPlans()
}

/* 重置 */
const handleReset = () => {
  searchQuery.value = ''
  currentPage.value = 1
  loadPlans()
}

/* 分页 */
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadPlans()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadPlans()
}

/* 打开详情弹窗 */
const openDetailDialog = async (row) => {
  detailForm.value = { ...row }
  await loadLands()
  await loadCrops()
  // 获取创建人姓名
  const res = await $API.getCreatorNameById(row.planId)
  if (res.data.code === '200') {
    detailForm.value.creatorName = res.data.data.creator || ''
  }
  nextTick(() => {
    // 确保地块ID正确设置，使下拉栏默认显示当前地块名
    const landId = parseInt(row.landId) || row.landId
    detailForm.value.landId = landId
    // 确保农作物ID正确设置，使下拉栏默认显示当前农作物名
    const cropId = parseInt(row.cropId) || row.cropId
    detailForm.value.cropId = cropId
    // 更新地块位置和地块名
    const land = landList.value.find((item) => {
      return parseInt(item.landId) === landId || item.landId === landId
    })
    if (land) {
      detailForm.value.landName = land.landName
      detailForm.value.landLocation = land.location
    }
    // 更新农作物名称
    const crop = cropList.value.find((item) => {
      return parseInt(item.cropId) === cropId || item.cropId === cropId
    })
    if (crop) {
      detailForm.value.cropName = crop.cropName
    }
  })
  detailDialogVisible.value = true
}

/* 保存详情 */
const saveDetail = async () => {
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
    const updateData = {
      planId: detailForm.value.planId,
      planName: detailForm.value.planName,
      landId: detailForm.value.landId,
      landArea: detailForm.value.landArea,
      cropId: detailForm.value.cropId,
      creatorName: detailForm.value.creator,
      expectedOutput: detailForm.value.expectedOutput,
      startTime: formatDate(detailForm.value.startTime),
      endTime: formatDate(detailForm.value.endTime),
      status: detailForm.value.status,
    }
    const res = await $API.updatePlantingPlan(updateData)
    if (res.data.code === '200') {
      ElMessage.success('保存成功')
      detailDialogVisible.value = false
      loadPlans()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

/* 发布/终止 */
const toggleStatus = async (row) => {
  const newStatus = row.status === 0 ? 1 : 4
  const action = row.status === 0 ? '发布' : '终止'

  try {
    await ElMessageBox.confirm(`确认${action}该计划吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await $API.updatePlantingPlanStatus(row.planId, newStatus)
    if (res.data.code === '200') {
      ElMessage.success(`${action}成功`)
      loadPlans()
    } else {
      ElMessage.error(res.data.message || `${action}失败`)
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

/* 删除 */
const confirmDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除计划"${row.planName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await $API.deletePlantingPlan(row.planId)
    if (res.data.code === '200') {
      ElMessage.success('删除成功')
      loadPlans()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

/* 打开添加对话框 */
const openAddDialog = () => {
  addForm.value = {
    planName: '',
    landId: '',
    landName: '',
    landLocation: '',
    cropId: '',
    cropName: '',
    expectedOutput: '',
    startTime: '',
    endTime: '',
  }
  loadLands()
  loadCrops()
  addDialogVisible.value = true
  nextTick(() => {
    addFormRef.value?.clearValidate()
  })
}

/* 地块选择变化 */
const handleLandChange = (landId) => {
  const land = landList.value.find((item) => item.landId === landId)
  if (land) {
    addForm.value.landName = land.landName
    addForm.value.landLocation = land.location
  }
}

/* 农作物选择变化 */
const handleCropChange = (cropId) => {
  const crop = cropList.value.find((item) => item.cropId === cropId)
  if (crop) {
    addForm.value.cropName = crop.cropName
  }
}

/* 保存添加 */
const saveAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
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
        const addData = {
          planName: addForm.value.planName,
          landName: addForm.value.landName,
          landLocation: addForm.value.landLocation,
          cropName: addForm.value.cropName,
          expectedOutput: addForm.value.expectedOutput,
          startTime: formatDate(addForm.value.startTime),
          endTime: formatDate(addForm.value.endTime),
        }
        const res = await $API.addPlantingPlan(addData)
        if (res.data.code === '200') {
          ElMessage.success('添加成功')
          addDialogVisible.value = false
          loadPlans()
        } else {
          ElMessage.error(res.data.message || '添加失败')
        }
      } catch (e) {
        console.error('添加失败:', e)
        ElMessage.error('添加失败')
      }
    }
  })
}

onMounted(() => {
  loadPlans()
})
</script>

<style scoped>
.plan-management {
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

/* 美化滚动条 */
.table-container::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.table-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  border: 2px solid #fff;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  border: 2px solid #f0f7ff;
}

.table-container::-webkit-scrollbar-corner {
  background: #f1f1f1;
}

/* Firefox 滚动条样式 */
.table-container {
  scrollbar-width: thin;
  scrollbar-color: #764ba2 #f1f1f1;
}
</style>
