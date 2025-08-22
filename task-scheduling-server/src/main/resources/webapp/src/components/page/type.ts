import type { IForm, IFormItem } from '@/components/form/type'

// PageContent
interface IPageContentConfig {
  pageName: string
  header: {
    title: string
    btnTxt: string
  }
  tableProps?: Record<string, any>
  columnList: Record<string, any>[]
  pagination: {
    currentPage: number
    pageSize: number
  }
}
interface IPageContentModel {
  dataList: any[]
  totalCount: number
}
interface IPageContentProps {
  config: IPageContentConfig
  model: IPageContentModel
}

// PageDialog
type DialogType = 'add' | 'edit'
interface IDialogFormItem extends IFormItem {
  belong?: DialogType
}
interface IDialogForm {
    formItems: IDialogFormItem[]
    formProps: Record<string, any>
    rowProps?: Record<string, any>
}
interface IPageDialogConfig {
  header: { addTitle: string; editTitle: string }
  dialogProps: Record<string, any>
  footer: { cancelTxt?: string; addConfirmTxt?: string; editConfirmTxt?: string }
  formConfig: { ui: IDialogForm; model?: Record<string, any> }
}
interface IPageDialogProps {
  config: IPageDialogConfig
}

// PageSearch
interface IPageSearchProps {
  config: IForm
  model?: Record<string, any>
}

export type {
  IPageContentConfig,
  IPageContentModel,
  IPageContentProps,
  DialogType,
  IDialogForm,
  IPageDialogConfig,
  IPageDialogProps,
  IPageSearchProps,
}
