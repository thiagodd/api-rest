import { Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router'
import { glossario } from '@/content/glossario'
import { normalizar } from '@/lib/busca'
import { paginaPorId } from '@/lib/navegacao'

/** Lista completa do glossário, com filtro e agrupamento por letra. */
export function Glossary() {
  const [filtro, setFiltro] = useState('')
  const grupos = useMemo(() => {
    const termo = normalizar(filtro.trim())
    const lista = [...glossario]
      .filter((t) => !termo || normalizar(`${t.termo} ${t.ingles ?? ''} ${t.definicao}`).includes(termo))
      .sort((a, b) => a.termo.localeCompare(b.termo, 'pt-BR'))
    const porLetra = new Map<string, typeof lista>()
    for (const t of lista) {
      const letra = normalizar(t.termo[0]).toUpperCase()
      porLetra.set(letra, [...(porLetra.get(letra) ?? []), t])
    }
    return [...porLetra.entries()]
  }, [filtro])

  return (
    <div className="not-prose my-6">
      <label className="mb-6 flex items-center gap-2 rounded-lg border bg-card px-3 focus-within:ring-2 focus-within:ring-ring/50">
        <Search className="size-4 text-muted-foreground" aria-hidden />
        <input
          value={filtro}
          onChange={(e) => setFiltro(e.target.value)}
          placeholder={`Filtrar ${glossario.length} termos…`}
          className="h-10 w-full bg-transparent text-sm outline-none"
          aria-label="Filtrar termos do glossário"
        />
      </label>
      {grupos.length === 0 && <p className="text-sm text-muted-foreground">Nenhum termo encontrado.</p>}
      <div className="space-y-8">
        {grupos.map(([letra, termos]) => (
          <section key={letra} aria-labelledby={`letra-${letra}`}>
            <h3 id={`letra-${letra}`} className="mb-2 font-mono text-sm font-semibold text-primary">
              {letra}
            </h3>
            <dl className="divide-y rounded-lg border">
              {termos.map((t) => {
                const pagina = t.pagina ? paginaPorId(t.pagina) : undefined
                return (
                  <div key={t.id} id={`termo-${t.id}`} className="grid gap-1 px-4 py-3 sm:grid-cols-[14rem_1fr] sm:gap-4">
                    <dt className="font-semibold leading-snug">
                      {t.termo}
                      {t.ingles && <span className="block text-xs font-normal text-muted-foreground italic">{t.ingles}</span>}
                    </dt>
                    <dd className="text-sm leading-relaxed text-muted-foreground">
                      {t.definicao}
                      {pagina && (
                        <Link to={pagina.url} className="mt-1 block text-xs font-medium text-primary hover:underline">
                          {pagina.id} · {pagina.titulo}
                        </Link>
                      )}
                    </dd>
                  </div>
                )
              })}
            </dl>
          </section>
        ))}
      </div>
    </div>
  )
}
