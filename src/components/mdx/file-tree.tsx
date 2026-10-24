import { File, FileCode2, Folder } from 'lucide-react'
import { cn } from '@/lib/utils'

interface Linha {
  nome: string
  nivel: number
  pasta: boolean
  marca?: 'novo' | 'alterado' | 'removido'
  comentario?: string
}

/**
 * Converte a árvore indentada (2 espaços por nível) em linhas.
 * Pastas terminam com "/". Marcas: "(novo)", "(alterado)", "(removido)". Comentários após "#".
 */
function interpretar(arvore: string): Linha[] {
  return arvore
    .replace(/^\n+|\s+$/g, '')
    .split('\n')
    .filter((l) => l.trim())
    .map((bruta) => {
      const espacos = bruta.match(/^\s*/)?.[0].length ?? 0
      let resto = bruta.trim()
      let comentario: string | undefined
      const hash = resto.indexOf(' #')
      if (hash >= 0) {
        comentario = resto.slice(hash + 2).trim()
        resto = resto.slice(0, hash).trim()
      }
      const marca = resto.match(/\((novo|alterado|removido)\)\s*$/)?.[1] as Linha['marca']
      if (marca) resto = resto.replace(/\s*\((novo|alterado|removido)\)\s*$/, '')
      return { nome: resto.replace(/\/$/, ''), nivel: Math.floor(espacos / 2), pasta: resto.endsWith('/'), marca, comentario }
    })
}

const estilosMarca = {
  novo: 'text-[var(--metodo-get)] bg-[color-mix(in_oklch,var(--metodo-get)_12%,transparent)]',
  alterado: 'text-[var(--metodo-put)] bg-[color-mix(in_oklch,var(--metodo-put)_12%,transparent)]',
  removido: 'text-[var(--metodo-delete)] bg-[color-mix(in_oklch,var(--metodo-delete)_12%,transparent)] line-through',
}

const extensoesCodigo = /\.(java|kt|ts|tsx|js|json|ya?ml|xml|sql|properties|gradle|kts|md|http|sh)$/

/** Estrutura de pastas de um projeto. Aceita a árvore como `tree` ou como texto filho. */
export function FileTree({ tree, children, title }: { tree?: string; children?: React.ReactNode; title?: string }) {
  const texto = tree ?? (typeof children === 'string' ? children : '')
  const linhas = interpretar(texto)
  return (
    <figure className="not-prose my-6 overflow-hidden rounded-lg border bg-code">
      {title && <figcaption className="border-b bg-code-header px-4 py-2 text-xs font-medium text-muted-foreground">{title}</figcaption>}
      <ul className="scrollbar-fina overflow-x-auto py-3 font-mono text-[0.8rem]" role="tree">
        {linhas.map((l, i) => {
          const Icone = l.pasta ? Folder : extensoesCodigo.test(l.nome) ? FileCode2 : File
          return (
            <li key={i} role="treeitem" className="flex items-center gap-2 px-4 py-0.5 whitespace-nowrap" style={{ paddingLeft: `${1 + l.nivel * 1.25}rem` }}>
              <Icone className={cn('size-3.5 shrink-0', l.pasta ? 'fill-primary/15 text-primary' : 'text-muted-foreground')} aria-hidden />
              <span className={cn(l.pasta && 'font-semibold', l.marca && 'rounded px-1', l.marca && estilosMarca[l.marca])}>{l.nome}</span>
              {l.marca && <span className="text-[10px] tracking-wide text-muted-foreground uppercase">{l.marca}</span>}
              {l.comentario && <span className="text-xs text-muted-foreground"># {l.comentario}</span>}
            </li>
          )
        })}
      </ul>
    </figure>
  )
}
