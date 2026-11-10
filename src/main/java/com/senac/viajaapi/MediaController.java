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
@RequestMapping("/medias")
@Tag(name = "medias", description = "Midias polimorficas (local, trip, user): listagem paginada, consulta, criacao, atualizacao e exclusao.")
public class MediaController {

    private final MediaRepository repository;
    private final MediaModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<Media> pagedResourcesAssembler;

    public MediaController(MediaRepository repository, MediaModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<Media> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar midias")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de midias com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Media>>> getAllMedias(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Media> page = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar midia por id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Midia encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Midia nao encontrada")
    @GetMapping("/{id}")
    public EntityModel<Media> getMediaById(@Parameter(description = "Identificador da midia") @PathVariable Long id) {
        Media media = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Midia nao encontrada com ID: " + id));
        return assembler.toModel(media);
    }

    @Operation(summary = "Criar midia")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Midia criada")
    @PostMapping
    public ResponseEntity<EntityModel<Media>> newMedia(@RequestBody @Valid Media newMedia) {
        Media saved = repository.save(newMedia);
        EntityModel<Media> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Atualizar midia")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Midia atualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Midia nao encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Media>> updateMedia(
            @PathVariable Long id, @RequestBody @Valid Media newMedia) {
        Media media = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Midia nao encontrada com ID: " + id));
        media.setEntityType(newMedia.getEntityType());
        media.setEntityId(newMedia.getEntityId());
        media.setUrl(newMedia.getUrl());
        media.setType(newMedia.getType());
        return ResponseEntity.ok(assembler.toModel(repository.save(media)));
    }

    @Operation(summary = "Excluir midia")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Midia excluída")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Midia nao encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedia(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
