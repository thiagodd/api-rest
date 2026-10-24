import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router'
import { Dialog, DialogDescription, DialogTitle, SheetContent } from '@/components/ui/dialog'
import { TooltipProvider } from '@/components/ui/tooltip'
import { Header, Logo } from './header'
import { useAtalhoBusca } from '@/lib/busca-estado'
import { SearchDialog } from './search-dialog'
import { Sidebar } from './sidebar'

export function AppLayout() {
  const [menuAberto, setMenuAberto] = useState(false)
  const { pathname, hash } = useLocation()
  useAtalhoBusca()

  // Volta ao topo ao trocar de página (âncoras são tratadas pela própria página)
  useEffect(() => {
    if (!hash) window.scrollTo({ top: 0 })
  }, [pathname, hash])

  return (
    <TooltipProvider>
      <a href="#conteudo" className="sr-only z-50 rounded bg-primary px-3 py-2 text-primary-foreground focus:not-sr-only focus:fixed focus:top-2 focus:left-2">
        Pular para o conteúdo
      </a>
      <Header aoAbrirMenu={() => setMenuAberto(true)} />
      <div className="flex">
        <aside className="sticky top-14 hidden h-[calc(100dvh-3.5rem)] w-72 shrink-0 border-r bg-sidebar lg:block xl:w-80">
          <Sidebar />
        </aside>
        <Dialog open={menuAberto} onOpenChange={setMenuAberto}>
          <SheetContent aria-describedby="menu-descricao">
            <DialogTitle className="sr-only">Navegação</DialogTitle>
            <DialogDescription id="menu-descricao" className="sr-only">
              Partes, módulos e páginas da documentação
            </DialogDescription>
            <div className="flex h-14 shrink-0 items-center border-b px-4">
              <Logo />
            </div>
            <div className="min-h-0 flex-1">
              <Sidebar aoNavegar={() => setMenuAberto(false)} />
            </div>
          </SheetContent>
        </Dialog>
        <main id="conteudo" className="min-w-0 flex-1">
          <Outlet />
        </main>
      </div>
      <SearchDialog />
    </TooltipProvider>
  )
}
