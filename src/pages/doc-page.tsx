import { CircleCheck, Circle, Clock, PencilLine } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router'
import { NivelBadge, PrioridadeBadge } from '@/components/badges'
import { PrevNext } from '@/components/layout/prev-next'
import { Toc } from '@/components/layout/toc'
import { componentesMdx } from '@/components/mdx'
import { PaginaContext } from '@/components/mdx/contexto'
import { Button } from '@/components/ui/button'
import { carregarPagina, metaPagina, paginaEmCache, type ModuloMdx } from '@/lib/conteudo'
import { paginaPorId, paginaPorSlug, rotuloParte, vizinhas, type PaginaPlana } from '@/lib/navegacao'
import { useProgresso } from '@/lib/progresso'
import { cn } from '@/lib/utils'
import { NotFound } from './not-found'

const rotulosStatus: Record<string, string> = {
  esqueleto: 'Esqueleto: o conteúdo desta página ainda vai ser escrito.',
  rascunho: 'Rascunho: esta página está sendo escrita.',
  revisao: 'Em revisão: o conteúdo pode mudar.',
}

export function DocPage() {
  const { parte = '', pagina = '' } = useParams()
  const atual = paginaPorSlug(parte, pagina)
  if (!atual) return <NotFound />
  return <ConteudoPagina key={atual.id} pagina={atual} />
}

function ConteudoPagina({ pagina }: { pagina: PaginaPlana }) {
  const [modulo, setModulo] = useState<ModuloMdx | undefined>(() => paginaEmCache(pagina.arquivo))
  const [erro, setErro] = useState<string>()
  const artigo = useRef<HTMLDivElement>(null)
  const { hash } = useLocation()
  const visitar = useProgresso((s) => s.visitar)
  const lida = useProgresso((s) => !!s.lidas[pagina.id])
  const alternarLida = useProgresso((s) => s.alternarLida)
  const meta = metaPagina(pagina.id)
  const { anterior, proxima } = vizinhas(pagina)

  useEffect(() => {
    visitar(pagina.id)
    document.title = `${pagina.id} ${pagina.titulo} · APIs REST com Spring Boot`
  }, [pagina, visitar])

  useEffect(() => {
    if (modulo) return
    let ativo = true
    carregarPagina(pagina.arquivo)
      .then((m) => ativo && setModulo(m))
      .catch((e: unknown) => ativo && setErro(String(e)))
    return () => {
      ativo = false
    }
  }, [pagina.arquivo, modulo])

  // Rola até a âncora depois que o conteúdo existe no DOM
  useEffect(() => {
    if (!modulo || !hash) return
    const alvo = document.getElementById(decodeURIComponent(hash.slice(1)))
    alvo?.scrollIntoView()
  }, [modulo, hash])

  const Conteudo = modulo?.default
  const status = modulo?.frontmatter.status ?? meta?.status

  return (
    <div className="mx-auto flex max-w-[78rem] gap-10 px-4 py-8 sm:px-6 lg:px-10 lg:py-10">
      <article className="min-w-0 max-w-3xl flex-1">
        <header className="mb-8">
          <nav aria-label="Você está em" className="mb-3 flex flex-wrap items-center gap-x-1.5 text-xs text-muted-foreground">
            <span className="font-medium text-primary">{rotuloParte(pagina.parte)}</span>
            <span aria-hidden>/</span>
            <span>{pagina.parte.titulo}</span>
            <span aria-hidden>/</span>
            <span>
              {pagina.modulo.id} · {pagina.modulo.titulo}
            </span>
          </nav>
          <h1 className="text-3xl leading-tight font-bold tracking-tight text-balance sm:text-4xl">
            <span className="mr-2 align-middle font-mono text-lg font-medium text-muted-foreground sm:text-xl">{pagina.id}</span>
            {pagina.titulo}
          </h1>
          <p className="mt-3 text-lg leading-relaxed text-muted-foreground text-pretty">{pagina.descricao}</p>
          <div className="mt-5 flex flex-wrap items-center gap-2">
            <NivelBadge nivel={pagina.nivel} />
            <PrioridadeBadge prioridade={pagina.prioridade} />
            {meta && (
              <span className="inline-flex items-center gap-1 rounded-md border bg-muted/50 px-2 py-0.5 text-xs text-muted-foreground">
                <Clock className="size-3.5" aria-hidden /> {meta.minutos} min de leitura
              </span>
            )}
            <button
              type="button"
              onClick={() => alternarLida(pagina.id)}
              className={cn(
                'ml-auto inline-flex items-center gap-1.5 rounded-md border px-2 py-0.5 text-xs transition-colors',
                lida ? 'border-primary/40 bg-primary/10 text-primary' : 'text-muted-foreground hover:text-foreground',
              )}
              aria-pressed={lida}
            >
              {lida ? <CircleCheck className="size-3.5" /> : <Circle className="size-3.5" />}
              {lida ? 'Lida' : 'Marcar como lida'}
            </button>
          </div>
          <PreRequisitos pagina={pagina} />
          {status && rotulosStatus[status] && (
            <p className="mt-5 flex items-center gap-2 rounded-lg border border-dashed px-3 py-2 text-sm text-muted-foreground">
              <PencilLine className="size-4 shrink-0" aria-hidden />
              {rotulosStatus[status]}
            </p>
          )}
        </header>

        <div ref={artigo} className="prose-doc">
          {erro ? (
            <p className="text-destructive">Não foi possível carregar esta página: {erro}</p>
          ) : Conteudo ? (
            <PaginaContext.Provider value={{ chave: pagina.id, frontmatter: modulo.frontmatter }}>
              <Conteudo components={componentesMdx} />
            </PaginaContext.Provider>
          ) : (
            <Esqueleto />
          )}
        </div>

        <footer className="mt-16 space-y-6 border-t pt-8">
          <div className="flex flex-col items-center gap-3 rounded-xl border bg-muted/30 px-4 py-5 text-center sm:flex-row sm:text-left">
            <div className="flex-1">
              <p className="font-semibold">{lida ? 'Página concluída.' : 'Terminou de ler?'}</p>
              <p className="text-sm text-muted-foreground">O progresso fica salvo neste navegador.</p>
            </div>
            <Button variant={lida ? 'outline' : 'default'} onClick={() => alternarLida(pagina.id)}>
              {lida ? <CircleCheck /> : <Circle />}
              {lida ? 'Lida (desfazer)' : 'Marcar como lida'}
            </Button>
          </div>
          <PrevNext anterior={anterior} proxima={proxima} />
        </footer>
      </article>

      <aside className="sticky top-20 hidden max-h-[calc(100dvh-6rem)] w-56 shrink-0 self-start overflow-y-auto xl:block scrollbar-fina">
        {Conteudo && <Toc alvo={artigo} chave={pagina.id} />}
      </aside>
    </div>
  )
}

function PreRequisitos({ pagina }: { pagina: PaginaPlana }) {
  const lidas = useProgresso((s) => s.lidas)
  const pre = pagina.preRequisitos.map(paginaPorId).filter((p): p is PaginaPlana => !!p)
  if (!pre.length && !pagina.preRequisitosNota) return null
  return (
    <div className="mt-4 flex flex-wrap items-baseline gap-x-2 gap-y-1 text-sm">
      <span className="text-muted-foreground">Antes, leia:</span>
      {pagina.preRequisitosNota && <span className="text-muted-foreground">{pagina.preRequisitosNota}</span>}
      {pre.map((p) => (
        <Link key={p.id} to={p.url} className="inline-flex items-center gap-1 rounded-md border px-1.5 py-0.5 text-xs transition-colors hover:border-primary/50 hover:text-primary" title={p.titulo}>
          {lidas[p.id] && <CircleCheck className="size-3 text-primary" aria-label="lida" />}
          <span className="font-mono">{p.id}</span>
          <span className="max-w-48 truncate">{p.titulo}</span>
        </Link>
      ))}
    </div>
  )
}

function Esqueleto() {
  return (
    <div className="space-y-4" aria-label="Carregando">
      {[80, 95, 60, 90, 70].map((w, i) => (
        <div key={i} className="h-4 animate-pulse rounded bg-muted" style={{ width: `${w}%` }} />
      ))}
    </div>
  )
}
