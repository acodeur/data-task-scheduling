<template>
  <div class="page-content">
    <div class="header">
      <h3 class="title">{{ config?.header?.title ?? '数据列表' }}</h3>
      <div class="button">
        <el-button type="primary" class="add-btn" @click="handleAdd">
          {{ config?.header?.btnTxt ?? '新建数据' }}
        </el-button>
      </div>
    </div>
    <div class="table">
      <el-table :data="dataList" v-bind="config.tableProps">
        <template v-for="item in config.columnList" :key="item.prop">
          <!-- 时间列 -->
          <el-table-column v-if="item.type === 'datetime'" v-bind="item">
            <template #default="scope">
              {{ formatDate(scope.row[item.prop]) }}
            </template>
          </el-table-column>
          <!-- 标签列 -->
          <el-table-column v-else-if="item.type === 'boolTag'" v-bind="item">
            <template #default="scope">
              <el-tag :type="scope.row[item.prop] ? 'success' : 'danger'">
                {{ scope.row[item.prop] ? item.trueTag : item.falseTag }}
              </el-tag>
            </template>
          </el-table-column>
          <!-- 操作列 -->
          <el-table-column v-else-if="item.type === 'operation'" v-bind="item">
            <template #default="scope">
              <el-button
                size="small"
                text
                type="primary"
                @click="handleEdit(scope.$index, scope.row)"
                ><el-icon><Edit /></el-icon> 编辑</el-button
              >
              <el-button
                size="small"
                text
                type="danger"
                @click="handleDelete(scope.$index, scope.row)"
                ><el-icon><Delete /></el-icon> 删除</el-button
              >
            </template>
          </el-table-column>
          <!-- 自定义列 -->
          <el-table-column v-else-if="item.type === 'custom'" v-bind="item">
            <template #default="scope">
              <slot :name="item.slot" :row="scope.row"></slot>
            </template>
          </el-table-column>
          <!-- 普通列 -->
          <el-table-column v-else v-bind="item"></el-table-column>
        </template>
      </el-table>
    </div>
    <div class="pagination">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :page-sizes="[5, 10, 20, 50]"
        :total="totalCount"
        v-model:page-size="pageSize"
        v-model:current-page="currentPage"
        :pager-count="7"
        @current-change="handleCurrentPageChange"
        @size-change="handlePageSizeChange"
      ></el-pagination>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/format'
import { ref, toRefs } from 'vue'
import type { IPageContentProps } from './type'

const props = defineProps<IPageContentProps>()
const emit = defineEmits([
  'handleAdd',
  'handleEdit',
  'handleDelete',
  'handleCurrentPageChange',
  'handlePageSizeChange',
])

const { pagination } = props.config
const pageSize = ref(pagination.pageSize)
const currentPage = ref(pagination.currentPage)
const { dataList, totalCount } = toRefs(props.model)

function handleAdd() {
  emit('handleAdd')
}
function handleEdit(index: number, row: any) {
  emit('handleEdit', index, row)
}
function handleDelete(index: number, row: any) {
  emit('handleDelete', index, row)
}

function handleCurrentPageChange(val: number) {
  emit('handleCurrentPageChange', val)
}
function handlePageSizeChange(val: number) {
  emit('handlePageSizeChange', val)
}

function updatePagination(
  pagination: { pageSize: number; currentPage: number } = { pageSize: 10, currentPage: 1 },
) {
  pageSize.value = pagination.pageSize
  currentPage.value = pagination.currentPage
}

defineExpose({
  updatePagination,
})
</script>

<style lang="less" scoped>
.page-content {
  padding: 10px;
  background-color: #f0f0f0;
  border-radius: 8px;
  .header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;

    .add-btn {
      width: 80px;
    }
  }
  .table {
    margin-top: 10px;
  }
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    .el-pagination {
      :deep(.el-select) {
        width: 100px;
      }
    }
  }
}
</style>
