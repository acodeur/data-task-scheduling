<template>
  <div class="pie-echart">
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
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'horizontal',
      left: 'left',
      top: '0',
    },
    series: [
      {
        name: '访问来源',
        type: 'pie',
        radius: '50%',
        data: props.labels.map((item, index) => {
          return {
            value: props.values[index],
            name: item,
          }
        }),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }
})
</script>

<style lang="less" scoped></style>
