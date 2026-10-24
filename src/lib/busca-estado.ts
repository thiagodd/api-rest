import { useEffect } from 'react'
import { create } from 'zustand'

/** Estado de abertura da busca global (Ctrl+K). */
export const useBusca = create<{ aberta: boolean; setAberta: (a: boolean) => void }>()((set) => ({
  aberta: false,
  setAberta: (aberta) => set({ aberta }),
}))

/** Atalhos Ctrl+K / Cmd+K e "/" para abrir a busca. */
export function useAtalhoBusca() {
  const setAberta = useBusca((s) => s.setAberta)
  useEffect(() => {
    const aoTeclar = (e: KeyboardEvent) => {
      const alvo = e.target as HTMLElement
      const editando = alvo.isContentEditable || ['INPUT', 'TEXTAREA', 'SELECT'].includes(alvo.tagName)
      if ((e.key.toLowerCase() === 'k' && (e.metaKey || e.ctrlKey)) || (e.key === '/' && !editando)) {
        e.preventDefault()
        setAberta(true)
      }
    }
    window.addEventListener('keydown', aoTeclar)
    return () => window.removeEventListener('keydown', aoTeclar)
  }, [setAberta])
}
