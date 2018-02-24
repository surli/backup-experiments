package com.ming.shopping.beauty.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.utils.Constant;
import com.ming.shopping.beauty.service.utils.ImageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by helloztt on 2017/12/20.
 */
@Configuration
@EnableWebMvc
@Import({ServiceConfig.class, MvcConfig.ThymeleafConfig.class})
@ComponentScan({"com.ming.shopping.beauty.service.controller", "com.ming.shopping.beauty.service.converter"})
public class MvcConfig extends WebMvcConfigurerAdapter {

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.extendMessageConverters(converters);
        // 必须确保 json MappingJackson2HttpMessageConverter 比 xml MappingJackson2XmlHttpMessageConverter 优先级高
        HttpMessageConverter xml = converters.stream().filter(httpMessageConverter
                -> httpMessageConverter instanceof MappingJackson2XmlHttpMessageConverter)
                .findAny().orElse(null);

        HttpMessageConverter json = converters.stream().filter(httpMessageConverter
                -> httpMessageConverter instanceof MappingJackson2HttpMessageConverter)
                .findAny().orElse(null);

        if (xml != null && json != null) {
            int index = converters.indexOf(xml);
            converters.remove(json);
            converters.add(index, json);
        }
        converters.add(new AbstractHttpMessageConverter<AuditStatus>() {
            @Override
            protected boolean supports(Class<?> clazz) {
                return AuditStatus.class.equals(clazz);
            }

            @Override
            protected AuditStatus readInternal(Class<? extends AuditStatus> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
                String inputString = mapper.readTree(inputMessage.getBody()).asText();
                logger.debug("greeting AuditStatus for " + inputString);
                return AuditStatus.valueOf(inputString);
            }

            @Override
            protected void writeInternal(AuditStatus status, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
                throw new HttpMessageNotWritableException("我不干");
            }
        });

        converters.add(new AbstractHttpMessageConverter<ManageLevel>() {
            @Override
            protected boolean supports(Class<?> clazz) {
                return ManageLevel.class.equals(clazz);
            }

            @Override
            protected ManageLevel readInternal(Class<? extends ManageLevel> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
                String inputString = mapper.readTree(inputMessage.getBody()).asText();
                logger.debug("greeting AuditStatus for " + inputString);
                return ManageLevel.valueOf(inputString);
            }

            @Override
            protected void writeInternal(ManageLevel status, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
                throw new HttpMessageNotWritableException("我不干");
            }
        });
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        super.addFormatters(registry);
        registry.addConverter(new Converter<LocalDateTime, String>() {
            @Override
            public String convert(LocalDateTime source) {
                return source.format(Constant.dateTimeFormatter);
            }
        });
        registry.addConverter(new Converter<LocalDate, String>() {
            @Override
            public String convert(LocalDate source) {
                return source.format(Constant.dateFormatter);
            }
        });
    }

    /**
     * 文件上传
     */
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        returnValueHandlers.add(0, new ImageResolver());
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        super.configureViewResolvers(registry);
        registry.viewResolver(thymeleafViewResolver);
    }

    @Import(ThymeleafConfig.ThymeleafTemplateConfig.class)
    static class ThymeleafConfig {
        @Autowired
        private TemplateEngine engine;

        @Bean
        private ThymeleafViewResolver thymeleafViewResolver() {
            ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
            viewResolver.setTemplateEngine(engine);
            viewResolver.setCharacterEncoding("UTF-8");
            viewResolver.setContentType("text/html;charset=UTF-8");
            return viewResolver;
        }

        @ComponentScan("me.jiangcai.dating.web.thymeleaf")
        static class ThymeleafTemplateConfig {

            @Autowired
            private WebApplicationContext webApplicationContext;

            @Bean
            public TemplateEngine templateEngine() throws IOException {
                SpringTemplateEngine engine = new SpringTemplateEngine();
                engine.setEnableSpringELCompiler(true);
                engine.setTemplateResolver(templateResolver());
                engine.addDialect(new Java8TimeDialect());
                engine.addDialect(new SpringSecurityDialect());
                return engine;
            }

            private ITemplateResolver templateResolver() throws IOException {
                SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
                resolver.setApplicationContext(webApplicationContext);
                resolver.setCharacterEncoding("UTF-8");
                resolver.setPrefix("classpath:");
                resolver.setSuffix(".html");
                resolver.setTemplateMode(TemplateMode.HTML);
                return resolver;
            }
        }

    }
}
