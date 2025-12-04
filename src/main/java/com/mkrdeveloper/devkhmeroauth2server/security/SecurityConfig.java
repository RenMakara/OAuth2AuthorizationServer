package com.mkrdeveloper.devkhmeroauth2server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    // ------------------------------------------------------------------------------------
    // This beans I commented because it provide in-memory user management,
    // which has been replaced with database-backed user details via UserDetailServiceImpl
    // and UserRepository for persistent storage using Spring Data JPA.
    // -------------------------------------------------------------------------------------

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails userDetails = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(userDetails);
//    }


//    @Bean
//    public InMemoryUserDetailsManager inMemoryUserDetailsManager(){
//        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//        manager.createUser(User.withUsername("admin")
//                .password(passwordEncoder().encode("mkrmkr"))
//                .roles("ADMIN")
//                .build());
//        manager.createUser(User.withUsername("user")
//                .password(passwordEncoder().encode("useruser"))
//                .roles("USER")
//                .build());
//        return manager;
//    }
    // -------------------------------------------------------------------------------------

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(){
        return AuthorizationServerSettings.builder().build();
    }


    @Bean
    @Order(1)
    public SecurityFilterChain oauth2Security(HttpSecurity http) throws Exception{

        /// Deprecated and removed in Spring Authorization Server 1.4+.==============

        //        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        //        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
        //                .oidc(Customizer.withDefaults());

        /// ==========================================================================

        OAuth2AuthorizationServerConfigurer auth2AuthorizationServerConfigurer =
                 OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(auth2AuthorizationServerConfigurer.getEndpointsMatcher())
                .with(auth2AuthorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer
                                .oidc(Customizer.withDefaults())	// Enable OpenID Connect 1.0
                )
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .anyRequest().authenticated()
                )
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception{

        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public", "/login", "/css/**").permitAll()
                .anyRequest().authenticated()
                );



        http.formLogin(form -> form
                .loginPage("/login")
                .permitAll()
        );
        return http.build();
    }

    @Bean
    RegisteredClientRepository registeredClientRepository() {
        // ------------------------------------
        // Client A: Authorization Code + PKCE
        // -----------------------------------
        RegisteredClient frontendClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("frontend-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8082/login/oauth2/code/frontend-client")
                .scope(OidcScopes.OPENID)
                .scope("profile")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(3))
                        .reuseRefreshTokens(false) // one-time refresh token
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true) // PKCE REQUIRED
                        .requireAuthorizationConsent(true) // Authorization Consent REQUIRED
                        .build())
                .build();


        // -----------------------------
        // Client B: Client Credentials
        // -----------------------------

        RegisteredClient backendService = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("backend-service")
                .clientSecret(passwordEncoder().encode("secret123"))

                // Client Authentication Method
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)

                // Grant Type
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .scope("write")

                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .build())

                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .build();



        return new InMemoryRegisteredClientRepository(frontendClient, backendService);
    }
}
