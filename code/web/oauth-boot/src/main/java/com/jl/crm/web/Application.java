package com.jl.crm.web;

import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SpringBootWebSecurityConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.util.UriTemplate;

import com.jl.crm.services.CrmService;
import com.jl.crm.services.ServiceConfiguration;

@ComponentScan
@EnableAutoConfiguration(exclude = { SpringBootWebSecurityConfiguration.class })
public class Application extends SpringBootServletInitializer {
	private static Class<Application> applicationClass = Application.class;

	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder application) {
		return application.sources(applicationClass);
	}

	public static void main(String[] args) {
		SpringApplication.run(applicationClass);
	}
}

@Configuration
@Import({ ServiceConfiguration.class, RepositoryRestMvcConfiguration.class })
class WebMvcConfiguration {

	String curieNamespace = "crm";

	@Bean
	MultipartConfigElement multipartConfigElement() {
		return new MultipartConfigElement("");
	}

	@Bean
	MultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}

	@Bean
	DefaultCurieProvider defaultCurieProvider() {
		return new DefaultCurieProvider(curieNamespace, new UriTemplate(
				"http://localhost:8080/rels/{rel}"));
	}
}

@Configuration
@EnableWebMvcSecurity
class MvcSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	CrmService crmService;

	String applicationName = "crm";

	@Override
	@Bean
	protected UserDetailsService userDetailsService() {
		return new CrmUserDetailsService(this.crmService);
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean
	TextEncryptor textEncryptor() {
		return Encryptors.noOpText();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
		auth.userDetailsService(new CrmUserDetailsService(this.crmService));
	}

	/* @formatter:off */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http		
			.authorizeRequests()
				.antMatchers( "/**", "/favicon.ico", "/webjars/**").permitAll()				
				.anyRequest().authenticated()
				.and()
			.formLogin()		
				.defaultSuccessUrl("/home")
				.failureUrl("/login")
				.loginPage("/login")
				.permitAll() 
				.and()
			/* .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and() */
			.logout()
				.permitAll()
				.and()
			.csrf()
				.disable() ;

	}
	/* @formatter:on */

}