package com.stugy.common.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public TokenBasedRememberMeServices rememberMeServices(StuGyUserDetailsService userDetailsService,
			@Value("${app.security.remember-me-key:stugy-local-remember-me-key}") String rememberMeKey) {
		TokenBasedRememberMeServices rememberMeServices = new TokenBasedRememberMeServices(
				rememberMeKey, userDetailsService);
		rememberMeServices.setTokenValiditySeconds(60 * 60 * 24 * 30);
		return rememberMeServices;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.rememberMe(rememberMe -> rememberMe.rememberMeServices(rememberMeServices))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/index.html", "/assets/**", "/api/users",
								"/api/auth/login", "/api/auth/me")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/api/study-groups")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/api/files/**")
						.permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(exception -> exception.authenticationEntryPoint(this::writeUnauthorized));

		return http.build();
	}

	private void writeUnauthorized(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"message\":\"로그인이 필요합니다.\"}");
	}
}
