package com.puntodeapoyo.users.controller;

import java.util.List;

import com.puntodeapoyo.users.dto.CreateInternalUserRequest;
import com.puntodeapoyo.users.dto.InternalUserResponse;
import com.puntodeapoyo.users.dto.UpdateInternalUserRequest;
import com.puntodeapoyo.users.service.InternalUserAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class InternalUserAdminController {

    private final InternalUserAdminService userAdminService;

    public InternalUserAdminController(InternalUserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public List<InternalUserResponse> listUsers() {
        return userAdminService.listUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InternalUserResponse createUser(@Valid @RequestBody CreateInternalUserRequest request) {
        return userAdminService.createUser(request);
    }

    @PatchMapping("/{id}")
    public InternalUserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateInternalUserRequest request) {
        return userAdminService.updateUser(id, request);
    }
}
