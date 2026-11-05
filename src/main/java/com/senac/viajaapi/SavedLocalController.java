package com.senac.viajaapi;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/{userId}/saved-locals")
@Tag(name = "saved-locals", description = "Locais salvos do usuario: listagem paginada, salvar e remover salvo.")
public class SavedLocalController {

    private final SavedLocalRepository repository;
    private final UserRepository userRepository;
    private final LocalRepository localRepository;
    private final SavedLocalModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<SavedLocal> pagedResourcesAssembler;

    public SavedLocalController(SavedLocalRepository repository, UserRepository userRepository, LocalRepository localRepository, SavedLocalModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<SavedLocal> pagedResourcesAssembler) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.localRepository = localRepository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar locais salvos do usuario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de locais salvos com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<SavedLocal>>> getSavedLocalsByUser(
            @Parameter(description = "Identificador do usuario") @PathVariable Long userId,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        requireOwner(userId);
        Page<SavedLocal> page = repository.findByUserId(userId, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Salvar local para o usuario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Local salvo")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Local ja salvo pelo usuario")
    @PostMapping
    public ResponseEntity<?> saveLocal(
            @Parameter(description = "Identificador do usuario") @PathVariable Long userId,
            @RequestBody @Valid SavedLocal newSavedLocal) {
        requireOwner(userId);
        if (newSavedLocal.getLocalId() == null || !localRepository.existsById(newSavedLocal.getLocalId())) {
            throw new IllegalArgumentException("Place does not exist");
        }
        if (repository.existsByUserIdAndLocalId(userId, newSavedLocal.getLocalId())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("erro", "Local ja salvo pelo usuario"));
        }
        newSavedLocal.setId(null);
        newSavedLocal.setUserId(userId);
        newSavedLocal.setCreatedAt(java.time.LocalDateTime.now());
        SavedLocal saved = repository.save(newSavedLocal);
        EntityModel<SavedLocal> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Remover salvo do local")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Salvo removido")
    @DeleteMapping("/{localId}")
    public ResponseEntity<?> unsaveLocal(
            @Parameter(description = "Identificador do usuario") @PathVariable Long userId,
            @Parameter(description = "Identificador do local") @PathVariable Long localId) {
        requireOwner(userId);
        if (!repository.existsByUserIdAndLocalId(userId, localId)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteByUserIdAndLocalId(userId, localId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{localId}")
    @Operation(summary = "Buscar um local salvo do usuario")
    public EntityModel<SavedLocal> getSavedLocal(@PathVariable Long userId, @PathVariable Long localId) {
        requireOwner(userId);
        SavedLocal saved = repository.findByUserIdAndLocalId(userId, localId)
                .orElseThrow(() -> new RuntimeException("Saved place does not exist"));
        return assembler.toModel(saved);
    }

    private void requireOwner(Long userId) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied"));
        if (!user.getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }
}
