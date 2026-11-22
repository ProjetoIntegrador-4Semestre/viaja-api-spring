# Viajá API

A API do Viajá é o servidor do app Android. Ela responde às buscas no catálogo de destinos, autentica quem entra no app e guarda os lugares salvos e os roteiros de cada conta. [Repositório do app Android](https://github.com/ProjetoIntegrador-4Semestre/ViaJa).

O servidor usa Java e Spring Boot. Os dados são acessados via Spring Data JPA e, no ambiente local padrão, ficam em um banco H2 em memória. Ao iniciar com o banco vazio, a API carrega cidades, lugares e uma conta de demonstração para que o app possa ser testado logo depois de instalado.

Os lugares usam um modelo comum para qualquer origem de dados. O catálogo demonstrativo é servido pelo provider `mock`, selecionado por padrão. A variável `VIAJA_LOCALS_PROVIDER` permite escolher outro provider disponível sem mudar os contratos consumidos pelo app.

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

O catálogo também oferece `GET /places/nearby?lat=...&lng=...&radius=...&type=...`. Resultados com foto informam `photoName`; a mídia correspondente fica em `GET /places/photos?name=...`, que resolve a foto sem enviar a chave do Google ao cliente.

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

## Google Places

Ative a Places API (New) em um projeto Google Cloud com faturamento e configure uma chave exclusiva para o servidor, restrita à API e aos endereços IP da implantação. Não reutilize essa chave no Android.

```sh
export GOOGLE_MAPS_SERVER_API_KEY="sua-chave-de-servidor"
export VIAJA_LOCALS_PROVIDER="google"
./mvnw spring-boot:run
```

Sem essas variáveis, a API continua usando o catálogo demonstrativo. Text Search, Nearby Search, detalhes e fotos usam máscaras de campos explícitas; campos mais caros só devem ser adicionados quando a experiência realmente precisar deles.

Para verificar o catálogo, o login e os fluxos de salvos, viagens e paradas, rode `python3 scripts/smoke_test.py` com uma instância local recém-iniciada. O script depende dos dados iniciais e cria e remove registros de demonstração. Use `--base-url` para apontar para outra instância de teste.

## Assinaturas no Stripe Sandbox

O Android abre o pagamento e o portal de assinaturas no navegador. A API recebe os eventos do Stripe, confere o pagamento e informa ao app se o Premium está ativo.

Para testar, use um Payment Link mensal de teste e habilite o Customer Portal no mesmo sandbox. Na API, configure `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `STRIPE_PAYMENT_LINK_ID`, `STRIPE_PRICE_ID` e `STRIPE_RETURN_URL`. A URL de retorno deve ser pública, usar HTTPS e terminar em `/billing/stripe/return`. Ative o perfil `stripe-sandbox` com `SPRING_PROFILES_ACTIVE` para guardar os vínculos em `data/`.

Aponte os eventos de checkout, assinatura e fatura para `POST /billing/stripe/webhook`. Comece o pagamento pelo app e use no Stripe o mesmo e-mail da conta Viajá.
