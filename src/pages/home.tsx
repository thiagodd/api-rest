import { ArrowRight, BookOpen, Flag, Gauge, Route, Star, Zap } from 'lucide-react'
import { useEffect } from 'react'
import { Link } from 'react-router'
import { NivelBadge, PrioridadeBadge } from '@/components/badges'
import { TrilhaMapa } from '@/components/home/trilha-mapa'
import { Button } from '@/components/ui/button'
import { Progress } from '@/components/ui/progress'
import { navigation, paginaPorId, paginas, type PaginaPlana } from '@/lib/navegacao'
import { useProgresso } from '@/lib/progresso'

function proximaParaLer(lidas: Record<string, string>, ultimaVisitada?: string): { pagina: PaginaPlana; retomando: boolean } {
  const ultima = ultimaVisitada ? paginaPorId(ultimaVisitada) : undefined
  if (ultima && !lidas[ultima.id]) return { pagina: ultima, retomando: true }
  const inicio = ultima ? ultima.indice : 0
  const seguinte = paginas.slice(inicio).find((p) => !lidas[p.id]) ?? paginas.find((p) => !lidas[p.id])
  return { pagina: seguinte ?? paginas[0], retomando: !!ultima }
}

export function Home() {
  const lidas = useProgresso((s) => s.lidas)
  const ultimaVisitada = useProgresso((s) => s.ultimaVisitada)
  const setSomenteEssenciais = useProgresso((s) => s.setSomenteEssenciais)
  const totalLidas = paginas.filter((p) => lidas[p.id]).length
  const percentual = Math.round((totalLidas / paginas.length) * 100)
  const essenciais = paginas.filter((p) => p.prioridade === 'essencial')
  const { pagina: proxima, retomando } = proximaParaLer(lidas, ultimaVisitada)
  const modulos = navigation.reduce((n, p) => n + p.modulos.length, 0)

  useEffect(() => {
    document.title = 'APIs REST com Spring Boot: do básico ao avançado'
  }, [])

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6 lg:px-10 lg:py-14">
      <section className="relative overflow-hidden rounded-2xl border bg-card px-6 py-10 sm:px-10 sm:py-14">
        <div aria-hidden className="pointer-events-none absolute -top-32 -right-32 size-[28rem] rounded-full bg-[radial-gradient(closest-side,color-mix(in_oklch,var(--primary)_16%,transparent),transparent)]" />
        <div className="relative grid items-center gap-8 xl:grid-cols-[minmax(0,1fr)_22rem]">
          <div className="max-w-2xl">
            <p className="mb-4 inline-flex items-center gap-2 rounded-full border bg-background/70 px-3 py-1 text-xs font-medium text-muted-foreground">
              <span className="size-1.5 rounded-full bg-primary" /> Java 25 · Spring Boot 4.1 · Spring Framework 7
            </p>
            <h1 className="text-4xl leading-[1.1] font-bold tracking-tight text-balance sm:text-5xl">
              APIs REST do básico ao avançado, <span className="text-primary">com Spring Boot</span>
            </h1>
            <p className="mt-4 text-lg leading-relaxed text-muted-foreground text-pretty">
              Do primeiro <code className="rounded bg-muted px-1.5 py-0.5 font-mono text-[0.9em]">GET</code> ao deploy em produção: HTTP de verdade, design profissional,
              confiabilidade e operação, sempre construindo a API do <strong className="text-foreground">Bazar</strong>, uma loja online de exemplo.
            </p>
            <div className="mt-8 flex flex-wrap items-center gap-3">
              <Button asChild size="lg">
                <Link to={proxima.url}>
                  {retomando ? 'Continuar de onde parei' : 'Começar a trilha'} <ArrowRight />
                </Link>
              </Button>
              <Button asChild size="lg" variant="outline">
                <a href="#trilha">Ver a trilha completa</a>
              </Button>
            </div>
            <p className="mt-3 text-sm text-muted-foreground">
              {retomando ? 'Próxima: ' : 'Primeira página: '}
              <Link to={proxima.url} className="font-medium text-foreground hover:text-primary">
                <span className="font-mono text-xs">{proxima.id}</span> {proxima.titulo}
              </Link>
            </p>
          </div>
          <IlustracaoHero />
        </div>
      </section>

      <section className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4" aria-label="Resumo">
        <Estatistica icone={BookOpen} valor={String(paginas.length)} rotulo={`páginas em ${modulos} módulos e ${navigation.length} partes`} />
        <Estatistica icone={Star} valor={String(essenciais.length)} rotulo="páginas essenciais (trilha mínima)" />
        <Estatistica icone={Flag} valor={`${essenciais.filter((p) => lidas[p.id]).length}/${essenciais.length}`} rotulo="essenciais lidas" />
        <div className="rounded-xl border bg-card p-4">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Gauge className="size-4" aria-hidden /> Progresso geral
          </div>
          <p className="mt-1 text-2xl font-bold tabular-nums">{percentual}%</p>
          <Progress value={percentual} className="mt-2" aria-label="Progresso geral" />
        </div>
      </section>

      <section className="mt-12 grid gap-6 lg:grid-cols-[1fr_1fr]">
        <div className="rounded-xl border bg-card p-6">
          <h2 className="flex items-center gap-2 text-lg font-semibold">
            <Route className="size-5 text-primary" aria-hidden /> Trilhas sugeridas
          </h2>
          <ul className="mt-4 space-y-4 text-sm">
            <li>
              <p className="font-medium">Trilha mínima (só essenciais)</p>
              <p className="text-muted-foreground">
                Constrói e opera uma API profissional com cerca de 60% das páginas.{' '}
                <button type="button" onClick={() => setSomenteEssenciais(true)} className="font-medium text-primary hover:underline">
                  Filtrar a navegação
                </button>
              </p>
            </li>
            <li>
              <p className="font-medium">Trilha completa</p>
              <p className="text-muted-foreground">Todas as páginas, na ordem. Nenhuma página usa um conceito ainda não explicado.</p>
            </li>
            <li>
              <p className="flex items-center gap-1.5 font-medium">
                <Zap className="size-4 text-amber-500" aria-hidden /> Com pressa e já experiente em CRUD
              </p>
              <p className="text-muted-foreground">
                Comece por{' '}
                {['02.02', '02.03', '08.01'].map((id) => {
                  const p = paginaPorId(id)!
                  return (
                    <Link key={id} to={p.url} className="mr-1 font-mono text-xs font-medium text-primary hover:underline" title={p.titulo}>
                      {id}
                    </Link>
                  )
                })}
                , a Parte 6 e a Parte 9.
              </p>
            </li>
          </ul>
        </div>
        <div className="rounded-xl border bg-card p-6">
          <h2 className="text-lg font-semibold">Como ler os selos</h2>
          <dl className="mt-4 grid gap-3 text-sm">
            {(
              [
                ['basico', 'Precisa saber para fazer qualquer API.'],
                ['intermediario', 'Separa um CRUD de uma API profissional.'],
                ['avancado', 'O que se espera de quem projeta e opera APIs em produção.'],
              ] as const
            ).map(([nivel, texto]) => (
              <div key={nivel} className="flex items-start gap-3">
                <dt className="w-28 shrink-0">
                  <NivelBadge nivel={nivel} />
                </dt>
                <dd className="text-muted-foreground">{texto}</dd>
              </div>
            ))}
            {(
              [
                ['essencial', 'Não pule.'],
                ['importante', 'Pule só se tiver pressa; volte depois.'],
                ['complementar', 'Pode pular sem perder a sequência.'],
              ] as const
            ).map(([prioridade, texto]) => (
              <div key={prioridade} className="flex items-start gap-3">
                <dt className="w-28 shrink-0">
                  <PrioridadeBadge prioridade={prioridade} />
                </dt>
                <dd className="text-muted-foreground">{texto}</dd>
              </div>
            ))}
          </dl>
        </div>
      </section>

      <section id="trilha" className="mt-16 scroll-mt-20">
        <div className="mb-8 max-w-2xl">
          <h2 className="text-2xl font-bold tracking-tight">A trilha de aprendizado</h2>
          <p className="mt-2 text-muted-foreground">
            Cada parte é uma etapa; cada ponto é uma página, colorido pelo nível e preenchido quando você a marca como lida. Clique em um módulo para começar por ele.
          </p>
        </div>
        <TrilhaMapa />
      </section>
    </div>
  )
}

function Estatistica({ icone: Icone, valor, rotulo }: { icone: typeof BookOpen; valor: string; rotulo: string }) {
  return (
    <div className="rounded-xl border bg-card p-4">
      <Icone className="size-4 text-muted-foreground" aria-hidden />
      <p className="mt-1 text-2xl font-bold tabular-nums">{valor}</p>
      <p className="text-sm text-muted-foreground">{rotulo}</p>
    </div>
  )
}

/** Ilustração do hero: uma troca HTTP estilizada, em SVG próprio. */
function IlustracaoHero() {
  return (
    <svg aria-hidden className="hidden w-full xl:block" viewBox="140 60 280 224" fill="none">
      <g fontFamily="JetBrains Mono Variable, monospace" fontSize="12">
        <rect x="150" y="70" width="230" height="74" rx="10" fill="var(--card)" stroke="var(--border)" />
        <rect x="164" y="86" width="38" height="18" rx="4" fill="color-mix(in oklch, var(--metodo-post) 16%, transparent)" />
        <text x="170" y="99" fill="var(--metodo-post)" fontWeight="700" fontSize="11">POST</text>
        <text x="210" y="99" fill="var(--foreground)">/v1/pedidos</text>
        <text x="164" y="126" fill="var(--muted-foreground)" fontSize="11">Idempotency-Key: 9f1c…</text>
        <path d="M265 150 v40" stroke="var(--primary)" strokeWidth="2" strokeDasharray="4 4" />
        <path d="M259 184 l6 8 6-8" stroke="var(--primary)" strokeWidth="2" fill="none" />
        <rect x="190" y="200" width="220" height="74" rx="10" fill="var(--card)" stroke="var(--border)" />
        <circle cx="210" cy="222" r="4" fill="var(--status-2xx)" />
        <text x="222" y="226" fill="var(--status-2xx)" fontWeight="700">201 Created</text>
        <text x="204" y="252" fill="var(--muted-foreground)" fontSize="11">Location: /v1/pedidos/BZR-…</text>
      </g>
    </svg>
  )
}
