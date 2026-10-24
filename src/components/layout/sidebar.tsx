import { BookMarked, Check, ChevronRight, Library, LayoutGrid, Home } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link, NavLink, useLocation } from 'react-router'
import { NivelBadge, PrioridadeBadge } from '@/components/badges'
import { IconeParte } from '@/components/icone'
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible'
import { Progress } from '@/components/ui/progress'
import { Switch } from '@/components/ui/switch'
import type { ParteNav } from '@/content/navigation'
import { navigation, paginas, paginasDaParte, rotuloParte } from '@/lib/navegacao'
import { useProgresso } from '@/lib/progresso'
import { cn } from '@/lib/utils'

const linksFixos = [
  { to: '/', rotulo: 'Início', icone: Home },
  { to: '/componentes', rotulo: 'Guia de componentes', icone: LayoutGrid },
  { to: '/glossario', rotulo: 'Glossário', icone: BookMarked },
  { to: '/referencias', rotulo: 'Referências', icone: Library },
]

export function Sidebar({ aoNavegar }: { aoNavegar?: () => void }) {
  const { pathname } = useLocation()
  const lidas = useProgresso((s) => s.lidas)
  const somenteEssenciais = useProgresso((s) => s.somenteEssenciais)
  const setSomenteEssenciais = useProgresso((s) => s.setSomenteEssenciais)
  const totalLidas = paginas.filter((p) => lidas[p.id]).length
  const percentual = Math.round((totalLidas / paginas.length) * 100)

  const parteAtual = paginas.find((p) => p.url === pathname)?.parte
  const [abertas, setAbertas] = useState<Set<string>>(() => new Set(parteAtual ? [parteAtual.slug] : []))

  // Abre a parte da página atual quando a navegação muda de parte
  const [parteAnterior, setParteAnterior] = useState(parteAtual)
  if (parteAtual !== parteAnterior) {
    setParteAnterior(parteAtual)
    if (parteAtual && !abertas.has(parteAtual.slug)) setAbertas(new Set([...abertas, parteAtual.slug]))
  }

  const refAtivo = useRef<HTMLAnchorElement>(null)
  useEffect(() => {
    refAtivo.current?.scrollIntoView({ block: 'nearest' })
  }, [pathname])

  const alternar = (slug: string, aberta: boolean) => {
    const proximas = new Set(abertas)
    if (aberta) proximas.add(slug)
    else proximas.delete(slug)
    setAbertas(proximas)
  }

  return (
    <nav aria-label="Navegação da documentação" className="flex h-full flex-col">
      <div className="space-y-3 border-b px-4 py-4">
        <div>
          <div className="mb-1.5 flex items-baseline justify-between text-xs">
            <span className="font-medium">Seu progresso</span>
            <span className="text-muted-foreground tabular-nums">
              {totalLidas}/{paginas.length} · {percentual}%
            </span>
          </div>
          <Progress value={percentual} aria-label="Progresso geral" />
        </div>
        <label className="flex cursor-pointer items-center justify-between gap-2 text-xs text-muted-foreground">
          <span>Mostrar só as essenciais (trilha mínima)</span>
          <Switch checked={somenteEssenciais} onCheckedChange={setSomenteEssenciais} aria-label="Mostrar só as páginas essenciais" />
        </label>
      </div>

      <div className="scrollbar-fina flex-1 overflow-y-auto px-2 py-3">
        <ul className="mb-3 space-y-0.5 border-b pb-3">
          {linksFixos.map(({ to, rotulo, icone: Icone }) => (
            <li key={to}>
              <NavLink
                to={to}
                end
                onClick={aoNavegar}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-2.5 rounded-md px-2 py-1.5 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                    isActive && 'bg-accent font-medium text-accent-foreground',
                  )
                }
              >
                <Icone className="size-4" aria-hidden />
                {rotulo}
              </NavLink>
            </li>
          ))}
        </ul>

        <ul className="space-y-1">
          {navigation.map((parte) => (
            <GrupoParte
              key={parte.slug}
              parte={parte}
              aberta={abertas.has(parte.slug)}
              aoAlternar={(a) => alternar(parte.slug, a)}
              pathname={pathname}
              lidas={lidas}
              somenteEssenciais={somenteEssenciais}
              refAtivo={refAtivo}
              aoNavegar={aoNavegar}
            />
          ))}
        </ul>
      </div>
    </nav>
  )
}

function GrupoParte({
  parte,
  aberta,
  aoAlternar,
  pathname,
  lidas,
  somenteEssenciais,
  refAtivo,
  aoNavegar,
}: {
  parte: ParteNav
  aberta: boolean
  aoAlternar: (aberta: boolean) => void
  pathname: string
  lidas: Record<string, string>
  somenteEssenciais: boolean
  refAtivo: React.RefObject<HTMLAnchorElement | null>
  aoNavegar?: () => void
}) {
  const todas = paginasDaParte(parte)
  const lidasDaParte = todas.filter((p) => lidas[p.id]).length
  const contemAtual = todas.some((p) => p.url === pathname)

  return (
    <li>
      <Collapsible open={aberta} onOpenChange={aoAlternar}>
        <CollapsibleTrigger
          className={cn(
            'group flex w-full items-start gap-2.5 rounded-md px-2 py-2 text-left transition-colors hover:bg-accent/60',
            contemAtual && 'text-foreground',
          )}
        >
          <span
            className={cn(
              'mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-md border bg-background text-muted-foreground',
              contemAtual && 'border-primary/40 text-primary',
            )}
          >
            <IconeParte nome={parte.icone} className="size-3.5" />
          </span>
          <span className="min-w-0 flex-1">
            <span className="block text-[11px] font-medium tracking-wide text-muted-foreground uppercase">{rotuloParte(parte)}</span>
            <span className="block text-sm leading-snug font-medium">{parte.titulo}</span>
            <span className="mt-1.5 flex items-center gap-2">
              <Progress value={(lidasDaParte / todas.length) * 100} className="h-1" aria-label={`Progresso em ${parte.titulo}`} />
              <span className="shrink-0 text-[10px] text-muted-foreground tabular-nums">
                {lidasDaParte}/{todas.length}
              </span>
            </span>
          </span>
          <ChevronRight className="mt-1 size-4 shrink-0 text-muted-foreground transition-transform group-data-[state=open]:rotate-90" aria-hidden />
        </CollapsibleTrigger>
        <CollapsibleContent>
          <div className="mt-1 mb-2 ml-[1.15rem] border-l pl-2">
            {parte.modulos.map((modulo) => {
              const visiveis = modulo.paginas.filter((p) => !somenteEssenciais || p.prioridade === 'essencial')
              if (visiveis.length === 0) return null
              return (
                <div key={modulo.id} className="mt-2 first:mt-1">
                  <div className="px-2 pb-1 text-[11px] font-semibold text-muted-foreground">
                    {modulo.id} · {modulo.titulo}
                  </div>
                  <ul>
                    {visiveis.map((p) => {
                      const url = `/${parte.slug}/${p.slug}`
                      const ativa = url === pathname
                      const lida = !!lidas[p.id]
                      return (
                        <li key={p.id}>
                          <Link
                            to={url}
                            ref={ativa ? refAtivo : undefined}
                            onClick={aoNavegar}
                            aria-current={ativa ? 'page' : undefined}
                            className={cn(
                              'group/item flex items-start gap-2 rounded-md px-2 py-1.5 text-[13px] leading-snug text-muted-foreground transition-colors hover:bg-accent/60 hover:text-foreground',
                              ativa && 'bg-accent font-medium text-accent-foreground hover:bg-accent',
                            )}
                          >
                            <span className="mt-px w-9 shrink-0 font-mono text-[11px] tabular-nums">
                              {lida ? (
                                <span className="inline-flex size-4 items-center justify-center rounded-full bg-primary text-primary-foreground" title="Lida">
                                  <Check className="size-3" strokeWidth={3} aria-label="Lida" />
                                </span>
                              ) : (
                                p.id
                              )}
                            </span>
                            <span className="min-w-0 flex-1">{p.titulo}</span>
                            <span className="flex shrink-0 items-center gap-0.5">
                              <NivelBadge nivel={p.nivel} compacto />
                              <PrioridadeBadge prioridade={p.prioridade} compacto />
                            </span>
                          </Link>
                        </li>
                      )
                    })}
                  </ul>
                </div>
              )
            })}
          </div>
        </CollapsibleContent>
      </Collapsible>
    </li>
  )
}
