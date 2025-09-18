package com.mohamedMoslemani.kyc.security;

import com.mohamedMoslemani.kyc.model.User;
import com.mohamedMoslemani.kyc.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

 @Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    String authority = "ROLE_" + user.getRole().name(); 

    return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .authorities(authority)
            .disabled(!user.isEnabled())
            .build();
}


}
