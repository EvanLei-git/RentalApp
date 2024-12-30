package gr.hua.dit.rentalapp.config;


import gr.hua.dit.rentalapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.Customizer;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(UserService userService, UserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // --- Public Endpoints (Guest) ---
                        // Static resources, home page, registration
                        .requestMatchers("/", "/home", "/register", "/saveUser",
                                "/images/**", "/js/**", "/css/**").permitAll()

                        // Allow GET requests to /api/properties/** for guest searching
                        .requestMatchers(HttpMethod.GET, "/api/properties/**").permitAll()

                        // --- Role-Based Endpoints ---
                        // Administrator endpoints
                        .requestMatchers("/api/administrators/**").hasRole("ADMIN")
                        // Landlord endpoints
                        .requestMatchers("/api/landlords/**").hasRole("LANDLORD")
                        // Tenant endpoints
                        .requestMatchers("/api/tenants/**").hasRole("TENANT")

                        // endpoint that only authenticated users can access
                        .requestMatchers("/api/applications/**").authenticated()

                        // Catch-all rule: everything else must be authenticated
                        .anyRequest().authenticated()
                )

                // --- Form Login  ---
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true) // redirect after successful login
                        .permitAll()
                )

                // --- Logout ---
                .logout((logout) -> logout.permitAll());


        return http.build();
    }
}

/*
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(UserService userService, UserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF protection, typical for REST APIs
                .authorizeRequests(auth -> auth
                        .requestMatchers("/users/add").permitAll() // Allow public access to user registration
                        .requestMatchers("/rentals/view/**", "/properties/**").permitAll() // Public access to view listings
                        .anyRequest().authenticated() // All other requests require authentication
                )
                .httpBasic(withDefaults()) // Use HTTP Basic for authentication; use withDefaults() for custom configuration if needed
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Use stateless session management
        return http.build();
    }
}
*/