package com.senac.viajaapi;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/trips/{tripId}/notes")
@Tag(name = "trip-notes", description = "Anotacoes de viagem: listagem paginada, consulta, criacao, atualizacao e exclusao.")
public class TripNoteController {

    private final TripNoteRepository repository;
    private final TripNoteModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<TripNote> pagedResourcesAssembler;

    public TripNoteController(TripNoteRepository repository, TripNoteModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<TripNote> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar anotacoes de uma viagem")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de anotacoes com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<TripNote>>> getTripNotesByTrip(
            @Parameter(description = "Identificador da viagem") @PathVariable Long tripId,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<TripNote> page = repository.findByTripId(tripId, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar anotacao por id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Anotacao encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Anotacao nao encontrada")
    @GetMapping("/{id}")
    public EntityModel<TripNote> getTripNoteById(@Parameter(description = "Identificador da anotacao") @PathVariable Long id) {
        TripNote note = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anotacao nao encontrada com ID: " + id));
        return assembler.toModel(note);
    }

    @Operation(summary = "Criar anotacao de viagem")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Anotacao criada")
    @PostMapping
    public ResponseEntity<EntityModel<TripNote>> newTripNote(
            @Parameter(description = "Identificador da viagem") @PathVariable Long tripId,
            @RequestBody @Valid TripNote newTripNote) {
        newTripNote.setTripId(tripId);
        newTripNote.setCreatedAt(LocalDateTime.now());
        TripNote saved = repository.save(newTripNote);
        EntityModel<TripNote> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Atualizar anotacao")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Anotacao atualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Anotacao nao encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<TripNote>> updateTripNote(
            @PathVariable Long id, @RequestBody @Valid TripNote newTripNote) {
        TripNote note = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anotacao nao encontrada com ID: " + id));
        note.setNote(newTripNote.getNote());
        return ResponseEntity.ok(assembler.toModel(repository.save(note)));
    }

    @Operation(summary = "Excluir anotacao")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Anotacao excluída")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Anotacao nao encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTripNote(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
