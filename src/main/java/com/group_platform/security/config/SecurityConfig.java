package com.group_platform.security.config;

import com.group_platform.security.entrypoint.CustomAuthenticationEntryPoint;
import com.group_platform.security.filter.CustomAuthenticationFilter;
import com.group_platform.security.filter.SessionValidationFilter;
import com.group_platform.security.handler.CustomAccessDeniedHandler;
import com.group_platform.security.handler.CustomAuthenticationFailureHandler;
import com.group_platform.security.handler.CustomAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity  //스프링 시큐리티한테 관리가 됨
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final SessionValidationFilter sessionValidationFilter;

    public SecurityConfig(CustomAuthenticationEntryPoint customAuthenticationEntryPoint, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler, CustomAuthenticationFailureHandler customAuthenticationFailureHandler, CustomAccessDeniedHandler customAccessDeniedHandler, SessionValidationFilter sessionValidationFilter) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.sessionValidationFilter = sessionValidationFilter;
    }

    @Bean
    // 단방향 해쉬 암호화 메서드
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filter(HttpSecurity http) throws Exception {
        // AuthenticationManager 가져오기
        AuthenticationManager authenticationManager = authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));

        // CustomAuthenticationFilter 생성 및 핸들러 연결
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManager);
        customAuthenticationFilter.setFilterProcessesUrl("/api/login"); // 로그인 요청 URL 변경 (필요시)
        customAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);
        customAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);



        // 인가처리 작업
        http
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/", "/api/login","/join","/api/test").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
//                        .requestMatchers("/my/**").hasAnyRole("ADMIN","USER")
                        .anyRequest().authenticated()   //authenticated : 로그인 된 사용자 모두, denyAll은 전부 막음
                );

        http.formLogin(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);

        //세션설정방법 (다중로그인 관리)
        http
                .sessionManagement((session)->session
                        //세션고정공격보호
                        //로그인 성공 시 세션 ID를 바꿔주는 안전장치
//                        .sessionFixation().changeSessionId()
                        .sessionFixation().newSession()
                        // default가 IF_REQUIRED라 설정할 필요없지만 명시해준다
                        // 이것으로 세션방식 로그인에서 HttpServletRequest같은걸 이용해서 직접 세션을 생성해서 넣어주는걸 안해도 된다.(알아서 해줌)
                        //        ALWAYS - 무조건 세션을 생성
                        //        IF_REQUIRED (기본값) - 필요할 때만 세션을 생성 (보통 로그인 시 생성)
                        //        NEVER - 세션을 생성하지 않지만, 기존 세션이 있으면 사용
                        //        STATELESS - 아예 세션을 생성하거나 사용X (REST API 무상태)
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1) //동시접속 중복로그인 갯수
                        //true : 초과시 새로운 로그인 차단, false : 초과시 기존 세션 하나 삭제
                        .maxSessionsPreventsLogin(true) //동시접속 갯수 초과시 처리 방법


                );

        //인가 관련 에러 처리
        http
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        //cors 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));


        //로그아웃 처리
        http
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")              // 로그아웃 처리 URI (기본은 /logout)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);  // 200 OK 반환
                        })
                        .invalidateHttpSession(true)           // 세션 무효화
                        .deleteCookies("SESSION")           // 쿠키 삭제 (세션 ID 등)
                );

        // 삭제한 유저에 대해 만약에라도 redis 남아있는 세션으로 요청을 보내는걸 방지(인증된 클라이언트인척)
        http.addFilterAfter(sessionValidationFilter, SecurityContextHolderFilter.class);

        // 기본 UsernamePasswordAuthenticationFilter 위치에 커스텀 필터 등록
        http.addFilterAt(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    //csr방식이기 때문에 필수이다.(form로그인 방식이 아니기 때문에)
    //AuthenticationManager는 사용자의 아이디와 비밀번호를 검증하는 핵심 컴포넌트다.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    //CORS오류 관련 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // 프론트 주소
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);  // 쿠키, 세션 인증 사용시 필요
        configuration.setAllowedHeaders(List.of("*")); // 필요한 헤더 설정 가능

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
