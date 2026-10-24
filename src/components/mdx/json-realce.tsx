import { Fragment } from 'react'

/**
 * Realce leve de JSON em tempo de execução (para corpos passados como props,
 * onde o Shiki do build não alcança). Cores seguem o tema do Shiki.
 */
const padrao = /("(?:\\.|[^"\\])*")(\s*:)?|\b(true|false|null)\b|(-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)|([{}[\],])/g

export function JsonRealce({ codigo }: { codigo: string }) {
  const partes: React.ReactNode[] = []
  let ultimo = 0
  for (const m of codigo.matchAll(padrao)) {
    const i = m.index ?? 0
    if (i > ultimo) partes.push(<Fragment key={`t${i}`}>{codigo.slice(ultimo, i)}</Fragment>)
    const [texto, string, doisPontos, literal, numero] = m
    let classe = 'text-muted-foreground'
    if (string) classe = doisPontos ? 'text-sky-700 dark:text-sky-300' : 'text-emerald-700 dark:text-emerald-300'
    else if (literal) classe = 'text-violet-700 dark:text-violet-300'
    else if (numero) classe = 'text-amber-700 dark:text-amber-300'
    partes.push(
      <span key={i} className={classe}>
        {string ?? texto}
      </span>,
    )
    if (string && doisPontos) partes.push(<Fragment key={`c${i}`}>{doisPontos}</Fragment>)
    ultimo = i + texto.length
  }
  partes.push(<Fragment key="fim">{codigo.slice(ultimo)}</Fragment>)
  return <>{partes}</>
}

export function formatarCorpo(corpo: unknown): { texto: string; json: boolean } {
  if (corpo === undefined || corpo === null) return { texto: '', json: false }
  if (typeof corpo === 'string') {
    try {
      return { texto: JSON.stringify(JSON.parse(corpo), null, 2), json: true }
    } catch {
      return { texto: corpo, json: false }
    }
  }
  return { texto: JSON.stringify(corpo, null, 2), json: true }
}
