package com.xunqin.config;

import com.xunqin.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .headers()
                .frameOptions().sameOrigin()
                .cacheControl().disable()
                .addHeaderWriter(new StaticHeadersWriter(
                    "Cache-Control", "no-cache, no-store, must-revalidate, private",
                    "Pragma", "no-cache",
                    "Expires", "0"
                ))
                .and()
            .authorizeRequests()
                .antMatchers("/", "/index.html", "/static/**", "/assets/**").permitAll()
                .antMatchers("/*.html").permitAll()
                .antMatchers("/auth/**", "/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs", "/api/**", "/volunteer-application/**").permitAll()
                .antMatchers("/missing-persons/**", "/success-cases/**", "/community/**", "/community/posts/**", "/volunteer-activities/**", "/health", "/ai/**").permitAll()
                .antMatchers("/uploads/**", "/images/**").permitAll()
                .antMatchers("/volunteer.html").permitAll()
                .anyRequest().authenticated();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
