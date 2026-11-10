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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/{userId}/interests")
@Tag(name = "user-interests", description = "Interesses do usuario: listagem paginada, adicionar e remover interesse de um usuario.")
public class UserInterestController {

    private final UserInterestRepository repository;
    private final UserInterestModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<UserInterest> pagedResourcesAssembler;

    public UserInterestController(UserInterestRepository repository, UserInterestModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<UserInterest> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar interesses do usuario", description = "Retorna interesses salvos de um usuario, paginados.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de interesses do usuario com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<UserInterest>>> getInterestsByUser(
            @Parameter(description = "Identificador do usuario") @PathVariable Long userId,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<UserInterest> page = repository.findById_UserId(userId, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar interesse do usuario por id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vinculo encontrado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vinculo usuario-interesse nao encontrado")
    @GetMapping("/{interestId}")
    public EntityModel<UserInterest> getById(
            @Parameter(description = "Identificador do usuario") @PathVariable Long userId,
            @Parameter(description = "Identificador do interesse") @PathVariable Long interestId) {
        UserInterest userInterest = repository.findById(new UserInterestId(userId, interestId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vinculo usuario-interesse nao encontrado"));
        return assembler.toModel(userInterest);
    }

    @Operation(summary = "Adicionar interesse ao usuario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Interesse adicionado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Usuario ja possui esse interesse")
    @PostMapping
    public ResponseEntity<?> addInterest(
            @Parameter(description = "Identificador do usuario") @PathVariable Long userId,
            @RequestBody @Valid UserInterest newUserInterest) {
        if (repository.existsByUserIdAndInterestId(userId, newUserInterest.getInterestId())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("erro", "Usuario ja possui esse interesse"));
        }
        newUserInterest.setUserId(userId);
        newUserInterest.setCreatedAt(LocalDateTime.now());
        UserInterest saved = repository.save(newUserInterest);
        EntityModel<UserInterest> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Remover interesse do usuario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Interesse removido")
    @DeleteMapping("/{interestId}")
    public ResponseEntity<?> removeInterest(
            @Parameter(description = "Identificador do usuario") @PathVariable Long userId,
            @Parameter(description = "Identificador do interesse") @PathVariable Long interestId) {
        if (!repository.existsByUserIdAndInterestId(userId, interestId)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteByUserIdAndInterestId(userId, interestId);
        return ResponseEntity.noContent().build();
    }
}
