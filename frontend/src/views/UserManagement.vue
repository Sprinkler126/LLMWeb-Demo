<template>
  <div class="user-management-container">
    <el-card>
      <template #header>
        <span>用户管理</span>
      </template>
      <el-empty description="用户管理功能仅管理员可用，可以在数据库中直接管理用户" />
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.user-management-container {
  padding: 20px;
}
</style>
