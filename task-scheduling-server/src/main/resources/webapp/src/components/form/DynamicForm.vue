<template>
  <el-form ref="elFormRef" :model="formModel" :rules="formRules" v-bind="iForm.formProps">
    <el-row v-bind="iForm.rowProps ?? { gutter: 200 }">
      <template v-for="item in iForm.formItems" :key="item.field">
        <el-col v-if="!item.hidden" v-bind="item.colProps ?? { span: 8 }">
          <el-form-item :label="item.label" :prop="item.field">
            <!-- slot优先 -->
            <slot
              v-if="item.slot"
              :name="item.slot"
              :form-item="item"
              :form-data="formModel"
            ></slot>
            <!-- 渲染内置组件 -->
            <component
              v-else
              :is="getComponent(item)"
              v-model="formModel[item.field]"
              v-bind="item.componentProps"
            >
              <!-- 处理Select选项 -->
              <template v-if="item.component === 'select'">
                <el-option
                  v-for="op in item.options"
                  :key="op.value"
                  :label="op.label"
                  :value="op.value"
                />
              </template>
            </component>
          </el-form-item>
        </el-col>
      </template>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { IForm, IFormItem } from './type'
import type { FormValidateCallback } from 'element-plus'
import { ElForm, ElInput, ElSelect, ElDatePicker } from 'element-plus'

const props = defineProps<{
  iForm: IForm
  initModel?: Record<string, any>
}>()

const elFormRef = ref<InstanceType<typeof ElForm>>()
const formModel = reactive<Record<string, any>>({})
const formRules = reactive<Record<string, any>>({})
/** 初始化表单数据与校验规则 */
const initForm = () => {
  props.iForm.formItems.forEach((item: IFormItem) => {
    formModel[item.field] = props.initModel?.[item.field] ?? ''
    if (item.required || item.rules) {
      formRules[item.field] = item.rules || {
        required: true,
        message: `${item.label}不能为空`,
        trigger: 'blur',
      }
    }
  })
}
watch(() => [props.iForm.formItems, props.initModel], initForm, {
  immediate: true,
})

/** 组件映射 */
const getComponent = (item: IFormItem) => {
  switch (item.component) {
    case 'input':
      return ElInput
    case 'select':
      return ElSelect
    case 'datepicker':
      return ElDatePicker
    case 'custom':
      return item.slot || 'div'
    default:
      return ElInput
  }
}

/** 对外暴露方法 */
defineExpose({
  elFormRef,
  formModel,
  formRules,
  validate: (callback?: FormValidateCallback) => elFormRef.value?.validate(callback),
  resetFields: () => elFormRef.value?.resetFields(),
})
</script>

<style lang="less" scoped></style>
