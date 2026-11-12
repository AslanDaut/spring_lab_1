package kz.iitu.spring_lab_1.web;

import kz.iitu.spring_lab_1.domain.AppUser;
import kz.iitu.spring_lab_1.domain.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error", "/css/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/books/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .formLogin(form -> form.loginPage("/login").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/?logout"));
        return http.build();
    }

    @Bean
    UserDetailsService databaseUserDetailsService(AppUserRepository users) {
        return username -> {
            AppUser u = users.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));
            return User.withUsername(u.getUsername())
                    .password(u.getPasswordHash())
                    .authorities(u.getRoles().stream()
                            .map(r -> "ROLE_" + r)
                            .toArray(String[]::new))
                    .disabled(!u.isEnabled())
                    .build();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    org.springframework.boot.CommandLineRunner initPasswords(
            AppUserRepository repo,
            PasswordEncoder encoder) {
        return args -> {
            String userHash = encoder.encode("user123");
            String adminHash = encoder.encode("admin123");
            System.out.println("REAL USER HASH:  " + userHash);
            System.out.println("REAL ADMIN HASH: " + adminHash);

            repo.findByUsername("user").ifPresent(u -> {
                u.setPasswordHash(userHash);
                repo.save(u);
            });
            repo.findByUsername("admin").ifPresent(u -> {
                u.setPasswordHash(adminHash);
                repo.save(u);
            });
        };
    }
}