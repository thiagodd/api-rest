# APIs REST com Spring Boot

Site de documentação em português para aprender APIs REST do básico ao avançado com Spring Boot 4.

## Rodando

```bash
npm install
npm run dev          # http://localhost:5173
```

## Scripts

| Comando | O que faz |
|---|---|
| `npm run dev` | Servidor de desenvolvimento com recarga automática |
| `npm run build` | Checagem de tipos + build de produção em `dist/` |
| `npm run preview` | Serve o build de produção |
| `npm run lint` | ESLint |
| `npm run gerar:curriculo` | Lê `planejamento/CURRICULO.md` e cria os MDX que faltam (nunca sobrescreve) |
| `npm run verificar:visual` | Após o build: capturas claro/escuro × desktop/celular em `verificacao/` e testes de busca, diagramas e progresso |

## Estrutura

- `src/content/navigation.ts`: fonte única da navegação (partes, módulos, páginas, nível e prioridade)
- `src/content/<parte>/<página>.mdx`: conteúdo
- `src/content/glossario.ts`: glossário
- `src/components/mdx/`: componentes usados no MDX (guia em `/componentes`)
- `planejamento/`: currículo, projeto final, guia de escrita e progresso

Para escrever páginas, siga `planejamento/GUIA-DE-ESCRITA.md`.
