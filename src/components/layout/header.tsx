import { Menu, Search } from 'lucide-react'
import { Link } from 'react-router'
import { Button } from '@/components/ui/button'
import { useBusca } from '@/lib/busca-estado'
import { ThemeToggle } from './theme-toggle'

export function Logo() {
  return (
    <Link to="/" className="flex items-center gap-2.5 font-semibold tracking-tight">
      <img src="/favicon.svg" alt="" className="size-7" />
      <span className="leading-tight">
        APIs REST
        <span className="block text-[11px] font-medium text-muted-foreground">com Spring Boot 4</span>
      </span>
    </Link>
  )
}

export function Header({ aoAbrirMenu }: { aoAbrirMenu: () => void }) {
  const setAberta = useBusca((s) => s.setAberta)
  const mac = typeof navigator !== 'undefined' && /Mac|iPhone|iPad/.test(navigator.platform)
  return (
    <header className="sticky top-0 z-40 border-b bg-background/85 backdrop-blur supports-[backdrop-filter]:bg-background/70">
      <div className="flex h-14 items-center gap-2 px-4 lg:px-6">
        <Button variant="ghost" size="icon" className="-ml-2 lg:hidden" onClick={aoAbrirMenu} aria-label="Abrir navegação">
          <Menu />
        </Button>
        <Logo />
        <div className="ml-auto flex items-center gap-1">
          <button
            type="button"
            onClick={() => setAberta(true)}
            className="flex h-9 items-center gap-2 rounded-md border bg-muted/40 px-3 text-sm text-muted-foreground transition-colors hover:bg-muted sm:w-64"
            aria-label="Buscar na documentação"
          >
            <Search className="size-4" aria-hidden />
            <span className="hidden sm:inline">Buscar…</span>
            <kbd className="ml-auto hidden rounded border bg-background px-1.5 font-mono text-[10px] sm:inline">{mac ? '⌘' : 'Ctrl'} K</kbd>
          </button>
          <ThemeToggle />
        </div>
      </div>
    </header>
  )
}
