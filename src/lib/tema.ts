import { create } from 'zustand'
import { PREFIXO } from './armazenamento'

export type PreferenciaTema = 'claro' | 'escuro' | 'sistema'

interface EstadoTema {
  preferencia: PreferenciaTema
  /** tema efetivamente aplicado */
  escuro: boolean
  setPreferencia: (p: PreferenciaTema) => void
}

// Mesma chave lida pelo script em index.html, que aplica o tema antes da renderização
const CHAVE = `${PREFIXO}tema`
const consulta = window.matchMedia('(prefers-color-scheme: dark)')

function lerPreferencia(): PreferenciaTema {
  try {
    const valor = localStorage.getItem(CHAVE)
    return valor === 'claro' || valor === 'escuro' ? valor : 'sistema'
  } catch {
    return 'sistema'
  }
}

function calcularEscuro(p: PreferenciaTema) {
  return p === 'escuro' || (p === 'sistema' && consulta.matches)
}

function aplicar(escuro: boolean) {
  document.documentElement.classList.toggle('dark', escuro)
  document.documentElement.style.colorScheme = escuro ? 'dark' : 'light'
}

const inicial = lerPreferencia()
aplicar(calcularEscuro(inicial))

export const useTema = create<EstadoTema>()((set) => ({
  preferencia: inicial,
  escuro: calcularEscuro(inicial),
  setPreferencia: (preferencia) => {
    try {
      localStorage.setItem(CHAVE, preferencia)
    } catch {
      /* armazenamento indisponível: o tema vale só nesta sessão */
    }
    const escuro = calcularEscuro(preferencia)
    aplicar(escuro)
    set({ preferencia, escuro })
  },
}))

consulta.addEventListener('change', () => {
  if (useTema.getState().preferencia !== 'sistema') return
  const escuro = calcularEscuro('sistema')
  aplicar(escuro)
  useTema.setState({ escuro })
})
