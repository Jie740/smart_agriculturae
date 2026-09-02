<template>
  <div class="mature-crop-monitor">
    <div class="header">
      <h1>{{ roleTitle }}-成熟作物统计</h1>
    </div>

    <div class="filter-bar">
      <el-select
        v-model="selectedLandId"
        placeholder="请选择地块"
        style="width: 300px"
        clearable
        @change="handleLandChange"
      >
        <el-option
          v-for="item in landList"
          :key="item.landId"
          :label="item.landName"
          :value="item.landId"
        />
      </el-select>
      <el-date-picker
        v-model="startMonth"
        type="month"
        placeholder="开始月份"
        value-format="YYYY-MM"
        clearable
        style="width: 150px"
        @change="handleDateChange"
      />
      <span>至</span>
      <el-date-picker
        v-model="endMonth"
        type="month"
        placeholder="结束月份"
        value-format="YYYY-MM"
        clearable
        style="width: 150px"
        @change="handleDateChange"
      />
      <el-button type="primary" @click="loadStatistics">查询</el-button>
      <el-button type="warning" @click="handleReset">重置</el-button>
    </div>

    <div class="statistics-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: #67c23a">
          <el-icon><Crop /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalCrops }}</div>
          <div class="stat-label">成熟作物总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #409eff">
          <el-icon><ScaleToOriginal /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalOutput }}</div>
          <div class="stat-label">总产量(kg)</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6a23c">
          <el-icon><MapLocation /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalLands }}</div>
          <div class="stat-label">涉及地块数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #f56c6c">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.avgOutput }}</div>
          <div class="stat-label">平均产量(kg)</div>
        </div>
      </div>
    </div>

    <div class="charts-container" v-loading="loading">
      <div class="chart-row">
        <div class="chart-card">
          <h3>各农作物产量统计</h3>
          <div ref="cropOutputChart" class="chart"></div>
        </div>
        <div class="chart-card">
          <h3>各地块产量统计</h3>
          <div ref="landOutputChart" class="chart"></div>
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-card">
          <h3>农作物占比分布</h3>
          <div ref="pieChart" class="chart"></div>
        </div>
      </div>
    </div>

    <div class="table-section">
      <h3>成熟作物记录</h3>
      <el-table :data="cropList" border stripe v-loading="tableLoading" max-height="300">
        <el-table-column prop="matureCropId" label="ID" width="80" align="center" />
        <el-table-column prop="landName" label="地块名称" width="150" align="center" />
        <el-table-column prop="cropName" label="农作物名称" width="150" align="center" />
        <el-table-column prop="outputQuantity" label="产量(kg)" width="120" align="center" />
        <el-table-column prop="harvestTime" label="更新时间" width="120" align="center" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)"
              >编辑</el-button
            >
            <el-button type="danger" size="small" @click="confirmDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="loadCropList"
          @current-change="loadCropList"
        />
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="地块">
          <el-input :value="formData.landName" disabled />
        </el-form-item>
        <el-form-item label="农作物">
          <el-input :value="formData.cropName" disabled />
        </el-form-item>
        <el-form-item label="产量(kg)" prop="outputQuantity">
          <el-input
            type="number"
            min="0"
            v-model="formData.outputQuantity"
            placeholder="请输入产量"
          />
        </el-form-item>
        <el-form-item label="更新时间" prop="harvestTime">
          <el-date-picker
            v-model="formData.harvestTime"
            type="date"
            placeholder="请选择更新时间"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
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
      <p>确定要删除该成熟作物记录吗？</p>
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Crop, ScaleToOriginal, MapLocation, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { ElMessageBox } from 'element-plus'
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

const landList = ref([])
const selectedLandId = ref('')
const startMonth = ref('')
const endMonth = ref('')
const loading = ref(false)
const tableLoading = ref(false)

// 列表数据
const cropList = ref([])
const cropListData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框状态
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const dialogTitle = ref('添加成熟作物')
const formRef = ref(null)

// 表单数据
const formData = ref({
  matureCropId: '',
  landId: '',
  cropId: '',
  outputQuantity: '',
  harvestTime: '',
})

// 表单验证规则
const formRules = {
  outputQuantity: [{ required: true, message: '请输入产量', trigger: 'blur' }],
  harvestTime: [{ required: true, message: '请选择更新时间', trigger: 'change' }],
}

// 待删除数据
const deleteCropInfo = ref({})

const statistics = ref({
  totalCrops: 0,
  totalOutput: 0,
  totalLands: 0,
  avgOutput: 0,
})

const cropOutputChart = ref(null)
const landOutputChart = ref(null)
const pieChart = ref(null)

let cropOutputChartInstance = null
let landOutputChartInstance = null
let pieChartInstance = null

const loadLands = async () => {
  try {
    const res = await $API.getLandAll()
    if (res.data && res.data.code === '200') {
      landList.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取地块列表失败:', error)
    ElMessage.error('获取地块列表失败')
  }
}

const loadStatistics = async () => {
  loading.value = true
  try {
    const params = {}
    if (selectedLandId.value) {
      params.landId = selectedLandId.value
    }
    if (startMonth.value) {
      params.startDate = startMonth.value
    }
    if (endMonth.value) {
      params.endDate = endMonth.value
    }

    const res = await $API.getMatureCropStatistics(params)
    if (res.data && res.data.code === '200') {
      const data = res.data.data
      statistics.value = {
        totalCrops: data.totalCrops || 0,
        totalOutput: data.totalOutput || 0,
        totalLands: data.totalLands || 0,
        avgOutput: data.avgOutput || 0,
      }
      await nextTick()
      initCharts()
      updateCharts(data)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
    updateChartsWithMockData()
  } finally {
    loading.value = false
  }
}

const updateChartsWithMockData = () => {
  const mockData = {
    cropOutput: [
      { cropName: '小麦', output: 1250 },
      { cropName: '玉米', output: 980 },
      { cropName: '水稻', output: 1560 },
      { cropName: '大豆', output: 720 },
      { cropName: '土豆', output: 890 },
    ],
    landOutput: [
      { landName: 'A地块', output: 680 },
      { landName: 'B地块', output: 920 },
      { landName: 'C地块', output: 1150 },
      { landName: 'D地块', output: 540 },
      { landName: 'E地块', output: 780 },
    ],
    pieData: [
      { name: '小麦', value: 1250 },
      { name: '玉米', value: 980 },
      { name: '水稻', value: 1560 },
      { name: '大豆', value: 720 },
      { name: '土豆', value: 890 },
    ],
  }

  statistics.value = {
    totalCrops: 45,
    totalOutput: 5400,
    totalLands: 12,
    avgOutput: 120,
  }

  nextTick(() => {
    initCharts()
    updateCropOutputChart(mockData.cropOutput)
    updateLandOutputChart(mockData.landOutput)
    updatePieChart(mockData.pieData)
  })
}

const handleLandChange = () => {
  loadStatistics()
}

const handleDateChange = () => {
  loadStatistics()
}

const handleReset = () => {
  selectedLandId.value = ''
  startMonth.value = ''
  endMonth.value = ''
  loadStatistics()
}

const initCharts = () => {
  if (cropOutputChart.value) {
    cropOutputChartInstance = echarts.init(cropOutputChart.value)
  }
  if (landOutputChart.value) {
    landOutputChartInstance = echarts.init(landOutputChart.value)
  }
  if (pieChart.value) {
    pieChartInstance = echarts.init(pieChart.value)
  }
}

const updateCharts = (data) => {
  updateCropOutputChart(data.cropOutput || [])
  updateLandOutputChart(data.landOutput || [])
  updatePieChart(data.pieData || [])
}

const updateCropOutputChart = (data) => {
  if (!cropOutputChartInstance) return

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.cropName),
      axisLabel: { rotate: 30 },
    },
    yAxis: {
      type: 'value',
      name: '产量(kg)',
    },
    series: [
      {
        name: '产量',
        type: 'bar',
        data: data.map((item) => item.output),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: '#95d475' },
          ]),
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}',
        },
      },
    ],
  }
  cropOutputChartInstance.setOption(option)
}

const updateLandOutputChart = (data) => {
  if (!landOutputChartInstance) return

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.landName),
    },
    yAxis: {
      type: 'value',
      name: '产量(kg)',
    },
    series: [
      {
        name: '产量',
        type: 'bar',
        data: data.map((item) => item.output),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#79bbff' },
          ]),
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}',
        },
      },
    ],
  }
  landOutputChartInstance.setOption(option)
}

const updatePieChart = (data) => {
  if (!pieChartInstance) return

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center',
    },
    series: [
      {
        name: '产量占比',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: data,
      },
    ],
  }
  pieChartInstance.setOption(option)
}

const handleResize = () => {
  cropOutputChartInstance?.resize()
  landOutputChartInstance?.resize()
  pieChartInstance?.resize()
}

onMounted(() => {
  loadLands()
  loadStatistics()
  loadCropList()
  loadCropListAll()
  window.addEventListener('resize', handleResize)
})

// 加载作物列表（分页）
const loadCropList = async () => {
  tableLoading.value = true
  try {
    const res = await $API.getMatureCropByPage(currentPage.value, pageSize.value)
    if (res.data && res.data.code === '200') {
      cropList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch (error) {
    console.error('获取成熟作物列表失败:', error)
    ElMessage.error('获取成熟作物列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 加载所有作物（用于下拉框）
const loadCropListAll = async () => {
  try {
    const res = await $API.getCropAll()
    if (res.data && res.data.code === '200') {
      cropListData.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取农作物列表失败:', error)
  }
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加成熟作物'
  formData.value = {
    matureCropId: '',
    landId: '',
    cropId: '',
    outputQuantity: '',
    harvestTime: '',
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 打开编辑对话框
const openEditDialog = (row) => {
  dialogTitle.value = '编辑成熟作物'
  formData.value = {
    matureCropId: row.matureCropId,
    landId: row.landId,
    landName: row.landName,
    cropId: row.cropId,
    cropName: row.cropName,
    outputQuantity: row.outputQuantity,
    harvestTime: row.harvestTime,
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 保存作物
const saveCrop = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (formData.value.matureCropId) {
          const harvestDate = new Date(formData.value.harvestTime)
          harvestDate.setDate(harvestDate.getDate() + 1)
          const updateData = {
            matureCropId: formData.value.matureCropId,
            outputQuantity: formData.value.outputQuantity,
            harvestTime: harvestDate,
          }
          await $API.updateMatureCrop(updateData)
          ElMessage.success('编辑成功')
        } else {
          await $API.addMatureCrop(formData.value)
          ElMessage.success('添加成功')
        }
        await loadCropList()
        await loadStatistics()
        dialogVisible.value = false
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      }
    }
  })
}

// 确认删除
const confirmDelete = (row) => {
  deleteCropInfo.value = row
  deleteDialogVisible.value = true
}

// 删除作物
const deleteCrop = async () => {
  try {
    await $API.deleteMatureCrop(deleteCropInfo.value.matureCropId)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    loadCropList()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  cropOutputChartInstance?.dispose()
  landOutputChartInstance?.dispose()
  pieChartInstance?.dispose()
})
</script>

<style scoped>
.mature-crop-monitor {
  padding: 15px;
  background-color: #f5f7fa;
  min-height: 100%;
}

.header {
  margin-bottom: 15px;
}

.header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.filter-bar {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-bottom: 15px;
}

.stat-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.charts-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 5px;
}

.chart-row {
  display: flex;
  gap: 15px;
}

.chart-card {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  min-height: 300px;
}

.chart-card h3 {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.chart {
  width: 100%;
  height: 280px;
}

.table-section {
  margin-top: 20px;
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.table-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 15px 0;
}

.pagination {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.header {
  margin-bottom: 15px;
}

.header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
</style>
