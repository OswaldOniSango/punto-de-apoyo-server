package com.puntodeapoyo.users.service.impl;

import java.util.List;

import com.puntodeapoyo.common.PhoneNormalizer;
import com.puntodeapoyo.users.dto.CreateInternalUserRequest;
import com.puntodeapoyo.users.dto.InternalUserResponse;
import com.puntodeapoyo.users.dto.UpdateInternalUserRequest;
import com.puntodeapoyo.users.model.InternalUser;
import com.puntodeapoyo.users.repository.InternalUserRepository;
import com.puntodeapoyo.users.repository.InternalUserRepository.CreateUserCommand;
import com.puntodeapoyo.users.repository.InternalUserRepository.UpdateUserCommand;
import com.puntodeapoyo.users.service.InternalUserAdminService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InternalUserAdminServiceImpl implements InternalUserAdminService {

    private final InternalUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InternalUserAdminServiceImpl(InternalUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<InternalUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(InternalUserResponse::from)
                .toList();
    }

    @Override
    public InternalUserResponse createUser(CreateInternalUserRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }

        try {
            InternalUser user = userRepository.create(new CreateUserCommand(
                    normalizeRequired(request.firstName()),
                    normalizeRequired(request.lastName()),
                    email,
                    PhoneNormalizer.normalize(request.phone()),
                    passwordEncoder.encode(request.password()),
                    request.role(),
                    request.status()
            ));
            return InternalUserResponse.from(user);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email", exception);
        }
    }

    @Override
    public InternalUserResponse updateUser(Long id, UpdateInternalUserRequest request) {
        if (!request.hasUpdates()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar al menos un campo para actualizar");
        }
        if (!userRepository.updatePartial(id, new UpdateUserCommand(request.role(), request.status()))) {
            throw userNotFound();
        }
        return userRepository.findById(id)
                .map(InternalUserResponse::from)
                .orElseThrow(this::userNotFound);
    }

    private String normalizeEmail(String email) {
        return normalizeRequired(email).toLowerCase();
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ResponseStatusException userNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
    }
}
