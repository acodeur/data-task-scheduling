<template>
  <div class="rose-echart">
    <base-echart :options="options"></base-echart>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseEchart from './BaseEchart.vue'
import * as echarts from 'echarts'
import type { EchartProps } from '../type'

const props = defineProps<EchartProps>()
const options = computed<echarts.EChartsOption>(() => {
  return {
    toolbox: {
      show: true,
      feature: {
        mark: { show: true },
        // dataView: { show: true, readOnly: false },
        // restore: { show: true },
        // saveAsImage: { show: true },
      },
    },
    legend: {
      orient: 'horizontal',
      left: 'left',
      top: '0',
    },
    tooltip: {
      trigger: 'item',
    },
    series: [
      {
        name: '访问来源',
        type: 'pie',
        // 内半径/外半径的大小
        radius: [10, 150],
        // 设置区域的位置
        center: ['50%', '50%'],
        bottom: '-15%',
        roseType: 'area', // 圆心角一样, 通过半径的不同表示大小
        itemStyle: {
          borderRadius: 8,
        },
        data: props.labels.map((item, index) => {
          return {
            value: props.values[index],
            name: item,
          }
        }),
        label: {
          show: false,
        },
      },
    ],
  }
})
</script>

<style lang="less" scoped></style>
