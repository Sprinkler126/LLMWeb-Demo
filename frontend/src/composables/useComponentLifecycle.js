/**
 * 组件生命周期管理 - 防止组件卸载后更新状态
 * 用于解决快速路由切换时的 "Cannot read properties of null" 错误
 */
import { ref, onBeforeUnmount } from 'vue'

export function useComponentLifecycle() {
  const isUnmounted = ref(false)

  onBeforeUnmount(() => {
    isUnmounted.value = true
  })

  /**
   * 在组件未卸载时执行回调
   * @param {Function} callback - 要执行的回调函数
   * @param {*} fallbackValue - 组件已卸载时的返回值
   */
  const safeExecute = (callback, fallbackValue = undefined) => {
    if (isUnmounted.value) {
      console.log('⚠️ 组件已卸载，跳过操作')
      return fallbackValue
    }
    return callback()
  }

  /**
   * 安全的异步操作包装
   * @param {Function} asyncFn - 异步函数
   */
  const safeAsync = async (asyncFn) => {
    try {
      const result = await asyncFn()
      if (isUnmounted.value) {
        console.log('⚠️ 组件已卸载，跳过后续操作')
        return null
      }
      return result
    } catch (error) {
      if (!isUnmounted.value) {
        throw error
      }
      console.log('⚠️ 组件已卸载，忽略错误')
      return null
    }
  }

  return {
    isUnmounted,
    safeExecute,
    safeAsync
  }
}
