package com.ming.shopping.beauty.service.utils;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.service.LoginService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author CJ
 */
public class LoginAuthentication implements Authentication {

    private final long id;
    private final LoginService loginService;
    private Login login;

    public LoginAuthentication(long id, LoginService loginService) {
        this.id = id;
        this.loginService = loginService;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        forUser();
        return login.getAuthorities();
    }

    private void forUser() {
        if (login == null)
            login = loginService.findOne(id);
    }

    @Override
    public Object getCredentials() {
        forUser();
        return login;
    }

    @Override
    public Object getDetails() {
        forUser();
        return login;
    }

    @Override
    public Object getPrincipal() {
        forUser();
        return login;
    }

    @Override
    public boolean isAuthenticated() {
//        user = null;
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        forUser();
        return login.getUsername();
    }
}
