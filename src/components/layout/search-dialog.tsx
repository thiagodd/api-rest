import { Command } from 'cmdk'
import { BookMarked, CornerDownLeft, FileText, Hash, LayoutGrid, Library, Search } from 'lucide-react'
import { Fragment, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router'
import { Dialog, DialogContent, DialogDescription, DialogTitle } from '@/components/ui/dialog'
import { carregarIndiceBusca, normalizar, pesquisar, type ResultadoBusca } from '@/lib/busca'
import { useBusca } from '@/lib/busca-estado'
import { paginaPorId, paginas } from '@/lib/navegacao'
import { useProgresso } from '@/lib/progresso'

export function SearchDialog() {
  const aberta = useBusca((s) => s.aberta)
  const setAberta = useBusca((s) => s.setAberta)
  const [consulta, setConsulta] = useState('')
  const [pronto, setPronto] = useState(false)
  const [resultados, setResultados] = useState<ResultadoBusca[]>([])
  const navigate = useNavigate()
  const ultimaVisitada = useProgresso((s) => s.ultimaVisitada)

  useEffect(() => {
    if (!aberta || pronto) return
    let ativo = true
    carregarIndiceBusca().then(() => ativo && setPronto(true))
    return () => {
      ativo = false
    }
  }, [aberta, pronto])

  useEffect(() => {
    if (!pronto) return
    let ativo = true
    const id = setTimeout(() => {
      if (ativo) setResultados(consulta.trim() ? pesquisar(consulta) : [])
    }, 60)
    return () => {
      ativo = false
      clearTimeout(id)
    }
  }, [consulta, pronto])

  const ir = (url: string) => {
    setAberta(false)
    setConsulta('')
    navigate(url)
  }

  const sugestoes = useMemo(() => {
    const continuar = ultimaVisitada ? paginaPorId(ultimaVisitada) : undefined
    return [...(continuar ? [continuar] : []), ...paginas.slice(0, 4).filter((p) => p !== continuar)].slice(0, 4)
  }, [ultimaVisitada])

  const agrupados = useMemo(() => {
    const grupos = new Map<string, ResultadoBusca[]>()
    for (const r of resultados) grupos.set(r.pagina.id, [...(grupos.get(r.pagina.id) ?? []), r])
    return [...grupos.values()]
  }, [resultados])

  const termos = useMemo(() => normalizar(consulta).split(/\s+/).filter((t) => t.length > 1), [consulta])

  return (
    <Dialog open={aberta} onOpenChange={setAberta}>
      <DialogContent className="overflow-hidden p-0" aria-describedby="busca-descricao">
        <DialogTitle className="sr-only">Buscar na documentação</DialogTitle>
        <DialogDescription id="busca-descricao" className="sr-only">
          Busque por títulos e conteúdo das páginas. Use as setas para navegar e Enter para abrir.
        </DialogDescription>
        <Command shouldFilter={false} loop className="flex max-h-[min(70vh,560px)] flex-col">
          <div className="flex items-center gap-3 border-b px-4">
            <Search className="size-4 shrink-0 text-muted-foreground" aria-hidden />
            <Command.Input
              value={consulta}
              onValueChange={setConsulta}
              placeholder="Buscar: idempotência, ETag, ProblemDetail…"
              className="h-12 w-full bg-transparent text-sm outline-none placeholder:text-muted-foreground"
            />
            <kbd className="hidden rounded border bg-muted px-1.5 py-0.5 font-mono text-[10px] text-muted-foreground sm:inline">Esc</kbd>
          </div>
          <Command.List className="scrollbar-fina flex-1 overflow-y-auto p-2">
            {!pronto && consulta && <div className="py-10 text-center text-sm text-muted-foreground">Carregando o índice…</div>}
            {pronto && consulta.trim() && (
              <Command.Empty className="py-10 text-center text-sm text-muted-foreground">Nada encontrado para “{consulta}”.</Command.Empty>
            )}

            {!consulta.trim() && (
              <>
                <Command.Group heading="Sugestões" className="[&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:py-1.5 [&_[cmdk-group-heading]]:text-xs [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group-heading]]:text-muted-foreground">
                  {sugestoes.map((p, i) => (
                    <ItemBusca key={p.id} valor={p.url} aoSelecionar={() => ir(p.url)} icone={<FileText className="size-4" />}>
                      <span className="truncate">{p.titulo}</span>
                      <span className="ml-auto shrink-0 text-xs text-muted-foreground">{i === 0 && ultimaVisitada === p.id ? 'Continuar' : p.id}</span>
                    </ItemBusca>
                  ))}
                </Command.Group>
                <Command.Group heading="Atalhos" className="[&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:py-1.5 [&_[cmdk-group-heading]]:text-xs [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group-heading]]:text-muted-foreground">
                  <ItemBusca valor="/glossario" aoSelecionar={() => ir('/glossario')} icone={<BookMarked className="size-4" />}>Glossário</ItemBusca>
                  <ItemBusca valor="/referencias" aoSelecionar={() => ir('/referencias')} icone={<Library className="size-4" />}>Referências</ItemBusca>
                  <ItemBusca valor="/componentes" aoSelecionar={() => ir('/componentes')} icone={<LayoutGrid className="size-4" />}>Guia de componentes</ItemBusca>
                </Command.Group>
              </>
            )}

            {agrupados.map((grupo) => {
              const pagina = grupo[0].pagina
              return (
                <Command.Group
                  key={pagina.id}
                  heading={`${pagina.id} · ${pagina.titulo}`}
                  className="mb-1 [&_[cmdk-group-heading]]:truncate [&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:py-1.5 [&_[cmdk-group-heading]]:text-xs [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group-heading]]:text-muted-foreground"
                >
                  {grupo.map((r) => {
                    const url = r.ancora ? `${pagina.url}#${r.ancora}` : pagina.url
                    return (
                      <ItemBusca key={url} valor={url} aoSelecionar={() => ir(url)} icone={r.ancora ? <Hash className="size-4" /> : <FileText className="size-4" />}>
                        <span className="min-w-0 flex-1">
                          <span className="block truncate font-medium">
                            <Destaque texto={r.titulo} termos={termos} />
                          </span>
                          {r.trecho && (
                            <span className="line-clamp-2 text-xs text-muted-foreground">
                              <Destaque texto={r.trecho} termos={termos} />
                            </span>
                          )}
                        </span>
                      </ItemBusca>
                    )
                  })}
                </Command.Group>
              )
            })}
          </Command.List>
          <div className="flex items-center gap-4 border-t px-4 py-2 text-[11px] text-muted-foreground">
            <span className="flex items-center gap-1">
              <kbd className="rounded border bg-muted px-1 font-mono">↑</kbd>
              <kbd className="rounded border bg-muted px-1 font-mono">↓</kbd> navegar
            </span>
            <span className="flex items-center gap-1">
              <kbd className="rounded border bg-muted px-1 font-mono">
                <CornerDownLeft className="inline size-3" />
              </kbd>
              abrir
            </span>
          </div>
        </Command>
      </DialogContent>
    </Dialog>
  )
}

function ItemBusca({ valor, aoSelecionar, icone, children }: { valor: string; aoSelecionar: () => void; icone: React.ReactNode; children: React.ReactNode }) {
  return (
    <Command.Item
      value={valor}
      onSelect={aoSelecionar}
      className="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2 text-sm data-[selected=true]:bg-accent data-[selected=true]:text-accent-foreground"
    >
      <span className="shrink-0 text-muted-foreground">{icone}</span>
      {children}
    </Command.Item>
  )
}

/** Destaca os termos buscados ignorando acentos e maiúsculas. */
function Destaque({ texto, termos }: { texto: string; termos: string[] }) {
  if (!termos.length) return <>{texto}</>
  const normal = normalizar(texto)
  const marcas: [number, number][] = []
  for (const termo of termos) {
    let i = normal.indexOf(termo)
    while (i !== -1) {
      marcas.push([i, i + termo.length])
      i = normal.indexOf(termo, i + termo.length)
    }
  }
  if (!marcas.length) return <>{texto}</>
  marcas.sort((a, b) => a[0] - b[0])
  const partes: React.ReactNode[] = []
  let cursor = 0
  for (const [inicio, fim] of marcas) {
    if (inicio < cursor) continue
    partes.push(<Fragment key={`t${inicio}`}>{texto.slice(cursor, inicio)}</Fragment>)
    partes.push(
      <mark key={`m${inicio}`} className="rounded-sm bg-primary/20 text-inherit">
        {texto.slice(inicio, fim)}
      </mark>,
    )
    cursor = fim
  }
  partes.push(<Fragment key="fim">{texto.slice(cursor)}</Fragment>)
  return <>{partes}</>
}
