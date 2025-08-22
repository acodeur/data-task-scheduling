export interface IForm {
  formItems: IFormItem[]
  formProps: Record<string, any>
  rowProps?: Record<string, any>
}
export interface IFormItem {
  field: string // 字段名
  label: string // 标签
  component: ComponentType // 组件类型
  componentProps?: Record<string, any> // 组件绑定属性
  options?: { label: string; value: any }[] // 下拉选项（用于Select）
  colProps?: Record<string, any> // 栅格列属性
  required?: boolean // 是否必填
  rules?: any[] // 校验规则
  slot?: string // 自定义slot名称
  hidden?: boolean // 是否隐藏
}

export type ComponentType = 'input' | 'select' | 'datepicker' | 'custom'
