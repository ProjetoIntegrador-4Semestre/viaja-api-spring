package com.senac.viajaapi;

import java.util.ArrayList;
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
@RequestMapping("/places")
@Tag(name = "places", description = "CRUD de lugares/locais: busca por texto, listagem paginada, consulta por id, criacao, atualizacao e exclusao.")
public class LocalController {

    private final LocalRepository repository;
    private final LocalModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<Local> pagedResourcesAssembler;

    public LocalController(LocalRepository repository, LocalModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<Local> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    private PlaceResponse toPlaceResponse(Local local) {
        return new PlaceResponse(
                String.valueOf(local.getId()),
                local.getSource(),
                local.getSourceId(),
                local.getName(),
                local.getAddress(),
                local.getLat() == null ? null : local.getLat().doubleValue(),
                local.getLng() == null ? null : local.getLng().doubleValue(),
                List.of(local.getType()),
                local.getRating(),
                local.getRatingCount(),
                local.getPrice(),
                null,
                null,
                null,
                new ArrayList<>()
        );
    }

    @Operation(summary = "Buscar lugares por texto", description = "Busca lugares pelo nome ou endereco, retornando uma lista simples (nao paginada) compatível com consumidores REST. Utiliza filtro contendo (contains) ignorando maiusculas/minusculas.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de lugares encontrados")
    @GetMapping("/search")
    public List<PlaceResponse> searchPlaces(@Parameter(description = "Texto livre para busca no nome ou endereço do lugar") @RequestParam(required = false) String q) {
        List<Local> locais;
        if (q == null || q.isBlank()) {
            locais = repository.findAll();
        } else {
            locais = repository.findAll().stream()
                    .filter(l -> l.getName().toLowerCase().contains(q.toLowerCase())
                            || (l.getAddress() != null && l.getAddress().toLowerCase().contains(q.toLowerCase())))
                    .collect(Collectors.toList());
        }
        return locais.stream().map(this::toPlaceResponse).collect(Collectors.toList());
    }

    @Operation(summary = "Listar lugares", description = "Retorna lista paginada de lugares cadastrados no sistema. Use page, size e sort para controlar a paginacao e ordenacao.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de lugares com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Local>>> getAllPlaces(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Local> page = repository.findAll(pageable);
        PagedModel<EntityModel<Local>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Buscar lugar por id", description = "Retorna um lugar especificado pelo seu identificador, incluindo links de navegacao HATEOAS.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lugar encontrado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lugar nao encontrado para o id informado")
    @GetMapping("/{id}")
    public EntityModel<Local> getPlaceById(@Parameter(description = "Identificador numerico do lugar") @PathVariable Long id) {
        Local local = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar nao encontrado com ID: " + id));
        return assembler.toModel(local);
    }

    @Operation(summary = "Criar lugar", description = "Cadastra um novo lugar no sistema, vinculando a uma cidade existente e definindo categoria, preco, avaliacao e fonte de dados.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Lugar criado com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos: campos obrigatorios ausentes ou cidade inexistente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do lugar a ser criado",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Local.class),
                    examples = @ExampleObject(value = "{ \"name\": \"Museu de Arte\", \"address\": \"Av. Central, 100\", \"lat\": -23.5, \"lng\": -46.6, \"type\": \"attraction\", \"rating\": 4.5, \"priceLevel\": 2, \"photoUrl\": \"https://exemplo.com/foto.jpg\" }"))
    )
    @PostMapping
    public ResponseEntity<EntityModel<Local>> newPlace(@RequestBody @Valid Local newLocal) {
        Local saved = repository.save(newLocal);
        EntityModel<Local> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Atualizar lugar", description = "Atualiza todos os campos de um lugar existente: nome, endereco, coordenadas, categoria, preco, avaliacao, foto, cidade e fonte.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lugar atualizado com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lugar nao encontrado para o id informado")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Local>> updatePlace(
            @Parameter(description = "Identificador numerico do lugar a ser atualizado") @PathVariable Long id,
            @RequestBody @Valid Local newLocal) {
        Local local = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar nao encontrado com ID: " + id));
        local.setName(newLocal.getName());
        local.setAddress(newLocal.getAddress());
        local.setLat(newLocal.getLat());
        local.setLng(newLocal.getLng());
        local.setType(newLocal.getType());
        local.setRating(newLocal.getRating());
        local.setPrice(newLocal.getPrice());
        local.setCityId(newLocal.getCityId());
        local.setSource(newLocal.getSource());
        local.setSourceId(newLocal.getSourceId());
        return ResponseEntity.ok(assembler.toModel(repository.save(local)));
    }

    @Operation(summary = "Excluir lugar", description = "Remove definitivamente um lugar do sistema pelo seu identificador.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Lugar excluído com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lugar nao encontrado para o id informado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlace(@Parameter(description = "Identificador numerico do lugar a ser excluído") @PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
