package com.taskflow.security;

import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        return userRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with email: "+email));
    }
}
