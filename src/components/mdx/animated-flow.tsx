import { ChevronLeft, ChevronRight, Pause, Play, RotateCcw } from 'lucide-react'
import { AnimatePresence, motion, useReducedMotion } from 'motion/react'
import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

export interface NoFluxo {
  id: string
  label: string
  /** Texto menor abaixo do rótulo (ex.: a classe do Spring) */
  sublabel?: string
}

export interface PassoFluxo {
  /** Nó onde o passo começa */
  from: string
  /** Nó de destino; omita para um passo de processamento dentro de `from` */
  to?: string
  /** Rótulo curto que viaja junto com o "pacote" (ex.: GET /v1/produtos/42) */
  label?: string
  title: string
  description?: React.ReactNode
  /** Passo de resposta: o pacote é desenhado com outra cor */
  response?: boolean
}

interface Posicao {
  x: number
  y: number
}

const INTERVALO_AUTOPLAY = 2600
const FOLGA_FAIXA = 18

/**
 * Diagrama que avança passo a passo: um "pacote" anima de um nó a outro,
 * com o título e a explicação de cada passo abaixo.
 */
export function AnimatedFlow({ nodes, steps, title }: { nodes: NoFluxo[]; steps: PassoFluxo[]; title?: string }) {
  const [passo, setPasso] = useState(0)
  const [tocando, setTocando] = useState(false)
  const [posicoes, setPosicoes] = useState<Record<string, Posicao>>({})
  const [vertical, setVertical] = useState(false)
  const palco = useRef<HTMLDivElement>(null)
  const refsNos = useRef<Record<string, HTMLDivElement | null>>({})
  const reduzirMovimento = useReducedMotion()

  const atual = steps[passo]
  const ultimo = steps.length - 1

  const medir = useCallback(() => {
    const raiz = palco.current
    if (!raiz) return
    const base = raiz.getBoundingClientRect()
    const novas: Record<string, Posicao> = {}
    for (const no of nodes) {
      const el = refsNos.current[no.id]
      if (!el) continue
      const r = el.getBoundingClientRect()
      // O pacote anda numa faixa fora dos nós (abaixo, ou à direita no modo vertical) para não cobrir o texto
      const emColuna = base.width < 560
      novas[no.id] = emColuna
        ? { x: r.right - base.left + FOLGA_FAIXA, y: r.top - base.top + r.height / 2 }
        : { x: r.left - base.left + r.width / 2, y: r.bottom - base.top + FOLGA_FAIXA }
    }
    setPosicoes(novas)
    setVertical(base.width < 560)
  }, [nodes])

  useLayoutEffect(() => {
    medir()
    const observador = new ResizeObserver(medir)
    if (palco.current) observador.observe(palco.current)
    return () => observador.disconnect()
  }, [medir, vertical])

  useEffect(() => {
    if (!tocando) return
    const id = setTimeout(() => {
      setPasso(Math.min(passo + 1, ultimo))
      if (passo + 1 >= ultimo) setTocando(false)
    }, INTERVALO_AUTOPLAY)
    return () => clearTimeout(id)
  }, [tocando, passo, ultimo])

  const ir = (n: number) => setPasso(Math.max(0, Math.min(ultimo, n)))

  const origem = atual && posicoes[atual.from]
  const destino = atual && posicoes[atual.to ?? atual.from]
  const corPacote = atual?.response ? 'var(--metodo-post)' : 'var(--primary)'
  const visitados = new Set(steps.slice(0, passo + 1).flatMap((s) => [s.from, s.to ?? s.from]))

  return (
    <figure
      className="not-prose my-8 overflow-hidden rounded-xl border bg-card"
      onKeyDown={(e) => {
        if (e.key === 'ArrowRight') ir(passo + 1)
        if (e.key === 'ArrowLeft') ir(passo - 1)
      }}
    >
      {title && <figcaption className="border-b px-4 py-2.5 text-sm font-semibold">{title}</figcaption>}

      <div ref={palco} className={cn('relative px-4 pt-8', vertical ? 'pb-8' : 'pb-16')} style={{ backgroundImage: 'radial-gradient(var(--border) 1px, transparent 1px)', backgroundSize: '18px 18px' }}>
        {/* faixa por onde o pacote viaja */}
        {Object.keys(posicoes).length > 0 && (
          <div
            aria-hidden
            className={cn('absolute border-dashed border-border', vertical ? 'top-12 bottom-12 border-l-2' : 'right-12 left-12 border-t-2')}
            style={vertical ? { left: Object.values(posicoes)[0].x } : { top: Object.values(posicoes)[0].y }}
          />
        )}
        <div className={cn('relative grid gap-4', vertical ? 'grid-cols-1 gap-8' : '')} style={vertical ? undefined : { gridTemplateColumns: `repeat(${nodes.length}, minmax(0, 1fr))` }}>
          {nodes.map((no) => {
            const ativo = atual && (no.id === atual.from || no.id === (atual.to ?? atual.from))
            const destinoAtivo = atual && no.id === (atual.to ?? atual.from)
            return (
              <div key={no.id} className={cn('flex', vertical ? 'justify-start' : 'justify-center')}>
                <motion.div
                  ref={(el) => {
                    refsNos.current[no.id] = el
                  }}
                  animate={{ scale: destinoAtivo && !reduzirMovimento ? 1.05 : 1 }}
                  transition={{ type: 'spring', stiffness: 300, damping: 22 }}
                  className={cn(
                    'relative z-10 flex min-h-16 w-full max-w-44 flex-col items-center justify-center rounded-lg border-2 bg-background px-2 py-2 text-center shadow-xs transition-colors',
                    vertical && 'w-40 max-w-none',
                    ativo ? 'border-primary' : visitados.has(no.id) ? 'border-primary/30' : 'border-border',
                  )}
                  style={destinoAtivo ? { boxShadow: `0 0 0 4px color-mix(in oklch, ${corPacote} 18%, transparent)` } : undefined}
                >
                  <span className="text-[13px] leading-tight font-semibold">{no.label}</span>
                  {no.sublabel && <span className="mt-0.5 font-mono text-[10px] leading-tight break-all text-muted-foreground">{no.sublabel}</span>}
                </motion.div>
              </div>
            )
          })}
        </div>

        {/* pacote que viaja entre os nós */}
        {origem && destino && (
          <motion.div
            key={passo}
            aria-hidden
            className="pointer-events-none absolute top-0 left-0 z-20"
            initial={{ x: origem.x, y: origem.y, opacity: 0 }}
            animate={{ x: destino.x, y: destino.y, opacity: 1 }}
            transition={reduzirMovimento ? { duration: 0 } : { duration: atual.to ? 0.9 : 0.3, ease: 'easeInOut' }}
          >
            <div className="-translate-x-1/2 -translate-y-1/2">
              <span className="block size-3.5 rounded-full ring-4" style={{ background: corPacote, boxShadow: `0 0 12px ${corPacote}`, ['--tw-ring-color' as string]: `color-mix(in oklch, ${corPacote} 25%, transparent)` }} />
              {atual.label && (
                <span
                  className={cn(
                    'absolute max-w-48 truncate rounded-md border bg-popover px-2 py-0.5 font-mono text-[10px] whitespace-nowrap shadow-sm',
                    vertical ? 'top-1/2 left-5 max-w-32 -translate-y-1/2' : 'top-4 left-1/2 -translate-x-1/2',
                  )}
                  style={{ color: corPacote }}
                >
                  {atual.label}
                </span>
              )}
            </div>
          </motion.div>
        )}
      </div>

      <div className="border-t px-4 py-4">
        <div className="mb-3 flex items-center gap-3">
          <span className="font-mono text-xs text-muted-foreground tabular-nums">
            Passo {passo + 1} de {steps.length}
          </span>
          <div className="flex flex-1 gap-1" role="tablist" aria-label="Passos">
            {steps.map((s, i) => (
              <button
                key={i}
                type="button"
                role="tab"
                aria-selected={i === passo}
                aria-label={`Passo ${i + 1}: ${s.title}`}
                onClick={() => ir(i)}
                className={cn('h-1.5 flex-1 rounded-full transition-colors', i <= passo ? 'bg-primary' : 'bg-muted hover:bg-muted-foreground/30')}
              />
            ))}
          </div>
        </div>
        <div className="min-h-20" aria-live="polite">
          <AnimatePresence mode="wait">
            <motion.div key={passo} initial={{ opacity: 0, y: 4 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -4 }} transition={{ duration: 0.18 }}>
              <p className="font-semibold">{atual?.title}</p>
              {atual?.description && <div className="mt-1 text-sm leading-relaxed text-muted-foreground">{atual.description}</div>}
            </motion.div>
          </AnimatePresence>
        </div>
        <div className="mt-3 flex flex-wrap items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => ir(passo - 1)} disabled={passo === 0}>
            <ChevronLeft /> Anterior
          </Button>
          <Button size="sm" onClick={() => ir(passo + 1)} disabled={passo === ultimo}>
            Próximo <ChevronRight />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              if (passo === ultimo) setPasso(0)
              setTocando((t) => !t)
            }}
            aria-label={tocando ? 'Pausar' : 'Reproduzir automaticamente'}
          >
            {tocando ? <Pause /> : <Play />} {tocando ? 'Pausar' : 'Reproduzir'}
          </Button>
          <Button variant="ghost" size="sm" className="ml-auto" onClick={() => (setPasso(0), setTocando(false))} aria-label="Reiniciar">
            <RotateCcw /> <span className="hidden sm:inline">Reiniciar</span>
          </Button>
        </div>
      </div>
    </figure>
  )
}
