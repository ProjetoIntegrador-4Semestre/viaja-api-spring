package com.senac.viajaapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/users")
@Tag(name = "users", description = "Conta do usuario autenticado: consulta, atualizacao e exclusao. Cadastro em /auth/signup.")
public class UserController {

    private final UserRepository repository;
    private final UserModelAssembler assembler;
    private final org.springframework.data.web.PagedResourcesAssembler<User> pagedResourcesAssembler;

    public UserController(UserRepository repository, UserModelAssembler assembler,
            org.springframework.data.web.PagedResourcesAssembler<User> pagedResourcesAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Listar usuarios", description = "Retorna lista paginada apenas da conta autenticada. Use page, size e sort para controlar a paginacao e ordenacao.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista paginada de usuarios com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<User>>> getAllUsers(
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<User> page = repository.findById(currentUserId(), pageable);
        PagedModel<EntityModel<User>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Buscar usuario por id", description = "Retorna um usuario especificado pelo seu identificador, incluindo links de navegacao HATEOAS.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario nao encontrado para o id informado")
    @GetMapping("/{id}")
    public EntityModel<User> getUserById(@Parameter(description = "Identificador numerico do usuario") @PathVariable Long id) {
        requireOwner(id);
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado com ID: " + id));
        return assembler.toModel(user);
    }

    @Operation(summary = "Atualizar usuario", description = "Atualiza os campos de um usuario existente: nome, email e modo padrao (turismo ou trabalho).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario atualizado com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario nao encontrado para o id informado")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<User>> updateUser(
            @Parameter(description = "Identificador numerico do usuario a ser atualizado") @PathVariable Long id,
            @RequestBody @Valid User newUser) {
        requireOwner(id);
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado com ID: " + id));
        user.setName(newUser.getName());
        user.setEmail(newUser.getEmail());
        user.setPhone(newUser.getPhone());
        user.setAddress(newUser.getAddress());
        user.setDefaultMode(newUser.getDefaultMode());
        return ResponseEntity.ok(assembler.toModel(repository.save(user)));
    }

    @Operation(summary = "Excluir usuario", description = "Remove definitivamente um usuario do sistema pelo seu identificador.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Usuario excluído com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario nao encontrado para o id informado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@Parameter(description = "Identificador numerico do usuario a ser excluído") @PathVariable Long id) {
        requireOwner(id);
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied")).getId();
    }

    private void requireOwner(Long id) {
        if (!currentUserId().equals(id)) throw new org.springframework.security.access.AccessDeniedException("Access denied");
    }
}
