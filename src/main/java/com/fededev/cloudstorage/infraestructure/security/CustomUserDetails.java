package com.fededev.cloudstorage.infraestructure.security;

import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.repository.UserRepository;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final UUID id;
    private final String email;
    private final String password;
    @Getter
    private final boolean deleted;

    public CustomUserDetails(AppUser user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.deleted = user.getDeletedAt() != null;
    }

    public CustomUserDetails(UUID id, String email, boolean deleted) {
        this.id = id;
        this.email = email;
        this.password = null;
        this.deleted = deleted;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return !deleted;
    }
}
