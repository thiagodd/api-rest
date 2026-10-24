import { Charset, Index } from 'flexsearch'
import { paginaPorId, type PaginaPlana } from './navegacao'

interface Secao {
  pagina: string
  ancora: string
  titulo: string
  texto: string
}

export interface ResultadoBusca {
  pagina: PaginaPlana
  ancora: string
  titulo: string
  trecho: string
}

let secoes: Secao[] = []
let indiceTitulos: Index | null = null
let indiceTextos: Index | null = null
let carregando: Promise<void> | null = null

/** Remove acentos e passa para minúsculas, preservando o comprimento do texto. */
export function normalizar(texto: string) {
  return texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
}

/** Carrega o texto das páginas (chunk separado) e monta o índice na primeira busca. */
export function carregarIndiceBusca(): Promise<void> {
  carregando ??= import('virtual:conteudo-busca').then(({ default: dados }) => {
    secoes = dados
    const opcoes = { tokenize: 'forward' as const, encoder: Charset.LatinBalance, cache: 50 }
    indiceTitulos = new Index(opcoes)
    indiceTextos = new Index(opcoes)
    secoes.forEach((s, i) => {
      const pagina = paginaPorId(s.pagina)
      const tituloCompleto = s.ancora ? s.titulo : `${s.pagina} ${s.titulo} ${pagina?.descricao ?? ''}`
      indiceTitulos!.add(i, tituloCompleto)
      indiceTextos!.add(i, s.texto)
    })
  })
  return carregando
}

function trecho(texto: string, termos: string[]): string {
  if (!texto) return ''
  const normal = normalizar(texto)
  const posicao = termos.map((t) => normal.indexOf(t)).filter((i) => i >= 0).sort((a, b) => a - b)[0]
  if (posicao === undefined) return texto.slice(0, 140)
  const inicio = Math.max(0, posicao - 50)
  return `${inicio > 0 ? '…' : ''}${texto.slice(inicio, inicio + 160).trim()}…`
}

export function pesquisar(consulta: string, limite = 30): ResultadoBusca[] {
  if (!indiceTitulos || !indiceTextos) return []
  const termos = normalizar(consulta).split(/\s+/).filter((t) => t.length > 1)
  const ids = [
    ...(indiceTitulos.search(consulta, { limit: limite }) as number[]),
    ...(indiceTextos.search(consulta, { limit: limite }) as number[]),
  ]
  const vistos = new Set<number>()
  const resultados: ResultadoBusca[] = []
  for (const id of ids) {
    if (vistos.has(id)) continue
    vistos.add(id)
    const secao = secoes[id]
    const pagina = paginaPorId(secao.pagina)
    if (!pagina) continue
    resultados.push({
      pagina,
      ancora: secao.ancora,
      titulo: secao.ancora ? secao.titulo : pagina.titulo,
      trecho: trecho(secao.ancora ? secao.texto : pagina.descricao, termos),
    })
    if (resultados.length >= limite) break
  }
  return resultados
}
