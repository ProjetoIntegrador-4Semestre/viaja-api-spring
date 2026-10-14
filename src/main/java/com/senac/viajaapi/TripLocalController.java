package com.senac.viajaapi;

import java.util.List;

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
@RequestMapping("/trip-locals")
@Tag(name = "trip-locals", description = "Paradas de roteiro: listagem paginada, consulta por viagem e por id, criacao, atualizacao e exclusao.")
public class TripLocalController {

    private final TripLocalRepository repository;
    private final TripLocalModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<TripLocal> pagedResourcesAssembler;

    public TripLocalController(TripLocalRepository repository, TripLocalModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<TripLocal> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar paradas de roteiro", description = "Retorna lista paginada de todas as paradas de roteiro cadastradas.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de paradas com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<TripLocal>>> getAllTripLocals(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<TripLocal> page = repository.findAll(pageable);
        PagedModel<EntityModel<TripLocal>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Listar paradas por viagem", description = "Retorna todas as paradas de uma viagem, ordenadas por dia e ordem.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de paradas da viagem")
    @GetMapping("/trip/{tripId}")
    public List<TripLocal> getTripLocalsByTrip(@Parameter(description = "Identificador numerico da viagem") @PathVariable Long tripId) {
        return repository.findById_TripIdOrderByDayAscOrderIndexAsc(tripId);
    }

    @Operation(summary = "Criar parada de roteiro")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Parada criada com sucesso")
    @PostMapping
    public ResponseEntity<EntityModel<TripLocal>> newTripLocal(@RequestBody @Valid TripLocal newTripLocal) {
        TripLocal saved = repository.save(newTripLocal);
        EntityModel<TripLocal> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Buscar parada por id", description = "Identificadores compostos: tripId e localId.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Parada encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Parada nao encontrada")
    @GetMapping("/{tripId}/{localId}")
    public EntityModel<TripLocal> getTripLocalById(
            @Parameter(description = "Identificador numerico da viagem") @PathVariable Long tripId,
            @Parameter(description = "Identificador numerico do local") @PathVariable Long localId) {
        TripLocal tripLocal = repository.findById(new TripLocalId(tripId, localId))
                .orElseThrow(() -> new RuntimeException("Parada nao encontrada com tripId: " + tripId + " e localId: " + localId));
        return assembler.toModel(tripLocal);
    }

    @Operation(summary = "Atualizar parada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Parada atualizada com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Parada nao encontrada")
    @PutMapping("/{tripId}/{localId}")
    public ResponseEntity<EntityModel<TripLocal>> updateTripLocal(
            @Parameter(description = "Identificador numerico da viagem") @PathVariable Long tripId,
            @Parameter(description = "Identificador numerico do local") @PathVariable Long localId,
            @RequestBody @Valid TripLocal newTripLocal) {
        TripLocal tripLocal = repository.findById(new TripLocalId(tripId, localId))
                .orElseThrow(() -> new RuntimeException("Parada nao encontrada com tripId: " + tripId + " e localId: " + localId));
        tripLocal.setOrderIndex(newTripLocal.getOrderIndex());
        tripLocal.setDay(newTripLocal.getDay());
        tripLocal.setNote(newTripLocal.getNote());
        return ResponseEntity.ok(assembler.toModel(repository.save(tripLocal)));
    }

    @Operation(summary = "Excluir parada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Parada excluída com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Parada nao encontrada")
    @DeleteMapping("/{tripId}/{localId}")
    public ResponseEntity<?> deleteTripLocal(
            @Parameter(description = "Identificador numerico da viagem") @PathVariable Long tripId,
            @Parameter(description = "Identificador numerico do local") @PathVariable Long localId) {
        if (!repository.existsById(new TripLocalId(tripId, localId))) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(new TripLocalId(tripId, localId));
        return ResponseEntity.noContent().build();
    }
}
