import api from './api'

const PAGE_SIZE_CACHE_KEY = 'sys_default_page_size'

/**
 * 读取系统默认分页条数（后台「系统配置-默认分页条数」）。
 * MainLayout 挂载时从 /admin/config/value/default_page_size 拉取并缓存，
 * 各列表页初始化 pageSize 时使用；配置缺失或未拉取时回退 10。
 */
export function getDefaultPageSize() {
  const v = parseInt(localStorage.getItem(PAGE_SIZE_CACHE_KEY), 10)
  return Number.isInteger(v) && v > 0 ? v : 10
}

/** 从后端刷新默认分页条数缓存，返回最新值（失败保持旧值） */
export async function loadDefaultPageSize() {
  try {
    const res = await api.get('/admin/config/value/default_page_size')
    const v = parseInt(res.data, 10)
    if (Number.isInteger(v) && v > 0) {
      localStorage.setItem(PAGE_SIZE_CACHE_KEY, String(v))
    }
  } catch (e) {
    // 配置接口不可用时保持旧缓存/默认值
  }
  return getDefaultPageSize()
}
