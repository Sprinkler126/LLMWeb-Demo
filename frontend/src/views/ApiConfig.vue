<template>
  <div class="api-config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>API配置管理</span>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>
            添加配置
          </el-button>
        </div>
      </template>

      <el-table :data="configList" stripe>
        <el-table-column prop="configName" label="配置名称" />
        <el-table-column prop="provider" label="提供商" width="120" />
        <el-table-column prop="modelName" label="模型名称" />
        <el-table-column prop="apiType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.apiType === 'ONLINE' ? 'success' : 'info'">
              {{ row.apiType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="testApiConfig(row.id)">测试</el-button>
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteConfig(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="配置名称">
          <el-input v-model="form.configName" />
        </el-form-item>
        <el-form-item label="提供商">
          <el-select v-model="form.provider" placeholder="请选择">
            <el-option label="OpenAI" value="OpenAI" />
            <el-option label="Anthropic" value="Anthropic" />
            <el-option label="Google" value="Google" />
            <el-option label="Aliyun" value="Aliyun" />
            <el-option label="Baidu" value="Baidu" />
            <el-option label="Local" value="Local" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="form.modelName" />
        </el-form-item>
        <el-form-item label="API端点">
          <el-input v-model="form.apiEndpoint" />
        </el-form-item>
        <el-form-item label="API密钥">
          <el-input v-model="form.apiKey" type="password" />
        </el-form-item>
        <el-form-item label="API类型">
          <el-radio-group v-model="form.apiType">
            <el-radio label="ONLINE">在线</el-radio>
            <el-radio label="LOCAL">本地</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="最大Token数">
          <el-input-number v-model="form.maxTokens" :min="100" :max="8000" />
        </el-form-item>
        <el-form-item label="温度">
          <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfigList, createConfig, updateConfig, deleteConfig as deleteConfigApi, testConfig } from '@/api/apiConfig'

const configList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加API配置')
const form = ref({
  configName: '',
  provider: 'OpenAI',
  modelName: '',
  apiEndpoint: '',
  apiKey: '',
  apiType: 'ONLINE',
  maxTokens: 2000,
  temperature: 0.7,
  timeout: 30,
  status: 1
})

onMounted(() => {
  loadConfigList()
})

const loadConfigList = async () => {
  try {
    const res = await getConfigList({ current: 1, size: 100 })
    configList.value = res.data.records
  } catch (error) {
    console.error(error)
  }
}

const openDialog = (row = null) => {
  if (row) {
    dialogTitle.value = '编辑API配置'
    form.value = { ...row, temperature: Number(row.temperature) }
  } else {
    dialogTitle.value = '添加API配置'
    form.value = {
      configName: '',
      provider: 'OpenAI',
      modelName: '',
      apiEndpoint: '',
      apiKey: '',
      apiType: 'ONLINE',
      maxTokens: 2000,
      temperature: 0.7,
      timeout: 30,
      status: 1
    }
  }
  dialogVisible.value = true
}

const saveConfig = async () => {
  try {
    if (form.value.id) {
      await updateConfig(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createConfig(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadConfigList()
  } catch (error) {
    console.error(error)
  }
}

const deleteConfig = (id) => {
  ElMessageBox.confirm('确定要删除这个API配置吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteConfigApi(id)
      ElMessage.success('删除成功')
      await loadConfigList()
    } catch (error) {
      console.error(error)
    }
  })
}

const testApiConfig = async (id) => {
  try {
    await testConfig(id)
    ElMessage.success('API测试成功')
  } catch (error) {
    ElMessage.error('API测试失败')
  }
}
</script>

<style scoped>
.api-config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
