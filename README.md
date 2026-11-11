# Viajá API

Backend Java/Spring Boot do Viajá. Mantém o padrão acadêmico de controllers, repositories JPA, assemblers HATEOAS, paginação, Swagger e carga inicial. Referência da disciplina: `/Users/rafa/IdeaProjects/corp-financial-api`.

## Documentação do projeto

Produto, planos e telas: [referência de produto](</Users/rafa/AndroidStudioProjects/Viaja/docs/product-baseline.md>). Estrutura/dados: [arquitetura](</Users/rafa/AndroidStudioProjects/Viaja/docs/architecture.md>). Funcionalidades verificadas e falhas abertas: [estado da implementação](</Users/rafa/AndroidStudioProjects/Viaja/docs/status.md>). O [índice](</Users/rafa/AndroidStudioProjects/Viaja/docs/README.md>) indica o papel de cada arquivo.

Esses caminhos são locais aos checkouts informados pelo responsável. Em outra máquina, consultar os mesmos caminhos `docs/` no repositório Android. Não duplicar neste README o escopo, a matriz de preços ou o planejamento das telas.

## Execução local

Usar JDK compatível com o `pom.xml` (Java 17 ou superior) e o Maven Wrapper:

```sh
./mvnw verify
java -jar target/api-viaja-0.0.1-SNAPSHOT.jar --server.port=18080
```

Encerrar a aplicação antes de reconstruir o JAR em uso. A porta padrão sem argumento é 8080; os testes desta recuperação usam 18080.

- Swagger UI: `http://localhost:18080/swagger-ui/index.html`
- OpenAPI: `http://localhost:18080/v3/api-docs`

A configuração atual cria H2 em memória e LoadDatabase insere três usuários, três cidades e três lugares, além de associações de demonstração. Reiniciar apaga as alterações e recria o catálogo. Isso é comportamento atual, não persistência de produção ou de cobrança já resolvida. As URLs ilustrativas de imagens e os dados de exemplo precisam de curadoria antes da apresentação final.

## Verificação existente

Com uma instância local recém-iniciada:

```sh
python3 scripts/smoke_test.py
```

`--base-url` altera o endereço consultado. A verificação espera contagens e registros iniciais; ela cria e remove vínculos/viagens de demonstração. Não executar em um banco de usuários reais. O script não imprime JWTs.

As verificações cobrem catálogo, paginação, OpenAPI, login, sessão, salvos, viagens, paradas e privacidade da conta. Incluem testes de acesso cruzado nos recursos já protegidos. `mvnw verify` compila e empacota a API. O script verifica as respostas HTTP de uma instância em execução.

## Contratos usados pelo Android

Não existe prefixo `/api` nos caminhos abaixo. Swagger precisa de revisão de exemplos; o código e a resposta executada prevalecem quando houver divergência.

| Recurso | Contrato relevante |
| --- | --- |
| Login | `POST /auth/login`, corpo `email`/`senha`; JWT em `Authorization` no header da resposta, usuário no JSON |
| Conta própria | `GET /users` lista apenas a conta autenticada; `GET/PUT/DELETE /users/{id}` conferem proprietário e não expõem hash; `POST /users` não está disponível |
| Cadastro | `POST /auth/signup`, corpo `name`/`email`/`senha` |
| Cidades | `GET /cities`, HAL, coleção `_embedded.cityList` |
| Lugares | `GET /places/search?q=` retorna lista simples; `GET /places/{id}` expõe os mesmos campos mais links |
| Viagens | `GET /trips` e `/trips/user/{userId}`, HAL em `_embedded.trips`; detalhe/criação/edição usam Trip com links |
| Paradas | `/trip-locals`; lista simples ordenada em `/trip-locals/trip/{tripId}`; detalhe/mutações por viagem/lugar |
| Notas e memos | `/trips/{tripId}/notes` e `/trip-memos` exigem a conta proprietária. A coleção de memos retorna apenas registros das viagens da conta. |
| Salvos | `/users/{userId}/saved-locals`, HAL em `_embedded.savedLocals`; `/{localId}` usa o ID do lugar |

Para ajustar o horário ou a conclusão de uma parada, use `PUT /trip-locals/{tripId}/{localId}/schedule` com `{"scheduledTime":"09:30","completed":false}`. O horário é local, no formato de 24 horas `HH:mm`. Envie `null` ou omita o horário para limpá-lo. O campo `completed` é obrigatório nessa rota. Paradas anteriores são lidas com horário ausente e como pendentes. O PUT de dia, ordem e nota preserva esses dois campos.

A conta local de demonstração é `rafael@email.com`, senha `senha123`.

```sh
curl -i http://localhost:18080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"rafael@email.com","senha":"senha123"}'
```

O login retorna 200. Use o valor completo `Bearer ...` do header `Authorization` nas consultas privadas. Dados de login ausentes retornam 400. Credenciais incorretas ou acesso privado sem token válido retornam 401. Uma conta autenticada sem permissão recebe 403 nos recursos com controle de proprietário. Recursos inexistentes retornam 404 quando a requisição chega ao roteamento, e `GET /auth/login` retorna 405.

Na leitura de lugares, `id` é textual e `price` é inteiro em centavos; descrição está incluída. A coleção paginada e a escrita `/places` ainda usam Local, um contrato diferente. Criação de viagem recebe nome/cidade/modo, datas e orçamento opcionais; proprietário vem do JWT. Sucesso retorna 201/Location. Excluir retorna 204 sem corpo. Salvos duplicados e lugares inexistentes são rejeitados com 400; associações ausentes retornam 404.

Salvos, viagens, paradas, notas, memos e conta própria conferem proprietário. Isso não significa autorização completa dos demais recursos. Exclusão de viagem remove memos, notas, paradas e mídia de viagem na mesma transação; a exclusão isolada de uma parada com memos ainda precisa de tratamento.

## Android local e hospedagem

O caminho que foi verificado no emulador usa encaminhamento de porta:

```sh
adb reverse tcp:18080 tcp:18080
```

No repositório Android, compilar debug com `-PapiBaseUrl=http://127.0.0.1:18080/`. Roteamento direto por `10.0.2.2` não foi suficiente na sessão verificada. A API hospedada não recebeu os contratos atuais durante esta recuperação; não confundir execução local com deploy concluído.

## Cobrança e segurança pendentes

Stripe test Checkout/webhooks é direção confirmada, não integração existente. Planos, quotas e política de acesso ficam na referência de produto. O CRUD atual de Subscription não pode se tornar autoridade para conceder Premium; faltam persistência durável, identificação do cliente/assinatura e controle de eventos. Não usar dados fictícios de assinatura como confirmação Stripe.

A exposição de hash e o acesso cruzado de UserController foram corrigidos localmente. O estado da implementação ainda registra acesso cruzado em outros recursos e escrita pública no catálogo. A publicação precisa de configuração própria para o console H2, a chave de assinatura JWT e os dados de demonstração.

`HELP.md` é template gerado pelo Spring e está ignorado no Git; não contém regras do Viajá. Os documentos antigos de UUID/PostgreSQL/rotas `/api` foram classificados e preservados na revisão documental, não aplicados como migração.

## Stripe Sandbox

A integração usa o Payment Link hospedado de assinatura, um webhook assinado e sessões do Customer Portal. O Android consulta `GET /billing/stripe/subscription` e pede as URLs com `POST /billing/stripe/checkout` ou `POST /billing/stripe/portal`. O status desse fluxo é separado do CRUD acadêmico de assinaturas.

Configure no ambiente da API, usando os valores do mesmo sandbox:

| Variável | Valor |
| --- | --- |
| `STRIPE_SECRET_KEY` | Chave secreta de teste do servidor |
| `STRIPE_WEBHOOK_SECRET` | Segredo de assinatura do destino de eventos, prefixo `whsec_` |
| `STRIPE_PAYMENT_LINK_ID` | ID do link de assinatura, prefixo `plink_` |
| `STRIPE_PRICE_ID` | ID do preço recorrente usado no link, prefixo `price_` |
| `STRIPE_RETURN_URL` | URL HTTPS pública da API terminando em `/billing/stripe/return` |
| `SPRING_PROFILES_ACTIVE` | `stripe-sandbox` para preservar os vínculos e eventos em H2 em arquivo |

Configure o Payment Link com um único preço recorrente mensal, sem teste grátis, e aponte o redirecionamento pós-pagamento para a mesma URL de retorno. Habilite o Customer Portal no sandbox. No Stripe, habilite a opção de limitar assinaturas ativas duplicadas no Checkout e no Payment Link. Um link estático pode ser reaberto fora do app. O servidor protege a associação à conta, mas a configuração no Stripe é necessária para barrar cobranças repetidas no link hospedado.

Envie eventos de teste para `POST /billing/stripe/webhook`. Selecione `checkout.session.completed`, `checkout.session.async_payment_succeeded`, `customer.subscription.updated`, `customer.subscription.deleted`, `invoice.paid` e `invoice.payment_failed`. Para teste local, o Stripe CLI pode encaminhar os eventos para `localhost:18080/billing/stripe/webhook`. Use o segredo informado pelo CLI nesse caso. Inicie o pagamento pelo app e informe no Stripe o mesmo e-mail da sua conta Viajá. O link avulso não contém a referência da conta.

A página de retorno só reabre o app. O backend confere assinatura do evento, ambiente de teste, link, referência da conta e preço. O app consulta o status ao retornar e permite atualizar manualmente. O perfil `stripe-sandbox` mantém o banco no diretório local `data/`. Ao hospedar a API, monte esse diretório em armazenamento persistente para manter os dados entre reinícios.
