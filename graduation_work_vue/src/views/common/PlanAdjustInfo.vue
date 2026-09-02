<template>
  <div class="adjust-management">
    <!-- 标题 -->
    <div class="header">
      <h1>{{ roleTitle }}-计划审批管理</h1>

      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        申请调整
      </el-button>
    </div>

    <!-- 搜索 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="输入计划名/申请人"
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
        :data="adjustList"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
        height="100%"
      >
        <el-table-column prop="adjustId" label="审批ID" width="100" align="center" />

        <el-table-column prop="planName" label="计划名" align="center" />

        <el-table-column prop="applicant" label="申请人" width="120" align="center" />

        <el-table-column prop="phone" label="联系电话" width="140" align="center" />

        <el-table-column prop="reason" label="调整原因" align="center" show-overflow-tooltip />

        <el-table-column prop="applyTime" label="申请时间" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.applyTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" effect="dark" size="small">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="scope">
            <template v-if="userRole !== 'user'">
              <el-button
                v-if="scope.row.status === 0"
                type="success"
                size="small"
                plain
                @click="handleApprove(scope.row)"
              >
                通过
              </el-button>
              <el-button
                v-if="scope.row.status === 0"
                type="danger"
                size="small"
                plain
                @click="handleReject(scope.row)"
              >
                拒绝
              </el-button>
              <el-button v-if="scope.row.status !== 0" type="info" size="small" plain disabled>
                已处理
              </el-button>
              <el-button type="primary" size="small" plain @click="openEditDialog(scope.row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" plain @click="handleDelete(scope.row)">
                删除
              </el-button>
            </template>
            <template v-else>
              <el-button
                v-if="scope.row.status === 0"
                type="danger"
                size="small"
                plain
                @click="handleCancel(scope.row)"
              >
                取消申请
              </el-button>
              <el-button v-if="scope.row.status !== 0" type="info" size="small" plain disabled>
                已处理
              </el-button>
            </template>
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

    <!-- 添加调整申请弹窗 -->
    <el-dialog v-model="addDialogVisible" title="申请计划调整" width="600px">
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="100px">
        <el-form-item label="计划" prop="planId">
          <el-select
            v-model="addForm.planId"
            placeholder="请选择计划"
            style="width: 100%"
            @change="handlePlanChange"
          >
            <el-option
              v-for="item in planList"
              :key="item.planId"
              :label="item.planName"
              :value="item.planId"
            />
          </el-select>
        </el-form-item>

        <el-collapse v-if="showPlanDetails">
          <el-collapse-item title="计划详情">
            <el-form-item label="地块名" prop="landId">
              <el-select
                v-model="addForm.landId"
                placeholder="请选择地块"
                style="width: 100%"
                @change="handleLandChange"
                disabled
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
              <el-input v-model="addForm.landLocation" disabled />
            </el-form-item>

            <el-form-item label="地块面积">
              <el-input v-model="addForm.landArea" disabled>
                <template #append>亩</template>
              </el-input>
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

            <el-form-item label="创建人">
              <el-input v-model="addForm.creator" disabled />
            </el-form-item>

            <el-form-item label="期望产出" prop="expectedOutput">
              <el-input v-model="addForm.expectedOutput" placeholder="请输入期望产出" disabled>
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
          </el-collapse-item>
        </el-collapse>

        <el-form-item label="申请人" prop="applicant">
          <el-input v-model="addForm.applicant" placeholder="请输入申请人姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="addForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAdd">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑调整申请弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑计划调整" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="addFormRules" label-width="100px">
        <el-form-item label="计划名">
          <el-input v-model="editForm.planName" disabled />
        </el-form-item>

        <el-form-item label="地块">
          <el-input v-model="editForm.landName" disabled />
        </el-form-item>

        <el-form-item label="农作物">
          <el-input v-model="editForm.cropName" disabled />
        </el-form-item>

        <el-form-item label="申请人" prop="applicant">
          <el-input v-model="editForm.applicant" placeholder="请输入申请人姓名" disabled />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入联系电话" disabled />
        </el-form-item>

        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="editForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEdit">保存修改</el-button>
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
const adjustList = ref([])
const loading = ref(false)

const searchQuery = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 添加申请相关
const addDialogVisible = ref(false)
const addFormRef = ref(null)
const showPlanDetails = ref(false)
const addForm = ref({
  planId: '',
  landId: '',
  landName: '',
  landLocation: '',
  landArea: '',
  cropId: '',
  cropName: '',
  creator: '',
  expectedOutput: '',
  startTime: '',
  endTime: '',
  applicant: '',
  phone: '',
  reason: '',
})

// 编辑申请相关
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = ref({
  adjustId: '',
  planName: '',
  landName: '',
  cropName: '',
  applicant: '',
  phone: '',
  reason: '',
})

const addFormRules = {
  planId: [{ required: true, message: '请选择计划', trigger: 'change' }],
  applicant: [{ required: true, message: '请输入申请人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    // { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }],
}

const planList = ref([])
const landList = ref([])
const cropList = ref([])

/* 状态文字 */
const statusText = (status) => {
  const map = {
    0: '未审批',
    1: '已通过',
    2: '未通过',
  }
  return map[status] || '未知'
}

/* 状态标签类型 */
const statusTagType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
  }
  return map[status] || 'info'
}

/* 时间格式 */
const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString()
}

/* 加载审批列表 */
const loadAdjusts = async () => {
  loading.value = true
  try {
    let res

    if (role.value === 'user') {
      res = await $API.getMyPlanAdjusts(searchQuery.value, currentPage.value, pageSize.value)
    } else {
      res = await $API.searchPlanAdjustsByPage(searchQuery.value, currentPage.value, pageSize.value)
    }

    if (res.data.code === '200') {
      adjustList.value = res.data.data.records.sort((a, b) => a.adjustId - b.adjustId)
      total.value = res.data.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

/* 加载计划列表 */
const loadPlans = async () => {
  try {
    let res
    if (role.value === 'user') {
      res = await $API.getMyPublishedPlan()
    } else {
      res = await $API.getPublishedPlans()
    }
    if (res.data.code === '200') {
      planList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载计划失败:', e)
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

/* 搜索 */
const handleSearch = () => {
  currentPage.value = 1
  loadAdjusts()
}

/* 重置 */
const handleReset = () => {
  searchQuery.value = ''
  currentPage.value = 1
  loadAdjusts()
}

/* 分页 */
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadAdjusts()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadAdjusts()
}

/* 通过审批 */
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认通过该调整申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
    })

    const res = await $API.updatePlanAdjustStatus(row.adjustId, 1)
    if (res.data.code === '200') {
      ElMessage.success('审批通过')
      loadAdjusts()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

/* 拒绝审批 */
const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm(`确认拒绝该调整申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await $API.updatePlanAdjustStatus(row.adjustId, 2)
    if (res.data.code === '200') {
      ElMessage.success('已拒绝')
      loadAdjusts()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

/* 打开添加对话框 */
const openAddDialog = () => {
  addForm.value = {
    planId: '',
    landId: '',
    landName: '',
    landLocation: '',
    landArea: '',
    cropId: '',
    cropName: '',
    creator: '',
    expectedOutput: '',
    startTime: '',
    endTime: '',
    applicant: '',
    phone: '',
    reason: '',
  }
  // 重置计划详情显示状态
  showPlanDetails.value = false
  // 加载计划、地块和农作物列表
  loadPlans()
  loadLands()
  loadCrops()
  addDialogVisible.value = true
  nextTick(() => {
    addFormRef.value?.clearValidate()
  })
}

/* 计划选择变化 */
const handlePlanChange = async (planId) => {
  try {
    const res = await $API.getPlantingPlanById(planId)
    if (res.data.code === '200') {
      const plan = res.data.data
      addForm.value.landId = plan.landId
      addForm.value.landName = plan.landName
      addForm.value.landLocation = plan.landLocation
      addForm.value.landArea = plan.landArea
      addForm.value.cropId = plan.cropId
      addForm.value.cropName = plan.cropName
      addForm.value.creator = plan.creator
      addForm.value.expectedOutput = plan.expectedOutput
      addForm.value.startTime = plan.startTime
      addForm.value.endTime = plan.endTime
      // 显示计划详情
      showPlanDetails.value = true
    }
  } catch (e) {
    console.error('获取计划详情失败:', e)
    ElMessage.error('获取计划详情失败')
  }
}

/* 地块选择变化 */
const handleLandChange = (landId) => {
  const land = landList.value.find((item) => {
    return parseInt(item.landId) === parseInt(landId) || item.landId === landId
  })
  if (land) {
    addForm.value.landId = land.landId
    addForm.value.landName = land.landName
    addForm.value.landLocation = land.location
    addForm.value.landArea = land.area
  }
}

/* 农作物选择变化 */
const handleCropChange = (cropId) => {
  const crop = cropList.value.find((item) => {
    return parseInt(item.cropId) === parseInt(cropId) || item.cropId === cropId
  })
  if (crop) {
    addForm.value.cropName = crop.cropName
  }
}

/* 打开编辑对话框 */
const openEditDialog = async (row) => {
  try {
    // 检查 adjustId 是否存在
    if (!row.adjustId) {
      ElMessage.error('调整ID不存在')
      return
    }
    // 获取调整详情
    const adjustRes = await $API.getPlantingPlanAdjustById(row.adjustId)
    if (adjustRes.data.code === '200') {
      const adjustDetail = adjustRes.data.data
      editForm.value = {
        adjustId: adjustDetail.adjustId,
        planName: adjustDetail.planName || row.planName,
        landName: adjustDetail.landName,
        cropName: adjustDetail.cropName,
        applicant: adjustDetail.applicant || row.applicant,
        phone: adjustDetail.phone || row.phone,
        reason: adjustDetail.reason,
      }
      editDialogVisible.value = true
      nextTick(() => {
        editFormRef.value?.clearValidate()
      })
    }
  } catch (e) {
    console.error('获取调整详情失败:', e)
    ElMessage.error('获取调整详情失败')
  }
}

/* 保存编辑 */
const saveEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
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
        const editData = {
          adjustId: editForm.value.adjustId,
          planId: editForm.value.planId,
          landId: editForm.value.landId,
          cropId: editForm.value.cropId,
          expectedOutput: editForm.value.expectedOutput,
          startTime: formatDate(editForm.value.startTime),
          endTime: formatDate(editForm.value.endTime),
          applicant: editForm.value.applicant,
          phone: editForm.value.phone,
          reason: editForm.value.reason,
          applyTime: editForm.value.applyTime,
          status: editForm.value.status,
        }
        console.log(editData)
        const res = await $API.updatePlanAdjust(editData)
        if (res.data.code === '200') {
          ElMessage.success('修改成功')
          editDialogVisible.value = false
          loadAdjusts()
        } else {
          ElMessage.error(res.data.message || '修改失败')
        }
      } catch (e) {
        console.error('修改失败:', e)
        ElMessage.error('修改失败')
      }
    }
  })
}

/* 删除申请 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除该调整申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await $API.deletePlanAdjust(row.adjustId)
    if (res.data.code === '200') {
      ElMessage.success('删除成功')
      loadAdjusts()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

/* 取消申请 */
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确认取消该调整申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await $API.cancelPlanAdjust(row.adjustId)
    if (res.data.code === '200') {
      ElMessage.success('取消成功')
      loadAdjusts()
    } else {
      ElMessage.error(res.data.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消失败')
    }
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
          applicant: addForm.value.applicant,
          phone: addForm.value.phone,
          planId: addForm.value.planId,
          landId: addForm.value.landId,
          cropId: addForm.value.cropId,
          expectedOutput: addForm.value.expectedOutput,
          startTime: formatDate(addForm.value.startTime),
          endTime: formatDate(addForm.value.endTime),
          reason: addForm.value.reason,
          applyTime: formatDate(new Date()),
          status: 0,
        }
        console.log(addData)
        const res = await $API.addPlanAdjust(addData)
        if (res.data.code === '200') {
          ElMessage.success('申请提交成功')
          addDialogVisible.value = false
          loadAdjusts()
        } else {
          ElMessage.error(res.data.message || '申请失败')
        }
      } catch (e) {
        console.error('申请失败:', e)
        ElMessage.error('申请失败')
      }
    }
  })
}

onMounted(() => {
  loadAdjusts()
})
</script>

<style scoped>
.adjust-management {
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
