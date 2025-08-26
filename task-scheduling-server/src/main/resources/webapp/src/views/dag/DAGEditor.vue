<template>
  <div class="dag-editor-container">
    <!-- 侧边栏：预定义任务列表 -->
    <aside class="task-palette">
      <h3>预定义任务</h3>
      <div
        v-for="task in predefinedTasks"
        :key="task.id"
        class="task-node-template"
        draggable="true"
        @dragstart="onDragStart($event, task)"
      >
        {{ task.name }}
      </div>
    </aside>

    <!-- 主画布：DAG工作区 -->
    <div class="flow-canvas" @dragover.prevent @drop="onDrop">
      <VueFlow v-model="elements" @node-double-click="onNodeDoubleClick">
        <MiniMap />
        <Controls />
      </VueFlow>
    </div>

    <!-- 弹窗：编辑任务详情 -->
    <div v-if="editingNode" class="modal-overlay">
      <div class="modal">
        <h3>编辑任务: {{ editingNode.data.label }}</h3>
        <label>
          任务名称:
          <input type="text" v-model="editingNode.data.label" />
        </label>
        <label>
          描述:
          <textarea v-model="editingNode.data.description"></textarea>
        </label>
        <div class="modal-actions">
          <button @click="saveNodeChanges">保存</button>
          <button @click="cancelEdit">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { VueFlow, useVueFlow } from '@vue-flow/core';
import { MiniMap } from '@vue-flow/minimap';
import { Controls } from '@vue-flow/controls';

const { onPaneReady, onConnect, addNodes, getElements, removeNodes, removeEdges } = useVueFlow();

const elements = ref([]);
const predefinedTasks = ref([
  { id: 'extract', name: '数据提取', description: '从数据库中提取数据' },
  { id: 'transform', name: '数据转换', description: '清洗和转换数据' },
  { id: 'load', name: '数据加载', description: '加载数据到目标系统' },
]);
const editingNode = ref(null);
let taskIdCounter = 0;

onPaneReady(({ panOnDrag }) => panOnDrag.value = true);

onConnect((params) => {
  elements.value.push({ ...params, type: 'step' });
});

const onDragStart = (event, task) => {
  event.dataTransfer.setData('application/vueflow', JSON.stringify(task));
  event.dataTransfer.effectAllowed = 'move';
};

const onDrop = (event) => {
  const task = JSON.parse(event.dataTransfer.getData('application/vueflow'));
  const position = { x: event.clientX, y: event.clientY };

  const newNode = {
    id: `task_${taskIdCounter++}`,
    label: task.name,
    position,
    type: 'default',
    data: {
      label: task.name,
      description: task.description,
    },
  };
  addNodes(newNode);
};

const onNodeDoubleClick = ({ node }) => {
  editingNode.value = node;
};

const saveNodeChanges = () => {
  if (editingNode.value) {
    // 这里更新了节点的数据，但VueFlow已经自动处理了响应式更新
    editingNode.value = null;
  }
};

const cancelEdit = () => {
  editingNode.value = null;
};
</script>

<style scoped>
.dag-editor-container {
  display: flex;
  height: 100vh;
}
.task-palette {
  width: 250px;
  background: #f0f2f5;
  padding: 15px;
  border-right: 1px solid #ddd;
}
.task-palette h3 {
  margin-top: 0;
  border-bottom: 1px solid #ccc;
  padding-bottom: 10px;
}
.task-node-template {
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 5px;
  background: #fff;
  border: 1px solid #ccc;
  cursor: grab;
  text-align: center;
}
.flow-canvas {
  flex: 1;
}
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
.modal label {
  display: block;
  margin-bottom: 10px;
}
.modal input, .modal textarea {
  width: 100%;
  padding: 8px;
  margin-top: 5px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.modal-actions {
  text-align: right;
  margin-top: 20px;
}
</style>
