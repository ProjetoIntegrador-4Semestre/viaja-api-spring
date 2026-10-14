package com.senac.viajaapi;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/trips")
@Tag(name = "trips", description = "CRUD de viagens: listagem paginada, consulta por usuario e por id, criacao, atualizacao e exclusao.")
public class TripController {

    private final TripRepository repository;
    private final TripModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<Trip> pagedResourcesAssembler;

    public TripController(TripRepository repository, TripModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<Trip> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    private TripResponse toTripResponse(Trip viagem) {
        return new TripResponse(
                String.valueOf(viagem.getId()),
                viagem.getUserId(),
                viagem.getName(),
                viagem.getMode(),
                viagem.getStartDate() == null ? null : viagem.getStartDate().toString(),
                viagem.getEndDate() == null ? null : viagem.getEndDate().toString(),
                viagem.getTotalBudgetCents() == null ? null : viagem.getTotalBudgetCents().longValue(),
                viagem.getStatus()
        );
    }

    @Operation(summary = "Listar viagens", description = "Retorna lista paginada de todas as viagens cadastradas no sistema. Use page, size e sort para controlar a paginacao e ordenacao.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de viagens com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Trip>>> getAllTrips(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Trip> page = repository.findAll(pageable);
        PagedModel<EntityModel<Trip>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Listar viagens por usuario", description = "Retorna lista paginada de viagens de um usuario especificado. Use page, size e sort para controlar a paginacao e ordenacao.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de viagens do usuario com links HATEOAS")
    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedModel<EntityModel<Trip>>> getTripsByUser(
            @Parameter(description = "Identificador numerico do usuario proprietario das viagens") @PathVariable Long userId,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Trip> page = repository.findByUserId(userId, pageable);
        PagedModel<EntityModel<Trip>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Criar viagem", description = "Cadastra uma nova viagem, definindo destino, modo (turismo ou trabalho), datas de inicio e fim, orcamento em centavos e status inicial 'active'.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Viagem criada com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos: destino ou modo obrigatorios ausentes")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da viagem a ser criada",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TripRequest.class),
                    examples = @ExampleObject(value = "{ \"destination\": \"Rio de Janeiro\", \"mode\": \"tourism\", \"startDate\": \"2026-12-01\", \"endDate\": \"2026-12-07\", \"budgetCents\": 300000 }"))
    )
    @PostMapping
    public TripResponse createTrip(@Valid @RequestBody TripRequest request) {
        Trip viagem = new Trip(
                null,
                request.getUserId(),
                request.getName(),
                request.getMode(),
                LocalDate.parse(request.getStartDate()),
                request.getEndDate() == null ? null : LocalDate.parse(request.getEndDate()),
                request.getBudgetCents() == null ? null : request.getBudgetCents().intValue(),
                "planned",
                null,
                null
        );
        Trip saved = repository.save(viagem);
        return toTripResponse(saved);
    }

    @Operation(summary = "Buscar viagem por id", description = "Retorna uma viagem especificada pelo seu identificador, incluindo links de navegacao HATEOAS.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Viagem encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Viagem nao encontrada para o id informado")
    @GetMapping("/{id}")
    public EntityModel<Trip> getTripById(@Parameter(description = "Identificador numerico da viagem") @PathVariable Long id) {
        Trip viagem = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem nao encontrada com ID: " + id));
        return assembler.toModel(viagem);
    }

    @Operation(summary = "Atualizar viagem", description = "Atualiza todos os campos de uma viagem existente: destino, modo, datas, orcamento e status.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Viagem atualizada com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Viagem nao encontrada para o id informado")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Trip>> updateTrip(
            @Parameter(description = "Identificador numerico da viagem a ser atualizada") @PathVariable Long id,
            @RequestBody @Valid TripRequest request) {
        Trip viagem = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem nao encontrada com ID: " + id));
        viagem.setName(request.getName());
        viagem.setMode(request.getMode());
        viagem.setStartDate(LocalDate.parse(request.getStartDate()));
        if (request.getEndDate() != null) {
            viagem.setEndDate(LocalDate.parse(request.getEndDate()));
        }
        viagem.setTotalBudgetCents(request.getBudgetCents() == null ? null : request.getBudgetCents().intValue());
        if (request.getStatus() != null) {
            viagem.setStatus(request.getStatus());
        }
        return ResponseEntity.ok(assembler.toModel(repository.save(viagem)));
    }

    @Operation(summary = "Excluir viagem", description = "Remove definitivamente uma viagem do sistema pelo seu identificador.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Viagem excluída com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Viagem nao encontrada para o id informado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrip(@Parameter(description = "Identificador numerico da viagem a ser excluída") @PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
