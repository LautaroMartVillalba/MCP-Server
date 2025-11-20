package ar.mcp.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception{
        /**
         * Configures the main security filter chain used by Spring Security.
         *
         * @param security the HttpSecurity builder provided by Spring
         * @return built SecurityFilterChain instance ready to be registered
         * @throws Exception when configuration cannot be applied
         */
        security
                .cors(cors-> cors.configurationSource(corsConfigurationSource()))
                //TODO resolve the csrf configuration
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/mcp/**").permitAll()
                    .requestMatchers("/api/**").permitAll()
                    .requestMatchers("/sse/**").permitAll()
                )
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                /*.addFilterBefore(null)*/;
                //TODO create JWT based authentication logic and implement

        return security.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        /**
         * Produces a `CorsConfigurationSource` used by Spring to permit cross-origin
         * requests for the API.
         *
         * Configuration details:
         * - allowed origin patterns: all
         * - allowed methods: GET, POST, PUT, DELETE, OPTIONS
         * - allowed headers: all
         * - allow credentials: true
         * - max age: 3600 seconds
         *
         * @return configured `CorsConfigurationSource`
         */
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

}
