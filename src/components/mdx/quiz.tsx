import { CircleCheck, CircleHelp, CircleX, RotateCcw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useProgresso } from '@/lib/progresso'
import { cn } from '@/lib/utils'
import { usePagina } from './contexto'

export interface PerguntaQuiz {
  question: React.ReactNode
  options: React.ReactNode[]
  /** Índice (a partir de 0) da opção correta */
  answer: number
  explanation?: React.ReactNode
}

/** Perguntas de múltipla escolha com explicação. A resposta fica salva no navegador. */
export function Quiz({ id = 'quiz', title, questions }: { id?: string; title?: string; questions: PerguntaQuiz[] }) {
  const { chave } = usePagina()
  const chaveCompleta = `${chave}:${id}`
  const respostas = useProgresso((s) => s.quizzes[chaveCompleta])
  const responder = useProgresso((s) => s.responderQuiz)
  const respondidas = questions.filter((_, i) => respostas?.[i] != null).length
  const acertos = questions.filter((q, i) => respostas?.[i] === q.answer).length

  return (
    <section className="not-prose my-8 overflow-hidden rounded-xl border bg-card">
      <header className="flex items-center gap-2 border-b bg-muted/40 px-4 py-3">
        <CircleHelp className="size-4.5 text-primary" aria-hidden />
        <span className="font-semibold">{title ?? 'Teste seu entendimento'}</span>
        <span className="ml-auto text-xs text-muted-foreground tabular-nums">
          {respondidas === questions.length ? `${acertos} de ${questions.length} certas` : `${respondidas}/${questions.length} respondidas`}
        </span>
      </header>
      <ol className="divide-y">
        {questions.map((q, i) => {
          const escolhida = respostas?.[i] ?? null
          const respondida = escolhida !== null
          const acertou = escolhida === q.answer
          return (
            <li key={i} className="px-4 py-4">
              <p className="mb-3 font-medium leading-snug">
                <span className="mr-2 font-mono text-xs text-muted-foreground">{i + 1}.</span>
                {q.question}
              </p>
              <div className="grid gap-2" role="radiogroup">
                {q.options.map((opcao, j) => {
                  const correta = j === q.answer
                  const marcada = j === escolhida
                  return (
                    <button
                      key={j}
                      type="button"
                      role="radio"
                      aria-checked={marcada}
                      disabled={respondida}
                      onClick={() => responder(chaveCompleta, i, j, questions.length)}
                      className={cn(
                        'flex items-start gap-3 rounded-lg border px-3 py-2.5 text-left text-sm transition-colors',
                        !respondida && 'hover:border-primary/50 hover:bg-accent/40',
                        respondida && correta && 'border-[var(--callout-tip)] bg-[color-mix(in_oklch,var(--callout-tip)_10%,transparent)]',
                        respondida && marcada && !correta && 'border-[var(--callout-danger)] bg-[color-mix(in_oklch,var(--callout-danger)_10%,transparent)]',
                        respondida && !correta && !marcada && 'opacity-60',
                      )}
                    >
                      <span className="mt-px flex size-5 shrink-0 items-center justify-center rounded-full border font-mono text-[11px]">
                        {respondida && correta ? (
                          <CircleCheck className="size-5 text-[var(--callout-tip)]" />
                        ) : respondida && marcada ? (
                          <CircleX className="size-5 text-[var(--callout-danger)]" />
                        ) : (
                          String.fromCharCode(65 + j)
                        )}
                      </span>
                      <span className="leading-relaxed">{opcao}</span>
                    </button>
                  )
                })}
              </div>
              {respondida && (
                <div className="mt-3 flex items-start gap-2 rounded-md bg-muted/50 px-3 py-2.5 text-sm leading-relaxed" aria-live="polite">
                  <span className={cn('font-semibold', acertou ? 'text-[var(--callout-tip)]' : 'text-[var(--callout-danger)]')}>{acertou ? 'Correto.' : 'Não é essa.'}</span>
                  <span className="flex-1 text-muted-foreground">{q.explanation}</span>
                  <button type="button" className="text-muted-foreground hover:text-foreground" onClick={() => responder(chaveCompleta, i, null, questions.length)} aria-label="Tentar de novo" title="Tentar de novo">
                    <RotateCcw className="size-3.5" />
                  </button>
                </div>
              )}
            </li>
          )
        })}
      </ol>
      {respondidas > 0 && (
        <footer className="border-t px-4 py-2.5 text-right">
          <Button variant="ghost" size="sm" onClick={() => questions.forEach((_, i) => responder(chaveCompleta, i, null, questions.length))}>
            <RotateCcw /> Refazer o quiz
          </Button>
        </footer>
      )}
    </section>
  )
}
