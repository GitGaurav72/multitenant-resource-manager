//package com.edstruments.multitenant_resource_manager.security;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfig {
//
//	@Autowired
//    private CustomUserDetailsService customUserDetailsService;
//	@Autowired
//    private JwtAuthenticationEntryPoint unauthorizedHandler;
//	@Autowired
//    private JwtTokenProvider tokenProvider;
//
//    @Bean
//    public JwtAuthenticationFilter jwtAuthenticationFilter() {
//        return new JwtAuthenticationFilter(tokenProvider);
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        AuthenticationManagerBuilder authenticationManagerBuilder = 
//            http.getSharedObject(AuthenticationManagerBuilder.class);
//        authenticationManagerBuilder
//            .userDetailsService(customUserDetailsService)
//            .passwordEncoder(passwordEncoder());
//        return authenticationManagerBuilder.build();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .cors(cors -> cors.configure(http))
//            .csrf(csrf -> csrf.disable())
//            .exceptionHandling(exception -> exception
//                .authenticationEntryPoint(unauthorizedHandler))
//            .sessionManagement(session -> session
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(
//                    "/",
//                    "/favicon.ico",
//                    "/**/*.png",
//                    "/**/*.gif",
//                    "/**/*.svg",
//                    "/**/*.jpg",
//                    "/**/*.html",
//                    "/**/*.css",
//                    "/**/*.js"
//                ).permitAll()
//                .requestMatchers("/api/auth/**", "/api/auth/signup").permitAll()
//                .requestMatchers("/api/tenants/**").hasRole("SUPER_ADMIN")
//                .anyRequest().authenticated()
//            );
//
//        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
//        
//        return http.build();
//    }
//}


package com.edstruments.multitenant_resource_manager.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                        JwtAuthenticationEntryPoint unauthorizedHandler,
                        JwtTokenProvider tokenProvider) {
        this.customUserDetailsService = customUserDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.tokenProvider = tokenProvider;
    }

    @Autowired
    public JwtAuthenticationFilter jwtAuthenticationFilter;
//    
//    () {
//        return new JwtAuthenticationFilter(tokenProvider);
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        AuthenticationManagerBuilder authenticationManagerBuilder = 
//            http.getSharedObject(AuthenticationManagerBuilder.class);
//        authenticationManagerBuilder
//            .userDetailsService(customUserDetailsService)
//            .passwordEncoder(passwordEncoder());
//        return authenticationManagerBuilder.build();
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            	    .requestMatchers("/api/auth/**").permitAll()
            	    .requestMatchers("/api/tenants/**").hasRole("SUPER_ADMIN")
            	    .requestMatchers("/api/users/**").hasRole("ADMIN")
            	    .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")
            	    .requestMatchers(HttpMethod.POST, "/api/resources/**").hasAnyRole("ADMIN", "MANAGER")
            	    .requestMatchers(HttpMethod.PUT, "/api/resources/**").hasAnyRole("ADMIN", "MANAGER")
            	    .requestMatchers(HttpMethod.DELETE, "/api/resources/**").hasAnyRole("ADMIN", "MANAGER")
            	    .requestMatchers(HttpMethod.GET, "/api/resources/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
            	    .anyRequest().authenticated()
            	
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}