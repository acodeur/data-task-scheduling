declare global {
  interface String {
    format(args: Record<string, any>): string;
  }
}

String.prototype.format = function (args: Record<string, any>) {
  return this.replace(/\$\{(\w+)\}/g, (match, key) => args[key] || '')
}

export * from './constants'
export * from './icons'
export * from './api'
