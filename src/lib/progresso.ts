import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { armazenamentoSeguro, PREFIXO } from './armazenamento'

interface EstadoProgresso {
  /** id da página → data em que foi marcada como lida (ISO) */
  lidas: Record<string, string>
  /** última página visitada */
  ultimaVisitada?: string
  /** marcações dos <Checklist>: chave → itens marcados */
  checklists: Record<string, string[]>
  /** respostas dos <Quiz>: chave → índice escolhido por pergunta */
  quizzes: Record<string, (number | null)[]>
  /** mostrar somente as páginas essenciais na sidebar */
  somenteEssenciais: boolean
  alternarLida: (id: string) => void
  marcarLida: (id: string, lida: boolean) => void
  visitar: (id: string) => void
  alternarItemChecklist: (chave: string, item: string) => void
  responderQuiz: (chave: string, pergunta: number, opcao: number | null, total: number) => void
  setSomenteEssenciais: (valor: boolean) => void
  reiniciar: () => void
}

export const useProgresso = create<EstadoProgresso>()(
  persist(
    (set) => ({
      lidas: {},
      checklists: {},
      quizzes: {},
      somenteEssenciais: false,
      alternarLida: (id) =>
        set((s) => {
          const lidas = { ...s.lidas }
          if (lidas[id]) delete lidas[id]
          else lidas[id] = new Date().toISOString()
          return { lidas }
        }),
      marcarLida: (id, lida) =>
        set((s) => {
          const lidas = { ...s.lidas }
          if (lida) lidas[id] ??= new Date().toISOString()
          else delete lidas[id]
          return { lidas }
        }),
      visitar: (id) => set({ ultimaVisitada: id }),
      alternarItemChecklist: (chave, item) =>
        set((s) => {
          const atuais = s.checklists[chave] ?? []
          const proximos = atuais.includes(item) ? atuais.filter((i) => i !== item) : [...atuais, item]
          return { checklists: { ...s.checklists, [chave]: proximos } }
        }),
      responderQuiz: (chave, pergunta, opcao, total) =>
        set((s) => {
          const respostas = [...(s.quizzes[chave] ?? Array<number | null>(total).fill(null))]
          respostas[pergunta] = opcao
          return { quizzes: { ...s.quizzes, [chave]: respostas } }
        }),
      setSomenteEssenciais: (valor) => set({ somenteEssenciais: valor }),
      reiniciar: () => set({ lidas: {}, ultimaVisitada: undefined, checklists: {}, quizzes: {} }),
    }),
    { name: `${PREFIXO}progresso`, storage: armazenamentoSeguro, version: 1 },
  ),
)
