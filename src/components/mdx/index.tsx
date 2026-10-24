import type { MDXComponents } from 'mdx/types'
import { lazy, Suspense } from 'react'
import { Link } from 'react-router'
import { Bad, BadVsGood, Good } from './bad-vs-good'
import { Callout } from './callout'
import { Checklist } from './checklist'
import { CodeBlock } from './code-block'
import { CodeTabs } from './code-tabs'
import { Comparison } from './comparison'
import { DeepDive } from './deep-dive'
import { Draft } from './draft'
import { Endpoint } from './endpoint'
import { Exercise, Hint, Solution } from './exercise'
import { FileTree } from './file-tree'
import { Glossary } from './glossary'
import { HttpExchange } from './http-exchange'
import { Mermaid } from './mermaid'
import { ProjectStep } from './project-step'
import { Quiz } from './quiz'
import { References } from './references'
import { StatusCodeTable } from './status-code-table'
import { Step, Steps } from './steps'
import { Term } from './term'

// O AnimatedFlow usa o Motion (pesado): carregado só nas páginas que o usam
const AnimatedFlowLazy = lazy(() => import('./animated-flow').then((m) => ({ default: m.AnimatedFlow })))

function AnimatedFlow(props: React.ComponentProps<typeof AnimatedFlowLazy>) {
  return (
    <Suspense fallback={<div className="not-prose my-8 h-80 animate-pulse rounded-xl border bg-muted/40" />}>
      <AnimatedFlowLazy {...props} />
    </Suspense>
  )
}

/** Componentes disponíveis em todo MDX, sem import. */
export const componentesMdx: MDXComponents = {
  pre: CodeBlock,
  // Links externos abrem em nova aba; internos navegam sem recarregar a página
  a: ({ href = '', ...props }) => {
    if (/^https?:\/\//.test(href)) return <a href={href} target="_blank" rel="noreferrer" {...props} />
    if (href.startsWith('/')) return <Link to={href} {...props} />
    return <a href={href} {...props} />
  },
  AnimatedFlow,
  Bad,
  BadVsGood,
  Callout,
  Checklist,
  CodeTabs,
  Comparison,
  DeepDive,
  Draft,
  Endpoint,
  Exercise,
  FileTree,
  Glossary,
  Good,
  Hint,
  HttpExchange,
  Mermaid,
  ProjectStep,
  Quiz,
  References,
  Solution,
  StatusCodeTable,
  Step,
  Steps,
  Term,
}
