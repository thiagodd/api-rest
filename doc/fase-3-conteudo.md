# Fase 3 — Escrita do conteúdo

## Contexto

Estou construindo um site de documentação em **português do Brasil** para aprender **APIs REST do básico ao avançado com Spring Boot**.

- Fase 1: o currículo está em `planejamento/` (`CURRICULO.md`, `DESCOBERTAS.md`, `PROJETO-FINAL.md`, `VERSOES.md`).
- Fase 2: o site está pronto, com componentes MDX e páginas esqueleto. As regras de escrita estão em `planejamento/GUIA-DE-ESCRITA.md` e o status de cada página em `planejamento/PROGRESSO.md`.

Nesta fase você **escreve o conteúdo de todas as páginas**. Leia todos os arquivos de `planejamento/` e a página `/componentes` antes de começar.

## Para quem você escreve

Um desenvolvedor Java que conhece o básico de Spring Boot e quer dominar APIs REST em nível profissional. Ele é inteligente, mas não conhece os conceitos ainda. Explique o **porquê** antes do **como**, use analogias e sempre mostre exemplos concretos. Profundidade é prioridade: prefira uma página longa e completa a uma superficial.

## Regras de conteúdo

- Siga a estrutura de página do `GUIA-DE-ESCRITA.md` e cubra **todos** os tópicos listados para a página no `CURRICULO.md`.
- Toda página tem pelo menos: um diagrama, um exemplo de requisição/resposta HTTP real (`<HttpExchange>`) quando o tema envolver HTTP, código Spring Boot completo e funcional, um `<BadVsGood>` ou seção de erros comuns, um exercício e um quiz.
- Os exemplos usam o **domínio do projeto final** (`PROJETO-FINAL.md`), para que o leitor reconheça as entidades de uma página para outra.
- Use as versões do `VERSOES.md`. Não use APIs depreciadas; quando algo mudou recentemente, mostre o jeito atual e mencione o antigo, pois o leitor vai encontrá-lo em projetos legados.
- Em temas internos do framework (ex.: `DispatcherServlet`, `HttpMessageConverter`), mostre as classes reais, a ordem das chamadas e trechos do código-fonte do Spring quando ajudar.
- Texto em português claro; termos técnicos consagrados podem ficar em inglês, explicados na primeira ocorrência e marcados com `<Term>`. Adicione cada termo novo ao glossário.
- Termine cada página com `<References>` apontando para as fontes oficiais (RFCs, documentação do Spring, OpenAPI).

## Precisão

- Confirme fatos técnicos na documentação oficial antes de escrevê-los. Não invente nomes de classes, propriedades ou anotações.
- Todo código Java do projeto final deve existir de verdade em `projeto-final/` (um projeto Spring Boot real, construído conforme as páginas avançam) e **compilar e passar nos testes**. Os trechos nas páginas devem corresponder a esse código.
- Requisições de exemplo devem refletir o comportamento real da API do projeto final.

## Como trabalhar

1. Escreva **uma parte do currículo por vez**, na ordem.
2. Ao terminar cada página, atualize o `PROGRESSO.md` (status "escrita").
3. Ao terminar cada parte:
   - Rode `npm run build` e corrija os erros.
   - Rode `./mvnw verify` no `projeto-final/`, se a parte alterou o projeto.
   - Revise as páginas da parte: erros técnicos, tópicos do currículo faltando, diagramas que não renderizam, links quebrados.
   - Marque as páginas como "revisada" no `PROGRESSO.md` e me dê um resumo curto do que foi escrito.
4. Se o contexto ficar grande ou a sessão for retomada, continue a partir do `PROGRESSO.md`, que deve sempre refletir o estado real.
5. Se perceber que falta um tópico importante no currículo, **não pare**: anote em `planejamento/SUGESTOES.md` e me avise no resumo da parte.
6. No final, faça uma revisão geral do site inteiro (navegação, busca, glossário, consistência entre páginas) e me entregue um relatório do que foi feito.

Comece pela primeira parte do currículo.
