import { createJSONStorage } from 'zustand/middleware'

/** localStorage com tolerância a falhas (modo privado, armazenamento bloqueado). */
export const armazenamentoSeguro = createJSONStorage(() => {
  try {
    const teste = '__bazar_teste__'
    localStorage.setItem(teste, teste)
    localStorage.removeItem(teste)
    return localStorage
  } catch {
    const memoria = new Map<string, string>()
    return {
      getItem: (k: string) => memoria.get(k) ?? null,
      setItem: (k: string, v: string) => void memoria.set(k, v),
      removeItem: (k: string) => void memoria.delete(k),
    }
  }
})

export const PREFIXO = 'bazar-docs:'
