/**
 * 全局路由错误处理器
 * 防止快速路由切换时的 DOM 访问错误
 */

// 捕获 Vue 组件更新错误
export function setupGlobalErrorHandler(app) {
  app.config.errorHandler = (err, instance, info) => {
    // 忽略组件卸载后的 DOM 访问错误
    if (err.message && err.message.includes('Cannot read properties of null')) {
      console.warn('⚠️ 捕获到组件卸载错误（已忽略）:', err.message)
      return
    }

    // 忽略重复导航错误
    if (err.message && err.message.includes('Avoided redundant navigation')) {
      console.warn('⚠️ 捕获到重复导航错误（已忽略）')
      return
    }

    // 其他错误正常抛出
    console.error('🚨 Vue Error:', err, info)
  }

  // 捕获未处理的 Promise 错误
  window.addEventListener('unhandledrejection', (event) => {
    const reason = event.reason
    
    // 忽略路由相关错误
    if (reason && reason.message) {
      if (reason.message.includes('Avoided redundant navigation') ||
          reason.message.includes('Cannot read properties of null')) {
        console.warn('⚠️ 捕获到未处理的 Promise 错误（已忽略）:', reason.message)
        event.preventDefault()
        return
      }
    }

    console.error('🚨 Unhandled Promise Rejection:', reason)
  })

  console.log('✅ 全局错误处理器已启用')
}
