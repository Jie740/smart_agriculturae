<template>
  <div class="land-management">
    <div class="header">
      <h1>{{ roleTitle }}-地块分配记录</h1>
      <el-button type="primary" @click="openAssignDialog">
        <el-icon><Plus /></el-icon>
        分配地块
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="输入地块名称/承包人姓名"
        style="width: 300px"
        clearable
        @clear="handleReset"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch"> 查询 </el-button>
      <el-button type="warning" @click="handleReset"> 重置 </el-button>
    </div>

    <div class="table-container">
      <el-table
        :data="landList"
        style="width: 100%"
        border
        stripe
        v-loading="loading"
        height="100%"
      >
        <el-table-column prop="landName" label="地块名称" min-width="150" align="center" />
        <el-table-column prop="area" label="面积" min-width="120" align="center" />
        <el-table-column prop="contractorName" label="承包人姓名" min-width="150" align="center" />
        <el-table-column prop="phone" label="手机号" min-width="130" align="center" />
        <el-table-column label="开始日期" min-width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column label="结束日期" min-width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-count="Math.ceil(total / pageSize)"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑地块分配信息" width="500px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="分配ID">
          <el-input v-model="editForm.allocationId" disabled />
        </el-form-item>
        <el-form-item label="地块名称">
          <el-input v-model="editForm.landName" disabled />
        </el-form-item>
        <el-form-item label="承包人姓名" prop="contractorName">
          <el-input v-model="editForm.contractorName" placeholder="请输入承包人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="editForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="editForm.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配地块对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配地块" width="500px">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="选择地块" prop="landId">
          <el-select
            v-model="assignForm.landId"
            placeholder="请选择地块"
            style="width: 100%"
            @change="handleLandChange"
          >
            <el-option
              v-for="land in landOptions"
              :key="land.landId"
              :label="land.landName"
              :value="land.landId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="assignForm.location" disabled placeholder="选择地块后自动显示" />
        </el-form-item>
        <el-form-item label="承包人姓名" prop="contractorName">
          <el-input v-model="assignForm.contractorName" placeholder="请输入承包人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="assignForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="assignForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="assignForm.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Search, Edit, Delete, Plus } from '@element-plus/icons-vue'
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

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

const formatDateTime = (date) => {
  if (!date) return ''
  // 解决时区问题：将日期加1天
  const d = new Date(date)
  d.setDate(d.getDate() + 1)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day} 00:00:00`
}

const landList = ref([])
const total = ref(0)

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const loading = ref(false)

const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = ref({
  allocationId: '',
  landName: '',
  contractorName: '',
  phone: '',
  startDate: null,
  endDate: null,
})

const editRules = {
  contractorName: [{ required: true, message: '请输入承包人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
}

// 分配地块相关
const assignDialogVisible = ref(false)
const assignFormRef = ref(null)
const assignForm = ref({
  landId: '',
  location: '',
  contractorName: '',
  phone: '',
  startDate: null,
  endDate: null,
})
const landOptions = ref([])

const assignRules = {
  landId: [{ required: true, message: '请选择地块', trigger: 'change' }],
  contractorName: [{ required: true, message: '请输入承包人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
}

// 打开分配地块对话框
const openAssignDialog = async () => {
  // 重置表单
  assignForm.value = {
    landId: '',
    location: '',
    contractorName: '',
    phone: '',
    startDate: null,
    endDate: null,
  }
  landOptions.value = []
  // 加载地块列表
  await loadLandOptions()
  assignDialogVisible.value = true
  nextTick(() => assignFormRef.value?.clearValidate())
}

// 加载地块选项
const loadLandOptions = async () => {
  try {
    // 获取所有地块，不分页
    const response = await $API.getAllLands()
    if (response.data && response.data.code === '200') {
      landOptions.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取地块列表失败:', error)
    ElMessage.error('获取地块列表失败')
  }
}

// 地块选择变化时查询位置
const handleLandChange = (landId) => {
  const selectedLand = landOptions.value.find((land) => land.landId === landId)
  if (selectedLand) {
    assignForm.value.location = selectedLand.location || ''
  } else {
    assignForm.value.location = ''
  }
}

// 保存分配
const saveAssign = async () => {
  if (!assignFormRef.value) return
  await assignFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      // 2. 分配地块
      const allocationData = {
        landId: assignForm.value.landId,
        contractorName: assignForm.value.contractorName,
        phone: assignForm.value.phone,
        startDate: formatDateTime(assignForm.value.startDate),
        endDate: formatDateTime(assignForm.value.endDate),
      }
      const res = await $API.assignLand(allocationData)
      if (res.data.code === '200') {
        ElMessage.success('地块分配成功')
        assignDialogVisible.value = false
        await loadLands()
      } else {
        ElMessage.error(res.data.message || '分配失败')
      }
    } catch (error) {
      console.error('分配失败:', error)
      ElMessage.error('分配失败')
    }
  })
}

const loadLands = async () => {
  loading.value = true
  try {
    let response
    if (searchQuery.value) {
      response = await $API.searchLandAllocations(
        searchQuery.value,
        currentPage.value,
        pageSize.value,
      )
    } else {
      response = await $API.getLandAllocations(currentPage.value, pageSize.value)
    }
    if (response.data && response.data.code === '200') {
      landList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('获取地块分配列表失败:', error)
    ElMessage.error('获取地块分配列表失败')
  } finally {
    loading.value = false
  }
}

const openEditDialog = (row) => {
  // 处理日期，确保正确显示
  let startDate = null
  let endDate = null

  if (row.startDate) {
    // 直接使用日期字符串创建日期对象
    // 对于 "2026-03-31 00:00:00" 这样的格式，会正确解析
    startDate = new Date(row.startDate)
  }

  if (row.endDate) {
    endDate = new Date(row.endDate)
  }

  editForm.value = {
    allocationId: row.allocationId || '',
    landName: row.landName || '',
    contractorName: row.contractorName || '',
    phone: row.phone || '',
    startDate: startDate,
    endDate: endDate,
  }
  editDialogVisible.value = true
  nextTick(() => editFormRef.value?.clearValidate())
}

const saveEdit = async () => {
  if (!editFormRef.value) return
  if (!editForm.value.allocationId) {
    ElMessage.error('修改失败：分配ID不存在')
    return
  }
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const data = {
        allocationId: editForm.value.allocationId,
        contractorName: editForm.value.contractorName,
        phone: editForm.value.phone,
        startDate: editForm.value.startDate ? formatDateTime(editForm.value.startDate) : '',
        endDate: editForm.value.endDate ? formatDateTime(editForm.value.endDate) : '',
      }
      const res = await $API.updateLandAllocation(data)
      if (res.data.code === '200') {
        ElMessage.success('修改成功')
        editDialogVisible.value = false
        await loadLands()
      } else {
        ElMessage.error(res.data.message || '修改失败')
      }
    } catch (error) {
      console.error('修改失败:', error)
      ElMessage.error('修改失败')
    }
  })
}

const handleDelete = async (row) => {
  if (!row.allocationId) {
    ElMessage.error('删除失败：分配ID不存在')
    return
  }
  try {
    await ElMessageBox.confirm('确认删除该地块分配记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await $API.deleteLandAllocation(row.allocationId)
    if (res.data.code === '200') {
      ElMessage.success('删除成功')
      await loadLands()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadLands()
}

const handleReset = async () => {
  searchQuery.value = ''
  currentPage.value = 1
  await loadLands()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadLands()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadLands()
}

onMounted(() => {
  loadLands()
})
</script>

<style scoped>
.land-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 20px;
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

.el-table {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-container {
  height: calc(100vh - 280px);
  overflow: auto;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
  padding: 10px;
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

.table-container {
  scrollbar-width: thin;
  scrollbar-color: #764ba2 #f1f1f1;
}
</style>
