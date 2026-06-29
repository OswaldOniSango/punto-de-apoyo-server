package com.puntodeapoyo.users.service;

import java.util.List;

import com.puntodeapoyo.users.dto.CreateInternalUserRequest;
import com.puntodeapoyo.users.dto.InternalUserResponse;
import com.puntodeapoyo.users.dto.UpdateInternalUserRequest;

public interface InternalUserAdminService {

    List<InternalUserResponse> listUsers();

    InternalUserResponse createUser(CreateInternalUserRequest request);

    InternalUserResponse updateUser(Long id, UpdateInternalUserRequest request);
}
