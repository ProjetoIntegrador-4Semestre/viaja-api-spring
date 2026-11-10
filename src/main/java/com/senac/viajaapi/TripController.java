package com.senac.viajaapi;

import java.time.LocalDate;
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
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final TripLocalRepository stopRepository;
    private final TripNoteRepository noteRepository;
    private final TripMemoRepository memoRepository;
    private final MediaRepository mediaRepository;
    private final TripModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<Trip> pagedResourcesAssembler;

    public TripController(TripRepository repository, UserRepository userRepository, CityRepository cityRepository, TripLocalRepository stopRepository, TripNoteRepository noteRepository, TripMemoRepository memoRepository, MediaRepository mediaRepository, TripModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<Trip> pagedResourcesAssembler) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.stopRepository = stopRepository;
        this.noteRepository = noteRepository;
        this.memoRepository = memoRepository;
        this.mediaRepository = mediaRepository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar viagens", description = "Retorna lista paginada das viagens do usuario autenticado. Use page, size e sort para controlar a paginacao e ordenacao.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de viagens com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Trip>>> getAllTrips(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Trip> page = repository.findByUserId(currentUserId(), pageable);
        PagedModel<EntityModel<Trip>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Listar viagens por usuario", description = "Retorna lista paginada de viagens de um usuario especificado. Use page, size e sort para controlar a paginacao e ordenacao.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de viagens do usuario com links HATEOAS")
    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedModel<EntityModel<Trip>>> getTripsByUser(
            @Parameter(description = "Identificador numerico do usuario proprietario das viagens") @PathVariable Long userId,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        requireUser(userId);
        Page<Trip> page = repository.findByUserId(userId, pageable);
        PagedModel<EntityModel<Trip>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Criar viagem", description = "Cadastra uma nova viagem, definindo destino, modo (turismo ou trabalho), datas de inicio e fim, orcamento em centavos e status inicial 'planned'.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Viagem criada com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos: destino ou modo obrigatorios ausentes")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da viagem a ser criada",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TripRequest.class),
                    examples = @ExampleObject(value = "{ \"name\": \"Ferias no Rio\", \"cityId\": 1, \"mode\": \"tourism\", \"startDate\": \"2026-12-01\", \"endDate\": \"2026-12-07\", \"budgetCents\": 300000 }"))
    )
    @PostMapping
    public ResponseEntity<EntityModel<Trip>> createTrip(@Valid @RequestBody TripRequest request) {
        Long userId = currentUserId();
        if (request.getUserId() != null) requireUser(request.getUserId());
        validate(request);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Trip trip = new Trip(userId, request.getCityId(), request.getName().trim(), request.getMode(),
                date(request.getStartDate()), date(request.getEndDate()),
                request.getBudgetCents() == null ? null : Math.toIntExact(request.getBudgetCents()),
                "planned", now, now);
        EntityModel<Trip> model = assembler.toModel(repository.save(trip));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @Operation(summary = "Buscar viagem por id", description = "Retorna uma viagem especificada pelo seu identificador, incluindo links de navegacao HATEOAS.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Viagem encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Viagem nao encontrada para o id informado")
    @GetMapping("/{id}")
    public EntityModel<Trip> getTripById(@Parameter(description = "Identificador numerico da viagem") @PathVariable Long id) {
        Trip viagem = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem nao encontrada com ID: " + id));
        requireUser(viagem.getUserId());
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem nao encontrada com ID: " + id));
        requireUser(viagem.getUserId());
        if (request.getUserId() != null) requireUser(request.getUserId());
        validate(request);
        viagem.setCityId(request.getCityId());
        viagem.setUpdatedAt(java.time.LocalDateTime.now());
        viagem.setName(request.getName().trim());
        viagem.setMode(request.getMode());
        viagem.setStartDate(date(request.getStartDate()));
        viagem.setEndDate(date(request.getEndDate()));
        viagem.setTotalBudgetCents(request.getBudgetCents() == null ? null : request.getBudgetCents().intValue());
        if (request.getStatus() != null) {
            viagem.setStatus(request.getStatus());
        }
        return ResponseEntity.ok(assembler.toModel(repository.save(viagem)));
    }

    @Operation(summary = "Excluir viagem", description = "Remove definitivamente uma viagem do sistema pelo seu identificador.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Viagem excluída com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Viagem nao encontrada para o id informado")
    @org.springframework.transaction.annotation.Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrip(@Parameter(description = "Identificador numerico da viagem a ser excluída") @PathVariable Long id) {
        Trip trip = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip does not exist"));
        requireUser(trip.getUserId());
        memoRepository.deleteByTripId(id);
        noteRepository.deleteByTripId(id);
        stopRepository.deleteById_TripId(id);
        mediaRepository.deleteByEntityTypeAndEntityId("trip", id);
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied")).getId();
    }

    private void requireUser(Long userId) {
        if (!currentUserId().equals(userId)) throw new org.springframework.security.access.AccessDeniedException("Access denied");
    }

    private LocalDate date(String value) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDate.parse(value); }
        catch (java.time.format.DateTimeParseException ex) { throw new IllegalArgumentException("Invalid date; use yyyy-MM-dd"); }
    }

    private void validate(TripRequest request) {
        if (request.getName() == null || request.getName().isBlank()) throw new IllegalArgumentException("Trip name is required");
        if (!List.of("tourism", "work").contains(request.getMode() == null ? "" : request.getMode())) throw new IllegalArgumentException("Invalid trip mode");
        if (request.getCityId() == null || !cityRepository.existsById(request.getCityId())) throw new IllegalArgumentException("City does not exist");
        LocalDate start = date(request.getStartDate());
        LocalDate end = date(request.getEndDate());
        if (end != null && (start == null || end.isBefore(start))) throw new IllegalArgumentException("Invalid trip date range");
        if (request.getBudgetCents() != null && (request.getBudgetCents() < 0 || request.getBudgetCents() > Integer.MAX_VALUE)) throw new IllegalArgumentException("Invalid budget");
        if (request.getStatus() != null && !List.of("planned", "active", "completed", "cancelled").contains(request.getStatus())) throw new IllegalArgumentException("Invalid trip status");
    }
}
