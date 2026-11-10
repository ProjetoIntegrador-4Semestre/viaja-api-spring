package com.senac.viajaapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@Tag(name = "auth", description = "Autenticação: login, cadastro e obtenção do token JWT.")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
            UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Login", description = "Autentica um usuário com email e senha. Gera um token JWT válido por 24 horas, retornado no header Authorization: Bearer <token>. O token deve ser enviado em todas as requisições subsequentes em endpoints protegidos.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticado com sucesso", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados de login ausentes ou inválidos")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciais invalidas: email ou senha incorretos")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciais de login do usuario",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(value = "{ \"email\": \"usuario@email.com\", \"senha\": \"senha123\" }"))
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authenticate(request);
    }

    @Operation(summary = "Cadastro de usuário", description = "Cadastra um novo usuário no sistema. A senha e armazenada com hash BCrypt. O modo padrao é definido como 'turismo'.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario criado com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email ja cadastrado ou dados invalidos")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo usuário",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SignupRequest.class),
                    examples = @ExampleObject(value = "{ \"name\": \"Rafael\", \"email\": \"rafael@email.com\", \"senha\": \"senha123\" }"))
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("erro", "Email já cadastrado"));
        }

        User usuario = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getSenha()),
                "tourism",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        userRepository.save(usuario);

        return ResponseEntity.status(201)
                .body(new ApiResponse("ok", "Usuário cadastrado com sucesso"));
    }

    private ResponseEntity<LoginResponse> authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(request.getEmail())
                .password("")
                .authorities("ROLE_USER")
                .build();
        String token = jwtService.generateToken(userDetails);

        User usuario = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        UserResponse userResponse = new UserResponse(
                usuario.getId(), usuario.getEmail(), usuario.getName(), usuario.getDefaultMode()
        );

        LoginResponse response = new LoginResponse("ok", "Login realizado com sucesso", userResponse);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        return ResponseEntity.ok().headers(headers).body(response);
    }
}
