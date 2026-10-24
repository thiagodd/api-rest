import { Check } from 'lucide-react'
import { Link } from 'react-router'
import { IconeParte } from '@/components/icone'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import type { Nivel, ParteNav } from '@/content/navigation'
import { navigation, paginasDaParte, rotuloParte, rotulosNivel } from '@/lib/navegacao'
import { useProgresso } from '@/lib/progresso'
import { cn } from '@/lib/utils'

const coresNivel: Record<Nivel, string> = {
  basico: 'var(--nivel-basico)',
  intermediario: 'var(--nivel-intermediario)',
  avancado: 'var(--nivel-avancado)',
}

/** Anel de progresso em SVG ao redor do ícone de cada parte. */
function AnelProgresso({ valor, children }: { valor: number; children: React.ReactNode }) {
  const raio = 26
  const circunferencia = 2 * Math.PI * raio
  return (
    <div className="relative size-16 shrink-0">
      <svg viewBox="0 0 60 60" className="size-16 -rotate-90" aria-hidden>
        <circle cx="30" cy="30" r={raio} fill="var(--card)" stroke="var(--border)" strokeWidth="4" />
        <circle
          cx="30"
          cy="30"
          r={raio}
          fill="none"
          stroke="var(--primary)"
          strokeWidth="4"
          strokeLinecap="round"
          strokeDasharray={circunferencia}
          strokeDashoffset={circunferencia * (1 - valor)}
          className="transition-[stroke-dashoffset] duration-700"
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center">{children}</div>
    </div>
  )
}

/** Faixa de nível predominante de uma parte (para o rótulo de etapa). */
function niveisDaParte(parte: ParteNav): Nivel[] {
  const conjunto = new Set(paginasDaParte(parte).map((p) => p.nivel))
  return (['basico', 'intermediario', 'avancado'] as const).filter((n) => conjunto.has(n))
}

/** Mapa da trilha: partes como etapas de um caminho, módulos como estações e páginas como pontos. */
export function TrilhaMapa() {
  const lidas = useProgresso((s) => s.lidas)

  return (
    <ol className="relative">
      {/* caminho */}
      <div aria-hidden className="absolute top-8 bottom-8 left-8 w-0.5 -translate-x-1/2 bg-gradient-to-b from-[var(--nivel-basico)] via-[var(--nivel-intermediario)] to-[var(--nivel-avancado)] opacity-50 lg:left-1/2" />
      {navigation.map((parte, i) => {
        const todas = paginasDaParte(parte)
        const lidasParte = todas.filter((p) => lidas[p.id]).length
        const concluida = lidasParte === todas.length
        const direita = i % 2 === 1
        return (
          <li key={parte.slug} className={cn('relative mb-8 flex items-start gap-5 last:mb-0 lg:w-1/2', direita ? 'lg:ml-auto lg:pl-12' : 'lg:flex-row-reverse lg:pr-12')}>
            <div className={cn('relative z-10 lg:absolute lg:top-0', direita ? 'lg:-left-8' : 'lg:-right-8')}>
              <AnelProgresso valor={lidasParte / todas.length}>
                <span className={cn('flex size-10 items-center justify-center rounded-full', concluida ? 'bg-primary text-primary-foreground' : 'bg-muted text-foreground')}>
                  {concluida ? <Check className="size-5" strokeWidth={3} /> : <IconeParte nome={parte.icone} className="size-5" />}
                </span>
              </AnelProgresso>
            </div>
            <div className="min-w-0 flex-1 rounded-xl border bg-card p-4 shadow-xs transition-shadow hover:shadow-md sm:p-5">
              <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
                <span className="text-xs font-semibold tracking-wide text-primary uppercase">{rotuloParte(parte)}</span>
                <span className="text-xs text-muted-foreground">
                  · {lidasParte}/{todas.length} lidas
                </span>
                <span className="ml-auto flex gap-1">
                  {niveisDaParte(parte).map((n) => (
                    <span key={n} className="rounded px-1.5 py-px text-[10px] font-medium" style={{ color: coresNivel[n], background: `color-mix(in oklch, ${coresNivel[n]} 12%, transparent)` }}>
                      {rotulosNivel[n]}
                    </span>
                  ))}
                </span>
              </div>
              <h3 className="mt-1 text-lg leading-snug font-semibold">{parte.titulo}</h3>
              <ul className="mt-3 space-y-2">
                {parte.modulos.map((modulo) => {
                  const primeira = modulo.paginas[0]
                  return (
                    <li key={modulo.id}>
                      <Link
                        to={`/${parte.slug}/${primeira.slug}`}
                        className="group flex flex-col gap-1.5 rounded-lg border bg-background/60 px-3 py-2 transition-colors hover:border-primary/40 hover:bg-accent/40 sm:flex-row sm:items-center sm:gap-3"
                      >
                        <span className="min-w-0 flex-1 text-sm">
                          <span className="font-mono text-xs text-muted-foreground">{modulo.id}</span> <span className="font-medium group-hover:text-primary">{modulo.titulo}</span>
                        </span>
                        <span className="flex flex-wrap gap-1" aria-label={`${modulo.paginas.length} páginas`}>
                          {modulo.paginas.map((p) => (
                            <PontoPagina key={p.id} id={p.id} titulo={p.titulo} nivel={p.nivel} essencial={p.prioridade === 'essencial'} lida={!!lidas[p.id]} />
                          ))}
                        </span>
                      </Link>
                    </li>
                  )
                })}
              </ul>
            </div>
          </li>
        )
      })}
    </ol>
  )
}

function PontoPagina({ id, titulo, nivel, essencial, lida }: { id: string; titulo: string; nivel: Nivel; essencial: boolean; lida: boolean }) {
  const cor = coresNivel[nivel]
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <span
          className={cn('block size-3 rounded-full border-2', !essencial && 'border-dashed')}
          style={{ borderColor: cor, background: lida ? cor : 'transparent' }}
          aria-label={`${id} ${titulo}${lida ? ' (lida)' : ''}`}
        />
      </TooltipTrigger>
      <TooltipContent>
        <span className="font-mono">{id}</span> {titulo}
        <span className="block text-muted-foreground">
          {rotulosNivel[nivel]} · {essencial ? 'Essencial' : 'Não essencial'}
          {lida && ' · lida'}
        </span>
      </TooltipContent>
    </Tooltip>
  )
}
