package com.example.authstarter.features.auth.config.userservice;

import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import com.example.authstarter.features.user.model.User;
import com.example.authstarter.features.user.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
        return new CustomUserPrincipal(user);
    }

    public CustomUserPrincipal loadUserById(String id){
        User user = userRepo.findById(UUID.fromString(id)).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
        return new CustomUserPrincipal(user);
    }
}
