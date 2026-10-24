import { PencilLine } from 'lucide-react'

/**
 * Marcador de conteúdo ainda não escrito (páginas esqueleto da fase 2).
 * A fase 3 substitui cada <Draft> pelo texto final.
 */
export function Draft({ title = 'A escrever', children }: { title?: string; children?: React.ReactNode }) {
  return (
    <div className="my-4 rounded-lg border-2 border-dashed border-muted-foreground/25 bg-muted/25 px-4 py-3">
      <p className="not-prose mb-1 flex items-center gap-1.5 text-xs font-semibold tracking-wide text-muted-foreground uppercase">
        <PencilLine className="size-3.5" aria-hidden /> {title}
      </p>
      <div className="prose-doc prose-sm text-muted-foreground [&>*:first-child]:mt-0 [&>*:last-child]:mb-0">{children}</div>
    </div>
  )
}
