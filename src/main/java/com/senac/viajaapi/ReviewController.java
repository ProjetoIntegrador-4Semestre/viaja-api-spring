package com.senac.viajaapi;

import java.time.LocalDateTime;

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
@RequestMapping("/locals/{localId}/reviews")
@Tag(name = "reviews", description = "Avaliacoes de locais (anonimas no v1): listagem paginada, consulta, criacao, atualizacao e exclusao.")
public class ReviewController {

    private final ReviewRepository repository;
    private final ReviewModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<Review> pagedResourcesAssembler;

    public ReviewController(ReviewRepository repository, ReviewModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<Review> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar avaliacoes de um local")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de avaliacoes com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Review>>> getReviewsByLocal(
            @Parameter(description = "Identificador do local") @PathVariable Long localId,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Review> page = repository.findByLocalId(localId, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar avaliacao por id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Avaliacao encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Avaliacao nao encontrada")
    @GetMapping("/{id}")
    public EntityModel<Review> getReviewById(@Parameter(description = "Identificador da avaliacao") @PathVariable Long id) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avaliacao nao encontrada com ID: " + id));
        return assembler.toModel(review);
    }

    @Operation(summary = "Criar avaliacao (anonima, v1)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Avaliacao criada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Avaliacao deve ser entre 1 e 5")
    @PostMapping
    public ResponseEntity<EntityModel<Review>> newReview(
            @Parameter(description = "Identificador do local") @PathVariable Long localId,
            @RequestBody @Valid Review newReview) {
        if (newReview.getRating() == null || newReview.getRating() < 1 || newReview.getRating() > 5) {
            throw new IllegalArgumentException("Avaliacao deve ser entre 1 e 5");
        }
        newReview.setLocalId(localId);
        newReview.setCreatedAt(LocalDateTime.now());
        Review saved = repository.save(newReview);
        EntityModel<Review> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Atualizar avaliacao")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Avaliacao atualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Avaliacao nao encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Review>> updateReview(
            @Parameter(description = "Identificador da avaliacao") @PathVariable Long id,
            @RequestBody @Valid Review newReview) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avaliacao nao encontrada com ID: " + id));
        review.setRating(newReview.getRating());
        return ResponseEntity.ok(assembler.toModel(repository.save(review)));
    }

    @Operation(summary = "Excluir avaliacao")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Avaliacao excluída")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Avaliacao nao encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@Parameter(description = "Identificador da avaliacao") @PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
