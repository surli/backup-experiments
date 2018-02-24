package com.ming.shopping.beauty.service.config;

import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author helloztt
 * @date 2017/12/21
 */
@Configuration
@Import({CommonConfig.class, DataSupportConfig.class})
@ComponentScan({"com.ming.shopping.beauty.service.service"})
@EnableJpaRepositories(basePackages = "com.ming.shopping.beauty.service.repository")
public class ServiceConfig {
    public static final String PROFILE_UNIT_TEST = "unit_test";
    public static final String PROFILE_TEST = "test";

    public static final String PROFILE_MYSQL = "mysql";
    public static final String PROFILE_JDBC = "jdbcProfile";
}
