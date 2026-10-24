import { useEffect, useState } from 'react'
import { cn } from '@/lib/utils'

interface ItemToc {
  id: string
  texto: string
  nivel: 2 | 3
}

/**
 * Índice "Nesta página": lê os h2/h3 do artigo renderizado e destaca a
 * seção atual (o último cabeçalho que já passou do topo da tela).
 */
export function Toc({ alvo, chave }: { alvo: React.RefObject<HTMLElement | null>; chave: string }) {
  const [itens, setItens] = useState<ItemToc[]>([])
  const [ativo, setAtivo] = useState<string>('')

  useEffect(() => {
    const raiz = alvo.current
    if (!raiz) return
    const cabecalhos = [...raiz.querySelectorAll<HTMLHeadingElement>(':scope > h2[id], :scope > h3[id]')]
    const aoRolar = () => {
      // 96px ≈ altura do cabeçalho fixo + folga
      let atual = cabecalhos[0].id
      for (const h of cabecalhos) if (h.getBoundingClientRect().top - 96 <= 0) atual = h.id
      setAtivo(atual)
    }
    // Lê o DOM no próximo frame, depois que o MDX foi pintado
    const frame = requestAnimationFrame(() => {
      setItens(cabecalhos.map((h) => ({ id: h.id, texto: h.textContent ?? '', nivel: h.tagName === 'H2' ? 2 : 3 })))
      if (cabecalhos.length) aoRolar()
    })
    if (cabecalhos.length) window.addEventListener('scroll', aoRolar, { passive: true })
    return () => {
      cancelAnimationFrame(frame)
      window.removeEventListener('scroll', aoRolar)
    }
  }, [alvo, chave])

  if (!itens.length) return null

  return (
    <nav aria-label="Nesta página" className="text-sm">
      <p className="mb-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase">Nesta página</p>
      <ul className="space-y-1 border-l">
        {itens.map((item) => (
          <li key={item.id}>
            <a
              href={`#${item.id}`}
              className={cn(
                '-ml-px block border-l-2 border-transparent py-1 leading-snug text-muted-foreground transition-colors hover:text-foreground',
                item.nivel === 2 ? 'pl-3' : 'pl-6 text-[13px]',
                ativo === item.id && 'border-primary font-medium text-foreground',
              )}
            >
              {item.texto}
            </a>
          </li>
        ))}
      </ul>
    </nav>
  )
}
