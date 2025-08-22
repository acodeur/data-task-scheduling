<template>
  <div class="base-echart">
    <div class="echart" ref="echartRef"></div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watchEffect } from 'vue'
import * as echarts from 'echarts'

interface IProps {
  options: echarts.EChartsOption | any
}

const props = defineProps<IProps>()
const echartRef = ref<HTMLElement>()
let echartInstance: echarts.ECharts

onMounted(() => {
  // 初始化echarts实例
  echartInstance = echarts.init(echartRef.value!, 'light', { renderer: 'canvas' })

  // 设置options
  watchEffect(() => {
    echartInstance.setOption(props.options)
  })

  // 自适应
  window.addEventListener('resize', () => {
    echartInstance.resize()
  })
})

// 销毁
onUnmounted(() => {
  window.removeEventListener('resize', () => {
    echartInstance.resize()
  })
  echartInstance.dispose()
})
</script>

<style lang="less" scoped>
.base-echart {
  .echart {
    width: 100%;
    height: 300px;
  }
}
</style>
