import { ChevronDown, Dumbbell, KeyRound, Lightbulb } from 'lucide-react'
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible'
import { cn } from '@/lib/utils'

function Revelavel({ rotulo, icone: Icone, destaque, children }: { rotulo: string; icone: typeof Lightbulb; destaque?: boolean; children: React.ReactNode }) {
  return (
    <Collapsible className="group/rev mt-3 rounded-lg border bg-background">
      <CollapsibleTrigger className={cn('flex w-full items-center gap-2 px-3 py-2 text-left text-sm font-medium', destaque && 'text-primary')}>
        <Icone className="size-4" aria-hidden />
        <span className="flex-1">{rotulo}</span>
        <span className="text-xs font-normal text-muted-foreground group-data-[state=open]/rev:hidden">mostrar</span>
        <ChevronDown className="size-4 text-muted-foreground transition-transform group-data-[state=open]/rev:rotate-180" aria-hidden />
      </CollapsibleTrigger>
      <CollapsibleContent>
        <div className="prose-doc prose-sm border-t px-3 py-1 [&_.not-prose]:my-3">{children}</div>
      </CollapsibleContent>
    </Collapsible>
  )
}

/** Dica recolhível. Use dentro de <Exercise>. */
export function Hint({ children, title = 'Dica' }: { children: React.ReactNode; title?: string }) {
  return (
    <Revelavel rotulo={title} icone={Lightbulb}>
      {children}
    </Revelavel>
  )
}

/** Solução recolhível. Use dentro de <Exercise>. */
export function Solution({ children, title = 'Solução' }: { children: React.ReactNode; title?: string }) {
  return (
    <Revelavel rotulo={title} icone={KeyRound} destaque>
      {children}
    </Revelavel>
  )
}

const rotulosDificuldade = { facil: 'Fácil', medio: 'Médio', dificil: 'Difícil' }

/** Exercício prático com dica e solução recolhíveis. */
export function Exercise({ title, difficulty, children }: { title: string; difficulty?: keyof typeof rotulosDificuldade; children: React.ReactNode }) {
  return (
    <section className="my-8 rounded-xl border border-l-4 border-l-[var(--nivel-intermediario)] bg-card px-4 py-4">
      <header className="not-prose mb-2 flex items-center gap-2">
        <Dumbbell className="size-4.5 text-[var(--nivel-intermediario)]" aria-hidden />
        <span className="text-[11px] font-semibold tracking-wide text-[var(--nivel-intermediario)] uppercase">Exercício</span>
        {difficulty && <span className="rounded-full border px-2 py-px text-[11px] text-muted-foreground">{rotulosDificuldade[difficulty]}</span>}
      </header>
      <p className="not-prose mb-1 font-semibold">{title}</p>
      <div className="prose-doc prose-sm [&>*:first-child]:mt-0 [&>p:last-of-type]:mb-0">{children}</div>
    </section>
  )
}
