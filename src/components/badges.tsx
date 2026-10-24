import { Star, StarHalf, Circle } from 'lucide-react'
import type { Nivel, Prioridade } from '@/content/navigation'
import { corMetodo, corStatus, nomeStatus } from '@/lib/http'
import { descricoesNivel, descricoesPrioridade, rotulosNivel, rotulosPrioridade } from '@/lib/navegacao'
import { cn } from '@/lib/utils'
import { Tooltip, TooltipContent, TooltipTrigger } from './ui/tooltip'

const coresNivel: Record<Nivel, string> = {
  basico: 'var(--nivel-basico)',
  intermediario: 'var(--nivel-intermediario)',
  avancado: 'var(--nivel-avancado)',
}

export function NivelBadge({ nivel, compacto = false, className }: { nivel: Nivel; compacto?: boolean; className?: string }) {
  const cor = coresNivel[nivel]
  const conteudo = (
    <span
      className={cn(
        'inline-flex shrink-0 items-center gap-1 rounded-md border font-medium',
        compacto ? 'size-5 justify-center text-[10px]' : 'px-2 py-0.5 text-xs',
        className,
      )}
      style={{ color: cor, borderColor: `color-mix(in oklch, ${cor} 35%, transparent)`, background: `color-mix(in oklch, ${cor} 10%, transparent)` }}
      aria-label={`Nível ${rotulosNivel[nivel]}`}
    >
      {compacto ? rotulosNivel[nivel][0] : rotulosNivel[nivel]}
    </span>
  )
  return (
    <Tooltip>
      <TooltipTrigger asChild>{conteudo}</TooltipTrigger>
      <TooltipContent>
        <strong>{rotulosNivel[nivel]}:</strong> {descricoesNivel[nivel]}
      </TooltipContent>
    </Tooltip>
  )
}

const iconesPrioridade = { essencial: Star, importante: StarHalf, complementar: Circle }

export function PrioridadeBadge({ prioridade, compacto = false, className }: { prioridade: Prioridade; compacto?: boolean; className?: string }) {
  const Icone = iconesPrioridade[prioridade]
  const conteudo = (
    <span
      className={cn(
        'inline-flex shrink-0 items-center gap-1 rounded-md font-medium',
        prioridade === 'essencial' && 'text-amber-600 dark:text-amber-400',
        prioridade === 'importante' && 'text-sky-700 dark:text-sky-300',
        prioridade === 'complementar' && 'text-muted-foreground',
        compacto ? 'size-5 justify-center' : 'border bg-muted/50 px-2 py-0.5 text-xs',
        className,
      )}
      aria-label={`Prioridade ${rotulosPrioridade[prioridade]}`}
    >
      <Icone className={cn(compacto ? 'size-3' : 'size-3.5', prioridade === 'essencial' && 'fill-current')} aria-hidden />
      {!compacto && rotulosPrioridade[prioridade]}
    </span>
  )
  return (
    <Tooltip>
      <TooltipTrigger asChild>{conteudo}</TooltipTrigger>
      <TooltipContent>
        <strong>{rotulosPrioridade[prioridade]}:</strong> {descricoesPrioridade[prioridade]}
      </TooltipContent>
    </Tooltip>
  )
}

export function MetodoBadge({ metodo, className }: { metodo: string; className?: string }) {
  const cor = corMetodo(metodo)
  return (
    <span
      className={cn('inline-flex shrink-0 items-center justify-center rounded px-1.5 py-0.5 font-mono text-[11px] font-bold tracking-wide', className)}
      style={{ color: cor, background: `color-mix(in oklch, ${cor} 14%, transparent)` }}
    >
      {metodo.toUpperCase()}
    </span>
  )
}

export function StatusBadge({ status, comNome = true, className }: { status: number; comNome?: boolean; className?: string }) {
  const cor = corStatus(status)
  return (
    <span
      className={cn('inline-flex shrink-0 items-center gap-1.5 rounded-md px-2 py-0.5 font-mono text-xs font-semibold', className)}
      style={{ color: cor, background: `color-mix(in oklch, ${cor} 13%, transparent)` }}
    >
      <span className="size-1.5 rounded-full" style={{ background: cor }} aria-hidden />
      {status}
      {comNome && nomeStatus(status) && <span className="font-sans font-medium">{nomeStatus(status)}</span>}
    </span>
  )
}
