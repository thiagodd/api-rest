import { Check, Copy, FileCode2, Terminal } from 'lucide-react'
import { createContext, useContext, useRef, useState } from 'react'
import { cn } from '@/lib/utils'

/** Dentro de <CodeTabs> o cabeçalho é desenhado pelas abas, não pelo bloco. */
export const DentroDeAbas = createContext(false)

const rotulosLinguagem: Record<string, string> = {
  java: 'Java', kotlin: 'Kotlin', bash: 'Terminal', shell: 'Terminal', http: 'HTTP', json: 'JSON', jsonc: 'JSON',
  yaml: 'YAML', xml: 'XML', properties: 'Properties', sql: 'SQL', javascript: 'JavaScript', typescript: 'TypeScript',
  tsx: 'TSX', html: 'HTML', css: 'CSS', dockerfile: 'Dockerfile', groovy: 'Groovy', graphql: 'GraphQL', proto: 'Protobuf',
  diff: 'Diff', ini: 'INI', toml: 'TOML', markdown: 'Markdown', csv: 'CSV', txt: 'Texto', nginx: 'Nginx',
}

export function rotuloLinguagem(lang?: string) {
  return lang ? (rotulosLinguagem[lang] ?? lang) : 'Texto'
}

export function BotaoCopiar({ obterTexto, className }: { obterTexto: () => string; className?: string }) {
  const [copiado, setCopiado] = useState(false)
  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(obterTexto())
      setCopiado(true)
      setTimeout(() => setCopiado(false), 1600)
    } catch {
      /* área de transferência indisponível */
    }
  }
  return (
    <button
      type="button"
      onClick={copiar}
      className={cn(
        'inline-flex size-7 items-center justify-center rounded-md text-muted-foreground transition-colors hover:bg-background hover:text-foreground',
        className,
      )}
      aria-label={copiado ? 'Copiado' : 'Copiar código'}
      title={copiado ? 'Copiado!' : 'Copiar'}
    >
      {copiado ? <Check className="size-3.5 text-primary" /> : <Copy className="size-3.5" />}
    </button>
  )
}

type PropsPre = React.ComponentProps<'pre'> & { 'data-title'?: string; 'data-lang'?: string; 'data-no-copy'?: string }

/** Substitui o <pre> do MDX: cabeçalho com nome do arquivo/linguagem e botão de copiar. */
export function CodeBlock({ children, className, ...props }: PropsPre) {
  const ref = useRef<HTMLPreElement>(null)
  const emAbas = useContext(DentroDeAbas)
  const titulo = props['data-title']
  const lang = props['data-lang']
  const terminal = lang === 'bash' || lang === 'shell'
  const obterTexto = () => ref.current?.querySelector('code')?.innerText.replace(/\n$/, '') ?? ''
  const pre = (
    <pre ref={ref} className={cn(className, emAbas && 'rounded-none border-0')} {...props}>
      {children}
    </pre>
  )

  if (emAbas) {
    return (
      <div className="relative">
        {pre}
        <BotaoCopiar obterTexto={obterTexto} className="absolute top-2 right-2 bg-code/80" />
      </div>
    )
  }

  return (
    <div className="not-prose group/codigo my-6 overflow-hidden rounded-lg border bg-code">
      <div className="flex h-10 items-center gap-2 border-b bg-code-header px-3 text-xs text-muted-foreground">
        {terminal ? <Terminal className="size-3.5" aria-hidden /> : <FileCode2 className="size-3.5" aria-hidden />}
        <span className={cn('truncate', titulo && 'font-mono text-foreground')}>{titulo ?? rotuloLinguagem(lang)}</span>
        {titulo && <span className="ml-1 hidden rounded bg-background/60 px-1.5 py-px text-[10px] sm:inline">{rotuloLinguagem(lang)}</span>}
        {props['data-no-copy'] ? null : <BotaoCopiar obterTexto={obterTexto} className="ml-auto" />}
      </div>
      {pre}
    </div>
  )
}
