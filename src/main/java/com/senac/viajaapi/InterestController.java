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
@RequestMapping("/interests")
@Tag(name = "interests", description = "Interesses: listagem paginada, consulta por id, criacao, atualizacao e exclusao.")
public class InterestController {

    private final InterestRepository repository;
    private final InterestModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<Interest> pagedResourcesAssembler;

    public InterestController(InterestRepository repository, InterestModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<Interest> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar interesses", description = "Retorna lista paginada de interesses cadastrados.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de interesses com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Interest>>> getAllInterests(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Interest> page = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar interesse por id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interesse encontrado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Interesse nao encontrado")
    @GetMapping("/{id}")
    public EntityModel<Interest> getInterestById(@Parameter(description = "Identificador do interesse") @PathVariable Long id) {
        Interest interest = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interesse nao encontrado com ID: " + id));
        return assembler.toModel(interest);
    }

    @Operation(summary = "Criar interesse")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Interesse criado")
    @PostMapping
    public ResponseEntity<EntityModel<Interest>> newInterest(@RequestBody @Valid Interest newInterest) {
        Interest saved = repository.save(newInterest);
        EntityModel<Interest> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Atualizar interesse")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interesse atualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Interesse nao encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Interest>> updateInterest(
            @PathVariable Long id, @RequestBody @Valid Interest newInterest) {
        Interest interest = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interesse nao encontrado com ID: " + id));
        interest.setName(newInterest.getName());
        interest.setCategory(newInterest.getCategory());
        return ResponseEntity.ok(assembler.toModel(repository.save(interest)));
    }

    @Operation(summary = "Excluir interesse")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Interesse excluído")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Interesse nao encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInterest(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
