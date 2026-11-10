package com.senac.viajaapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/subscriptions")
@Tag(name = "subscriptions", description = "Assinaturas de usuario: listagem paginada, consulta, criacao, atualizacao e exclusao.")
public class SubscriptionController {

    private final SubscriptionRepository repository;
    private final SubscriptionModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<Subscription> pagedResourcesAssembler;

    public SubscriptionController(SubscriptionRepository repository, SubscriptionModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<Subscription> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar assinaturas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de assinaturas com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Subscription>>> getAllSubscriptions(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Subscription> page = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar assinaturas por usuario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assinaturas do usuario")
    @GetMapping("/users/{userId}")
    public ResponseEntity<EntityModel<Subscription>> getSubscriptionByUser(
            @Parameter(description = "Identificador do usuario") @PathVariable Long userId) {
        var subs = repository.findByUserId(userId);
        if (subs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(assembler.toModel(subs.get(0)));
    }

    @Operation(summary = "Buscar assinatura por id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assinatura encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assinatura nao encontrada")
    @GetMapping("/{id}")
    public EntityModel<Subscription> getSubscriptionById(@Parameter(description = "Identificador da assinatura") @PathVariable Long id) {
        Subscription subscription = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assinatura nao encontrada com ID: " + id));
        return assembler.toModel(subscription);
    }

    @Operation(summary = "Criar assinatura")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Assinatura criada")
    @PostMapping
    public ResponseEntity<EntityModel<Subscription>> newSubscription(@RequestBody @Valid Subscription newSubscription) {
        Subscription saved = repository.save(newSubscription);
        EntityModel<Subscription> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Atualizar assinatura")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assinatura atualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assinatura nao encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Subscription>> updateSubscription(
            @PathVariable Long id, @RequestBody @Valid Subscription newSubscription) {
        Subscription subscription = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assinatura nao encontrada com ID: " + id));
        subscription.setPlan(newSubscription.getPlan());
        subscription.setStatus(newSubscription.getStatus());
        subscription.setAmount(newSubscription.getAmount());
        subscription.setStartedAt(newSubscription.getStartedAt());
        subscription.setExpiresAt(newSubscription.getExpiresAt());
        return ResponseEntity.ok(assembler.toModel(repository.save(subscription)));
    }

    @Operation(summary = "Excluir assinatura")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Assinatura excluída")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assinatura nao encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubscription(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
