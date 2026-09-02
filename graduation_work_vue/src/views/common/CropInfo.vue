<template>
  <div class="crop-management">
    <div class="header">
      <h1>{{ roleTitle }}-农作物管理</h1>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加农作物
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="输入农作物名称/类型"
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
        :data="filteredCrops"
        style="width: 100%"
        border
        stripe
        v-loading="loading"
        height="100%"
      >
        <el-table-column prop="cropId" label="农作物ID" width="120" align="center" />
        <el-table-column prop="cropName" label="农作物名称" width="150" align="center" />
        <el-table-column prop="cropType" label="农作物类型" width="120" align="center" />
        <el-table-column prop="growthCycle" label="生长周期(天)" width="120" align="center" />
        <el-table-column prop="suitableTemperature" label="适应温度" width="150" align="center" />
        <el-table-column prop="suitableHumidity" label="适应湿度" width="150" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" align="center" />
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
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

    <!-- 添加/编辑农作物对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="农作物ID" v-if="formData.cropId">
          <el-input v-model="formData.cropId" disabled />
        </el-form-item>
        <el-form-item label="农作物名称" prop="cropName">
          <el-input v-model="formData.cropName" placeholder="请输入农作物名称" />
        </el-form-item>
        <el-form-item label="农作物类型" prop="cropType">
          <el-input v-model="formData.cropType" placeholder="请输入农作物类型" />
        </el-form-item>
        <el-form-item label="生长周期(天)" prop="growthCycle">
          <el-input v-model="formData.growthCycle" type="number" placeholder="请输入生长周期" />
        </el-form-item>
        <el-form-item label="适应温度" prop="suitableTemperature">
          <el-input v-model="formData.suitableTemperature" placeholder="请输入适应温度" />
        </el-form-item>
        <el-form-item label="适应湿度" prop="suitableHumidity">
          <el-input v-model="formData.suitableHumidity" placeholder="请输入适应湿度" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCrop">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="300px">
      <p>确定要删除农作物 {{ deleteCropInfo.cropName }} 吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteCrop">删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
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

// 格式化时间
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

// 农作物数据
const cropList = ref([])
const total = ref(0)

// 搜索和分页
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框状态
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const dialogTitle = ref('添加农作物')

// 表单引用
const formRef = ref(null)

// 表单验证规则
const formRules = {
  cropName: [{ required: true, message: '请输入农作物名称', trigger: 'blur' }],
  cropType: [{ required: true, message: '请输入农作物类型', trigger: 'blur' }],
  growthCycle: [{ required: true, message: '请输入生长周期', trigger: 'blur' }],
  suitableTemperature: [{ required: true, message: '请输入适应温度', trigger: 'blur' }],
  suitableHumidity: [{ required: true, message: '请输入适应湿度', trigger: 'blur' }],
}

// 表单数据
const formData = ref({
  cropId: '',
  cropName: '',
  cropType: '',
  growthCycle: '',
  suitableTemperature: '',
  suitableHumidity: '',
  description: '',
  createTime: '',
})

// 待删除农作物
const deleteCropInfo = ref({})

// 加载状态
const loading = ref(false)

// 过滤后的农作物列表
const filteredCrops = computed(() => {
  return cropList.value
})

// 加载农作物列表
const loadCrops = async () => {
  loading.value = true
  try {
    let response
    if (searchQuery.value) {
      response = await $API.searchCropsByPage(searchQuery.value, currentPage.value, pageSize.value)
    } else {
      response = await $API.getCropsByPage(currentPage.value, pageSize.value)
    }
    if (response.data && response.data.code === '200') {
      cropList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('获取农作物列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加农作物'
  formData.value = {
    cropId: '',
    cropName: '',
    cropType: '',
    growthCycle: '',
    suitableTemperature: '',
    suitableHumidity: '',
    description: '',
    createTime: '',
  }
  dialogVisible.value = true
  // 清除表单验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 打开编辑对话框
const openEditDialog = (crop) => {
  dialogTitle.value = '编辑农作物'
  formData.value = {
    ...crop,
  }
  dialogVisible.value = true
  // 清除表单验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 确认删除
const confirmDelete = (crop) => {
  deleteCropInfo.value = crop
  deleteDialogVisible.value = true
}

// 保存农作物
const saveCrop = async () => {
  // 验证表单
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const cropData = {
          ...formData.value,
        }
        if (formData.value.cropId) {
          // 编辑现有农作物
          await $API.updateCrop(cropData)
          ElMessage({
            message: '编辑农作物成功',
            type: 'success',
          })
        } else {
          // 添加新农作物
          // 去掉createTime和cropId
          delete cropData.createTime
          delete cropData.cropId
          await $API.addCrop(cropData)
          ElMessage({
            message: '添加农作物成功',
            type: 'success',
          })
        }

        // 重新加载农作物列表
        await loadCrops()
        dialogVisible.value = false
      } catch (error) {
        console.error('保存农作物失败:', error)
        ElMessage({
          message: '保存农作物失败',
          type: 'error',
        })
      }
    }
  })
}

// 删除农作物
const deleteCrop = async () => {
  try {
    // 调用后端API删除农作物
    await $API.deleteCrop(deleteCropInfo.value.cropId)
    // 重新加载农作物列表
    await loadCrops()
    deleteDialogVisible.value = false
    ElMessage({
      message: '删除农作物成功',
      type: 'success',
    })
  } catch (error) {
    console.error('删除农作物失败:', error)
    ElMessage({
      message: '删除农作物失败',
      type: 'error',
    })
  }
}

// 搜索处理
const handleSearch = async () => {
  console.log('搜索关键词:', searchQuery.value)
  currentPage.value = 1
  await loadCrops()
}

// 重置搜索
const handleReset = async () => {
  searchQuery.value = ''
  currentPage.value = 1
  await loadCrops()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadCrops()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadCrops()
}

// 初始化加载
onMounted(() => {
  console.log('组件挂载，开始加载农作物数据')
  loadCrops()
})
</script>

<style scoped>
.crop-management {
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

.search-icon {
  color: #909399;
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
