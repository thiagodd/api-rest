import { FileCode2, GitBranch, Hammer } from 'lucide-react'
import { cn } from '@/lib/utils'

export interface ArquivoAlterado {
  path: string
  change?: 'novo' | 'alterado' | 'removido'
}

const estilos = {
  novo: { rotulo: 'novo', cor: 'var(--metodo-get)' },
  alterado: { rotulo: 'alterado', cor: 'var(--metodo-put)' },
  removido: { rotulo: 'removido', cor: 'var(--metodo-delete)' },
}

/** Destaca a etapa do projeto final (Bazar) construída na página. */
export function ProjectStep({
  title,
  module,
  tag,
  files = [],
  children,
}: {
  title: string
  /** Módulo do Bazar (catalogo, pedidos...) */
  module?: string
  /** Tag do repositório de exemplos com o código desta etapa, ex.: pagina-05.04 */
  tag?: string
  files?: (string | ArquivoAlterado)[]
  children?: React.ReactNode
}) {
  const arquivos = files.map((f) => (typeof f === 'string' ? { path: f, change: 'alterado' as const } : f))
  return (
    <section className="my-8 overflow-hidden rounded-xl border-2 border-primary/30 bg-gradient-to-br from-primary/6 to-transparent">
      <header className="not-prose flex flex-wrap items-center gap-3 border-b border-primary/20 px-4 py-3">
        <span className="flex size-9 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-sm">
          <Hammer className="size-4.5" aria-hidden />
        </span>
        <div className="min-w-0 flex-1">
          <p className="text-[11px] font-semibold tracking-wide text-primary uppercase">Projeto Bazar{module && ` · módulo ${module}`}</p>
          <p className="font-semibold leading-snug">{title}</p>
        </div>
        {tag && (
          <code className="inline-flex items-center gap-1.5 rounded-md border bg-background px-2 py-1 font-mono text-xs">
            <GitBranch className="size-3.5 text-muted-foreground" aria-hidden />
            {tag}
          </code>
        )}
      </header>
      {children && <div className="prose-doc prose-sm px-4 py-1">{children}</div>}
      {arquivos.length > 0 && (
        <div className={cn('not-prose px-4 py-3', children && 'border-t border-primary/15')}>
          <p className="mb-2 text-xs font-semibold text-muted-foreground">Arquivos desta etapa</p>
          <ul className="space-y-1">
            {arquivos.map((a) => {
              const estilo = estilos[a.change ?? 'alterado']
              return (
                <li key={a.path} className="flex items-center gap-2 font-mono text-xs">
                  <FileCode2 className="size-3.5 shrink-0 text-muted-foreground" aria-hidden />
                  <span className="min-w-0 truncate" title={a.path}>
                    {a.path}
                  </span>
                  <span className="ml-auto shrink-0 rounded px-1.5 py-px font-sans text-[10px] font-medium" style={{ color: estilo.cor, background: `color-mix(in oklch, ${estilo.cor} 12%, transparent)` }}>
                    {estilo.rotulo}
                  </span>
                </li>
              )
            })}
          </ul>
        </div>
      )}
    </section>
  )
}
