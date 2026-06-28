package com.puntodeapoyo.security;

import java.util.Collection;
import java.util.List;

import com.puntodeapoyo.users.model.InternalUser;
import com.puntodeapoyo.users.model.UserRole;
import com.puntodeapoyo.users.model.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class InternalUserPrincipal implements UserDetails {

    private final InternalUser user;
    private final List<GrantedAuthority> authorities;

    public InternalUserPrincipal(InternalUser user) {
        this.user = user;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
    }

    public Long id() {
        return user.id();
    }

    public String firstName() {
        return user.firstName();
    }

    public String lastName() {
        return user.lastName();
    }

    public String email() {
        return user.email();
    }

    public String phone() {
        return user.phone();
    }

    public UserRole role() {
        return user.role();
    }

    public UserStatus status() {
        return user.status();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.passwordHash();
    }

    @Override
    public String getUsername() {
        return user.email();
    }

    @Override
    public boolean isEnabled() {
        return user.status() == UserStatus.ACTIVE;
    }
}
