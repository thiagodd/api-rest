import { CircleCheck, CircleX } from 'lucide-react'

function Lado({ tipo, title, children }: { tipo: 'ruim' | 'bom'; title?: string; children: React.ReactNode }) {
  const ruim = tipo === 'ruim'
  const cor = ruim ? 'var(--callout-danger)' : 'var(--callout-tip)'
  const Icone = ruim ? CircleX : CircleCheck
  return (
    <div
      className="flex min-w-0 flex-col overflow-hidden rounded-lg border"
      style={{ borderColor: `color-mix(in oklch, ${cor} 40%, transparent)` }}
    >
      <div className="flex items-center gap-2 px-4 py-2.5 text-sm font-semibold" style={{ color: cor, background: `color-mix(in oklch, ${cor} 9%, transparent)` }}>
        <Icone className="size-4 shrink-0" aria-hidden />
        <span>{ruim ? 'Evite' : 'Prefira'}</span>
        {title && <span className="font-medium text-foreground">· {title}</span>}
      </div>
      <div className="prose-doc prose-sm flex-1 px-4 py-1 [&_.not-prose]:my-3">{children}</div>
    </div>
  )
}

/** Abordagem ruim. Use dentro de <BadVsGood>. */
export function Bad({ title, children }: { title?: string; children: React.ReactNode }) {
  return (
    <Lado tipo="ruim" title={title}>
      {children}
    </Lado>
  )
}

/** Abordagem recomendada. Use dentro de <BadVsGood>. */
export function Good({ title, children }: { title?: string; children: React.ReactNode }) {
  return (
    <Lado tipo="bom" title={title}>
      {children}
    </Lado>
  )
}

/** Duas abordagens lado a lado (ruim × boa), com explicação opcional em `why`. */
export function BadVsGood({ title, why, children }: { title?: string; why?: React.ReactNode; children: React.ReactNode }) {
  return (
    <div className="@container my-6">
      {title && <p className="not-prose mb-2 text-sm font-semibold">{title}</p>}
      <div className="grid gap-3 @2xl:grid-cols-2">{children}</div>
      {why && <p className="not-prose mt-3 rounded-md bg-muted/60 px-4 py-2.5 text-sm leading-relaxed text-muted-foreground">{why}</p>}
    </div>
  )
}
