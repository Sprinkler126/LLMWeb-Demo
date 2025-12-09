<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span class="title">个人信息</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="profileForm" label-width="120px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" />
            </el-form-item>

            <el-form-item label="角色">
              <el-tag :type="getRoleTagType(profileForm.roleCode)">
                {{ profileForm.roleName }}
              </el-tag>
            </el-form-item>

            <el-form-item label="账号状态">
              <el-tag :type="profileForm.status === 1 ? 'success' : 'danger'">
                {{ profileForm.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </el-form-item>

            <el-form-item label="注册时间">
              <span>{{ formatDate(profileForm.createdAt) }}</span>
            </el-form-item>

            <el-form-item label="最后登录">
              <span>{{ formatDate(profileForm.lastLoginTime) }}</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="loading">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- API配额 -->
        <el-tab-pane label="API配额" name="quota">
          <div class="quota-section">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-statistic title="总配额" :value="apiUsage.apiQuota || 0">
                  <template #suffix>次</template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="已使用" :value="apiUsage.apiUsed || 0">
                  <template #suffix>次</template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="剩余" :value="apiUsage.remaining || 0">
                  <template #suffix>次</template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic 
                  title="使用率" 
                  :value="apiUsage.usagePercent || 0"
                  :precision="2"
                >
                  <template #suffix>%</template>
                </el-statistic>
              </el-col>
            </el-row>

            <el-divider />

            <el-progress 
              :percentage="Math.min(apiUsage.usagePercent || 0, 100)" 
              :color="getProgressColor(apiUsage.usagePercent)"
              :stroke-width="20"
              text-inside
            />

            <el-alert
              v-if="apiUsage.usagePercent > 80"
              title="提示"
              :description="`您的API配额使用已超过80%，剩余 ${apiUsage.remaining} 次调用`"
              type="warning"
              :closable="false"
              style="margin-top: 20px"
            />
          </div>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form 
            :model="passwordForm" 
            :rules="passwordRules"
            ref="passwordFormRef"
            label-width="120px" 
            class="profile-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input 
                v-model="passwordForm.oldPassword" 
                type="password" 
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                show-password
                placeholder="请输入新密码（至少6位）"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword" :loading="loading">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, updatePassword, getApiUsage } from '@/api/profile'

const activeTab = ref('basic')
const loading = ref(false)
const passwordFormRef = ref(null)

// 个人信息表单
const profileForm = reactive({
  username: '',
  email: '',
  roleName: '',
  roleCode: '',
  status: 1,
  createdAt: null,
  lastLoginTime: null
})

// API使用统计
const apiUsage = reactive({
  apiQuota: 0,
  apiUsed: 0,
  remaining: 0,
  usagePercent: 0
})

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载个人信息
const loadProfile = async () => {
  try {
    const { data } = await getProfile()
    Object.assign(profileForm, data)
  } catch (error) {
    console.error('加载个人信息失败:', error)
    ElMessage.error(error.response?.data?.message || '加载个人信息失败')
  }
}

// 加载API使用统计
const loadApiUsage = async () => {
  try {
    const { data } = await getApiUsage()
    Object.assign(apiUsage, data)
  } catch (error) {
    console.error('加载API使用统计失败:', error)
  }
}

// 更新个人信息
const handleUpdateProfile = async () => {
  loading.value = true
  try {
    await updateProfile({
      email: profileForm.email
    })
    ElMessage.success('个人信息更新成功')
    await loadProfile()
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error(error.response?.data?.message || '更新个人信息失败')
  } finally {
    loading.value = false
  }
}

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await updatePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      
      // 重置表单
      resetPasswordForm()
      
      // 延迟跳转到登录页
      setTimeout(() => {
        // 清除token并跳转到登录页
        localStorage.removeItem('token')
        window.location.href = '/login'
      }, 1500)
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error(error.response?.data?.message || '修改密码失败')
    } finally {
      loading.value = false
    }
  })
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

// 获取角色标签类型
const getRoleTagType = (roleCode) => {
  const typeMap = {
    'SUPER_ADMIN': 'danger',
    'ADMIN': 'warning',
    'USER': 'info'
  }
  return typeMap[roleCode] || 'info'
}

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 80) return '#e6a23c'
  if (percentage >= 60) return '#409eff'
  return '#67c23a'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  loadProfile()
  loadApiUsage()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.profile-tabs {
  margin-top: 20px;
}

.profile-form {
  max-width: 600px;
  margin-top: 20px;
}

.quota-section {
  padding: 20px;
}

.el-statistic {
  text-align: center;
}
</style>
