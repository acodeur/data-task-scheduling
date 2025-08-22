<template>
  <div class="page-search">
    <div class="search-form">
      <dynamic-form ref="formRef" :i-form="config" :init-model="model">
        <template v-for="item in slotItems" :key="item" v-slot:[item]="{ formItem, formData }">
          <slot :name="item" :form-item="formItem" :form-data="formData"></slot>
        </template>
      </dynamic-form>
    </div>
    <div class="search-btns">
      <el-button @click="handleReset" icon="Refresh">重置</el-button>
      <el-button type="primary" @click="handleSearch" icon="Search">查询</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { IPageSearchProps } from './type'

const props = defineProps<IPageSearchProps>()
const emits = defineEmits(['handleReset', 'handleSearch'])

const formRef = ref()
const slotItems = computed(() => {
  const slots = props.config.formItems.filter((item) => item.slot).map((item) => item.slot)
  return [...new Set(slots)]
})

const handleReset = () => {
  formRef.value?.resetFields()
  emits('handleReset')
}

const handleSearch = () => {
  formRef.value
    ?.validate()
    .then(() => {
      emits('handleSearch', formRef.value.formModel)
    })
    .catch(() => {
      console.log('❌ 检索表单校验失败：', formRef.value.formModel)
    })
}
</script>

<style lang="less" scoped>
.page-search {
  padding: 10px;
  background-color: #f0f0f0;
  border-radius: 8px;

  .search-btns {
    text-align: right;
    margin-bottom: 10px;
  }
}
</style>
