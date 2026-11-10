package com.senac.viajaapi;

import java.time.LocalDateTime;
import java.util.List;

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
@RequestMapping("/trip-memos")
@Tag(name = "trip-memos", description = "Anotacoes de paradas de roteiro: listagem paginada, consulta, criacao, atualizacao e exclusao.")
public class TripMemoController {

    private final TripMemoRepository repository;
    private final TripLocalRepository tripLocalRepository;
    private final TripMemoModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<TripMemo> pagedResourcesAssembler;

    public TripMemoController(TripMemoRepository repository, TripLocalRepository tripLocalRepository,
            TripMemoModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<TripMemo> pagedResourcesAssembler) {
        this.repository = repository;
        this.tripLocalRepository = tripLocalRepository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar anotacoes de paradas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de anotacoes com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<TripMemo>>> getAllTripMemos(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<TripMemo> page = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Listar anotacoes de uma parada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Anotacoes da parada")
    @GetMapping("/trip-locals/{tripId}/{localId}/memos")
    public List<TripMemo> getMemosByTripLocal(
            @Parameter(description = "Identificador da viagem") @PathVariable Long tripId,
            @Parameter(description = "Identificador do local") @PathVariable Long localId) {
        return repository.findByTripIdAndLocalId(tripId, localId);
    }

    @Operation(summary = "Buscar anotacao por id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Anotacao encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Anotacao nao encontrada")
    @GetMapping("/{id}")
    public EntityModel<TripMemo> getTripMemoById(@Parameter(description = "Identificador da anotacao") @PathVariable Long id) {
        TripMemo memo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anotacao nao encontrada com ID: " + id));
        return assembler.toModel(memo);
    }

    @Operation(summary = "Criar anotacao de parada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Anotacao criada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Parada de roteiro nao encontrada")
    @PostMapping
    public ResponseEntity<EntityModel<TripMemo>> newTripMemo(@RequestBody @Valid TripMemo newTripMemo) {
        if (tripLocalRepository.findById(new TripLocalId(newTripMemo.getTripId(), newTripMemo.getLocalId())).isEmpty()) {
            throw new IllegalArgumentException("Parada de roteiro nao encontrada para tripId=" + newTripMemo.getTripId()
                    + " e localId=" + newTripMemo.getLocalId());
        }
        newTripMemo.setCreatedAt(LocalDateTime.now());
        TripMemo saved = repository.save(newTripMemo);
        EntityModel<TripMemo> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Atualizar anotacao")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Anotacao atualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Anotacao nao encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<TripMemo>> updateTripMemo(
            @PathVariable Long id, @RequestBody @Valid TripMemo newTripMemo) {
        TripMemo memo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anotacao nao encontrada com ID: " + id));
        memo.setNote(newTripMemo.getNote());
        return ResponseEntity.ok(assembler.toModel(repository.save(memo)));
    }

    @Operation(summary = "Excluir anotacao")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Anotacao excluída")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Anotacao nao encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTripMemo(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
