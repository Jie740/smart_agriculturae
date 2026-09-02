<template>
  <div class="adjust-management">
    <!-- 标题 -->
    <div class="header">
      <h1>{{ roleTitle }}-农资审批管理</h1>

      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        申请农资
      </el-button>
    </div>

    <!-- 搜索 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="输入农资名/申请人"
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
        <el-table-column prop="applyId" label="申请ID" width="100" align="center" />

        <el-table-column prop="materialName" label="农资名" align="center" />

        <el-table-column prop="applicant" label="申请人" width="120" align="center" />

        <el-table-column prop="phone" label="联系电话" width="140" align="center" />

        <el-table-column prop="quantity" label="数量" align="center" />

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
            <el-button
              v-if="role !== 'user' && scope.row.status === 0"
              type="success"
              size="small"
              plain
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="role !== 'user' && scope.row.status === 0"
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
            <el-button
              v-if="role === 'user' && scope.row.status === 0"
              type="warning"
              size="small"
              plain
              @click="handleCancel(scope.row)"
            >
              取消申请
            </el-button>
            <el-button
              v-if="role !== 'user'"
              type="primary"
              size="small"
              plain
              @click="openEditDialog(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="role !== 'user'"
              type="danger"
              size="small"
              plain
              @click="handleDelete(scope.row)"
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

    <!-- 添加调整申请弹窗 -->
    <el-dialog v-model="addDialogVisible" title="申请农资" width="600px">
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="100px">
        <el-form-item label="农资" prop="materialId">
          <el-select
            v-model="addForm.materialId"
            placeholder="请选择农资"
            style="width: 100%"
            @change="handleMaterialChange"
          >
            <el-option
              v-for="item in materialList"
              :key="item.materialId"
              :label="item.materialName"
              :value="item.materialId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="农资类型">
          <el-input v-model="addForm.typeName" disabled placeholder="选择农资后显示" />
        </el-form-item>

        <el-form-item label="数量">
          <el-input type="number" min="1" v-model="addForm.quantity" placeholder="请输入数量" />
        </el-form-item>

        <el-form-item label="申请人" prop="applicant">
          <el-input v-model="addForm.applicant" placeholder="请输入申请人姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>

      <el-form-item label="申请时间" prop="applyTime">
        <el-date-picker
          v-model="addForm.applyTime"
          type="datetime"
          placeholder="选择申请时间"
          style="width: 100%"
        />
      </el-form-item>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAdd">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑调整申请弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑农资调整" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="addFormRules" label-width="100px">
        <el-form-item label="申请ID">
          <el-input v-model="editForm.applyId" disabled />
        </el-form-item>

        <el-form-item label="农资" prop="materialId">
          <el-select
            :disabled="editForm.status !== '0'"
            v-model="editForm.materialId"
            placeholder="请选择农资"
            style="width: 100%"
            @change="handleMaterialChange"
          >
            <el-option
              v-for="item in materialList"
              :key="item.materialId"
              :label="item.materialName"
              :value="item.materialId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="农资类型">
          <el-input v-model="editForm.typeName" disabled />
        </el-form-item>

        <el-form-item :disabled="editForm.status !== '0'" label="申请人" prop="applicant">
          <el-input
            :disabled="editForm.status !== '0'"
            v-model="editForm.applicant"
            placeholder="请输入申请人姓名"
          />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input
            :disabled="editForm.status !== '0'"
            v-model="editForm.phone"
            placeholder="请输入联系电话"
          />
        </el-form-item>

        <el-form-item label="数量" prop="quantity">
          <el-input
            :disabled="editForm.status !== '0'"
            v-model="editForm.quantity"
            type="number"
            min="1"
            placeholder="请输入数量"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="未审批" value="0" />
            <el-option label="已通过" value="1" />
            <el-option label="未通过" value="2" />
          </el-select>
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

const adjustList = ref([])
const loading = ref(false)

const searchQuery = ref('')
const role = ref(localStorage.getItem('role') || '')
const roleTitle = computed(() => {
  const map = {
    user: '承包人',
    enterprise_admin: '企业管理员',
    system_admin: '系统管理员',
  }
  return map[role.value] || '系统管理员'
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 添加申请相关
const addDialogVisible = ref(false)
const addFormRef = ref(null)
const addForm = ref({
  materialId: '',
  typeId: '',
  typeName: '',
  quantity: '',
  applyTime: '',
  applicant: '',
  phone: '',
})

// 编辑申请相关
const editDialogVisible = ref(false)
const editForm = ref({
  applyId: '',
  materialId: '',
  typeId: '',
  typeName: '',
  quantity: '',
  applyTime: '',
  applicant: '',
  phone: '',
  status: '',
})

const addFormRules = {
  materialId: [{ required: true, message: '请选择农资', trigger: 'change' }],
  applicant: [{ required: true, message: '请输入申请人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    // { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

const materialList = ref([])
const editFormRef = ref(null)

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
const loadApply = async () => {
  loading.value = true
  try {
    let res

    if (role.value === 'user') {
      if (searchQuery.value) {
        res = await $API.getMyMaterialApplies(searchQuery.value, currentPage.value, pageSize.value)
      } else {
        res = await $API.getMyMaterialApplies('', currentPage.value, pageSize.value)
      }
    } else if (searchQuery.value) {
      res = await $API.searchMaterialApplyByPage(
        searchQuery.value,
        currentPage.value,
        pageSize.value,
      )
    } else {
      res = await $API.getMaterialApplyByPage(currentPage.value, pageSize.value)
    }

    if (res.data.code === '200') {
      adjustList.value = res.data.data.records.sort((a, b) => a.applyId - b.applyId)
      total.value = res.data.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

/* 加载农资列表 */
const loadMaterials = async () => {
  try {
    const res = await $API.getMaterialList()
    if (res.data.code === '200') {
      materialList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载农资失败:', e)
  }
}

/* 搜索 */
const handleSearch = () => {
  currentPage.value = 1
  loadApply()
}

/* 重置 */
const handleReset = () => {
  searchQuery.value = ''
  currentPage.value = 1
  loadApply()
}

/* 分页 */
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadApply()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadApply()
}

/* 通过审批 */
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认通过该调整申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
    })

    const res = await $API.updateMaterialApplyStatus(row.applyId, 1)
    if (res.data.code === '200') {
      ElMessage.success('审批通过')
      loadApply()
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

    const res = await $API.updateMaterialApplyStatus(row.applyId, 2)
    if (res.data.code === '200') {
      ElMessage.success('已拒绝')
      loadApply()
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
    materialId: '',
    typeName: '',
    quantity: 1,
    applicant: '',
    phone: '',
    applyTime: '',
  }
  // 重置农资详情显示状态
  addDialogVisible.value = true
  nextTick(() => {
    addFormRef.value?.clearValidate()
  })
}

/* 农资选择变化 */
const handleMaterialChange = async (materialId) => {
  try {
    const res = await $API.getMaterialTypeById(materialId)
    const typeName = res.data.data.typeName
    if (res.data.code === '200') {
      // 如果是添加操作，更新添加表单
      if (addDialogVisible.value) {
        addForm.value.materialId = materialId
        addForm.value.typeName = typeName
      }
      // 如果是编辑操作，更新编辑表单
      if (editDialogVisible.value) {
        editForm.value.materialId = materialId
        editForm.value.typeName = typeName
      }
    }
  } catch (e) {
    console.error('获取农资详情失败:', e)
    ElMessage.error('获取农资详情失败')
  }
}

/* 打开编辑对话框 */
const openEditDialog = async (row) => {
  try {
    // 获取调整详情
    const res = await $API.getMaterialApplyById(row.applyId)
    const adjustDetail = res.data.data || {}
    editForm.value = {
      applyId: row.applyId,
      materialId: adjustDetail.materialId || row.materialId,
      materialName: adjustDetail.materialName || row.materialName,
      typeName: adjustDetail.typeName || row.typeName,
      quantity: row.quantity,
      applyTime: adjustDetail.applyTime ? new Date(adjustDetail.applyTime) : null,
      applicant: row.applicant,
      phone: row.phone,
      status: String(adjustDetail.status || row.status),
    }
    editDialogVisible.value = true
    nextTick(() => {
      editFormRef.value?.clearValidate()
    })
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
          applyId: editForm.value.applyId,
          materialId: editForm.value.materialId,
          applicant: editForm.value.applicant,
          phone: editForm.value.phone,
          quantity: editForm.value.quantity,
          status: Number(editForm.value.status),
        }
        const res = await $API.updateMaterialApply(editData)
        if (res.data.code === '200') {
          ElMessage.success('修改成功')
          editDialogVisible.value = false
          loadApply()
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

    const res = await $API.deleteMaterialApply(row.applyId)
    if (res.data.code === '200') {
      ElMessage.success('删除成功')
      loadApply()
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
    await ElMessageBox.confirm(`确认取消该申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await $API.deleteMaterialApply(row.applyId)
    if (res.data.code === '200') {
      ElMessage.success('取消成功')
      loadApply()
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
          materialId: addForm.value.materialId,
          quantity: addForm.value.quantity,
          applyTime: formatDate(addForm.value.applyTime),
          applyTime: formatDate(new Date()),
        }
        const res = await $API.addMaterialApproval(addData)
        if (res.data.code === '200') {
          ElMessage.success('申请提交成功')
          addDialogVisible.value = false
          loadApply()
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
  loadApply()
  loadMaterials()
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
