<template>
  <div class="equipment-management">
    <div class="header">
      <h1>{{ roleTitle }}-设备管理</h1>
      <div class="header-buttons">
        <el-button type="warning" @click="openRepairDialog">
          <el-icon><Setting /></el-icon>
          报修申请
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        :placeholder="role === 'user' ? '输入设备名称' : '输入承包人姓名'"
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
        <el-table-column prop="recordId" label="设备记录ID" width="150" align="center" />
        <el-table-column prop="equipmentName" label="设备名" width="150" align="center" />
        <el-table-column prop="typeName" label="设备类型" width="150" align="center" />
        <el-table-column prop="ownerName" label="隶属人" width="150" align="center" />
        <el-table-column prop="ownerPhone" label="手机号" width="130" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <span :class="statusClass(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="role !== 'user'"
          label="操作"
          width="260"
          fixed="right"
          align="center"
        >
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                v-if="role !== 'user'"
                type="primary"
                size="small"
                @click="openEditDialog(scope.row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                v-if="role !== 'user' && scope.row.status === 1"
                type="warning"
                size="small"
                @click="openRepairDetailDialog(scope.row)"
              >
                查看故障原因
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
            </div>
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

    <!-- 编辑设备对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="设备记录ID">
          <el-input v-model="formData.recordId" disabled />
        </el-form-item>
        <el-form-item label="设备名">
          <el-input v-model="formData.equipmentName" disabled />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input v-model="formData.typeName" disabled />
        </el-form-item>
        <el-form-item label="隶属人">
          <el-input v-model="formData.ownerName" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.ownerPhone" disabled />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正在使用" value="0" />
            <el-option label="报修中" value="1" />
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

    <!-- 报修申请对话框 -->
    <el-dialog v-model="repairDialogVisible" title="报修申请" width="500px">
      <el-form
        ref="repairFormRef"
        :model="repairFormData"
        :rules="repairFormRules"
        label-width="100px"
      >
        <el-form-item label="申请人姓名" prop="applicantName">
          <el-input v-model="repairFormData.applicantName" placeholder="请输入申请人姓名" />
        </el-form-item>
        <el-form-item label="申请人电话" prop="applicantPhone">
          <el-input v-model="repairFormData.applicantPhone" placeholder="请输入申请人电话" />
        </el-form-item>
        <el-form-item label="选择设备" prop="equipmentId">
          <el-select
            v-model="repairFormData.equipmentId"
            placeholder="请选择设备"
            style="width: 100%"
          >
            <el-option
              v-for="item in equipmentOptions"
              :key="item.equipmentId"
              :label="item.equipmentName"
              :value="item.equipmentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDescription">
          <el-input
            v-model="repairFormData.faultDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入故障描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="repairDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRepair">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看故障原因对话框 -->
    <el-dialog v-model="repairDetailDialogVisible" title="故障原因" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请人姓名">{{
          repairDetail.applicantName
        }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ repairDetail.phone }}</el-descriptions-item>
        <el-descriptions-item label="设备名称" :span="2">{{
          repairDetail.equipmentName
        }}</el-descriptions-item>
        <el-descriptions-item label="故障原因" :span="2">{{
          repairDetail.faultDescription
        }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="repairDetailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Edit, Delete, Search, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import $API from '@/api'

// 设备数据
const equipmentList = ref([])
const total = ref(0)

// 搜索和分页
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框状态
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const repairDetailDialogVisible = ref(false)
const dialogTitle = ref('编辑设备')

// 报修详情数据
const repairDetail = ref({
  applicantName: '',
  phone: '',
  equipmentName: '',
  faultDescription: '',
})

// 表单引用
const formRef = ref(null)

// 表单验证规则
const formRules = {
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// 表单数据
const formData = ref({
  recordId: '',
  equipmentName: '',
  typeName: '',
  ownerName: '',
  ownerPhone: '',
  status: '0',
})

// 待删除设备
const deleteEquipmentInfo = ref({})

// 报修申请对话框状态
const repairDialogVisible = ref(false)
const repairFormRef = ref(null)

// 设备选项列表
const equipmentOptions = ref([])

// 报修申请表单数据
const repairFormData = ref({
  applicantName: '',
  applicantPhone: '',
  equipmentId: '',
  faultDescription: '',
})

// 报修申请表单验证规则
const repairFormRules = {
  applicantName: [{ required: true, message: '请输入申请人姓名', trigger: 'blur' }],
  applicantPhone: [{ required: true, message: '请输入申请人电话', trigger: 'blur' }],
  equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  faultDescription: [{ required: true, message: '请输入故障描述', trigger: 'blur' }],
}

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
const loadEquipments = async () => {
  loading.value = true
  try {
    let response
    if (role.value === 'user') {
      response = await $API.getMyEquipmentByPage(
        searchQuery.value,
        currentPage.value,
        pageSize.value,
      )
    } else if (searchQuery.value) {
      response = await $API.searchEquipmentRecordsByPage(
        searchQuery.value,
        currentPage.value,
        pageSize.value,
      )
    } else {
      response = await $API.getEquipmentRecordsByPage(currentPage.value, pageSize.value)
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

// 打开编辑对话框
const openEditDialog = (row) => {
  dialogTitle.value = '编辑设备'
  formData.value = {
    recordId: row.recordId,
    equipmentName: row.equipmentName,
    typeName: row.typeName,
    ownerName: row.ownerName,
    ownerPhone: row.ownerPhone,
    status: row.status?.toString() || '0',
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
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
          recordId: formData.value.recordId,
          status: parseInt(formData.value.status),
        }
        console.log(equipmentData)
        await $API.updateEquipmentRecordStatus(equipmentData)
        ElMessage.success('编辑成功')
        await loadEquipments()
        dialogVisible.value = false
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
    await $API.deleteEquipmentRecord(deleteEquipmentInfo.value.recordId)
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
    0: '正在使用',
    1: '报修中',
  }
  return statusMap[status] || '未知状态'
}

// 状态样式映射
const statusClass = (status) => {
  const classMap = {
    0: 'status-normal',
    1: 'status-repairing',
  }
  return classMap[status] || ''
}

// 打开报修申请对话框
const openRepairDialog = async () => {
  await loadEquipmentOptions()
  repairFormData.value = {
    applicantName: '',
    applicantPhone: '',
    equipmentId: '',
    faultDescription: '',
  }
  repairDialogVisible.value = true
  nextTick(() => {
    repairFormRef.value?.clearValidate()
  })
}

// 加载设备选项列表
const loadEquipmentOptions = async () => {
  try {
    let res = {}
    if (role.value === 'user') {
      res = await $API.getMyEquipmentList()
      if (res.data && res.data.code === '200') {
        equipmentOptions.value = res.data.data || []
      }
    } else {
      res = await $API.getEquipmentList()
      if (res.data && res.data.code === '200') {
        equipmentOptions.value = res.data.data || []
      }
    }
    if (res.data && res.data.code === '200') {
      equipmentOptions.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
  }
}

// 打开故障原因对话框
const openRepairDetailDialog = async (row) => {
  try {
    const res = await $API.getEquipmentRepairApplyByRecordId(
      row.recordId,
      row.ownerName || '',
      row.ownerPhone || '',
    )
    if (res.data.code === '200' && res.data.data) {
      repairDetail.value = {
        applicantName: res.data.data.applicantName || '',
        phone: res.data.data.phone || '',
        equipmentName: res.data.data.equipmentName || row.equipmentName || '',
        faultDescription: res.data.data.faultDescription || '',
      }
      repairDetailDialogVisible.value = true
    } else {
      ElMessage.error(res.data.message || '用户不存在')
    }
  } catch (error) {
    console.error('获取故障原因失败:', error)
    ElMessage.error('用户不存在')
  }
}

// 提交报修申请
const submitRepair = async () => {
  if (!repairFormRef.value) return
  await repairFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const repairData = {
          applicantName: repairFormData.value.applicantName,
          applicantPhone: repairFormData.value.applicantPhone,
          equipmentId: repairFormData.value.equipmentId,
          faultDescription: repairFormData.value.faultDescription,
        }
        const res = await $API.addEquipmentRepairApply(repairData)
        // 处理后端返回的错误信息
        if (res.data.code === '500') {
          ElMessage.error(res.data.message)
          repairDialogVisible.value = false
          return
        }
        ElMessage.success('报修申请提交成功')
        repairDialogVisible.value = false
        loadEquipments()
      } catch (error) {
        console.error('提交报修申请失败:', error)
      }
    }
  })
}

// 初始化加载
onMounted(() => {
  loadEquipments()
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

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
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

.type-header {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
