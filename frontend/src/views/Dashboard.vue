<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stats-row">
      <!-- 用户总数 -->
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 对话总数 -->
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon message-icon">
              <el-icon :size="40"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalMessages || 0 }}</div>
              <div class="stat-label">对话总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最近一天对话数 -->
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon recent-icon">
              <el-icon :size="40"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.recentDayMessages || 0 }}</div>
              <div class="stat-label">最近一天对话数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 违规占比 -->
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon violation-icon">
              <el-icon :size="40"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.violationRate || 0 }}%</div>
              <div class="stat-label">违规占比</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细统计卡片 -->
    <el-row :gutter="20" class="detail-row">
      <el-col :span="12">
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>会话统计</span>
              <el-icon><TrendCharts /></el-icon>
            </div>
          </template>
          <div class="detail-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="detail-item">
                  <div class="detail-label">会话总数</div>
                  <div class="detail-value">{{ statistics.totalSessions || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="detail-item">
                  <div class="detail-label">活跃会话</div>
                  <div class="detail-value">{{ statistics.activeSessions || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <el-divider />
            <div class="detail-description">
              最近24小时内有更新的会话被视为活跃会话
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>用户增长</span>
              <el-icon><UserFilled /></el-icon>
            </div>
          </template>
          <div class="detail-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="detail-item">
                  <div class="detail-label">用户总数</div>
                  <div class="detail-value">{{ statistics.totalUsers || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="detail-item">
                  <div class="detail-label">今日新增</div>
                  <div class="detail-value new-users">
                    +{{ statistics.newUsersToday || 0 }}
                  </div>
                </div>
              </el-col>
            </el-row>
            <el-divider />
            <div class="detail-description">
              今日0点以后注册的用户数量
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 违规详情卡片 -->
    <el-row :gutter="20" class="detail-row">
      <el-col :span="24">
        <el-card class="detail-card violation-card">
          <template #header>
            <div class="card-header">
              <span>合规监测</span>
              <el-icon><DocumentChecked /></el-icon>
            </div>
          </template>
          <div class="detail-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="detail-item">
                  <div class="detail-label">总消息数</div>
                  <div class="detail-value">{{ statistics.totalMessages || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="detail-item">
                  <div class="detail-label">违规消息数</div>
                  <div class="detail-value violation-count">
                    {{ statistics.violationMessages || 0 }}
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="detail-item">
                  <div class="detail-label">违规率</div>
                  <div class="detail-value" :class="getViolationRateClass(statistics.violationRate)">
                    {{ statistics.violationRate || 0 }}%
                  </div>
                </div>
              </el-col>
            </el-row>
            <el-divider />
            <el-progress 
              :percentage="statistics.violationRate || 0" 
              :color="getViolationProgressColor(statistics.violationRate)"
              :stroke-width="15"
            >
              <template #default="{ percentage }">
                <span class="progress-text">{{ percentage }}%</span>
              </template>
            </el-progress>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 刷新按钮 -->
    <div class="refresh-section">
      <el-button type="primary" @click="loadStatistics" :loading="loading">
        <el-icon class="refresh-icon"><Refresh /></el-icon>
        刷新数据
      </el-button>
      <span class="last-update">最后更新: {{ lastUpdateTime }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  User, 
  ChatDotRound, 
  Clock, 
  Warning, 
  TrendCharts, 
  UserFilled, 
  DocumentChecked,
  Refresh 
} from '@element-plus/icons-vue'
import { getStatistics } from '@/api/dashboard'

const loading = ref(false)
const lastUpdateTime = ref('')

const statistics = reactive({
  totalUsers: 0,
  totalSessions: 0,
  totalMessages: 0,
  recentDayMessages: 0,
  violationMessages: 0,
  violationRate: 0,
  activeSessions: 0,
  newUsersToday: 0
})

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    const { data } = await getStatistics()
    Object.assign(statistics, data)
    
    // 更新最后更新时间
    lastUpdateTime.value = new Date().toLocaleString('zh-CN')
    
    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error(error.response?.data?.message || '加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 获取违规率样式类
const getViolationRateClass = (rate) => {
  if (rate >= 10) return 'high-violation'
  if (rate >= 5) return 'medium-violation'
  return 'low-violation'
}

// 获取违规率进度条颜色
const getViolationProgressColor = (rate) => {
  if (rate >= 10) return '#f56c6c'
  if (rate >= 5) return '#e6a23c'
  return '#67c23a'
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 100px);
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.recent-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.violation-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.detail-row {
  margin-bottom: 20px;
}

.detail-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.detail-content {
  padding: 10px 0;
}

.detail-item {
  text-align: center;
  padding: 10px;
}

.detail-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.detail-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.detail-value.new-users {
  color: #67c23a;
}

.detail-value.violation-count {
  color: #e6a23c;
}

.detail-value.high-violation {
  color: #f56c6c;
}

.detail-value.medium-violation {
  color: #e6a23c;
}

.detail-value.low-violation {
  color: #67c23a;
}

.detail-description {
  font-size: 13px;
  color: #909399;
  text-align: center;
  margin-top: 10px;
}

.violation-card {
  background: linear-gradient(to right, #ffeaa7 0%, #fdcb6e 100%);
}

.progress-text {
  font-weight: bold;
  color: white;
}

.refresh-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.refresh-icon {
  margin-right: 5px;
}

.last-update {
  font-size: 14px;
  color: #909399;
}
</style>
