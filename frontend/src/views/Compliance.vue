<template>
  <div class="compliance-container">
    <el-card>
      <template #header>
        <span>合规检测</span>
      </template>
      <el-empty description="合规检测功能需要配置Python服务后使用" />
      <p style="text-align: center; color: #909399; margin-top: 20px;">
        Python服务接口: POST http://localhost:5000/api/compliance/check
      </p>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.compliance-container {
  padding: 20px;
}
</style>
