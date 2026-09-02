<template>
  <div class="crop-monitor">
    <div class="header">
      <h1>农作物监控</h1>
    </div>

    <div class="filter-bar">
      <el-select
        v-model="selectedLandId"
        placeholder="请选择地块"
        style="width: 300px"
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
        v-model="selectedDate"
        type="date"
        placeholder="请选择日期"
        style="width: 200px"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        @change="handleDateChange"
      />
    </div>

    <div class="charts-container" v-loading="loading">
      <div class="chart-row">
        <div class="chart-card">
          <h3>温度监控</h3>
          <div ref="temperatureChart" class="chart"></div>
        </div>
        <div class="chart-card">
          <h3>湿度监控</h3>
          <div ref="humidityChart" class="chart"></div>
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-card">
          <h3>光照强度</h3>
          <div ref="lightChart" class="chart"></div>
        </div>
        <div class="chart-card">
          <h3>土壤PH值</h3>
          <div ref="phChart" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import $API from '@/api'

const landList = ref([])
const selectedLandId = ref('')
const selectedDate = ref('')
const loading = ref(false)
const userRole = ref(localStorage.getItem('role') || '')

const temperatureChart = ref(null)
const humidityChart = ref(null)
const lightChart = ref(null)
const phChart = ref(null)
let temperatureChartInstance = null
let humidityChartInstance = null
let lightChartInstance = null
let phChartInstance = null

const loadLands = async () => {
  try {
    let res
    if (userRole.value === 'user') {
      res = await $API.getMyLands()
    } else {
      res = await $API.getLandAll()
    }
    if (res.data && res.data.code === '200') {
      landList.value = res.data.data || []
      if (landList.value.length > 0) {
        selectedLandId.value = landList.value[0].landId
        await loadMonitorData()
      }
    }
  } catch (error) {
    console.error('获取地块列表失败:', error)
    ElMessage.error('获取地块列表失败')
  }
}

const loadMonitorData = async () => {
  if (!selectedLandId.value) return

  loading.value = true
  try {
    await nextTick()
    initCharts()
    updateCharts()
  } catch (error) {
    console.error('加载监控数据失败:', error)
    ElMessage.error('加载监控数据失败')
  } finally {
    loading.value = false
  }
}

const handleLandChange = () => {
  loadMonitorData()
}

const handleDateChange = () => {
  loadMonitorData()
}

const initCharts = () => {
  if (temperatureChart.value) {
    temperatureChartInstance = echarts.init(temperatureChart.value)
  }
  if (humidityChart.value) {
    humidityChartInstance = echarts.init(humidityChart.value)
  }
  if (lightChart.value) {
    lightChartInstance = echarts.init(lightChart.value)
  }
  if (phChart.value) {
    phChartInstance = echarts.init(phChart.value)
  }
}

const updateCharts = () => {
  const times = []
  const now = selectedDate.value ? new Date(selectedDate.value) : new Date()
  for (let i = 11; i >= 0; i--) {
    const time = new Date(now - i * 3600000)
    times.push(time.getHours() + ':00')
  }

  const temperatureData = generateRandomData(12, 15, 35)
  const humidityData = generateRandomData(12, 40, 80)
  const lightData = generateRandomData(12, 200, 800)
  const phData = generateRandomData(12, 5.5, 7.5)

  updateTemperatureChart(times, temperatureData)
  updateHumidityChart(times, humidityData)
  updateLightChart(times, lightData)
  updatePhChart(times, phData)
}

const generateRandomData = (count, min, max) => {
  const data = []
  for (let i = 0; i < count; i++) {
    data.push((Math.random() * (max - min) + min).toFixed(1))
  }
  return data
}

const updateTemperatureChart = (times, data) => {
  if (!temperatureChartInstance) return

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
    },
    yAxis: {
      type: 'value',
      name: '温度(°C)',
    },
    series: [
      {
        name: '温度',
        type: 'line',
        smooth: true,
        data: data,
        itemStyle: {
          color: '#f56c6c',
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.5)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.1)' },
          ]),
        },
      },
    ],
  }
  temperatureChartInstance.setOption(option)
}

const updateHumidityChart = (times, data) => {
  if (!humidityChartInstance) return

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
    },
    yAxis: {
      type: 'value',
      name: '湿度(%)',
    },
    series: [
      {
        name: '湿度',
        type: 'line',
        smooth: true,
        data: data,
        itemStyle: {
          color: '#409eff',
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' },
          ]),
        },
      },
    ],
  }
  humidityChartInstance.setOption(option)
}

const updateLightChart = (times, data) => {
  if (!lightChartInstance) return

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
    },
    yAxis: {
      type: 'value',
      name: '光照(lux)',
    },
    series: [
      {
        name: '光照强度',
        type: 'line',
        smooth: true,
        data: data,
        itemStyle: {
          color: '#e6a23c',
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230, 162, 60, 0.5)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.1)' },
          ]),
        },
      },
    ],
  }
  lightChartInstance.setOption(option)
}

const updatePhChart = (times, data) => {
  if (!phChartInstance) return

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
    },
    yAxis: {
      type: 'value',
      name: 'PH值',
      min: 4,
      max: 9,
    },
    series: [
      {
        name: 'PH值',
        type: 'line',
        smooth: true,
        data: data,
        itemStyle: {
          color: '#67c23a',
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' },
          ]),
        },
        markLine: {
          data: [
            { yAxis: 5.5, name: '偏酸', lineStyle: { color: '#f56c6c' } },
            { yAxis: 7.5, name: '偏碱', lineStyle: { color: '#409eff' } },
          ],
        },
      },
    ],
  }
  phChartInstance.setOption(option)
}

const handleResize = () => {
  temperatureChartInstance?.resize()
  humidityChartInstance?.resize()
  lightChartInstance?.resize()
  phChartInstance?.resize()
}

onMounted(() => {
  loadLands()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  temperatureChartInstance?.dispose()
  humidityChartInstance?.dispose()
  lightChartInstance?.dispose()
  phChartInstance?.dispose()
})
</script>

<style scoped>
.crop-monitor {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
}

.header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.filter-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.charts-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chart-row {
  display: flex;
  gap: 15px;
}

.chart-card {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.chart-card h3 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
}

.chart {
  width: 100%;
  height: 220px;
}
</style>
