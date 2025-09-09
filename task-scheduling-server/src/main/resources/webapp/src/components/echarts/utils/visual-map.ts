// ECharts visual map configurations
export const visualMapConfigs = {
  continuous: {
    type: 'continuous',
    left: 'right',
    top: 'center',
    dimension: 2,
    calculable: true,
    precision: 0.1,
    text: ['圆形大小：'],
    textGap: 30,
    textStyle: {
      color: '#fff'
    },
    inRange: {
      color: ['#121122', '#d94e5d', '#eac736', '#50a3ba'],
      symbolSize: [6, 60]
    },
    outOfRange: {
      symbolSize: [6, 60],
      color: ['#d94e5d']
    },
    controller: {
      inRange: {
        color: ['#d94e5d']
      },
      outOfRange: {
        color: ['#444']
      }
    }
  },
  piecewise: {
    type: 'piecewise',
    left: 'right',
    top: 'center',
    pieces: [
      { min: 0, max: 100, color: '#50a3ba' },
      { min: 100, max: 300, color: '#eac736' },
      { min: 300, max: 500, color: '#d94e5d' },
      { min: 500, color: '#121122' }
    ],
    textStyle: {
      color: '#fff'
    }
  }
}

export default visualMapConfigs