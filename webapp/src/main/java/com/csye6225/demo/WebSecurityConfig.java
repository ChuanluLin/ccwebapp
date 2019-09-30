package com.csye6225.demo;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMethod;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery("select email as username, password, 1 as enabled "
                        + "from user where email=?")
                .authoritiesByUsernameQuery("select email as username, 'USER' as authority "
                        + "from user where email=?")
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // it indicate basic authentication is requires
                .httpBasic()
                .and()
                .authorizeRequests()
                // "create" operation will be accessible directly, no need of any authentication
                .antMatchers( "/v1/user").permitAll()
                // it's indicate all request in the url will be secure
                // actually for there is only one user, authenticated() is enough
                .antMatchers( "/v1/user/self").hasAuthority("USER")
                .antMatchers( "/v1/recipie/").hasAuthority("USER")
                .antMatchers( HttpMethod.DELETE,"/v1/recipie/{id}").hasAuthority("USER")
                .antMatchers( HttpMethod.PUT,"/v1/recipie/{id}").hasAuthority("USER");
        http.
                csrf().disable()
                // don't create session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
