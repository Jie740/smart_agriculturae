<template>
  <div class="equipment-management">
    <div class="header">
      <h1>{{ roleTitle }}-设备管理</h1>
      <div class="header-buttons">
        <el-button type="success" @click="openTypeDialog">
          <el-icon><Setting /></el-icon>
          类型管理
        </el-button>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加设备
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="输入设备名"
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
        :data="equipmentList"
        style="width: 100%"
        border
        stripe
        v-loading="loading"
        height="100%"
      >
        <el-table-column prop="equipmentId" label="设备ID" width="120" align="center" />
        <el-table-column prop="equipmentName" label="设备名" width="150" align="center" />
        <el-table-column prop="equipmentTypeName" label="设备类型" width="150" align="center">
          <template #default="scope">
            <span :class="scope.row.equipmentTypeName ? '' : 'empty-type'">
              {{ scope.row.equipmentTypeName || '类型未知' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <span :class="statusClass(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
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

    <!-- 添加/编辑设备对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="设备ID" v-if="formData.equipmentId">
          <el-input v-model="formData.equipmentId" disabled />
        </el-form-item>
        <el-form-item label="设备名" prop="equipmentName">
          <el-input v-model="formData.equipmentName" placeholder="请输入设备名" />
        </el-form-item>
        <el-form-item label="设备类型" prop="equipmentTypeId">
          <el-select
            v-model="formData.equipmentTypeId"
            placeholder="请选择设备类型"
            style="width: 100%"
          >
            <el-option
              v-for="item in typeList"
              :key="item.equipmentTypeId"
              :label="item.equipmentTypeName"
              :value="item.equipmentTypeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常闲置" value="0" />
            <el-option label="已被借用" value="1" />
            <el-option label="正在维修" value="2" />
            <el-option label="已损坏" value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEquipment">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="300px">
      <p>确定要删除设备 {{ deleteEquipmentInfo.equipmentName }} 吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteEquipment">删除</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 类型管理对话框 -->
    <el-dialog v-model="typeDialogVisible" title="设备类型管理" width="600px">
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
        <el-table-column prop="equipmentTypeId" label="类型ID" width="100" align="center" />
        <el-table-column prop="equipmentTypeName" label="类型名称" align="center" />
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
          <el-input v-model="editTypeData.equipmentTypeId" disabled />
        </el-form-item>
        <el-form-item label="类型名称">
          <el-input v-model="editTypeData.equipmentTypeName" placeholder="请输入类型名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editTypeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditType">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Plus, Edit, Delete, Search, Setting } from '@element-plus/icons-vue'
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
const equipmentList = ref([])
const total = ref(0)
const typeList = ref([])

// 搜索和分页
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框状态
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const typeDialogVisible = ref(false)
const editTypeDialogVisible = ref(false)
const dialogTitle = ref('添加设备')

// 表单引用
const formRef = ref(null)

// 表单验证规则
const formRules = {
  equipmentName: [{ required: true, message: '请输入设备名', trigger: 'blur' }],
  equipmentTypeId: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
}

// 表单数据
const formData = ref({
  equipmentId: '',
  equipmentName: '',
  equipmentTypeId: '',
  status: '0',
})

// 待删除设备
const deleteEquipmentInfo = ref({})

// 新类型名称
const newTypeName = ref('')

// 编辑类型数据
const editTypeData = ref({
  equipmentTypeId: '',
  equipmentTypeName: '',
})

// 加载状态
const loading = ref(false)

// 加载设备列表
const loadEquipments = async () => {
  loading.value = true
  try {
    let response
    if (searchQuery.value) {
      response = await $API.searchEquipmentByPage(
        searchQuery.value,
        currentPage.value,
        pageSize.value,
      )
    } else {
      response = await $API.getEquipmentByPage(currentPage.value, pageSize.value)
    }
    if (response.data && response.data.code === '200') {
      equipmentList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

// 加载类型列表
const loadTypes = async () => {
  try {
    const res = await $API.getEquipmentTypes()
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
    const res = await $API.addEquipmentType(newTypeName.value.trim())
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
  if (!editTypeData.value.equipmentTypeName.trim()) {
    ElMessage.warning('请输入类型名称')
    return
  }
  try {
    console.log(editTypeData)
    const res = await $API.updateEquipmentType(editTypeData.value)
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
    const res = await $API.deleteEquipmentType(row.equipmentTypeId)
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
    equipmentId: '',
    equipmentName: '',
    equipmentTypeId: '',
    status: '0',
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 打开编辑对话框
const openEditDialog = (row) => {
  dialogTitle.value = '编辑设备'
  const typeId = getTypeIdByName(row.equipmentTypeName)
  formData.value = {
    equipmentId: row.equipmentId,
    equipmentName: row.equipmentName,
    equipmentTypeId: typeId,
    status: row.status?.toString() || '0',
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 根据类型名称获取类型ID
const getTypeIdByName = (typeName) => {
  const type = typeList.value.find((item) => item.equipmentTypeName === typeName)
  return type ? type.equipmentTypeId : ''
}

// 确认删除
const confirmDelete = (row) => {
  deleteEquipmentInfo.value = row
  deleteDialogVisible.value = true
}

// 保存设备
const saveEquipment = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const equipmentData = {
          ...formData.value,
          status: formData.value.status,
        }
        if (formData.value.equipmentId) {
          await $API.updateEquipment(equipmentData)
          ElMessage.success('编辑成功')
          await loadEquipments()
          dialogVisible.value = false
        } else {
          console.log(equipmentData)
          const res = await $API.addEquipment(equipmentData)
          if (res.data.code === '200') {
            ElMessage.success('添加成功')
            await loadEquipments()
            dialogVisible.value = false
          } else if (res.data.code === '500') {
            ElMessage.error(res.data.message || '添加失败')
          } else {
            ElMessage.error(res.data.message || '添加失败')
          }
        }
      } catch (error) {
        console.error('保存设备失败:', error)
        ElMessage.error('保存设备失败')
      }
    }
  })
}

// 删除设备
const deleteEquipment = async () => {
  try {
    await $API.deleteEquipment(deleteEquipmentInfo.value.equipmentId)
    await loadEquipments()
    deleteDialogVisible.value = false
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除设备失败:', error)
    ElMessage.error('删除设备失败')
  }
}

// 搜索处理
const handleSearch = async () => {
  currentPage.value = 1
  await loadEquipments()
}

// 重置搜索
const handleReset = async () => {
  searchQuery.value = ''
  currentPage.value = 1
  await loadEquipments()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadEquipments()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadEquipments()
}

// 状态文本映射
const statusText = (status) => {
  const statusMap = {
    0: '正常闲置',
    1: '已被借用',
    2: '正在维修',
    3: '已损坏',
  }
  return statusMap[status] || '未知状态'
}

// 状态样式映射
const statusClass = (status) => {
  const classMap = {
    0: 'status-normal',
    1: 'status-borrowed',
    2: 'status-repairing',
    3: 'status-damaged',
  }
  return classMap[status] || ''
}

// 初始化加载
onMounted(() => {
  loadEquipments()
  loadTypes()
})
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
.status-normal {
  color: #67c23a;
  font-weight: 600;
}

.status-borrowed {
  color: #409eff;
  font-weight: 600;
}

.status-repairing {
  color: #e6a23c;
  font-weight: 600;
}

.status-damaged {
  color: #f56c6c;
  font-weight: 600;
}

.empty-type {
  color: #c0c4cc;
  font-style: italic;
}

.type-header {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
