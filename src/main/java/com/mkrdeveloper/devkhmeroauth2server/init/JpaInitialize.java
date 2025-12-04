package com.mkrdeveloper.devkhmeroauth2server.init;


import com.mkrdeveloper.devkhmeroauth2server.entity.Authority;
import com.mkrdeveloper.devkhmeroauth2server.entity.User;
import com.mkrdeveloper.devkhmeroauth2server.repository.AuthorityRepository;
import com.mkrdeveloper.devkhmeroauth2server.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaInitialize {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    void init(){
        Authority read = Authority.builder().name("read").build();
        Authority write = Authority.builder().name("write").build();
        Authority update = Authority.builder().name("update").build();
        Authority delete = Authority.builder().name("delete").build();

        authorityRepository.saveAll(java.util.List.of(read, write, update, delete));

        User admin = User.builder()
                .uuid(UUID.randomUUID().toString())
                .username("admin")
                .password(passwordEncoder.encode("12345"))
                .familyName("Admin")
                .givenName("Localhost")
                .gender("MALE")
                .email("gdevith@gmail.com")
                .jobTitle("Business & Singer")
                .dob(LocalDate.of(1990, 1, 1))
                .authorities(Set.of(read, write, update, delete))
                .build();
        User vanda = User.builder()
                .uuid(UUID.randomUUID().toString())
                .username("Vanda")
                .password(passwordEncoder.encode("12345"))
                .familyName("Mann")
                .givenName("Vanda")
                .gender("MALE")
                .email("mannvanda@gmail.com")
                .jobTitle("Rapper")
                .dob(LocalDate.of(1996, 1, 4))
                .authorities(Set.of(read, write))
                .build();

        userRepository.save(admin);
        userRepository.save(vanda);
    }
}
