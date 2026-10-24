import { ExternalLink, Library, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { IconeParte } from '@/components/icone'
import { normalizar } from '@/lib/busca'
import { meta, type Referencia } from '@/lib/conteudo'
import { navigation, paginasDaParte, rotuloParte, type PaginaPlana } from '@/lib/navegacao'

interface FonteConsolidada extends Referencia {
  paginas: PaginaPlana[]
}

function dominio(url: string) {
  try {
    return new URL(url).hostname.replace(/^www\./, '')
  } catch {
    return ''
  }
}

/** Referências de todas as páginas, sem repetição, agrupadas pela parte em que aparecem primeiro. */
function consolidar() {
  const porChave = new Map<string, FonteConsolidada>()
  const grupos = navigation.map((parte) => ({ parte, fontes: [] as FonteConsolidada[] }))
  navigation.forEach((parte, i) => {
    for (const pagina of paginasDaParte(parte)) {
      for (const r of meta[pagina.id]?.referencias ?? []) {
        const chave = (r.url ?? r.titulo).replace(/\/+$/, '').toLowerCase()
        const existente = porChave.get(chave)
        if (existente) {
          if (!existente.paginas.includes(pagina)) existente.paginas.push(pagina)
          continue
        }
        const nova = { ...r, paginas: [pagina] }
        porChave.set(chave, nova)
        grupos[i].fontes.push(nova)
      }
    }
  })
  return { grupos: grupos.filter((g) => g.fontes.length), total: porChave.size }
}

// As referências vêm do build (frontmatter): calculadas uma vez
const { grupos, total } = consolidar()

export function Referencias() {
  const [filtro, setFiltro] = useState('')
  const termo = normalizar(filtro.trim())

  useEffect(() => {
    document.title = 'Referências · APIs REST com Spring Boot'
  }, [])

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-10">
      <header className="mb-8">
        <p className="mb-2 flex items-center gap-2 text-xs font-semibold tracking-wide text-primary uppercase">
          <Library className="size-4" aria-hidden /> Fontes
        </p>
        <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">Referências</h1>
        <p className="mt-3 text-lg text-muted-foreground">
          {total} fontes citadas nas páginas, sem repetição, agrupadas pela parte em que aparecem primeiro. Cada uma indica as páginas que a usam.
        </p>
      </header>
      <label className="mb-8 flex items-center gap-2 rounded-lg border bg-card px-3 focus-within:ring-2 focus-within:ring-ring/50">
        <Search className="size-4 text-muted-foreground" aria-hidden />
        <input value={filtro} onChange={(e) => setFiltro(e.target.value)} placeholder="Filtrar por título ou domínio (ex.: rfc, spring, zalando)…" className="h-10 w-full bg-transparent text-sm outline-none" aria-label="Filtrar referências" />
      </label>
      <div className="space-y-10">
        {grupos.map(({ parte, fontes }) => {
          const visiveis = fontes.filter((f) => !termo || normalizar(`${f.titulo} ${f.url ?? ''}`).includes(termo))
          if (!visiveis.length) return null
          return (
            <section key={parte.slug} aria-labelledby={`ref-${parte.slug}`}>
              <h2 id={`ref-${parte.slug}`} className="mb-3 flex items-center gap-2 text-lg font-semibold">
                <IconeParte nome={parte.icone} className="size-4.5 text-primary" />
                <span className="text-sm font-medium text-muted-foreground">{rotuloParte(parte)}</span> {parte.titulo}
              </h2>
              <ul className="divide-y rounded-lg border bg-card">
                {visiveis.map((f) => (
                  <li key={f.url ?? f.titulo} className="flex flex-col gap-1.5 px-4 py-3 text-sm sm:flex-row sm:items-start sm:gap-4">
                    <div className="min-w-0 flex-1">
                      {f.url ? (
                        <a href={f.url} target="_blank" rel="noreferrer" className="group">
                          <span className="font-medium group-hover:text-primary group-hover:underline">{f.titulo}</span>
                          <span className="mt-0.5 flex items-center gap-1 text-xs break-all text-muted-foreground">
                            {dominio(f.url)} <ExternalLink className="size-3 shrink-0" aria-hidden />
                          </span>
                        </a>
                      ) : (
                        <span className="text-muted-foreground">{f.titulo}</span>
                      )}
                    </div>
                    <div className="flex shrink-0 flex-wrap gap-1 sm:max-w-56 sm:justify-end">
                      {f.paginas.map((p) => (
                        <Link key={p.id} to={p.url} title={p.titulo} className="rounded border px-1.5 py-px font-mono text-[11px] text-muted-foreground transition-colors hover:border-primary/50 hover:text-primary">
                          {p.id}
                        </Link>
                      ))}
                    </div>
                  </li>
                ))}
              </ul>
            </section>
          )
        })}
      </div>
    </div>
  )
}
