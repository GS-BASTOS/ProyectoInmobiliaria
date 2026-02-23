package com.inmobiliaria.app.security;

import com.inmobiliaria.app.domain.AppUser;
import com.inmobiliaria.app.repo.AppUserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initUsers(AppUserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("kundi").isEmpty()) {
                AppUser admin = new AppUser();
                admin.setUsername("kundi");
                admin.setPassword(encoder.encode("1234"));
                admin.setDisplayName("Kundi");
                admin.setRole("ADMIN");
                repo.save(admin);
            }
        };
    }
}
