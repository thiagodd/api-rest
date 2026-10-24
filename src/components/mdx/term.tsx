import { Link } from 'react-router'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { glossario } from '@/content/glossario'
import { paginaPorId } from '@/lib/navegacao'

/**
 * Marca um termo do glossário. Mostra a definição em tooltip (hover/foco) e
 * em popover no toque. `id` é a chave em src/content/glossario.ts.
 */
export function Term({ id, children }: { id: string; children?: React.ReactNode }) {
  const termo = glossario.find((t) => t.id === id)
  if (!termo) {
    if (import.meta.env.DEV) console.warn(`<Term>: termo "${id}" não existe em glossario.ts`)
    return <>{children ?? id}</>
  }
  const pagina = termo.pagina ? paginaPorId(termo.pagina) : undefined
  const conteudo = (
    <>
      <span className="block font-semibold text-foreground">
        {termo.termo}
        {termo.ingles && <span className="ml-1 font-normal text-muted-foreground italic">({termo.ingles})</span>}
      </span>
      <span className="mt-1 block text-muted-foreground">{termo.definicao}</span>
      {pagina && (
        <Link to={pagina.url} className="mt-2 block text-primary hover:underline">
          Visto em {pagina.id} · {pagina.titulo}
        </Link>
      )}
    </>
  )
  const gatilho = (
    <button type="button" className="cursor-help border-b border-dotted border-primary/70 font-[inherit] text-inherit decoration-0 hover:border-solid hover:text-primary">
      {children ?? termo.termo}
    </button>
  )

  return (
    <Popover>
      <Tooltip>
        <TooltipTrigger asChild>
          <PopoverTrigger asChild>{gatilho}</PopoverTrigger>
        </TooltipTrigger>
        <TooltipContent className="max-w-xs text-[13px]">{conteudo}</TooltipContent>
      </Tooltip>
      <PopoverContent className="text-[13px] leading-relaxed">{conteudo}</PopoverContent>
    </Popover>
  )
}
