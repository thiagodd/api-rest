import { ExternalLink, Library } from 'lucide-react'
import type { Referencia } from '@/lib/conteudo'
import { usePagina } from './contexto'

function dominio(url: string) {
  try {
    return new URL(url).hostname.replace(/^www\./, '')
  } catch {
    return url
  }
}

/** Lista de fontes. Sem `items`, usa as referências do frontmatter da página. */
export function References({ items, title }: { items?: Referencia[]; title?: string }) {
  const { frontmatter } = usePagina()
  const lista = items ?? frontmatter?.referencias ?? []
  if (!lista.length) return <p className="text-sm text-muted-foreground">Sem referências externas para esta página.</p>
  return (
    <section className="not-prose my-6 rounded-lg border bg-muted/20">
      {title && (
        <header className="flex items-center gap-2 border-b px-4 py-2.5 text-sm font-semibold">
          <Library className="size-4 text-muted-foreground" aria-hidden /> {title}
        </header>
      )}
      <ol className="divide-y">
        {lista.map((r, i) => (
          <li key={`${r.titulo}-${i}`} className="flex items-start gap-3 px-4 py-2.5 text-sm">
            <span className="mt-px w-5 shrink-0 font-mono text-xs text-muted-foreground">{i + 1}.</span>
            {r.url ? (
              <a href={r.url} target="_blank" rel="noreferrer" className="group min-w-0 flex-1">
                <span className="font-medium group-hover:text-primary group-hover:underline">{r.titulo}</span>
                <span className="mt-0.5 flex items-center gap-1 text-xs text-muted-foreground">
                  {dominio(r.url)} <ExternalLink className="size-3" aria-hidden />
                </span>
              </a>
            ) : (
              <span className="flex-1 text-muted-foreground">{r.titulo}</span>
            )}
          </li>
        ))}
      </ol>
    </section>
  )
}
