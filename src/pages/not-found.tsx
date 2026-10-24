import { Link } from 'react-router'
import { Button } from '@/components/ui/button'

export function NotFound() {
  return (
    <div className="mx-auto flex max-w-xl flex-col items-center px-4 py-24 text-center">
      <p className="font-mono text-sm font-semibold text-[var(--status-4xx)]">HTTP/1.1 404 Not Found</p>
      <h1 className="mt-3 text-3xl font-bold tracking-tight">Recurso não encontrado</h1>
      <p className="mt-3 text-muted-foreground">Esta página não existe (ou mudou de endereço). Tente a busca com Ctrl+K.</p>
      <Button asChild className="mt-6">
        <Link to="/">Voltar para o início</Link>
      </Button>
    </div>
  )
}
