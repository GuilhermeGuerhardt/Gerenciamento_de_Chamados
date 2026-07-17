package br.com.chamados.security;

import br.com.chamados.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Marca o ultimo acesso e leva cada perfil para a tela inicial. */
    @Bean
    public AuthenticationSuccessHandler successHandler(UsuarioService usuarioService) {
        return (request, response, authentication) -> {
            usuarioService.registrarAcesso(authentication.getName());
            response.sendRedirect(request.getContextPath() + "/");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                .requestMatchers("/login", "/erro/**").permitAll()

                // Cadastro de clientes, contatos e usuarios e area administrativa.
                .requestMatchers("/clientes/**").hasAnyRole("ADMIN", "TECNICO")
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/h2-console/**").hasRole("ADMIN")

                // Somente a equipe interna mexe no atendimento.
                .requestMatchers("/chamados/*/atribuir", "/chamados/*/status",
                                 "/chamados/*/reabrir", "/chamados/*/excluir").hasAnyRole("ADMIN", "TECNICO")

                // Lista contatos de qualquer cliente; um solicitante nao pode consultar
                // a agenda de outra empresa.
                .requestMatchers("/chamados/contatos").hasAnyRole("ADMIN", "TECNICO")

                .anyRequest().authenticated())

            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .successHandler(successHandler)
                .failureUrl("/login?erro")
                .permitAll())

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?saiu")
                .permitAll())

            .exceptionHandling(ex -> ex.accessDeniedPage("/erro/acesso-negado"))

            .rememberMe(Customizer.withDefaults());

        // O console do H2 roda dentro de um frame e nao envia o token CSRF;
        // liberamos apenas essa rota, que ja exige perfil ADMIN acima.
        http.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
