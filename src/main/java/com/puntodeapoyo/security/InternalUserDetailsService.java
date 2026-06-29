package com.puntodeapoyo.security;

import com.puntodeapoyo.users.repository.InternalUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class InternalUserDetailsService implements UserDetailsService {

    private final InternalUserRepository userRepository;

    public InternalUserDetailsService(InternalUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .map(InternalUserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario interno no encontrado"));
    }
}
