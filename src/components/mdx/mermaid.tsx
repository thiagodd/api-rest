import { useEffect, useId, useState } from 'react'
import { useTema } from '@/lib/tema'

// Mermaid calcula cores derivadas com khroma, que não entende oklch: as cores ficam em hex aqui.
const temas = {
  claro: {
    background: '#ffffff',
    primaryColor: '#ecfdf5',
    primaryBorderColor: '#6ab89a',
    primaryTextColor: '#1f2937',
    secondaryColor: '#eff6ff',
    secondaryBorderColor: '#8fb3e6',
    tertiaryColor: '#f8fafc',
    tertiaryBorderColor: '#cbd5e1',
    lineColor: '#64748b',
    textColor: '#1f2937',
    mainBkg: '#ecfdf5',
    nodeBorder: '#6ab89a',
    clusterBkg: '#f8fafc',
    clusterBorder: '#cbd5e1',
    edgeLabelBackground: '#ffffff',
    actorBkg: '#ecfdf5',
    actorBorder: '#6ab89a',
    actorTextColor: '#1f2937',
    actorLineColor: '#94a3b8',
    signalColor: '#334155',
    signalTextColor: '#1f2937',
    labelBoxBkgColor: '#eff6ff',
    labelBoxBorderColor: '#8fb3e6',
    labelTextColor: '#1f2937',
    loopTextColor: '#1f2937',
    noteBkgColor: '#fffbeb',
    noteBorderColor: '#f0c674',
    noteTextColor: '#1f2937',
    activationBkgColor: '#d1fae5',
    activationBorderColor: '#6ab89a',
    sequenceNumberColor: '#ffffff',
  },
  escuro: {
    background: '#1c2128',
    primaryColor: '#1f3a33',
    primaryBorderColor: '#4f9a7f',
    primaryTextColor: '#e5e7eb',
    secondaryColor: '#1e2a3d',
    secondaryBorderColor: '#5577aa',
    tertiaryColor: '#22272e',
    tertiaryBorderColor: '#444c56',
    lineColor: '#8b949e',
    textColor: '#e5e7eb',
    mainBkg: '#1f3a33',
    nodeBorder: '#4f9a7f',
    clusterBkg: '#22272e',
    clusterBorder: '#444c56',
    edgeLabelBackground: '#22272e',
    actorBkg: '#1f3a33',
    actorBorder: '#4f9a7f',
    actorTextColor: '#e5e7eb',
    actorLineColor: '#636e7b',
    signalColor: '#c9d1d9',
    signalTextColor: '#e5e7eb',
    labelBoxBkgColor: '#1e2a3d',
    labelBoxBorderColor: '#5577aa',
    labelTextColor: '#e5e7eb',
    loopTextColor: '#e5e7eb',
    noteBkgColor: '#3a3220',
    noteBorderColor: '#a0823c',
    noteTextColor: '#e5e7eb',
    activationBkgColor: '#26463d',
    activationBorderColor: '#4f9a7f',
    sequenceNumberColor: '#1c2128',
  },
}

// Renderizações em fila: o mermaid usa estado global e não lida bem com chamadas simultâneas
let fila: Promise<unknown> = Promise.resolve()

async function renderizar(id: string, chart: string, escuro: boolean) {
  const { default: mermaid } = await import('mermaid')
  const tarefa = fila.then(async () => {
    mermaid.initialize({
      startOnLoad: false,
      securityLevel: 'strict',
      theme: 'base',
      themeVariables: { ...(escuro ? temas.escuro : temas.claro), fontFamily: 'Inter Variable, ui-sans-serif, system-ui, sans-serif', fontSize: '14px' },
      flowchart: { curve: 'basis', padding: 12, htmlLabels: true },
      sequence: { mirrorActors: false, showSequenceNumbers: false, actorMargin: 60 },
    })
    return mermaid.render(id, chart)
  })
  fila = tarefa.catch(() => undefined)
  return tarefa
}

/** Diagrama Mermaid que acompanha o tema claro/escuro. Também é gerado a partir de blocos ```mermaid. */
export function Mermaid({ chart, title, caption }: { chart: string; title?: string; caption?: string }) {
  const escuro = useTema((s) => s.escuro)
  const id = `mermaid-${useId().replace(/[^a-zA-Z0-9]/g, '')}`
  const [resultado, setResultado] = useState<{ svg?: string; erro?: string; chave: string }>({ chave: '' })
  const chave = `${escuro}:${chart}`

  useEffect(() => {
    let ativo = true
    renderizar(`${id}-${escuro ? 'e' : 'c'}`, chart.trim(), escuro)
      .then(({ svg }) => ativo && setResultado({ svg, chave }))
      .catch((e: unknown) => ativo && setResultado({ erro: e instanceof Error ? e.message : String(e), chave }))
    return () => {
      ativo = false
    }
  }, [chart, escuro, id, chave])

  const carregando = resultado.chave !== chave && !resultado.svg

  return (
    <figure className="not-prose my-6 overflow-hidden rounded-lg border bg-card">
      {title && <figcaption className="border-b px-4 py-2 text-xs font-medium text-muted-foreground">{title}</figcaption>}
      <div className="scrollbar-fina overflow-x-auto p-4">
        {resultado.erro ? (
          <pre className="text-xs whitespace-pre-wrap text-destructive">Erro no diagrama Mermaid: {resultado.erro}</pre>
        ) : carregando ? (
          <div className="h-40 animate-pulse rounded-md bg-muted" aria-label="Carregando diagrama" />
        ) : (
          <div className="mermaid-diagram" role="img" aria-label={title ?? 'Diagrama'} dangerouslySetInnerHTML={{ __html: resultado.svg ?? '' }} />
        )}
      </div>
      {caption && <p className="border-t px-4 py-2 text-xs text-muted-foreground">{caption}</p>}
    </figure>
  )
}
