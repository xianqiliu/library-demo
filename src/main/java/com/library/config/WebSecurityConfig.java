package com.library.config;

import com.library.service.Impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		
		return authProvider;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/swagger-ui/**", "/library-openapi/**").permitAll()
				.antMatchers(HttpMethod.POST, "/api/users").permitAll()
				.antMatchers(HttpMethod.GET, "/api/users").hasAnyAuthority("ADMIN")
				.antMatchers(HttpMethod.GET, "/api/books/**").hasAnyAuthority("USER","ADMIN")
				.antMatchers(HttpMethod.POST, "/api/books/**").hasAnyAuthority("ADMIN")
				.antMatchers(HttpMethod.PUT, "/api/books/**").hasAnyAuthority("ADMIN")
				.antMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyAuthority("ADMIN")
				.anyRequest().authenticated()
				.and()
				.httpBasic()
				.and()
				.csrf().disable()
				.headers().frameOptions().disable() //for H2 web console
				.and()
				;
	}

	@Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers("/h2-ui/**");
    }
}
