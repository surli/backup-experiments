package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.login.Login;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by helloztt on 2018/1/5.
 */
public class LoginServiceTest extends CoreServiceTest {
    @Autowired
    private LoginService loginService;
    @Test
    public void getLogin() throws Exception {
        Login login = mockLogin();
        assertThat(login).isNotNull();
        assertThat(login.getUser()).isNotNull();
        assertThat(login.getUser().isActive()).isFalse();
    }

}