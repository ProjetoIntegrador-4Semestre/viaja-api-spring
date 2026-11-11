# Viajá API

A API do Viajá é o servidor do app Android. Ela responde às buscas no catálogo de destinos, autentica quem entra no app e guarda os lugares salvos e os roteiros de cada conta. [Repositório do app Android](https://github.com/ProjetoIntegrador-4Semestre/ViaJa).

O servidor usa Java e Spring Boot. Os dados são acessados via Spring Data JPA e, no ambiente local padrão, ficam em um banco H2 em memória. Ao iniciar com o banco vazio, a API carrega cidades, lugares e uma conta de demonstração para que o app possa ser testado logo depois de instalado.

## Recursos usados pelo app

| Recurso | Rotas principais |
| --- | --- |
| Cadastro e login | `POST /auth/signup` e `POST /auth/login` |
| Conta | `/users/{id}` |
| Cidades e lugares | `GET /cities`, `GET /places/search?q=...` e `GET /places/{id}` |
| Lugares salvos | `/users/{userId}/saved-locals` |
| Viagens e notas | `/trips` e `/trips/{tripId}/notes` |
| Paradas do roteiro | `/trip-locals` |
| Assinatura em ambiente de testes | `/billing/stripe` |

O app usa o JWT devolvido no cabeçalho `Authorization` do login para acessar dados privados. Conta, lugares salvos, viagens, paradas e notas são vinculados ao usuário autenticado. A lista de paradas de uma viagem fica em `GET /trip-locals/trip/{tripId}`. Para atualizar o horário e a conclusão de uma parada, use `PUT /trip-locals/{tripId}/{localId}/schedule`, com `scheduledTime` no formato `HH:mm` e `completed` como booleano. Para limpar o horário, envie `null` em `scheduledTime` ou omita esse campo.

As rotas começam diretamente pelo nome do recurso. Coleções como cidades, viagens e lugares salvos usam HAL, com os itens em `_embedded`. A busca de lugares e a lista de paradas retornam listas JSON simples. Nos resultados da busca e no detalhe de um lugar, o preço é informado em centavos.

## Como rodar localmente

Com JDK 17 ou superior, execute na raiz do repositório:

```sh
./mvnw verify
java -jar target/api-viaja-0.0.1-SNAPSHOT.jar --server.port=18080
```

O primeiro comando compila, testa e empacota o projeto. O segundo inicia a API na porta 18080, usada também nas instruções do app e no script de verificação. Sem o argumento `--server.port`, a API usa a porta 8080. No perfil padrão, os dados locais são recriados a cada inicialização.

Com o servidor em execução, abra [Swagger UI](http://localhost:18080/swagger-ui/index.html) para explorar as rotas ou consulte o [documento OpenAPI](http://localhost:18080/v3/api-docs). Para testar o login com a conta de demonstração:

```sh
curl -i http://localhost:18080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"rafael@email.com","senha":"senha123"}'
```

Use o valor completo `Bearer ...` retornado em `Authorization` para chamar as rotas privadas.

Para verificar o catálogo, o login e os fluxos de salvos, viagens e paradas, rode `python3 scripts/smoke_test.py` com uma instância local recém-iniciada. O script depende dos dados iniciais e cria e remove registros de demonstração. Use `--base-url` para apontar para outra instância de teste.

## Assinaturas no Stripe Sandbox

O app pede à API o link de pagamento ou a sessão do portal de assinaturas e abre a página do Stripe no navegador. Depois do pagamento, a página de retorno reabre o app. O status da assinatura vem da API, que valida os eventos recebidos pelo webhook do Stripe.

Para testar esse fluxo, configure as variáveis abaixo com valores do mesmo sandbox:

| Variável | Valor |
| --- | --- |
| `STRIPE_SECRET_KEY` | Chave secreta de teste |
| `STRIPE_WEBHOOK_SECRET` | Segredo usado para verificar a assinatura dos eventos |
| `STRIPE_PAYMENT_LINK_ID` | ID do Payment Link |
| `STRIPE_PRICE_ID` | ID do preço mensal associado ao link |
| `STRIPE_RETURN_URL` | URL HTTPS pública da API terminada em `/billing/stripe/return` |
| `SPRING_PROFILES_ACTIVE` | `stripe-sandbox`, que guarda os dados em H2 no diretório `data/` |

No Stripe, configure o Payment Link com um único preço mensal, sem período grátis, e use a URL de retorno como destino após o pagamento. Habilite o cancelamento no Customer Portal e a limitação de assinaturas ativas duplicadas no Checkout e no Payment Link.

Envie para `POST /billing/stripe/webhook` os eventos `checkout.session.completed`, `checkout.session.async_payment_succeeded`, `customer.subscription.updated`, `customer.subscription.deleted`, `invoice.paid` e `invoice.payment_failed`. Em testes locais, o Stripe CLI pode encaminhar esses eventos para `localhost:18080/billing/stripe/webhook`. Nesse caso, use o segredo fornecido pelo CLI.

Inicie o pagamento pelo app para que a API possa associá-lo à sua conta e use o mesmo e-mail no Stripe e no Viajá. Se hospedar o ambiente de testes, mantenha o diretório `data/` em armazenamento persistente para conservar os vínculos e eventos entre reinícios.
