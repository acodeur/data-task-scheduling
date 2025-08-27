<template>
  <div class="task-detail">
    <!-- 基本信息 -->
    <div class="detail-section">
      <h3>基本信息</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务名称">{{ task.name }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ getTaskTypeName(task.type) }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ task.category.name }}</el-descriptions-item>
        <el-descriptions-item label="版本">v{{ task.version }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ task.author }}</el-descriptions-item>
        <el-descriptions-item label="使用次数">{{ task.usage_count }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(task.created) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(task.updated) }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate
            v-if="task.rating"
            v-model="task.rating"
            disabled
            show-score
            text-color="#ff9900"
          />
          <span v-else>暂无评分</span>
        </el-descriptions-item>
        <el-descriptions-item label="可见性">
          <el-tag :type="task.isPublic ? 'success' : 'info'">
            {{ task.isPublic ? '公开' : '私有' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 描述 -->
    <div class="detail-section">
      <h3>任务描述</h3>
      <p class="description">{{ task.description || '暂无描述' }}</p>
    </div>

    <!-- 标签 -->
    <div class="detail-section">
      <h3>标签</h3>
      <div class="tags">
        <el-tag
          v-for="tag in task.tags"
          :key="tag"
          type="info"
          style="margin-right: 8px; margin-bottom: 8px;"
        >
          {{ tag }}
        </el-tag>
        <el-tag v-if="task.tags.length === 0" type="info">暂无标签</el-tag>
      </div>
    </div>

    <!-- 参数配置 -->
    <div class="detail-section">
      <h3>参数配置</h3>
      <el-table :data="task.parameters" style="width: 100%">
        <el-table-column prop="name" label="参数名" width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="required" label="必需" width="80">
          <template #default="{ row }">
            <el-tag :type="row.required ? 'danger' : 'info'" size="small">
              {{ row.required ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defaultValue" label="默认值" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" />
      </el-table>
      <div v-if="task.parameters.length === 0" class="empty-state">
        暂无参数配置
      </div>
    </div>

    <!-- 执行配置 -->
    <div class="detail-section">
      <h3>执行配置</h3>
      <el-card>
        <pre class="config-json">{{ JSON.stringify(task.config, null, 2) }}</pre>
      </el-card>
    </div>

    <!-- 操作按钮 -->
    <div class="detail-actions">
      <el-button type="primary" @click="$emit('edit', task)">
        <el-icon><Edit /></el-icon>
        编辑
      </el-button>
      <el-button @click="$emit('duplicate', task)">
        <el-icon><CopyDocument /></el-icon>
        复制
      </el-button>
      <el-button @click="handleTest">
        <el-icon><VideoPlay /></el-icon>
        测试运行
      </el-button>
      <el-button type="danger" @click="$emit('delete', task)">
        <el-icon><Delete /></el-icon>
        删除
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElDescriptions, ElDescriptionsItem, ElTag, ElRate, ElTable, ElTableColumn, ElCard, ElButton, ElIcon } from 'element-plus'
import { Edit, CopyDocument, VideoPlay, Delete } from '@element-plus/icons-vue'
import type { TaskTemplate, TaskType } from '@/types/task'
import dayjs from 'dayjs'

interface Props {
  task: TaskTemplate
}

defineProps<Props>()

const emit = defineEmits<{
  edit: [task: TaskTemplate]
  duplicate: [task: TaskTemplate]
  delete: [task: TaskTemplate]
}>()

const getTaskTypeName = (type: TaskType) => {
  const names: Record<TaskType, string> = {
    sql: 'SQL查询',
    script: '脚本执行',
    http: 'HTTP请求',
    file_transfer: '文件传输',
    data_sync: '数据同步',
    email: '邮件发送',
    custom: '自定义'
  }
  return names[type] || '未知'
}

const formatTime = (date: Date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const handleTest = () => {
  // 测试运行逻辑
  console.log('测试运行任务')
}
</script>

<style lang="less" scoped>
.task-detail {
  .detail-section {
    margin-bottom: 24px;

    h3 {
      margin: 0 0 16px 0;
      color: #303133;
      font-weight: 600;
      font-size: 16px;
      border-bottom: 2px solid #E4E7ED;
      padding-bottom: 8px;
    }

    .description {
      color: #606266;
      line-height: 1.6;
      margin: 0;
    }

    .tags {
      display: flex;
      flex-wrap: wrap;
    }

    .config-json {
      background-color: #F5F7FA;
      border: 1px solid #E4E7ED;
      border-radius: 4px;
      padding: 12px;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 12px;
      color: #606266;
      overflow-x: auto;
      margin: 0;
    }

    .empty-state {
      text-align: center;
      color: #909399;
      padding: 20px;
    }
  }

  .detail-actions {
    display: flex;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid #E4E7ED;
  }
}
</style>