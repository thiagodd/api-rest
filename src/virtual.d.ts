declare module 'virtual:conteudo-meta' {
  const meta: Record<
    string,
    { minutos: number; status: string; referencias: { titulo: string; url?: string }[] }
  >
  export default meta
}

declare module 'virtual:conteudo-busca' {
  const secoes: { pagina: string; ancora: string; titulo: string; texto: string }[]
  export default secoes
}
