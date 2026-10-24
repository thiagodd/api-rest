import type { ComponentType } from 'react'
import meta from 'virtual:conteudo-meta'

export interface Referencia {
  titulo: string
  url?: string
}

export interface Frontmatter {
  id: string
  titulo: string
  descricao: string
  status?: 'esqueleto' | 'rascunho' | 'revisao' | 'pronta'
  referencias?: Referencia[]
}

export interface ModuloMdx {
  default: ComponentType<{ components?: Record<string, unknown> }>
  frontmatter: Frontmatter
}

const modulos = import.meta.glob<ModuloMdx>('../content/**/*.mdx')

const cache = new Map<string, ModuloMdx>()

/** Carrega o MDX de uma página (um chunk por página). */
export async function carregarPagina(arquivo: string): Promise<ModuloMdx> {
  const emCache = cache.get(arquivo)
  if (emCache) return emCache
  const carregador = modulos[`../content/${arquivo}`]
  if (!carregador) throw new Error(`MDX não encontrado: ${arquivo}`)
  const modulo = await carregador()
  cache.set(arquivo, modulo)
  return modulo
}

export function paginaEmCache(arquivo: string): ModuloMdx | undefined {
  return cache.get(arquivo)
}

export function metaPagina(id: string) {
  return meta[id]
}

export { meta }
