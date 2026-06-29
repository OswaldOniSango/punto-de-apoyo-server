package com.puntodeapoyo.users.service;

import java.util.List;

import com.puntodeapoyo.users.dto.InternalUserResponse;

public interface InternalUserLookupService {

    List<InternalUserResponse> listActiveEngineers();
}
