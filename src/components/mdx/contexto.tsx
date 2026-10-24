import { createContext, useContext } from 'react'
import type { Frontmatter } from '@/lib/conteudo'

/** Página que está sendo renderizada: usado por References, Checklist e Quiz. */
export const PaginaContext = createContext<{ chave: string; frontmatter?: Partial<Frontmatter> }>({ chave: 'avulsa' })

export function usePagina() {
  return useContext(PaginaContext)
}
