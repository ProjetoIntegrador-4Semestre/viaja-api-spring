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
@RequestMapping("/cities")
@Tag(name = "cities", description = "CRUD de cidades/destinos: listagem paginada, consulta por id, criacao, atualizacao e exclusao.")
public class CityController {

    private final CityRepository repository;
    private final CityModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<City> pagedResourcesAssembler;

    public CityController(CityRepository repository, CityModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<City> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar cidades", description = "Retorna lista paginada de cidades cadastradas no sistema. Use os parametros page, size e sort para controlar a paginacao e ordenacao.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de cidades com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<City>>> getAllCities(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<City> page = repository.findAll(pageable);
        PagedModel<EntityModel<City>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Buscar cidade por id", description = "Retorna uma cidade especifica pelo seu identificador, incluindo links de navegacao HATEOAS para endpoints relacionados.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cidade encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidade nao encontrada para o id informado")
    @GetMapping("/{id}")
    public EntityModel<City> getCityById(@Parameter(description = "Identificador numerico da cidade") @PathVariable Long id) {
        City city = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cidade nao encontrada com ID: " + id));
        return assembler.toModel(city);
    }

    @Operation(summary = "Criar cidade", description = "Cadastra uma nova cidade/destino no sistema, incluindo nome, estado, pais e URL da imagem de capa.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cidade criada com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos: campos obrigatorios ausentes ou formato incorreto")
    @PostMapping
    public ResponseEntity<EntityModel<City>> newCity(@RequestBody @Valid City newCity) {
        City saved = repository.save(newCity);
        EntityModel<City> entityModel = assembler.toModel(saved);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Operation(summary = "Atualizar cidade", description = "Atualiza todos os campos de uma cidade existente: nome, estado, pais e URL da imagem.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cidade atualizada com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidade nao encontrada para o id informado")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<City>> updateCity(
            @Parameter(description = "Identificador numerico da cidade a ser atualizada") @PathVariable Long id,
            @RequestBody @Valid City newCity) {
        City city = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cidade nao encontrada com ID: " + id));
        city.setName(newCity.getName());
        city.setState(newCity.getState());
        city.setCountry(newCity.getCountry());
        city.setImageUrl(newCity.getImageUrl());
        return ResponseEntity.ok(assembler.toModel(repository.save(city)));
    }

    @Operation(summary = "Excluir cidade", description = "Remove definitivamente uma cidade do sistema pelo seu identificador.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Cidade excluída com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidade nao encontrada para o id informado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCity(@Parameter(description = "Identificador numerico da cidade a ser excluída") @PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
