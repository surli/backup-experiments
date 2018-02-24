package com.ming.shopping.beauty.service.config;

import com.ming.shopping.beauty.service.config.security.UnauthorizedEntryPoint;
import com.ming.shopping.beauty.service.controller.QRController;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.wx.pay.model.WeixinPayUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * @author helloztt
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@Order(99)//毕竟不是老大 100就让给别人了
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LoginService loginService;
    @Autowired
    private Environment environment;

    @Autowired
    public void registerSharedAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(loginService).passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);

        WebSecurity.IgnoredRequestConfigurer configurer = web.ignoring()
                .antMatchers("/_version")
                // 微信校验
                .antMatchers("/MP_verify_*.txt", "/**/favicon.ico", "/weixin/sdk/config")
                // 微信事件
                .antMatchers("/_weixin_event/");
        if (environment.acceptsProfiles("staging", "test", "dev")) {
            // 特定环境下 允许任意人对日志访问
            configurer.antMatchers("/loggingConfig", "/loggingConfig/**");
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        http.addFilterBefore(filter, CsrfFilter.class);

        http.headers().frameOptions().sameOrigin();

        // 在测试环境下 随意上传
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry =
                http.antMatcher("/**")
                        .authorizeRequests();

        // TODO: 2018/1/8 一下地址都需要与接口核对
        registry
                //模拟登录
                .antMatchers("/loginAs/**").permitAll()
                .antMatchers(QRController.QR_URL).permitAll()
                //扫码登录
                .antMatchers("/managerLogin/**").permitAll()
                //登录前的校验
                .antMatchers("/init", "/isExist", "/isRegister/**", "/sendAuthCode/**", SystemService.LOGIN, SystemService.TO_LOGIN, SystemService.AUTH).permitAll()
                //管理后台登录
                .antMatchers("/currentManager", "/managerLoginRequest", "/managerLogin/**", "/manageLoginResult/**").permitAll()
                // 登录跳转页面
                .antMatchers("/wechatJoin**", "/wechatRegister/**").permitAll()
                .antMatchers("/toLoginWechat", "/wechatLogin").permitAll()
                // 微信绑定
                .antMatchers("/wechat/bindTo**").permitAll()
                //微信支付相关
                .antMatchers(WeixinPayUrl.relUrl, "/_payment/**").permitAll()
                // 手机号码可用性检测
                .antMatchers("/loginData/mobileValidation").permitAll()
                //错误页面
                .antMatchers("/error").permitAll()
                // 其他必须接受保护
                .antMatchers("/**").authenticated()
                .and().csrf().disable()
                .logout().logoutUrl("/logout").permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new UnauthorizedEntryPoint());
    }
}
