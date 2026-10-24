import { ArrowLeft, ArrowRight } from 'lucide-react'
import { Link } from 'react-router'
import type { PaginaPlana } from '@/lib/navegacao'
import { cn } from '@/lib/utils'

export function PrevNext({ anterior, proxima }: { anterior?: PaginaPlana; proxima?: PaginaPlana }) {
  return (
    <nav aria-label="Páginas anterior e próxima" className="grid gap-3 sm:grid-cols-2">
      {anterior ? <Cartao pagina={anterior} direcao="anterior" /> : <span />}
      {proxima && <Cartao pagina={proxima} direcao="proxima" />}
    </nav>
  )
}

function Cartao({ pagina, direcao }: { pagina: PaginaPlana; direcao: 'anterior' | 'proxima' }) {
  const proxima = direcao === 'proxima'
  return (
    <Link
      to={pagina.url}
      rel={proxima ? 'next' : 'prev'}
      className={cn(
        'group flex items-center gap-3 rounded-lg border p-4 transition-colors hover:border-primary/50 hover:bg-accent/40',
        proxima && 'flex-row-reverse text-right sm:col-start-2',
      )}
    >
      {proxima ? (
        <ArrowRight className="size-4 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5" aria-hidden />
      ) : (
        <ArrowLeft className="size-4 shrink-0 text-muted-foreground transition-transform group-hover:-translate-x-0.5" aria-hidden />
      )}
      <span className="min-w-0">
        <span className="block text-xs text-muted-foreground">{proxima ? 'Próximo' : 'Anterior'}</span>
        <span className="block font-medium leading-snug">
          <span className="font-mono text-xs text-muted-foreground">{pagina.id}</span> {pagina.titulo}
        </span>
      </span>
    </Link>
  )
}
