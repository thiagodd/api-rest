/**
 * Plugin do Vite que lê os MDX de src/content e expõe dois módulos virtuais:
 *
 *   virtual:conteudo-meta  → tempo de leitura, status e referências de cada página (carregado sempre)
 *   virtual:conteudo-busca → texto puro de cada seção, para a busca global (carregado sob demanda)
 */
import GithubSlugger from 'github-slugger'
import matter from 'gray-matter'
import { readdirSync, readFileSync, statSync } from 'node:fs'
import { join, resolve } from 'node:path'
import type { Plugin } from 'vite'

const PALAVRAS_POR_MINUTO = 200

const ID_META = 'virtual:conteudo-meta'
const ID_BUSCA = 'virtual:conteudo-busca'

interface Referencia {
  titulo: string
  url?: string
}

interface MetaPagina {
  minutos: number
  status: string
  referencias: Referencia[]
}

interface SecaoBusca {
  pagina: string
  ancora: string
  titulo: string
  texto: string
}

function listarMdx(pasta: string): string[] {
  return readdirSync(pasta).flatMap((nome) => {
    const caminho = join(pasta, nome)
    if (statSync(caminho).isDirectory()) return listarMdx(caminho)
    return nome.endsWith('.mdx') ? [caminho] : []
  })
}

/** Converte MDX em texto aproximado: sem frontmatter, JSX, comentários e marcação. */
export function mdxParaTexto(mdx: string): string {
  return mdx
    .replace(/\{\/\*[\s\S]*?\*\/\}/g, ' ')
    .replace(/^(import|export) .*$/gm, ' ')
    .replace(/```[^\n]*\n/g, ' ')
    .replace(/```/g, ' ')
    .replace(/<\/?[A-Za-z][\w.]*((?:\s+[\w-]+(?:=(?:"[^"]*"|'[^']*'|\{[^}]*\}))?)*)\s*\/?>/g, (_, atributos: string) => {
      // Mantém o texto de atributos legíveis (title, label, path...) para a busca
      const valores = [...(atributos ?? '').matchAll(/(?:title|label|path|summary)="([^"]*)"/g)].map((m) => m[1])
      return ` ${valores.join(' ')} `
    })
    .replace(/!?\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/[*_~`#>|]/g, ' ')
    .replace(/\\([{}])/g, '$1')
    .replace(/&lt;/g, '<')
    .replace(/\s+/g, ' ')
    .trim()
}

function tituloPuro(md: string): string {
  return md.replace(/[*_`]/g, '').trim()
}

function lerConteudo(pasta: string) {
  const meta: Record<string, MetaPagina> = {}
  const busca: SecaoBusca[] = []

  for (const arquivo of listarMdx(pasta)) {
    const { data, content } = matter(readFileSync(arquivo, 'utf8'))
    const id = String(data.id ?? '')
    if (!id) continue

    const textoCompleto = mdxParaTexto(content)
    const palavras = textoCompleto.split(' ').filter(Boolean).length
    meta[id] = {
      minutos: Math.max(1, Math.round(palavras / PALAVRAS_POR_MINUTO)),
      status: String(data.status ?? 'pronta'),
      referencias: Array.isArray(data.referencias) ? data.referencias : [],
    }

    // Uma entrada por seção (## e ###), com a mesma âncora gerada pelo rehype-slug
    const slugger = new GithubSlugger()
    let atual: SecaoBusca = { pagina: id, ancora: '', titulo: String(data.titulo ?? ''), texto: '' }
    const blocos: SecaoBusca[] = [atual]
    let emCodigo = false
    for (const linha of content.split('\n')) {
      if (linha.startsWith('```')) emCodigo = !emCodigo
      const cabecalho = !emCodigo && linha.match(/^(##|###) (.+)$/)
      if (cabecalho) {
        const titulo = tituloPuro(cabecalho[2])
        atual = { pagina: id, ancora: slugger.slug(titulo), titulo, texto: '' }
        blocos.push(atual)
      } else {
        atual.texto += `${linha}\n`
      }
    }
    for (const bloco of blocos) {
      bloco.texto = mdxParaTexto(bloco.texto)
      if (bloco.texto || bloco.ancora === '') busca.push(bloco)
    }
  }
  return { meta, busca }
}

export function conteudoPlugin(): Plugin {
  let pasta = ''
  return {
    name: 'bazar:conteudo',
    configResolved(config) {
      pasta = resolve(config.root, 'src/content')
    },
    resolveId(id) {
      if (id === ID_META || id === ID_BUSCA) return `\0${id}`
    },
    load(id) {
      if (id !== `\0${ID_META}` && id !== `\0${ID_BUSCA}`) return
      for (const arquivo of listarMdx(pasta)) this.addWatchFile(arquivo)
      const { meta, busca } = lerConteudo(pasta)
      return `export default ${JSON.stringify(id === `\0${ID_META}` ? meta : busca)}`
    },
    handleHotUpdate({ file, server }) {
      if (!file.endsWith('.mdx')) return
      for (const id of [ID_META, ID_BUSCA]) {
        const mod = server.moduleGraph.getModuleById(`\0${id}`)
        if (mod) server.moduleGraph.invalidateModule(mod)
      }
    },
  }
}
