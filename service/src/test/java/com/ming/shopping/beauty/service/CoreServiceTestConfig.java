package com.ming.shopping.beauty.service;

import com.huotu.vefification.test.VerificationCodeTestConfig;
import com.ming.shopping.beauty.service.config.MvcConfig;
import com.ming.shopping.beauty.service.config.SecurityConfig;
import com.ming.shopping.beauty.service.config.ServiceConfig;
import com.ming.shopping.beauty.service.utils.TestDataSource;
import me.jiangcai.lib.test.config.H2DataSourceConfig;
import me.jiangcai.wx.WeixinSpringConfig;
import me.jiangcai.wx.test.WeixinTestConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author helloztt
 */
@Configuration
@ImportResource("classpath:/service_config_test.xml")
@PropertySource({"classpath:/test_wx.properties"})
@Import({WeixinTestConfig.class, VerificationCodeTestConfig.class
        , ServiceConfig.class, MvcConfig.class, SecurityConfig.class})
public class CoreServiceTestConfig extends H2DataSourceConfig implements WebMvcConfigurer {
    private static final Log log = LogFactory.getLog(CoreServiceTestConfig.class);
    @Autowired
    private Environment environment;

    @Bean
    public DataSource dataSource() {
        if (environment.acceptsProfiles(ServiceConfig.PROFILE_MYSQL)) {
            DriverManagerDataSource dataSource;
            if (environment.acceptsProfiles(ServiceConfig.PROFILE_JDBC))
                dataSource = new TestDataSource();
            else
                dataSource = new DriverManagerDataSource();

            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            // ?profileSQL=true
            dataSource.setUrl("jdbc:mysql://localhost/shopping");
            dataSource.setUsername("root");
            return dataSource;
        }
        if (environment.acceptsProfiles("h2file")) {
            return fileDataSource("shopping");
        }
        return memDataSource("com/ming/shopping","MySQL");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer pathMatchConfigurer) {

    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer contentNegotiationConfigurer) {

    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer asyncSupportConfigurer) {

    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer defaultServletHandlerConfigurer) {

    }

    @Override
    public void addFormatters(FormatterRegistry formatterRegistry) {

    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry resourceHandlerRegistry) {

    }

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

    }

    @Override
    public void addViewControllers(ViewControllerRegistry viewControllerRegistry) {

    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry viewResolverRegistry) {

    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> list) {
        // 即使没有安全系统；依然可以根据 AuthenticationPrincipal 获取当前登录状态
        list.add(new AuthenticationPrincipalArgumentResolver());
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> list) {

    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> list) {

    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> list) {

    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> list) {

    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> list) {

    }

    @Override
    public Validator getValidator() {
        return null;
    }

    @Override
    public MessageCodesResolver getMessageCodesResolver() {
        return null;
    }
}
