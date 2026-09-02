<template>
  <div class="land-management">
    <div class="header">
      <h1>{{ roleTitle }}-地块管理</h1>
      <el-button v-if="userRole !== 'user'" type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加地块
      </el-button>
    </div>

    <div class="search-bar" v-if="userRole !== 'user'">
      <el-input
        v-model="searchQuery"
        placeholder="输入地块名称/位置"
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
        :data="filteredLands"
        style="width: 100%"
        border
        stripe
        v-loading="loading"
        height="100%"
      >
        <el-table-column prop="landId" label="地块ID" width="120" align="center" />
        <el-table-column prop="landName" label="地块名称" width="150" align="center" />
        <el-table-column prop="location" label="位置" width="200" align="center" />
        <el-table-column prop="area" label="面积" width="120" align="center" />
        <el-table-column prop="soilType" label="土壤类型" width="120" align="center" />
        <el-table-column v-if="userRole === 'user'" label="开始日期" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column v-if="userRole === 'user'" label="结束日期" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column v-if="userRole !== 'user'" label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column
          v-if="userRole !== 'user'"
          label="操作"
          width="260"
          fixed="right"
          align="center"
        >
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="confirmDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="userRole !== 'user'">
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

    <!-- 添加/编辑地块对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="地块ID" v-if="formData.landId">
          <el-input v-model="formData.landId" disabled />
        </el-form-item>
        <el-form-item label="地块名称" prop="landName">
          <el-input v-model="formData.landName" placeholder="请输入地块名称" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="面积" prop="area">
          <el-input v-model="formData.area" type="number" placeholder="请输入面积" />
        </el-form-item>
        <el-form-item label="土壤类型" prop="soilType">
          <el-input v-model="formData.soilType" placeholder="请输入土壤类型" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveLand">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="300px">
      <p>确定要删除地块 {{ deleteLandInfo.landName || deleteLandInfo.land_name }} 吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteLand">删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Plus, Edit, Delete, Search, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
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

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  d.setDate(d.getDate() + 1)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = '00'
  const minutes = '00'
  const seconds = '00'
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const landList = ref([])
const total = ref(0)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const dialogTitle = ref('添加地块')
const formRef = ref(null)

const formRules = {
  landName: [{ required: true, message: '请输入地块名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  area: [{ required: true, message: '请输入面积', trigger: 'blur' }],
  soilType: [{ required: true, message: '请输入土壤类型', trigger: 'blur' }],
}

const formData = ref({
  landId: '',
  landName: '',
  location: '',
  area: '',
  soilType: '',
  createTime: '',
})

const deleteLandInfo = ref({})
const loading = ref(false)

const filteredLands = computed(() => {
  return landList.value
})

const loadMyLands = async () => {
  loading.value = true
  try {
    const response = await $API.getMyLands()
    if (response.data && response.data.code === '200') {
      landList.value = response.data.data || []
      total.value = response.data.data.length || 0
    }
  } catch (error) {
    console.error('获取我的地块失败:', error)
  } finally {
    loading.value = false
  }
}

const loadLands = async () => {
  loading.value = true
  try {
    let response
    if (searchQuery.value) {
      response = await $API.searchLandsByPage(searchQuery.value, currentPage.value, pageSize.value)
    } else {
      response = await $API.getLandsByPage(currentPage.value, pageSize.value)
    }
    if (response.data && response.data.code === '200') {
      landList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('获取地块列表失败:', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  dialogTitle.value = '添加地块'
  formData.value = {
    landId: '',
    landName: '',
    location: '',
    area: '',
    soilType: '',
    createTime: '',
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const openEditDialog = (land) => {
  dialogTitle.value = '编辑地块'
  formData.value = {
    landId: land.land_id || land.landId,
    landName: land.land_name || land.landName,
    location: land.location,
    area: land.area,
    soilType: land.soil_type || land.soilType,
    createTime: land.create_time || land.createTime,
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const confirmDelete = (land) => {
  deleteLandInfo.value = land
  deleteDialogVisible.value = true
}

const saveLand = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const landData = { ...formData.value }
        if (formData.value.landId) {
          await $API.updateLand(landData)
          ElMessage({ message: '编辑地块成功', type: 'success' })
        } else {
          delete landData.createTime
          delete landData.landId
          await $API.addLand(landData)
          ElMessage({ message: '添加地块成功', type: 'success' })
        }
        await loadLands()
        dialogVisible.value = false
      } catch (error) {
        console.error('保存地块失败:', error)
        ElMessage({ message: '保存地块失败', type: 'error' })
      }
    }
  })
}

const deleteLand = async () => {
  try {
    await $API.deleteLand(deleteLandInfo.value.landId || deleteLandInfo.value.land_id)
    await loadLands()
    deleteDialogVisible.value = false
    ElMessage({ message: '删除地块成功', type: 'success' })
  } catch (error) {
    console.error('删除地块失败:', error)
    ElMessage({ message: '删除地块失败', type: 'error' })
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
  if (userRole.value === 'user') {
    loadMyLands()
  } else {
    loadLands()
  }
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.el-table {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-button {
  margin-right: 8px;
}

.table-container {
  height: calc(100vh - 300px);
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
