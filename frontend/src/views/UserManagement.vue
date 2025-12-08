<template>
  <div class="user-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="header-title">用户管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true">
          <el-form-item label="用户名">
            <el-input 
              v-model="searchForm.username" 
              placeholder="输入用户名搜索"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input 
              v-model="searchForm.email" 
              placeholder="输入邮箱搜索"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select 
              v-model="searchForm.roleId" 
              placeholder="选择角色"
              clearable
              @clear="handleSearch"
              style="width: 150px"
            >
              <el-option label="全部" :value="null" />
              <el-option 
                v-for="role in roleList" 
                :key="role.id" 
                :label="role.roleName" 
                :value="role.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select 
              v-model="searchForm.status" 
              placeholder="选择状态"
              clearable
              @clear="handleSearch"
              style="width: 120px"
            >
              <el-option label="全部" :value="null" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户列表 -->
      <el-table :data="filteredUserList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.roleCode)">
              {{ scope.row.roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="apiQuota" label="API配额" width="100" />
        <el-table-column prop="apiUsage" label="已使用" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="scope">
            <el-button size="small" @click="showRoleDialog(scope.row)">
              分配角色
            </el-button>
            <el-button size="small" @click="showQuotaDialog(scope.row)">
              配额管理
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)" 
                       :disabled="scope.row.id === 1">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增用户对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增用户" width="500px">
      <el-form :model="newUser" :rules="userRules" ref="createFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="newUser.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="newUser.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="newUser.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="newUser.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" 
                       :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="API配额" prop="apiQuota">
          <el-input-number v-model="newUser.apiQuota" :min="0" :step="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确认</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <el-text>{{ currentUser?.username }}</el-text>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="selectedRoleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" 
                       :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRole">确认</el-button>
      </template>
    </el-dialog>

    <!-- 配额管理对话框 -->
    <el-dialog v-model="quotaDialogVisible" title="配额管理" width="400px">
      <el-form label-width="100px">
        <el-form-item label="用户">
          <el-text>{{ currentUser?.username }}</el-text>
        </el-form-item>
        <el-form-item label="当前配额">
          <el-text>{{ currentUser?.apiQuota }}</el-text>
        </el-form-item>
        <el-form-item label="已使用">
          <el-text>{{ currentUser?.apiUsage }}</el-text>
        </el-form-item>
        <el-form-item label="新配额">
          <el-input-number v-model="newQuota" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quotaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateQuota">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getAllUsers,
  createUser,
  deleteUser,
  assignRole,
  updateUserStatus,
  updateApiQuota,
  getEnabledRoles
} from '@/api/admin'

// 数据
const loading = ref(false)
const userList = ref([])
const roleList = ref([])
const currentUser = ref(null)

// 对话框控制
const createDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const quotaDialogVisible = ref(false)

// 表单数据
const createFormRef = ref(null)
const newUser = ref({
  username: '',
  password: '',
  email: '',
  roleId: null,
  apiQuota: 1000
})

const selectedRoleId = ref(null)
const newQuota = ref(0)

// 表单校验规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 加载数据
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAllUsers()
    if (res.code === 200) {
      userList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    const res = await getEnabledRoles()
    if (res.code === 200) {
      roleList.value = res.data
    }
  } catch (error) {
    console.error('加载角色列表失败', error)
  }
}

// 角色标签类型
const getRoleTagType = (roleCode) => {
  const typeMap = {
    'SUPER_ADMIN': 'danger',
    'ADMIN': 'warning',
    'USER': 'info'
  }
  return typeMap[roleCode] || 'info'
}

// 显示新增对话框
const showCreateDialog = () => {
  newUser.value = {
    username: '',
    password: '',
    email: '',
    roleId: null,
    apiQuota: 1000
  }
  createDialogVisible.value = true
}

// 创建用户
const handleCreate = async () => {
  const valid = await createFormRef.value.validate()
  if (!valid) return

  try {
    const res = await createUser(newUser.value)
    if (res.code === 200) {
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      loadUsers()
    }
  } catch (error) {
    ElMessage.error(error.message || '创建失败')
  }
}

// 显示角色对话框
const showRoleDialog = (user) => {
  currentUser.value = user
  selectedRoleId.value = user.roleId
  roleDialogVisible.value = true
}

// 分配角色
const handleAssignRole = async () => {
  try {
    const res = await assignRole(currentUser.value.id, selectedRoleId.value)
    if (res.code === 200) {
      ElMessage.success('角色分配成功')
      roleDialogVisible.value = false
      loadUsers()
    }
  } catch (error) {
    ElMessage.error(error.message || '角色分配失败')
  }
}

// 显示配额对话框
const showQuotaDialog = (user) => {
  currentUser.value = user
  newQuota.value = user.apiQuota
  quotaDialogVisible.value = true
}

// 更新配额
const handleUpdateQuota = async () => {
  try {
    const res = await updateApiQuota(currentUser.value.id, newQuota.value)
    if (res.code === 200) {
      ElMessage.success('配额更新成功')
      quotaDialogVisible.value = false
      loadUsers()
    }
  } catch (error) {
    ElMessage.error(error.message || '配额更新失败')
  }
}

// 状态变更
const handleStatusChange = async (user) => {
  try {
    const res = await updateUserStatus(user.id, user.status)
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      // 失败时还原状态
      user.status = user.status === 1 ? 0 : 1
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    user.status = user.status === 1 ? 0 : 1
  }
}

// 删除用户
const handleDelete = async (user) => {
  if (user.id === 1) {
    ElMessage.warning('不能删除超级管理员')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.username} 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteUser(user.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadUsers()
  loadRoles()
})
</script>

<style scoped>
.user-management-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
