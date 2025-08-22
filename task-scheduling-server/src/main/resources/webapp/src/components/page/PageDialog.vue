<template>
  <div class="page-dialog">
    <el-dialog
      v-model="visiable"
      :title="type === 'add' ? addTitle : editTitle"
      v-bind="dialogProps"
    >
      <dynamic-form
        ref="formRef"
        :i-form="dialogFormConfig.ui"
        :init-model="dialogFormConfig.model ?? {}"
      >
        <template v-for="item in slotItems" :key="item" v-slot:[item]="{ formItem, formData }">
          <slot :name="item" :form-item="formItem" :form-data="formData"></slot>
        </template>
      </dynamic-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="visiable = false">{{ cancelTxt ?? '取消' }}</el-button>
          <el-button type="primary" @click="handleConfirm">{{
            type === 'add' ? (addConfirmTxt ?? '保存') : (editConfirmTxt ?? '确认')
          }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, toRefs } from 'vue'
import type { IPageDialogProps, DialogType } from './type'

const emits = defineEmits(['handleConfirm'])
const props = defineProps<IPageDialogProps>()
const { header, dialogProps, footer, formConfig } = toRefs(props.config)
const { addTitle, editTitle } = header.value
const { cancelTxt, addConfirmTxt, editConfirmTxt } = footer.value
const visiable = ref(false)
const type = ref<DialogType>()
const formRef = ref()
const dialogFormConfig = computed(() => {
  formConfig.value.ui.formItems
    .filter((item) => item.belong)
    .forEach((item) => {
      item.hidden = item.belong !== type.value
    })
  return formConfig.value
})
const slotItems = computed(() => {
  const slots = formConfig.value.ui.formItems.filter((item) => item.slot).map((item) => item.slot)
  return [...new Set(slots)]
})

function handleConfirm() {
  formRef.value.validate((valid: boolean) => {
    if (!valid) {
      ElMessage.error('请填写正确的表单信息')
    } else {
      emits('handleConfirm', formRef.value.formModel, type.value)
    }
  })
}

defineExpose({ visiable, type, formRef })
</script>

<style lang="less" scoped>
.page-dialog {
  padding: 5px;
  background-color: #f0f0f0;
  border-radius: 8px;
}
</style>
