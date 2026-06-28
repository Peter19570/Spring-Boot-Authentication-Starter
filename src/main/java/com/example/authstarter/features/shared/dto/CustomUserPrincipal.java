package com.example.authstarter.features.shared.dto;

import com.example.authstarter.features.user.model.User;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record CustomUserPrincipal(
        UUID id,
        String email,
        String password,
        Collection<? extends GrantedAuthority> authorities

        // I'm keeping it light here... u can add fields u deem necessary, your call

) implements UserDetails{

    public CustomUserPrincipal(User user){
        this(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                List.of()
        );
    }

    public CustomUserPrincipal(
            UUID id,
            String email,
            Collection<? extends GrantedAuthority> authorities) {
        this(id, email, null, authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
