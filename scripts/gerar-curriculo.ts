/**
 * Lê planejamento/CURRICULO.md e gera:
 *   - src/content/navigation.ts   (só com --forcar-navegacao, se já existir)
 *   - src/content/<parte>/<pagina>.mdx   (nunca sobrescreve um arquivo existente)
 *   - planejamento/PROGRESSO.md   (só se não existir)
 *
 * Uso: npm run gerar:curriculo [-- --forcar-navegacao]
 */
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs'
import { dirname, join, relative } from 'node:path'
import { fileURLToPath } from 'node:url'

const raiz = join(dirname(fileURLToPath(import.meta.url)), '..')
const caminhoCurriculo = join(raiz, 'planejamento/CURRICULO.md')
const pastaConteudo = join(raiz, 'src/content')
const caminhoNavegacao = join(pastaConteudo, 'navigation.ts')
const caminhoProgresso = join(raiz, 'planejamento/PROGRESSO.md')

const forcarNavegacao = process.argv.includes('--forcar-navegacao')

type Nivel = 'basico' | 'intermediario' | 'avancado'
type Prioridade = 'essencial' | 'importante' | 'complementar'

interface Referencia {
  titulo: string
  url?: string
}

interface PaginaCurriculo {
  id: string
  titulo: string
  slug: string
  nivel: Nivel
  prioridade: Prioridade
  descricao: string
  descricaoMd: string
  preRequisitos: string[]
  preRequisitosTexto: string
  topicos: string
  diagramas: string
  codigo: string
  referencias: Referencia[]
}

interface ModuloCurriculo {
  id: string
  titulo: string
  paginas: PaginaCurriculo[]
}

interface ParteCurriculo {
  numero: number | null
  titulo: string
  slug: string
  pasta: string
  icone: string
  modulos: ModuloCurriculo[]
}

const icones: Record<string, string> = {
  '1': 'Rocket',
  '2': 'Network',
  '3': 'Leaf',
  '4': 'Database',
  '5': 'FlaskConical',
  '6': 'PenTool',
  '7': 'FileText',
  '8': 'Workflow',
  '9': 'Zap',
  '10': 'Plug',
  '11': 'ShieldCheck',
  '12': 'Activity',
  '13': 'Gauge',
  '14': 'Container',
  '15': 'Boxes',
  '16': 'Store',
  apendices: 'BookOpen',
}

const niveis: Record<string, Nivel> = {
  Básico: 'basico',
  Intermediário: 'intermediario',
  Avançado: 'avancado',
}

const prioridades: Record<string, Prioridade> = {
  Essencial: 'essencial',
  Importante: 'importante',
  Complementar: 'complementar',
}

const palavrasFinaisDescartaveis = new Set(['e', 'de', 'do', 'da', 'dos', 'das', 'o', 'a', 'os', 'as', 'com', 'para', 'em', 'no', 'na', 'por'])

function slugify(texto: string, max = 60): string {
  const base = texto
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[`*]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
  if (base.length <= max) return base
  const partes = base.slice(0, max + 1).split('-')
  partes.pop()
  while (partes.length > 1 && palavrasFinaisDescartaveis.has(partes[partes.length - 1])) partes.pop()
  return partes.join('-')
}

/** Remove marcação markdown simples para uso em texto puro (títulos, descrições). */
function textoPuro(md: string): string {
  return md
    .replace(/\*\*([^*]+)\*\*/g, '$1')
    .replace(/\*([^*]+)\*/g, '$1')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/\s+/g, ' ')
    .trim()
}

/** Escapa caracteres que o MDX interpretaria como JSX/expressão, preservando code spans. */
function escaparMdx(md: string): string {
  return md
    .split(/(`[^`]*`)/g)
    .map((trecho) => (trecho.startsWith('`') ? trecho : trecho.replace(/[{}]/g, (c) => `\\${c}`).replace(/</g, '&lt;')))
    .join('')
}

function capitalizar(texto: string): string {
  return texto.replace(/^([*_]*)(\p{Ll})/u, (_, prefixo: string, letra: string) => prefixo + letra.toUpperCase())
}

function parseReferencias(texto: string): Referencia[] {
  const limpo = texto.trim()
  if (!limpo || limpo === '—' || limpo === '-') return []
  return limpo
    .split(/\s+·\s+/)
    .map((item) => item.trim())
    .filter((item) => item && !/^p[áa]ginas? de origem\.?$/i.test(item))
    .map((item) => {
      const url = item.match(/https?:\/\/\S+/)?.[0]?.replace(/[.,;)]+$/, '')
      const resto = textoPuro(item.replace(/https?:\/\/\S+/, '').replace(/[:–-]\s*$/, '')).replace(/\.$/, '')
      if (url) return { titulo: !resto ? tituloDeUrl(url) : /^\(.*\)$/.test(resto) ? `${tituloDeUrl(url)} ${resto}` : resto, url }
      return { titulo: resto }
    })
}

function tituloDeUrl(url: string): string {
  const u = new URL(url)
  const host = u.hostname.replace(/^www\./, '')
  const rfc = url.match(/rfc(\d{3,5})/i)
  if (rfc && /rfc-editor|ietf/.test(host)) return `RFC ${rfc[1]}`
  const caminho = decodeURIComponent(u.pathname).replace(/\/+$/, '').replace(/\.html?$/, '')
  const ultimo = caminho.split('/').filter(Boolean).slice(-2).join(' / ')
  return ultimo ? `${host}: ${ultimo}` : host
}

function parseCurriculo(md: string): ParteCurriculo[] {
  const linhas = md.split('\n')
  const partes: ParteCurriculo[] = []
  let parte: ParteCurriculo | null = null
  let modulo: ModuloCurriculo | null = null
  let pagina: Partial<PaginaCurriculo> & { campos?: Record<string, string> } | null = null
  let campoAtual: string | null = null

  const fecharPagina = () => {
    if (!pagina || !modulo) return
    const campos = pagina.campos ?? {}
    const pre = campos['Pré-requisitos'] ?? ''
    const completa: PaginaCurriculo = {
      id: pagina.id!,
      titulo: pagina.titulo!,
      slug: slugify(pagina.titulo!),
      nivel: pagina.nivel!,
      prioridade: pagina.prioridade!,
      descricaoMd: capitalizar((campos['Por que importa'] ?? '').trim()),
      descricao: capitalizar(textoPuro(campos['Por que importa'] ?? '')),
      preRequisitos: [...new Set(pre.match(/\b\d{2}\.\d{2}\b/g) ?? [])],
      preRequisitosTexto: textoPuro(pre).replace(/\.$/, ''),
      topicos: (campos['Tópicos'] ?? '').trimEnd(),
      diagramas: (campos['Diagramas'] ?? '').trimEnd(),
      codigo: (campos['Código'] ?? '').trimEnd(),
      referencias: parseReferencias(campos['Referências'] ?? ''),
    }
    modulo.paginas.push(completa)
    pagina = null
    campoAtual = null
  }

  for (const linha of linhas) {
    const parteMatch = linha.match(/^# Parte (\d+): (.+)$/)
    if (parteMatch || linha.trim() === '# Apêndices') {
      fecharPagina()
      const numero = parteMatch ? Number(parteMatch[1]) : null
      const titulo = parteMatch ? parteMatch[2].trim() : 'Apêndices'
      const tituloLimpo = titulo.replace(/\s*🔒\s*/g, '').trim()
      const ordem = numero ?? partes.length + 1
      parte = {
        numero,
        titulo: tituloLimpo,
        slug: slugify(tituloLimpo, 40),
        pasta: `${String(ordem).padStart(2, '0')}-${slugify(tituloLimpo, 40)}`,
        icone: icones[numero ? String(numero) : 'apendices'] ?? 'BookOpen',
        modulos: [],
      }
      partes.push(parte)
      modulo = null
      continue
    }
    if (!parte) continue

    const moduloMatch = linha.match(/^## (M\d{2}): (.+)$/)
    if (moduloMatch) {
      fecharPagina()
      modulo = { id: moduloMatch[1], titulo: textoPuro(moduloMatch[2]), paginas: [] }
      parte.modulos.push(modulo)
      continue
    }
    if (/^## /.test(linha) || /^---\s*$/.test(linha)) {
      fecharPagina()
      if (/^## /.test(linha)) modulo = null
      continue
    }

    const paginaMatch = linha.match(/^#### (\d{2}\.\d{2}) · (.+)$/)
    if (paginaMatch) {
      fecharPagina()
      pagina = { id: paginaMatch[1], titulo: textoPuro(paginaMatch[2]), campos: {} }
      continue
    }
    if (!pagina) continue

    const nivelMatch = linha.match(/^`(Básico|Intermediário|Avançado)` · `(Essencial|Importante|Complementar)`/)
    if (nivelMatch) {
      pagina.nivel = niveis[nivelMatch[1]]
      pagina.prioridade = prioridades[nivelMatch[2]]
      continue
    }

    const campoMatch = linha.match(/^- \*\*([^*]+):\*\*\s*(.*)$/)
    if (campoMatch) {
      campoAtual = campoMatch[1]
      pagina.campos![campoAtual] = campoMatch[2]
      continue
    }
    if (campoAtual && /^\s{2,}\S/.test(linha)) {
      const anterior = pagina.campos![campoAtual]
      pagina.campos![campoAtual] = `${anterior}\n${linha.slice(2)}`
    }
  }
  fecharPagina()
  return partes
}

// ---------- navigation.ts ----------

function gerarNavegacao(partes: ParteCurriculo[]): string {
  const dados = partes.map((p) => ({
    numero: p.numero,
    titulo: p.titulo,
    slug: p.slug,
    pasta: p.pasta,
    icone: p.icone,
    modulos: p.modulos.map((m) => ({
      id: m.id,
      titulo: m.titulo,
      paginas: m.paginas.map((pg) => ({
        id: pg.id,
        slug: pg.slug,
        titulo: pg.titulo,
        descricao: pg.descricao,
        nivel: pg.nivel,
        prioridade: pg.prioridade,
        preRequisitos: pg.preRequisitos,
        ...(pg.preRequisitos.length === 0 && pg.preRequisitosTexto && !/^nenhum$/i.test(pg.preRequisitosTexto)
          ? { preRequisitosNota: pg.preRequisitosTexto }
          : {}),
      })),
    })),
  }))

  return `/**
 * Fonte única da navegação do site: partes → módulos → páginas.
 *
 * Gerado a partir de planejamento/CURRICULO.md por \`npm run gerar:curriculo\`.
 * Depois de gerado, este arquivo é a referência: edite aqui (e no CURRICULO.md)
 * quando uma página mudar de título, nível ou prioridade. Para regenerar do zero,
 * use \`npm run gerar:curriculo -- --forcar-navegacao\`.
 *
 * O conteúdo de cada página fica em src/content/<pasta da parte>/<id>-<slug>.mdx.
 */

export type Nivel = 'basico' | 'intermediario' | 'avancado'
export type Prioridade = 'essencial' | 'importante' | 'complementar'

export interface PaginaNav {
  /** ID \`MM.PP\` do currículo (módulo.página) */
  id: string
  slug: string
  titulo: string
  descricao: string
  nivel: Nivel
  prioridade: Prioridade
  /** IDs das páginas que devem ser lidas antes */
  preRequisitos: string[]
  /** Pré-requisito descrito em texto quando não é uma lista de IDs */
  preRequisitosNota?: string
}

export interface ModuloNav {
  id: string
  titulo: string
  paginas: PaginaNav[]
}

export interface ParteNav {
  /** null para os apêndices */
  numero: number | null
  titulo: string
  slug: string
  /** Pasta em src/content */
  pasta: string
  /** Nome de um ícone do lucide-react */
  icone: string
  modulos: ModuloNav[]
}

export const navigation: ParteNav[] = ${JSON.stringify(dados, null, 2)}
`
}

// ---------- MDX esqueleto ----------

function yamlString(valor: string): string {
  return JSON.stringify(valor)
}

function listaMd(texto: string): string {
  const t = texto.trim()
  if (!t) return ''
  // Campos com sublista: o texto da primeira linha costuma estar vazio
  const linhas = t.split('\n')
  if (linhas[0].startsWith('- ')) return escaparMdx(t)
  if (linhas.length === 1) return escaparMdx(t)
  return escaparMdx(`${linhas[0]}\n\n${linhas.slice(1).join('\n')}`)
}

function gerarMdx(p: PaginaCurriculo): string {
  const refs = p.referencias.length
    ? `referencias:\n${p.referencias
        .map((r) => `  - titulo: ${yamlString(r.titulo)}${r.url ? `\n    url: ${yamlString(r.url)}` : ''}`)
        .join('\n')}`
    : 'referencias: []'

  const semNada = (t: string) => !t.trim() || /^nenhum\.?$/i.test(t.trim())

  return `---
id: ${yamlString(p.id)}
titulo: ${yamlString(p.titulo)}
descricao: ${yamlString(p.descricao)}
status: esqueleto
${refs}
---

{/* Página esqueleto gerada a partir do CURRICULO.md. Siga planejamento/GUIA-DE-ESCRITA.md
    e remova cada <Draft> à medida que a seção for escrita. */}

## O problema

<Draft title="Por que importa (do currículo)">
${escaparMdx(p.descricaoMd)}

Comece com o problema que o conceito resolve e uma analogia do mundo real.
</Draft>

## O conceito

<Draft title="Tópicos previstos">
${listaMd(p.topicos)}
</Draft>

## Diagrama

<Draft title="Diagramas previstos">
${semNada(p.diagramas) ? 'Nenhum diagrama previsto no currículo. Avalie se um fluxo ajuda; se não, remova esta seção.' : listaMd(p.diagramas)}
</Draft>

## Na prática

<Draft title="Código previsto">
${semNada(p.codigo) ? 'Nenhum código previsto no currículo. Use requisições HTTP reais do Bazar quando fizer sentido.' : listaMd(p.codigo)}
</Draft>

## Erros comuns

<Draft>
Design ruim vs bom com \`<BadVsGood>\` e os erros mais frequentes.
</Draft>

## Boas práticas

<Draft>
Lista curta e acionável de boas práticas.
</Draft>

## Por baixo dos panos

<Draft>
Opcional: use \`<DeepDive>\` para o funcionamento interno. Remova a seção se não fizer sentido.
</Draft>

## No projeto Bazar

<Draft>
Opcional: use \`<ProjectStep>\` quando esta página construir uma parte do projeto final.
</Draft>

## Resumo

<Draft>
Resumo em poucos itens, um \`<Exercise>\` e um \`<Quiz>\`.
</Draft>

## Referências

<References />
`
}

// ---------- PROGRESSO.md ----------

function gerarProgresso(partes: ParteCurriculo[]): string {
  const total = partes.reduce((n, p) => n + p.modulos.reduce((m, mod) => m + mod.paginas.length, 0), 0)
  const linhas: string[] = [
    '# Progresso do conteúdo',
    '',
    '> Status de cada página do site. A fase 3 atualiza este arquivo ao escrever cada página.',
    '> Status possíveis: **esqueleto** (gerado na fase 2) · **rascunho** (escrita em andamento) · **revisão** (escrita, falta revisar) · **pronta**.',
    '> O status também fica no frontmatter de cada MDX (`status:`), e o site mostra um aviso nas páginas que ainda não estão prontas.',
    '',
    `**Total:** ${total} páginas · **esqueleto:** ${total} · **rascunho:** 0 · **revisão:** 0 · **pronta:** 0`,
    '',
  ]
  for (const p of partes) {
    linhas.push(`## ${p.numero ? `Parte ${p.numero}: ` : ''}${p.titulo}`, '')
    for (const m of p.modulos) {
      linhas.push(`### ${m.id}: ${m.titulo}`, '', '| ID | Página | Nível | Prioridade | Arquivo | Status |', '|---|---|---|---|---|---|')
      for (const pg of m.paginas) {
        const arquivo = `src/content/${p.pasta}/${pg.id.replace('.', '-')}-${pg.slug}.mdx`
        linhas.push(`| ${pg.id} | ${pg.titulo} | ${pg.nivel} | ${pg.prioridade} | \`${arquivo}\` | esqueleto |`)
      }
      linhas.push('')
    }
  }
  return linhas.join('\n')
}

// ---------- execução ----------

const partes = parseCurriculo(readFileSync(caminhoCurriculo, 'utf8'))
const paginas = partes.flatMap((p) => p.modulos.flatMap((m) => m.paginas))

const problemas: string[] = []
const slugs = new Set<string>()
for (const p of partes) {
  for (const pg of p.modulos.flatMap((m) => m.paginas)) {
    if (!pg.nivel || !pg.prioridade) problemas.push(`${pg.id}: sem nível/prioridade`)
    const chave = `${p.slug}/${pg.slug}`
    if (slugs.has(chave)) problemas.push(`${pg.id}: slug duplicado ${chave}`)
    slugs.add(chave)
  }
}
const ids = new Set(paginas.map((p) => p.id))
for (const pg of paginas) for (const pre of pg.preRequisitos) if (!ids.has(pre)) problemas.push(`${pg.id}: pré-requisito inexistente ${pre}`)
if (problemas.length) {
  console.error(problemas.join('\n'))
  process.exit(1)
}

mkdirSync(pastaConteudo, { recursive: true })

if (!existsSync(caminhoNavegacao) || forcarNavegacao) {
  writeFileSync(caminhoNavegacao, gerarNavegacao(partes))
  console.log(`✔ ${relative(raiz, caminhoNavegacao)}`)
} else {
  console.log(`• ${relative(raiz, caminhoNavegacao)} já existe (use --forcar-navegacao para regenerar)`)
}

let criados = 0
for (const p of partes) {
  for (const pg of p.modulos.flatMap((m) => m.paginas)) {
    const arquivo = join(pastaConteudo, p.pasta, `${pg.id.replace('.', '-')}-${pg.slug}.mdx`)
    if (existsSync(arquivo)) continue
    mkdirSync(dirname(arquivo), { recursive: true })
    writeFileSync(arquivo, gerarMdx(pg))
    criados++
  }
}
console.log(`✔ ${criados} MDX criados (${paginas.length - criados} já existiam)`)

if (!existsSync(caminhoProgresso)) {
  writeFileSync(caminhoProgresso, gerarProgresso(partes))
  console.log(`✔ ${relative(raiz, caminhoProgresso)}`)
} else {
  console.log(`• ${relative(raiz, caminhoProgresso)} já existe (não foi alterado)`)
}

console.log(`${partes.length} partes · ${partes.reduce((n, p) => n + p.modulos.length, 0)} módulos · ${paginas.length} páginas`)
