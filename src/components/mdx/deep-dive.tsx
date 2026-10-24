import { ChevronDown, Microscope } from 'lucide-react'
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible'

/** Seção recolhível "Por baixo dos panos", para detalhes internos opcionais. */
export function DeepDive({ title = 'Por baixo dos panos', children, defaultOpen = false }: { title?: string; children: React.ReactNode; defaultOpen?: boolean }) {
  return (
    <Collapsible defaultOpen={defaultOpen} className="group my-6 rounded-lg border border-dashed bg-muted/30">
      <CollapsibleTrigger className="flex w-full items-center gap-3 px-4 py-3 text-left">
        <span className="flex size-7 items-center justify-center rounded-md bg-background text-primary shadow-xs">
          <Microscope className="size-4" aria-hidden />
        </span>
        <span className="flex-1">
          <span className="block text-sm font-semibold">{title}</span>
          <span className="block text-xs text-muted-foreground group-data-[state=open]:hidden">Opcional · clique para expandir</span>
        </span>
        <ChevronDown className="size-4 text-muted-foreground transition-transform group-data-[state=open]:rotate-180" aria-hidden />
      </CollapsibleTrigger>
      <CollapsibleContent>
        <div className="prose-doc border-t border-dashed px-4 py-1 [&_.not-prose]:my-4">{children}</div>
      </CollapsibleContent>
    </Collapsible>
  )
}
