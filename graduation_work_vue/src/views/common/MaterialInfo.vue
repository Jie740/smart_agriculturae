<template>
  <div class="equipment-management">
    <div class="header">
      <h1>{{ roleTitle }}-农资管理</h1>
      <div class="header-buttons">
        <el-button type="success" @click="openTypeDialog">
          <el-icon><Setting /></el-icon>
          类型管理
        </el-button>
        <el-button type="warning" @click="openRecordDialog">
          <el-icon><List /></el-icon>
          出入库记录
        </el-button>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          入库农资
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="输入农资名"
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
      <el-select
        v-model="selectedTypeId"
        placeholder="选择农资类型"
        style="width: 150px; margin-left: 15px"
        clearable
        @change="handleSearch"
        class="type-select"
      >
        <el-option
          v-for="item in typeList"
          :key="item.typeId"
          :label="item.typeName"
          :value="item.typeId"
        />
      </el-select>
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
        <el-table-column prop="materialId" label="农资ID" width="120" align="center" />
        <el-table-column prop="materialName" label="农资名" width="150" align="center" />
        <el-table-column prop="typeName" label="农资类型" width="150" align="center" />
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑农资对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="农资ID" v-if="formData.materialId">
          <el-input v-model="formData.materialId" disabled />
        </el-form-item>
        <el-form-item label="农资名" prop="materialName">
          <el-input v-model="formData.materialName" placeholder="请输入农资名" />
        </el-form-item>
        <el-form-item label="农资类型" prop="typeId">
          <el-select v-model="formData.typeId" placeholder="请选择农资类型" style="width: 100%">
            <el-option
              v-for="item in typeList"
              :key="item.typeId"
              :label="item.typeName"
              :value="item.typeId"
            />
          </el-select>
        </el-form-item>
        <!-- 数量必须为非负整数 -->
        <el-form-item label="数量" prop="stock">
          <el-input type="number" min="0" v-model="formData.stock" placeholder="请输入数量" />
        </el-form-item>
      </el-form>
      <!-- 预警库存默认为0 -->
      <el-form-item v-if="formData.materialId" label="预警库存" prop="warningStock">
        <el-input
          type="number"
          min="0"
          v-model="formData.warningStock"
          placeholder="请输入预警库存"
        />
      </el-form-item>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveMaterial">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="300px">
      <p>确定要删除设备 {{ deleteMaterialInfo.materialName }} 吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteMaterial">删除</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 类型管理对话框 -->
    <el-dialog v-model="typeDialogVisible" title="农资类型管理" width="600px">
      <div class="type-header">
        <el-input
          v-model="newTypeName"
          placeholder="输入新类型名称"
          style="width: 250px"
          clearable
        />
        <el-button type="primary" @click="addType">添加类型</el-button>
      </div>
      <el-table :data="typeList" border stripe style="margin-top: 16px" max-height="300">
        <el-table-column prop="typeId" label="类型ID" width="100" align="center" />
        <el-table-column prop="typeName" label="类型名称" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editType(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteType(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 编辑类型对话框 -->
    <el-dialog v-model="editTypeDialogVisible" title="编辑类型" width="400px">
      <el-form label-width="100px">
        <el-form-item label="类型ID">
          <el-input v-model="editTypeData.typeId" disabled />
        </el-form-item>
        <el-form-item label="类型名称">
          <el-input v-model="editTypeData.typeName" placeholder="请输入类型名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editTypeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditType">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 出入库记录对话框 -->
    <el-dialog v-model="recordDialogVisible" title="出入库记录" width="900px">
      <div class="record-search-bar">
        <el-input
          v-model="recordKeyword"
          placeholder="输入农资名搜索"
          style="width: 250px"
          clearable
        />
        <el-button type="primary" @click="loadRecords">查询</el-button>
        <el-button type="warning" @click="resetRecords">重置</el-button>
        <el-button type="success" @click="openAddRecordDialog('in')">
          <el-icon><Plus /></el-icon>
          入库
        </el-button>
        <el-button type="danger" @click="openAddRecordDialog('out')">
          <el-icon><Minus /></el-icon>
          出库
        </el-button>
      </div>
      <el-table :data="recordList" border stripe style="margin-top: 16px" max-height="400">
        <el-table-column prop="stockRecordId" label="记录ID" width="100" align="center" />
        <el-table-column prop="materialName" label="农资名称" width="200" align="center" />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.recordType === 1 ? 'warning' : 'success'">
              {{ scope.row.recordType === 1 ? '出库' : '入库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" align="center" />
        <el-table-column prop="createTime" label="操作时间" width="180" align="center" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editRecord(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteRecord(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="record-pagination">
        <el-pagination
          v-model:current-page="recordCurrentPage"
          v-model:page-size="recordPageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="recordTotal"
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </div>
    </el-dialog>

    <!-- 添加/编辑记录对话框 -->
    <el-dialog v-model="addRecordDialogVisible" :title="recordDialogTitle" width="500px">
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordFormRules" label-width="100px">
        <el-form-item label="农资" prop="materialId">
          <el-select v-model="recordForm.materialId" placeholder="请选择农资" style="width: 100%">
            <el-option
              v-for="item in materialList"
              :key="item.materialId"
              :label="item.materialName"
              :value="item.materialId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="recordType">
          <el-select v-model="recordForm.recordType" placeholder="请选择类型" style="width: 100%">
            <el-option label="入库" :value="1" />
            <el-option label="出库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input type="number" min="1" v-model="recordForm.quantity" placeholder="请输入数量" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addRecordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveRecord">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Plus, Edit, Delete, Search, Setting, List, Minus } from '@element-plus/icons-vue'
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

// 设备数据
const materialList = ref([])
const total = ref(0)
const typeList = ref([])

// 搜索和分页
const searchQuery = ref('')
const selectedTypeId = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框状态
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const typeDialogVisible = ref(false)
const editTypeDialogVisible = ref(false)
const dialogTitle = ref('添加农资')

// 表单引用
const formRef = ref(null)

// 表单验证规则
const formRules = {
  materialName: [{ required: true, message: '请输入农资名', trigger: 'blur' }],
  typeId: [{ required: true, message: '请选择农资类型', trigger: 'change' }],
}

// 表单数据
const formData = ref({
  materialId: '',
  materialName: '',
  typeId: '',
  stock: '0',
})

// 待删除设备
const deleteMaterialInfo = ref({})

// 新类型名称
const newTypeName = ref('')

// 编辑类型数据
const editTypeData = ref({
  typeId: '',
  typeName: '',
})

// 加载状态
const loading = ref(false)

// 加载设备列表
const loadMaterials = async () => {
  loading.value = true
  try {
    let response = await $API.searchMaterialsByPage(
      selectedTypeId.value,
      searchQuery.value,
      currentPage.value,
      pageSize.value,
    )

    if (response.data && response.data.code === '200') {
      materialList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取农资列表失败')
  } finally {
    loading.value = false
  }
}

// 加载类型列表
const loadTypes = async () => {
  try {
    const res = await $API.getMaterialTypes()
    if (res.data.code === '200') {
      typeList.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取类型列表失败:', error)
    ElMessage.error('获取类型列表失败')
  }
}

// 打开类型管理对话框
const openTypeDialog = () => {
  loadTypes()
  typeDialogVisible.value = true
}

// 添加类型
const addType = async () => {
  if (!newTypeName.value.trim()) {
    ElMessage.warning('请输入类型名称')
    return
  }
  try {
    const typeName = newTypeName.value.trim()
    const res = await $API.addMaterialType(typeName)
    if (res.data.code === '200') {
      ElMessage.success('添加成功')
      newTypeName.value = ''
      loadTypes()
    } else {
      ElMessage.error(res.data.message || '添加失败')
    }
  } catch (error) {
    console.error('添加类型失败:', error)
    ElMessage.error('添加类型失败')
  }
}

// 编辑类型
const editType = (row) => {
  editTypeData.value = { ...row }
  editTypeDialogVisible.value = true
}

// 保存编辑类型
const saveEditType = async () => {
  if (!editTypeData.value.typeName.trim()) {
    ElMessage.warning('请输入类型名称')
    return
  }
  try {
    const res = await $API.updateMaterialType(editTypeData.value)
    if (res.data.code === '200') {
      ElMessage.success('修改成功')
      editTypeDialogVisible.value = false
      loadTypes()
    } else {
      ElMessage.error(res.data.message || '修改失败')
    }
  } catch (error) {
    console.error('修改类型失败:', error)
    ElMessage.error('修改类型失败')
  }
}

// 删除类型
const deleteType = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该类型吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await $API.deleteMaterialType(row.typeId)
    if (res.data.code === '200') {
      ElMessage.success('删除成功')
      loadTypes()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除类型失败:', error)
      ElMessage.error('删除类型失败')
    }
  }
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加设备'
  formData.value = {
    materialId: '',
    materialName: '',
    typeId: '',
    stock: '0',
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 打开编辑对话框
const openEditDialog = (row) => {
  dialogTitle.value = '编辑农资'
  const typeId = getTypeIdByName(row.typeName)
  formData.value = {
    materialId: row.materialId,
    materialName: row.materialName,
    typeId: typeId,
    stock: row.stock?.toString() || '0',
    warningStock: row.warningStock?.toString() || '0',
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 根据类型名称获取类型ID
const getTypeIdByName = (typeName) => {
  const type = typeList.value.find((item) => item.typeName === typeName)
  return type ? type.typeId : ''
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
          ...formData.value,
        }
        if (formData.value.materialId) {
          await $API.updateMaterial(materialData)
          ElMessage.success('编辑成功')
        } else {
          await $API.addMaterial(materialData)
          ElMessage.success('添加成功')
        }
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
    await $API.deleteMaterial(deleteMaterialInfo.value.materialId)
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
  selectedTypeId.value = ''
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
  loadTypes()
})

// 出入库记录相关
const recordDialogVisible = ref(false)
const addRecordDialogVisible = ref(false)
const recordFormRef = ref(null)
const recordList = ref([])
const recordTotal = ref(0)
const recordCurrentPage = ref(1)
const recordPageSize = ref(10)
const recordDialogTitle = ref('添加记录')

const recordForm = ref({
  stockRecordId: '',
  materialId: '',
  recordType: '',
  quantity: '',
})

const recordFormRules = {
  materialId: [{ required: true, message: '请选择农资', trigger: 'change' }],
  recordType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

const recordKeyword = ref('')

const openRecordDialog = () => {
  recordDialogVisible.value = true
  recordKeyword.value = ''
  loadRecords()
}

const loadRecords = async () => {
  try {
    const res = await $API.getMaterialStockRecordPage(
      recordKeyword.value,
      recordCurrentPage.value,
      recordPageSize.value,
    )
    if (res.data.code === '200') {
      recordList.value = res.data.data.records || []
      recordTotal.value = res.data.data.total || 0
    }
  } catch (error) {
    console.error('获取记录失败:', error)
    ElMessage.error('获取记录失败')
  }
}

const resetRecords = () => {
  recordKeyword.value = ''
  recordCurrentPage.value = 1
  loadRecords()
}

const openAddRecordDialog = (type) => {
  recordDialogTitle.value = type === 'in' ? '添加入库记录' : '添加出库记录'
  recordForm.value = {
    stockRecordId: '',
    materialId: '',
    recordType: type === 'in' ? 1 : 2,
    quantity: '',
  }
  addRecordDialogVisible.value = true
  nextTick(() => {
    recordFormRef.value?.clearValidate()
  })
}

const editRecord = (row) => {
  recordDialogTitle.value = '编辑记录'
  recordForm.value = {
    stockRecordId: row.stockRecordId,
    materialId: row.materialId,
    recordType: row.recordType,
    quantity: row.quantity,
  }
  addRecordDialogVisible.value = true
  nextTick(() => {
    recordFormRef.value?.clearValidate()
  })
}

const saveRecord = async () => {
  if (!recordFormRef.value) return
  await recordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (recordForm.value.stockRecordId) {
          await $API.updateMaterialStockRecord(recordForm.value)
          ElMessage.success('编辑成功')
        } else {
          await $API.addMaterialStockRecord(recordForm.value)
          ElMessage.success('添加成功')
        }
        await loadRecords()
        addRecordDialogVisible.value = false
      } catch (error) {
        console.error('保存记录失败:', error)
        ElMessage.error('保存记录失败')
      }
    }
  })
}

const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await $API.deleteMaterialStockRecord(row.stockRecordId)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除记录失败:', error)
      ElMessage.error('删除记录失败')
    }
  }
}
</script>

<style scoped>
.equipment-management {
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

.type-header {
  display: flex;
  gap: 10px;
  align-items: center;
}

.annotation {
  margin-left: 20px;
  color: #909399;
  font-size: 12px;
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
