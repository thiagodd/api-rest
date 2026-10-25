# Sugestões para o currículo (fase 3)

> Tópicos ou ajustes percebidos durante a escrita. Nenhum bloqueia o trabalho; ficam aqui para decisão.

| Origem | Sugestão | Motivo |
|---|---|---|
| Parte 1 (várias páginas) | ~~Definir o link da **documentação de segurança**~~ Resolvido em 24/10/2026: https://security-doc-beta.vercel.app/. A primeira menção de cada página aponta para lá; as páginas novas seguem a mesma regra. | O currículo diz "link a definir na fase 2", mas a fase 2 não definiu. As páginas citam a "documentação de segurança 🔒" sem link; quando houver URL, basta procurar por "documentação de segurança" em `src/content`. |
| 09.04 | O currículo pede o aspecto `@Auditavel` no código do Bazar. Ele foi verificado numa sonda (proxy CGLIB, autoinvocação), mas só entra no projeto junto com os serviços de pedidos (parte 5), para que nenhuma ação seja auditada duas vezes (interceptor no controller + aspecto no serviço). A página que criar o primeiro serviço deve mover `@Auditavel` do controller para o serviço e acrescentar `spring-boot-starter-aspectj`. | Os controllers atuais falam direto com repositórios; não há camada de serviço onde aplicar o aspecto. |
