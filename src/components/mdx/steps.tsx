import { Children, isValidElement } from 'react'

/** Um passo dentro de <Steps>. */
export function Step({ title, children, numero }: { title: string; children?: React.ReactNode; numero?: number }) {
  return (
    <div role="listitem" className="relative pb-8 pl-12 last:pb-0">
      <span className="absolute top-0 left-0 flex size-8 items-center justify-center rounded-full border-2 border-primary/40 bg-background font-mono text-sm font-semibold text-primary">
        {numero}
      </span>
      <p className="not-prose pt-1 text-base font-semibold">{title}</p>
      {children && <div className="prose-doc mt-1 [&>*:first-child]:mt-1 [&>*:last-child]:mb-0">{children}</div>}
    </div>
  )
}

/** Passo a passo numerado. Os passos são <div role="listitem"> para o conteúdo manter o estilo de prosa. */
export function Steps({ children }: { children: React.ReactNode }) {
  const passos = Children.toArray(children).filter(isValidElement) as React.ReactElement<React.ComponentProps<typeof Step>>[]
  return (
    <div role="list" className="relative my-8 before:absolute before:top-2 before:bottom-2 before:left-[15px] before:w-0.5 before:bg-border">
      {passos.map((passo, i) => (
        <Step key={i} {...passo.props} numero={i + 1} />
      ))}
    </div>
  )
}
