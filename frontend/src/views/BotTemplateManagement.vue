<template>
  <div class="bot-template-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>机器人模板管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建模板
          </el-button>
        </div>
      </template>

      <!-- 状态说明 -->
      <el-alert
        title="状态说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <ul style="margin: 5px 0; padding-left: 20px">
          <li><strong>停用(0)</strong>：模板不可用，任何地方都不显示</li>
          <li><strong>公开(1)</strong>：用户在聊天界面可见并可选择</li>
          <li><strong>内部(2)</strong>：仅供系统内部流程使用，用户不可见</li>
        </ul>
      </el-alert>

      <!-- 模板列表 -->
      <el-table :data="templates" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模板名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="系统消息" min-width="300">
          <template #default="scope">
            <el-text line-clamp="2" style="width: 100%">
              {{ scope.row.systemMessage }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">停用</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">公开</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="warning">内部</el-tag>
            <el-tag v-else type="danger">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模板' : '新建模板'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input v-model="form.name" placeholder="请输入模板名称" />
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入模板描述"
          />
        </el-form-item>

        <el-form-item label="系统消息" required>
          <el-input
            v-model="form.systemMessage"
            type="textarea"
            :rows="6"
            placeholder="请输入系统消息内容，这将定义机器人的角色和行为"
          />
        </el-form-item>

        <el-form-item label="状态" required>
          <el-radio-group v-model="form.status">
            <el-radio :label="0">停用</el-radio>
            <el-radio :label="1">公开（用户可见）</el-radio>
            <el-radio :label="2">内部（系统专用）</el-radio>
          </el-radio-group>
          <div style="margin-top: 8px; color: #909399; font-size: 12px">
            <span v-if="form.status === 0">模板将不可用</span>
            <span v-else-if="form.status === 1">用户可在聊天界面选择此模板</span>
            <span v-else-if="form.status === 2">仅供系统内部流程使用，用户不可见</span>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllTemplates, createTemplate, updateTemplate, deleteTemplate } from '@/api/botTemplate'

const templates = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)

const form = ref({
  id: null,
  name: '',
  description: '',
  systemMessage: '',
  status: 1
})

onMounted(() => {
  loadTemplates()
})

const loadTemplates = async () => {
  try {
    const res = await getAllTemplates()
    templates.value = res.data
  } catch (error) {
    ElMessage.error('加载模板列表失败')
    console.error(error)
  }
}

const handleCreate = () => {
  isEdit.value = false
  form.value = {
    id: null,
    name: '',
    description: '',
    systemMessage: '',
    status: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    name: row.name,
    description: row.description,
    systemMessage: row.systemMessage,
    status: row.status
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!form.value.systemMessage) {
    ElMessage.warning('请输入系统消息')
    return
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateTemplate(form.value)
      ElMessage.success('更新成功')
    } else {
      await createTemplate(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadTemplates()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    console.error(error)
  } finally {
    saving.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个模板吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTemplate(id)
      ElMessage.success('删除成功')
      await loadTemplates()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  })
}
</script>

<style scoped>
.bot-template-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-text) {
  font-size: 13px;
  color: #606266;
}
</style>
