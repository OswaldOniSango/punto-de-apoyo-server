package com.puntodeapoyo.users.controller;

import java.util.List;

import com.puntodeapoyo.users.dto.InternalUserResponse;
import com.puntodeapoyo.users.service.InternalUserLookupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class InternalUserLookupController {

    private final InternalUserLookupService userLookupService;

    public InternalUserLookupController(InternalUserLookupService userLookupService) {
        this.userLookupService = userLookupService;
    }

    @GetMapping("/engineers")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public List<InternalUserResponse> listActiveEngineers() {
        return userLookupService.listActiveEngineers();
    }
}
