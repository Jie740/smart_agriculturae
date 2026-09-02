<template>
  <div class="material-management">
    <div class="header">
      <h1>{{ roleTitle }}-农资管理</h1>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        :placeholder="role === 'user' ? '输入农资名' : '输入承包人姓名'"
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

    <div class="table-container">
      <el-table
        :data="materialList"
        style="width: 100%"
        border
        stripe
        v-loading="loading"
        height="100%"
      >
        <el-table-column
          prop="contractorMaterialId"
          label="承包人农资ID"
          width="180"
          align="center"
        />
        <el-table-column prop="materialName" label="农资名" width="150" align="center" />
        <el-table-column prop="type" label="农资类型" width="150" align="center" />
        <el-table-column prop="contractorName" label="承包人" width="150" align="center" />
        <el-table-column prop="phone" label="手机号" width="130" align="center" />
        <el-table-column prop="stock" label="库存" width="120" align="center">
          <template #default="scope">
            <span
              v-if="
                scope.row.stock <= scope.row.warningStock + 5 &&
                scope.row.stock > scope.row.warningStock
              "
              style="color: #e6a23c; font-weight: bold"
            >
              {{ scope.row.stock }} ⚠
            </span>
            <span
              v-else-if="scope.row.stock <= scope.row.warningStock"
              style="color: #f56c6c; font-weight: bold"
            >
              {{ scope.row.stock }} 🔴
            </span>
            <span v-else>{{ scope.row.stock }}</span>
          </template>
        </el-table-column>
        <!-- 预警库存默认为0 -->
        <el-table-column prop="warningStock" label="预警库存" width="120" align="center" />

        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              v-if="role !== 'user'"
              type="danger"
              size="small"
              @click="confirmDelete(scope.row)"
            >
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 编辑农资对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="承包人农资ID">
          <el-input v-model="formData.contractorMaterialId" disabled />
        </el-form-item>
        <el-form-item label="农资名">
          <el-input v-model="formData.materialName" disabled />
        </el-form-item>
        <el-form-item label="农资类型">
          <el-input v-model="formData.type" disabled />
        </el-form-item>
        <el-form-item label="承包人">
          <el-input v-model="formData.contractorName" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" disabled />
        </el-form-item>
        <!-- 数量必须为非负整数 -->
        <el-form-item label="数量" prop="stock">
          <el-input
            type="number"
            min="0"
            v-model="formData.stock"
            placeholder="请输入数量"
            :disabled="role === 'user'"
          />
        </el-form-item>
        <!-- 预警库存默认为0 -->
        <el-form-item label="预警库存" prop="warningStock">
          <el-input
            type="number"
            min="0"
            v-model="formData.warningStock"
            placeholder="请输入预警库存"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveMaterial">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="300px">
      <p>确定要删除农资 {{ deleteMaterialInfo.materialName }} 吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteMaterial">删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Edit, Delete, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import $API from '@/api'

// 设备数据
const materialList = ref([])
const total = ref(0)

// 搜索和分页
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框状态
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const dialogTitle = ref('编辑农资')

// 表单引用
const formRef = ref(null)

// 表单验证规则
const formRules = {
  stock: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

// 表单数据
const formData = ref({
  contractorMaterialId: '',
  materialName: '',
  type: '',
  contractorName: '',
  phone: '',
  stock: '0',
  warningStock: '0',
})

// 待删除设备
const deleteMaterialInfo = ref({})

// 加载状态
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
// 加载设备列表
const loadMaterials = async () => {
  loading.value = true
  try {
    let response
    if (role.value === 'user') {
      response = await $API.getMyContractorMaterialsByPage(
        searchQuery.value,
        currentPage.value,
        pageSize.value,
      )
    } else {
      if (searchQuery.value) {
        response = await $API.searchContractorMaterialsByPage(
          searchQuery.value,
          currentPage.value,
          pageSize.value,
        )
      } else {
        response = await $API.getContractorMaterialsByPage(currentPage.value, pageSize.value)
      }
    }

    if (response.data && response.data.code === '200') {
      materialList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    }
    if (response.data && response.data.code === '500') {
      materialList.value = []
      total.value = 0
      ElMessage.error(response.data.message || '获取农资列表失败')
    }
  } catch (error) {
    ElMessage.error('获取农资列表失败')
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框
const openEditDialog = (row) => {
  dialogTitle.value = '编辑农资'
  formData.value = {
    contractorMaterialId: row.contractorMaterialId,
    materialName: row.materialName,
    type: row.type,
    contractorName: row.contractorName,
    phone: row.phone,
    stock: row.stock?.toString() || '0',
    warningStock: row.warningStock?.toString() || '0',
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 确认删除
const confirmDelete = (row) => {
  deleteMaterialInfo.value = row
  deleteDialogVisible.value = true
}

// 保存设备
const saveMaterial = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const materialData = {
          contractorMaterialId: formData.value.contractorMaterialId,
          stock: parseInt(formData.value.stock),
          warningStock: parseInt(formData.value.warningStock),
        }
        await $API.updateContractorMaterialStock(materialData)
        ElMessage.success('编辑成功')
        await loadMaterials()
        dialogVisible.value = false
      } catch (error) {
        console.error('保存设备失败:', error)
        ElMessage.error('保存设备失败')
      }
    }
  })
}

// 删除设备
const deleteMaterial = async () => {
  try {
    await $API.deleteContractorMaterialStock(deleteMaterialInfo.value.contractorMaterialId)
    await loadMaterials()
    deleteDialogVisible.value = false
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除农资失败')
  }
}

// 搜索处理
const handleSearch = async () => {
  currentPage.value = 1
  await loadMaterials()
}

// 重置搜索
const handleReset = async () => {
  searchQuery.value = ''
  currentPage.value = 1
  await loadMaterials()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadMaterials()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadMaterials()
}

// 初始化加载
onMounted(() => {
  loadMaterials()
})
</script>

<style scoped>
.material-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
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

.header-buttons {
  display: flex;
  gap: 10px;
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

/* 状态样式 */
.stock-normal {
  color: #67c23a;
  font-weight: 600;
}

.stock-borrowed {
  color: #409eff;
  font-weight: 600;
}

.stock-repairing {
  color: #e6a23c;
  font-weight: 600;
}

.stock-damaged {
  color: #f56c6c;
  font-weight: 600;
}

.type-select {
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.type-select:hover {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.type-select:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}
</style>
