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
@RequestMapping("/locals/{localId}/interests")
@Tag(name = "local-interests", description = "Categorias de um local: listagem paginada, adicionar e remover interesse de um local.")
public class LocalInterestController {

    private final LocalInterestRepository repository;
    private final LocalInterestModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<LocalInterest> pagedResourcesAssembler;

    public LocalInterestController(LocalInterestRepository repository, LocalInterestModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<LocalInterest> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar interesses de um local")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de interesses do local com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<LocalInterest>>> getInterestsByLocal(
            @Parameter(description = "Identificador do local") @PathVariable Long localId,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<LocalInterest> page = repository.findById_LocalId(localId, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Vincular interesse ao local")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Interesse vinculado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Vinculo ja existe")
    @PostMapping
    public ResponseEntity<?> addInterest(
            @Parameter(description = "Identificador do local") @PathVariable Long localId,
            @RequestBody @Valid LocalInterest newLocalInterest) {
        if (repository.existsByLocalIdAndInterestId(localId, newLocalInterest.getInterestId())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("erro", "Local ja possui esse interesse"));
        }
        newLocalInterest.setLocalId(localId);
        LocalInterest saved = repository.save(newLocalInterest);
        EntityModel<LocalInterest> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Remover interesse do local")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Vinculo removido")
    @DeleteMapping("/{interestId}")
    public ResponseEntity<?> removeInterest(
            @Parameter(description = "Identificador do local") @PathVariable Long localId,
            @Parameter(description = "Identificador do interesse") @PathVariable Long interestId) {
        if (!repository.existsByLocalIdAndInterestId(localId, interestId)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteByLocalIdAndInterestId(localId, interestId);
        return ResponseEntity.noContent().build();
    }
}
