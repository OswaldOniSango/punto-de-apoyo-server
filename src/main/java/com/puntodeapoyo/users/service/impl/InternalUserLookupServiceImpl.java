package com.puntodeapoyo.users.service.impl;

import java.util.List;

import com.puntodeapoyo.users.dto.InternalUserResponse;
import com.puntodeapoyo.users.model.UserRole;
import com.puntodeapoyo.users.model.UserStatus;
import com.puntodeapoyo.users.repository.InternalUserRepository;
import com.puntodeapoyo.users.service.InternalUserLookupService;
import org.springframework.stereotype.Service;

@Service
public class InternalUserLookupServiceImpl implements InternalUserLookupService {

    private final InternalUserRepository userRepository;

    public InternalUserLookupServiceImpl(InternalUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<InternalUserResponse> listActiveEngineers() {
        return userRepository.findByRoleAndStatus(UserRole.ENGINEER, UserStatus.ACTIVE).stream()
                .map(InternalUserResponse::from)
                .toList();
    }
}
