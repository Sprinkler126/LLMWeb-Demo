<template>
  <div class="role-permission-container">
    <el-card>
      <template #header>
        <span class="header-title">角色权限配置</span>
      </template>

      <el-row :gutter="20">
        <!-- 左侧：角色列表 -->
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>
              <span>角色列表</span>
            </template>
            <el-menu :default-active="selectedRoleId?.toString()" @select="handleRoleSelect">
              <el-menu-item v-for="role in roleList" :key="role.id" :index="role.id.toString()">
                <el-icon><User /></el-icon>
                <span>{{ role.roleName }}</span>
                <el-tag size="small" :type="getRoleTagType(role.roleCode)" style="margin-left: 10px">
                  {{ role.roleCode }}
                </el-tag>
              </el-menu-item>
            </el-menu>
          </el-card>
        </el-col>

        <!-- 右侧：权限配置 -->
        <el-col :span="16">
          <el-card shadow="never" v-loading="loading">
            <template #header>
              <div class="card-header">
                <span>权限配置 - {{ currentRole?.roleName }}</span>
                <el-button 
                  type="primary" 
                  @click="handleSavePermissions"
                  :disabled="!currentRole || currentRole.roleCode === 'SUPER_ADMIN'"
                >
                  保存配置
                </el-button>
              </div>
            </template>

            <div v-if="!currentRole" class="empty-state">
              <el-empty description="请选择一个角色" />
            </div>

            <div v-else-if="currentRole.roleCode === 'SUPER_ADMIN'" class="empty-state">
              <el-alert
                title="超级管理员拥有所有权限，无需配置"
                type="info"
                :closable="false"
              />
            </div>

            <div v-else class="permission-tree-container">
              <!-- 权限树 -->
              <el-tree
                ref="permissionTreeRef"
                :data="permissionTree"
                show-checkbox
                node-key="id"
                :props="treeProps"
                :default-checked-keys="checkedPermissions"
                @check="handlePermissionCheck"
              >
                <template #default="{ node, data }">
                  <span class="custom-tree-node">
                    <span class="node-label">
                      <el-icon><component :is="getPermissionIcon(data.permissionCode)" /></el-icon>
                      {{ data.permissionName }}
                    </span>
                    <span class="node-description">
                      <el-text size="small" type="info">{{ data.description }}</el-text>
                    </span>
                  </span>
                </template>
              </el-tree>

              <!-- 权限统计 -->
              <div class="permission-stats">
                <el-divider />
                <el-space wrap>
                  <el-statistic title="可用权限总数" :value="totalPermissions" />
                  <el-statistic title="已选权限" :value="checkedPermissions.length" />
                  <el-statistic 
                    title="权限覆盖率" 
                    :value="permissionCoverage" 
                    suffix="%" 
                  />
                </el-space>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  Setting,
  Connection,
  ChatLineSquare,
  Warning,
  Key
} from '@element-plus/icons-vue'
import {
  getAllRoles,
  getAllPermissions,
  getRolePermissions,
  assignPermissions
} from '@/api/admin'

// 数据
const loading = ref(false)
const roleList = ref([])
const permissionList = ref([])
const permissionTree = ref([])
const currentRole = ref(null)
const selectedRoleId = ref(null)
const checkedPermissions = ref([])
const permissionTreeRef = ref(null)

// 树形控件配置
const treeProps = {
  children: 'children',
  label: 'permissionName'
}

// 计算属性
const totalPermissions = computed(() => permissionList.value.length)
const permissionCoverage = computed(() => {
  if (totalPermissions.value === 0) return 0
  return Math.round((checkedPermissions.value.length / totalPermissions.value) * 100)
})

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await getAllRoles()
    if (res.code === 200) {
      roleList.value = res.data
      if (roleList.value.length > 0 && !selectedRoleId.value) {
        selectedRoleId.value = roleList.value[0].id
        handleRoleSelect(selectedRoleId.value.toString())
      }
    }
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  }
}

// 加载所有权限
const loadPermissions = async () => {
  try {
    const res = await getAllPermissions()
    if (res.code === 200) {
      permissionList.value = res.data
      buildPermissionTree()
    }
  } catch (error) {
    ElMessage.error('加载权限列表失败')
  }
}

// 构建权限树
const buildPermissionTree = () => {
  const tree = []
  const moduleMap = new Map()

  permissionList.value.forEach(permission => {
    if (permission.parentId === 0) {
      // 模块节点
      moduleMap.set(permission.id, {
        ...permission,
        children: []
      })
      tree.push(moduleMap.get(permission.id))
    }
  })

  permissionList.value.forEach(permission => {
    if (permission.parentId !== 0) {
      // 权限节点
      const parent = moduleMap.get(permission.parentId)
      if (parent) {
        parent.children.push(permission)
      }
    }
  })

  permissionTree.value = tree
}

// 角色选择
const handleRoleSelect = async (roleId) => {
  selectedRoleId.value = parseInt(roleId)
  currentRole.value = roleList.value.find(r => r.id === selectedRoleId.value)
  await loadRolePermissions()
}

// 加载角色权限
const loadRolePermissions = async () => {
  if (!selectedRoleId.value) return

  loading.value = true
  try {
    const res = await getRolePermissions(selectedRoleId.value)
    if (res.code === 200) {
      checkedPermissions.value = res.data.permissionIds || []
      // 设置树形控件的选中状态
      if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(checkedPermissions.value)
      }
    }
  } catch (error) {
    ElMessage.error('加载角色权限失败')
  } finally {
    loading.value = false
  }
}

// 权限选择变化
const handlePermissionCheck = () => {
  if (permissionTreeRef.value) {
    const checkedNodes = permissionTreeRef.value.getCheckedNodes()
    const halfCheckedNodes = permissionTreeRef.value.getHalfCheckedNodes()
    
    // 只保存叶子节点的ID（实际权限）
    checkedPermissions.value = checkedNodes
      .filter(node => !node.children || node.children.length === 0)
      .map(node => node.id)
  }
}

// 保存权限配置
const handleSavePermissions = async () => {
  if (!currentRole.value) return

  try {
    const res = await assignPermissions(currentRole.value.id, checkedPermissions.value)
    if (res.code === 200) {
      ElMessage.success('权限配置保存成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '权限配置保存失败')
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

// 权限图标
const getPermissionIcon = (permissionCode) => {
  if (permissionCode?.includes('USER')) return User
  if (permissionCode?.includes('API')) return Connection
  if (permissionCode?.includes('COMPLIANCE')) return Warning
  if (permissionCode?.includes('PERMISSION')) return Key
  if (permissionCode?.includes('SYSTEM')) return Setting
  return ChatLineSquare
}

// 初始化
onMounted(async () => {
  await loadPermissions()
  await loadRoles()
})
</script>

<style scoped>
.role-permission-container {
  padding: 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.permission-tree-container {
  padding: 10px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 20px;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.node-description {
  font-size: 12px;
  color: #909399;
}

.permission-stats {
  margin-top: 20px;
}

:deep(.el-tree-node__content) {
  height: 40px;
}
</style>
