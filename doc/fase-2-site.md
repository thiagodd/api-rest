# Fase 2 — Construção do site

## Contexto

Estou construindo um site de documentação em **português do Brasil** para aprender **APIs REST do básico ao avançado com Spring Boot**. Na fase 1, o currículo foi definido e está em `planejamento/` (leia **todos** os arquivos dessa pasta antes de começar, principalmente `CURRICULO.md`).

Nesta fase você constrói **o site e todos os componentes**, mas **não escreve o conteúdo das páginas** (isso é a fase 3). Cada página do currículo deve existir como um arquivo MDX com título, descrição e os tópicos listados como esqueleto.

## Stack

- **Vite + React + TypeScript**
- **Tailwind CSS** + **shadcn/ui**
- **MDX** para o conteúdo (`src/content/<parte>/<pagina>.mdx`)
- **React Router**
- **Shiki** para destaque de código (nome do arquivo, linhas destacadas, diff, botão de copiar)
- **Mermaid** para fluxos e diagramas de sequência (compatível com tema claro/escuro)
- **Framer Motion** para diagramas animados passo a passo
- **lucide-react** para ícones
- **cmdk** + **FlexSearch** (ou similar) para busca global
- Use outras bibliotecas se melhorarem a apresentação (ex.: React Flow para diagramas interativos)

Não use imagens de terceiros: ilustrações como SVG/componentes próprios ou Mermaid.

## Estrutura do site

- **Navegação gerada a partir de um único arquivo de configuração** (`src/content/navigation.ts`), construído com base no `CURRICULO.md`: partes, módulos e páginas, com nível e prioridade
- **Sidebar** recolhível por parte, mostrando a página atual e quais já foram lidas
- **Índice "Nesta página"** à direita, com destaque da seção ativa
- **Busca global** (Ctrl+K) em títulos e conteúdo
- **Modo claro/escuro**
- **Rodapé** com "Anterior / Próximo"
- **Badges** de nível (Básico, Intermediário, Avançado) e prioridade em cada página e na sidebar
- **Tempo estimado de leitura**
- **Progresso** salvo no `localStorage`: marcar página como lida, barra de progresso geral e por parte
- **Página inicial** com a trilha de aprendizado visual (partes e módulos como um mapa) e um botão "Continuar de onde parei"
- **Glossário** com tooltip nos termos marcados ao longo do texto
- **Página de referências** consolidando as fontes
- Totalmente responsivo

## Componentes MDX

Crie todos estes componentes, com visual consistente e bonito:

| Componente | Uso |
|---|---|
| `<Callout type="info\|tip\|warning\|danger">` | Avisos e dicas |
| `<CodeTabs>` | Mesmo exemplo em várias formas (Java, curl, HTTPie, JavaScript/fetch) |
| `<HttpExchange>` | Requisição e resposta HTTP completas, com método, URL, headers, body e status colorido |
| `<Endpoint method="GET" path="/pedidos/{id}">` | Cartão de endpoint estilo referência de API, com parâmetros, respostas e exemplos |
| `<BadVsGood>` | Duas abordagens lado a lado (design ruim vs bom), com explicação |
| `<Steps>` | Passo a passo numerado |
| `<Mermaid>` | Wrapper de diagramas Mermaid |
| `<AnimatedFlow>` | Diagrama que avança passo a passo com botões (ex.: requisição passando pelo `DispatcherServlet`) |
| `<FileTree>` | Estrutura de pastas de um projeto |
| `<Comparison>` | Tabela comparativa (ex.: PUT vs PATCH, offset vs cursor) |
| `<StatusCodeTable>` | Tabela de status codes com explicação de quando usar cada um |
| `<DeepDive>` | Seção recolhível "Por baixo dos panos" |
| `<ProjectStep>` | Destaca a etapa do projeto final construída na página, com os arquivos alterados |
| `<Checklist>` | Checklist marcável |
| `<Quiz>` | Perguntas de múltipla escolha com explicação |
| `<Exercise>` | Exercício prático com dica e solução recolhíveis |
| `<Term>` | Marca um termo do glossário (tooltip) |
| `<References>` | Lista de fontes no final da página |

Crie uma página **`/componentes`** demonstrando cada componente com exemplos realistas sobre APIs REST, para servir de guia na fase 3.

## Guia de escrita para a fase 3

Crie `planejamento/GUIA-DE-ESCRITA.md` com:

- A estrutura obrigatória de cada página (ver abaixo)
- Como e quando usar cada componente, com exemplos de MDX
- Convenções: tom de voz, termos em inglês, formatação de código, nomes de pacotes do projeto final

Estrutura padrão de cada página:

1. O problema que o conceito resolve (com analogia do mundo real)
2. Explicação do conceito
3. Diagrama ou fluxo
4. Exemplo prático: requisições HTTP reais e código Spring Boot
5. Design ruim vs bom / erros comuns
6. Boas práticas
7. Por baixo dos panos (quando fizer sentido)
8. Etapa do projeto final (quando houver)
9. Resumo, exercício e quiz
10. Referências

## Como trabalhar

1. Leia `planejamento/` e me mostre a arquitetura que vai seguir (pastas, bibliotecas, como a navegação é gerada). Espere minha aprovação.
2. Construa o layout, a navegação, a busca, o tema e o progresso.
3. Construa os componentes e a página `/componentes`.
4. Gere todos os arquivos MDX esqueleto a partir do currículo.
5. Rode `npm run build` e `npm run lint` e corrija todos os erros.
6. Abra o site e verifique visualmente: tema claro e escuro, celular e desktop, diagramas renderizando, busca funcionando.
7. Crie `planejamento/PROGRESSO.md` listando todas as páginas com status "esqueleto". A fase 3 vai atualizar esse arquivo.
8. Ao terminar, me diga como rodar o site e o que ficou pronto.
