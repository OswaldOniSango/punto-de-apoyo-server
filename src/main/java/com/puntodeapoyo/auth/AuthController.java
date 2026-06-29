package com.puntodeapoyo.auth;

import com.puntodeapoyo.security.InternalUserPrincipal;
import com.puntodeapoyo.security.JwtService;
import com.puntodeapoyo.security.JwtService.GeneratedToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        if (request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new BadCredentialsException("Email y password son requeridos");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        InternalUserPrincipal principal = (InternalUserPrincipal) authentication.getPrincipal();
        GeneratedToken token = jwtService.generateToken(principal);

        return new AuthResponse(
                "Bearer",
                token.value(),
                token.expiresAt(),
                new AuthResponse.AuthenticatedUser(
                        principal.id(),
                        principal.firstName(),
                        principal.lastName(),
                        principal.email(),
                        principal.phone(),
                        principal.role()
                )
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @org.springframework.web.bind.annotation.ExceptionHandler(AuthenticationException.class)
    void invalidCredentials() {
    }
}
