package com.senac.viajaapi;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/billing/stripe")
public class StripeBillingController {
    private final StripeMembershipRepository memberships;
    private final StripeEventRepository events;
    private final UserRepository users;
    private final TripAccess access;
    private final ObjectMapper json;
    private final RestClient stripe;
    private final String secret;
    private final String signingSecret;
    private final String linkId;
    private final String priceId;
    private final String returnUrl;

    public StripeBillingController(StripeMembershipRepository memberships, StripeEventRepository events,
            UserRepository users, TripAccess access, ObjectMapper json,
            @Value("${stripe.secret-key:}") String secret,
            @Value("${stripe.webhook-secret:}") String signingSecret,
            @Value("${stripe.payment-link-id:}") String linkId,
            @Value("${stripe.price-id:}") String priceId,
            @Value("${stripe.return-url:}") String returnUrl) {
        this.memberships = memberships;
        this.events = events;
        this.users = users;
        this.access = access;
        this.json = json;
        this.secret = secret;
        this.signingSecret = signingSecret;
        this.linkId = linkId;
        this.priceId = priceId;
        this.returnUrl = returnUrl;
        var factory = new JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5)).build());
        factory.setReadTimeout(java.time.Duration.ofSeconds(8));
        stripe = RestClient.builder().baseUrl("https://api.stripe.com").requestFactory(factory)
                .defaultHeader("Stripe-Version", "2025-06-30.basil")
                .defaultHeaders(headers -> headers.setBasicAuth(secret, "")).build();
    }

    @GetMapping("/subscription")
    @Transactional
    public SubscriptionResponse subscription() {
        Long userId = access.currentUserId();
        User user = lockUser(userId);
        var state = memberships.findById(userId).orElse(null);
        if (state == null) return new SubscriptionResponse("free", false, null, false, false);
        if (state.reference != null || (state.subscriptionId != null
                && !Set.of("canceled", "incomplete_expired").contains(state.status))) {
            requireConfiguration();
            refreshSubscription(user, state);
        }
        boolean active = state.paidUntil != null && state.paidUntil.isAfter(Instant.now())
                && Set.of("active", "past_due").contains(state.status);
        return new SubscriptionResponse(state.status, active, state.paidUntil, state.cancelAtPeriodEnd,
                state.customerId != null);
    }

    @PostMapping("/checkout")
    @Transactional
    public HostedUrl checkout() {
        requireConfiguration();
        Long userId = access.currentUserId();
        lockUser(userId);
        var state = memberships.findById(userId).orElseGet(StripeMembership::new);
        if (state.subscriptionId != null && !Set.of("canceled", "incomplete_expired").contains(state.status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Manage the existing subscription first");
        }
        JsonNode link = get("/v1/payment_links/{id}", linkId);
        String url = hosted(link.path("url").asText(), "buy.stripe.com");
        JsonNode lineItems = get("/v1/payment_links/{id}/line_items?limit=2", linkId).path("data");
        JsonNode price = lineItems.path(0).path("price");
        if (!link.path("active").asBoolean(false) || !link.path("livemode").isBoolean()
                || link.path("livemode").asBoolean() || lineItems.size() != 1
                || !priceId.equals(price.path("id").asText()) || !"recurring".equals(price.path("type").asText())
                || !"month".equals(price.path("recurring").path("interval").asText())
                || price.path("recurring").path("interval_count").asInt() != 1
                || link.path("subscription_data").path("trial_period_days").asInt(0) > 0) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe test link unavailable");
        }
        state.userId = userId;
        if (state.reference == null) state.reference = UUID.randomUUID().toString();
        if (state.subscriptionId == null) state.status = "pending";
        memberships.save(state);
        return new HostedUrl(UriComponentsBuilder.fromUriString(url)
                .replaceQueryParam("client_reference_id", "{reference}")
                .encode().buildAndExpand(state.reference).toUriString());
    }

    @PostMapping("/portal")
    public HostedUrl portal() {
        requireConfiguration();
        var state = memberships.findById(access.currentUserId())
                .filter(item -> item.customerId != null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Stripe customer"));
        var form = new LinkedMultiValueMap<String, String>();
        form.add("customer", state.customerId);
        form.add("return_url", returnUrl);
        try {
            JsonNode portal = stripe.post().uri("/v1/billing_portal/sessions")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(JsonNode.class);
            return new HostedUrl(hosted(portal.path("url").asText(), "billing.stripe.com"));
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe portal unavailable");
        }
    }

    @GetMapping(value = "/return", produces = MediaType.TEXT_HTML_VALUE)
    public String returnToApp() {
        return "<html lang=\"pt-BR\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Voltar ao Viajá</title><p>Confira a assinatura no aplicativo.</p>"
                + "<a href=\"viaja://billing/return\">Voltar ao Viajá</a></html>";
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void webhook(@RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestBody byte[] body) {
        requireConfiguration();
        if (!StripeSignature.matches(signingSecret, signature, body, Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature");
        }
        JsonNode event = json.readTree(body);
        String eventId = event.path("id").asText();
        if (!eventId.startsWith("evt_") || !event.path("livemode").isBoolean() || event.path("livemode").asBoolean()) return;

        JsonNode object = event.path("data").path("object");
        String type = event.path("type").asText();
        boolean checkout = Set.of("checkout.session.completed", "checkout.session.async_payment_succeeded").contains(type);
        String subscriptionId;
        Long userId;
        if (checkout) {
            if (!linkId.equals(object.path("payment_link").asText()) || !"subscription".equals(object.path("mode").asText())) return;
            subscriptionId = object.path("subscription").asText();
            userId = memberships.findUserIdByReference(object.path("client_reference_id").asText())
                    .or(() -> memberships.findUserIdBySubscriptionId(subscriptionId)).orElse(null);
        } else if (Set.of("customer.subscription.updated", "customer.subscription.deleted", "invoice.paid", "invoice.payment_failed").contains(type)) {
            subscriptionId = type.startsWith("invoice.")
                    ? object.path("parent").path("subscription_details").path("subscription").asText(object.path("subscription").asText())
                    : object.path("id").asText();
            userId = memberships.findUserIdBySubscriptionId(subscriptionId).orElse(null);
        } else {
            return;
        }
        if (userId == null || !subscriptionId.matches("sub_[A-Za-z0-9]+")) return;
        User user = lockUser(userId);
        var state = memberships.findById(userId).orElseThrow();
        if (events.existsById(eventId)) return;
        boolean firstCheckout = checkout && !subscriptionId.equals(state.subscriptionId);
        if (firstCheckout) {
            if (!object.path("client_reference_id").asText().equals(state.reference)
                    || !user.getEmail().equalsIgnoreCase(object.path("customer_details").path("email").asText())
                    || (state.subscriptionId != null && !Set.of("canceled", "incomplete_expired").contains(state.status))) return;
        } else if (!subscriptionId.equals(state.subscriptionId)) {
            return;
        }
        String customer = checkout ? object.path("customer").asText() : state.customerId;
        if (reconcile(state, subscriptionId, customer)) events.save(new StripeEvent(eventId));
    }

    private void refreshSubscription(User user, StripeMembership state) {
        if (state.reference == null) {
            reconcile(state, state.subscriptionId, state.customerId);
            return;
        }
        JsonNode sessions = get("/v1/checkout/sessions?payment_link={id}&status=complete&limit=100", linkId);
        for (JsonNode session : sessions.path("data")) {
            if (!state.reference.equals(session.path("client_reference_id").asText())
                    || !linkId.equals(session.path("payment_link").asText())
                    || !"subscription".equals(session.path("mode").asText())
                    || !"paid".equals(session.path("payment_status").asText())
                    || !session.path("livemode").isBoolean() || session.path("livemode").asBoolean()
                    || !user.getEmail().equalsIgnoreCase(session.path("customer_details").path("email").asText())) {
                continue;
            }
            if (reconcile(state, session.path("subscription").asText(), session.path("customer").asText())) return;
        }
    }

    private boolean reconcile(StripeMembership state, String subscriptionId, String expectedCustomer) {
        if (subscriptionId == null || !subscriptionId.matches("sub_[A-Za-z0-9]+")) return false;
        JsonNode current = get("/v1/subscriptions/{id}?expand[]=latest_invoice", subscriptionId);
        var items = current.path("items").path("data");
        var item = items.path(0);
        String customer = current.path("customer").asText();
        if (!current.path("livemode").isBoolean() || current.path("livemode").asBoolean()
                || items.size() != 1 || !priceId.equals(item.path("price").path("id").asText())
                || !customer.matches("cus_[A-Za-z0-9]+")
                || !customer.equals(expectedCustomer)) return false;

        if (!subscriptionId.equals(state.subscriptionId)) state.paidUntil = null;
        state.subscriptionId = subscriptionId;
        state.customerId = customer;
        state.reference = null;
        state.status = current.path("status").asText();
        state.cancelAtPeriodEnd = current.path("cancel_at_period_end").asBoolean();
        long periodEnd = item.path("current_period_end").asLong(current.path("current_period_end").asLong());
        JsonNode invoice = current.path("latest_invoice");
        if ("paid".equals(invoice.path("status").asText()) && periodEnd > 0) {
            for (JsonNode line : invoice.path("lines").path("data")) {
                String linePrice = line.path("pricing").path("price_details").path("price")
                        .asText(line.path("price").path("id").asText());
                long paidEnd = Math.min(periodEnd, line.path("period").path("end").asLong());
                if (priceId.equals(linePrice) && paidEnd > 0) {
                    Instant paidThrough = Instant.ofEpochSecond(paidEnd);
                    if (state.paidUntil == null || paidThrough.isAfter(state.paidUntil)) state.paidUntil = paidThrough;
                }
            }
        }
        memberships.save(state);
        return true;
    }

    private User lockUser(Long id) {
        return users.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private JsonNode get(String path, String id) {
        try {
            return stripe.get().uri(path, id).retrieve().body(JsonNode.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe unavailable");
        }
    }

    private void requireConfiguration() {
        if (!secret.startsWith("sk_test_") || !signingSecret.startsWith("whsec_")
                || !linkId.matches("plink_[A-Za-z0-9]+") || !priceId.matches("price_[A-Za-z0-9]+")
                || !returnUrl.startsWith("https://")) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe sandbox is not configured");
        }
    }

    private String hosted(String url, String host) {
        try {
            URI uri = URI.create(url);
            if ("https".equals(uri.getScheme()) && host.equals(uri.getHost())
                    && uri.getUserInfo() == null && uri.getPort() == -1) return url;
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid Stripe URL");
        }
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid Stripe URL");
    }

    public record HostedUrl(String url) {}
    public record SubscriptionResponse(String status, boolean premiumActive, Instant paidUntil,
            boolean cancelAtPeriodEnd, boolean canManage) {}
}
