import { Children, isValidElement, useId } from 'react'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { DentroDeAbas, rotuloLinguagem } from './code-block'

interface PropsFilho {
  'data-title'?: string
  'data-lang'?: string
}

/**
 * Mesmo exemplo em várias formas. Cada bloco de código filho vira uma aba;
 * o rótulo vem do `title="..."` do bloco ou, sem ele, da linguagem.
 */
export function CodeTabs({ children, title }: { children: React.ReactNode; title?: string }) {
  const base = useId()
  const blocos = Children.toArray(children).filter(isValidElement) as React.ReactElement<PropsFilho>[]
  const rotulos = blocos.map((b) => b.props['data-title'] ?? rotuloLinguagem(b.props['data-lang']))
  if (!blocos.length) return null

  return (
    <Tabs defaultValue={`${base}-0`} className="not-prose my-6 overflow-hidden rounded-lg border bg-code">
      <div className="flex items-center border-b bg-code-header">
        {title && <span className="hidden shrink-0 border-r px-3 text-xs font-medium text-muted-foreground sm:block">{title}</span>}
        <TabsList className="scrollbar-fina overflow-x-auto px-1">
          {rotulos.map((rotulo, i) => (
            <TabsTrigger key={i} value={`${base}-${i}`}>
              {rotulo}
            </TabsTrigger>
          ))}
        </TabsList>
      </div>
      <DentroDeAbas.Provider value={true}>
        {blocos.map((bloco, i) => (
          <TabsContent key={i} value={`${base}-${i}`}>
            {bloco}
          </TabsContent>
        ))}
      </DentroDeAbas.Provider>
    </Tabs>
  )
}
