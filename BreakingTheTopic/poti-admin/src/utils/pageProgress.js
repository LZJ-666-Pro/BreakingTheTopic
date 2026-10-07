// 页面切换顶部加载进度条（NProgress 风格，无依赖实现）
import { reactive } from 'vue'

export const progress = reactive({
  visible: false,
  percent: 0
})

let timer = null
let doneTimer = null

function start() {
  if (doneTimer) { clearTimeout(doneTimer); doneTimer = null }
  progress.visible = true
  progress.percent = 20
  if (timer) clearInterval(timer)
  // 模拟进度推进：越接近 90% 越慢，等待真实页面加载完成
  timer = setInterval(() => {
    if (progress.percent < 90) {
      progress.percent += Math.max(0.5, (90 - progress.percent) * 0.12)
    }
  }, 120)
}

function done() {
  if (!progress.visible) return
  if (timer) { clearInterval(timer); timer = null }
  progress.percent = 100
  doneTimer = setTimeout(() => {
    progress.visible = false
    progress.percent = 0
  }, 300)
}

export function startProgress() { start() }
export function doneProgress() { done() }
