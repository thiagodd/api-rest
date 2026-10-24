import { navigation, type ModuloNav, type Nivel, type PaginaNav, type ParteNav, type Prioridade } from '@/content/navigation'

export interface PaginaPlana extends PaginaNav {
  parte: ParteNav
  modulo: ModuloNav
  url: string
  /** Caminho do MDX relativo a src/content */
  arquivo: string
  indice: number
}

export const paginas: PaginaPlana[] = navigation.flatMap((parte) =>
  parte.modulos.flatMap((modulo) =>
    modulo.paginas.map((pagina) => ({
      ...pagina,
      parte,
      modulo,
      url: `/${parte.slug}/${pagina.slug}`,
      arquivo: `${parte.pasta}/${pagina.id.replace('.', '-')}-${pagina.slug}.mdx`,
      indice: 0,
    })),
  ),
)
paginas.forEach((p, i) => (p.indice = i))

const porId = new Map(paginas.map((p) => [p.id, p]))
const porUrl = new Map(paginas.map((p) => [p.url, p]))

export function paginaPorId(id: string): PaginaPlana | undefined {
  return porId.get(id)
}

export function paginaPorSlug(parteSlug: string, paginaSlug: string): PaginaPlana | undefined {
  return porUrl.get(`/${parteSlug}/${paginaSlug}`)
}

export function vizinhas(pagina: PaginaPlana) {
  return { anterior: paginas[pagina.indice - 1], proxima: paginas[pagina.indice + 1] }
}

export function paginasDaParte(parte: ParteNav): PaginaPlana[] {
  return paginas.filter((p) => p.parte === parte)
}

export function rotuloParte(parte: ParteNav): string {
  return parte.numero ? `Parte ${parte.numero}` : 'Apêndices'
}

export const rotulosNivel: Record<Nivel, string> = {
  basico: 'Básico',
  intermediario: 'Intermediário',
  avancado: 'Avançado',
}

export const rotulosPrioridade: Record<Prioridade, string> = {
  essencial: 'Essencial',
  importante: 'Importante',
  complementar: 'Complementar',
}

export const descricoesPrioridade: Record<Prioridade, string> = {
  essencial: 'Não pule.',
  importante: 'Pule só se tiver pressa; volte depois.',
  complementar: 'Pode pular sem perder a sequência.',
}

export const descricoesNivel: Record<Nivel, string> = {
  basico: 'Precisa saber para fazer qualquer API.',
  intermediario: 'Separa um CRUD de uma API profissional.',
  avancado: 'O que se espera de quem projeta e opera APIs em produção.',
}

export { navigation }
