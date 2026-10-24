import { useEffect, useRef } from 'react'
import { Toc } from '@/components/layout/toc'
import { componentesMdx } from '@/components/mdx'
import { PaginaContext } from '@/components/mdx/contexto'
import Guia, { frontmatter } from './componentes.mdx'

/** Página /componentes: demonstração de todos os componentes MDX, guia para a fase 3. */
export function Componentes() {
  const artigo = useRef<HTMLDivElement>(null)
  useEffect(() => {
    document.title = 'Guia de componentes · APIs REST com Spring Boot'
  }, [])
  return (
    <div className="mx-auto flex max-w-[78rem] gap-10 px-4 py-8 sm:px-6 lg:px-10 lg:py-10">
      <article className="min-w-0 max-w-3xl flex-1">
        <header className="mb-8">
          <p className="mb-2 text-xs font-semibold tracking-wide text-primary uppercase">Guia para autores</p>
          <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">{frontmatter.titulo}</h1>
          <p className="mt-3 text-lg leading-relaxed text-muted-foreground">{frontmatter.descricao}</p>
        </header>
        <div ref={artigo} className="prose-doc">
          <PaginaContext.Provider value={{ chave: 'componentes', frontmatter }}>
            <Guia components={componentesMdx} />
          </PaginaContext.Provider>
        </div>
      </article>
      <aside className="sticky top-20 hidden max-h-[calc(100dvh-6rem)] w-56 shrink-0 self-start overflow-y-auto xl:block scrollbar-fina">
        <Toc alvo={artigo} chave="componentes" />
      </aside>
    </div>
  )
}
