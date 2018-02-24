package com.ming.shopping.beauty.config;

import com.ming.shopping.beauty.client.config.ClientConfig;
import com.ming.shopping.beauty.manage.config.ManageConfig;
import com.ming.shopping.beauty.service.config.MvcConfig;
import com.ming.shopping.beauty.service.config.SecurityConfig;
import me.jiangcai.lib.git.GitSpringConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author helloztt
 */
@Configuration
@Import({MvcConfig.class, SecurityConfig.class, ClientConfig.class, ManageConfig.class, GitSpringConfig.class})
@ComponentScan("com.ming.shopping.beauty.controller")
public class WebConfig {
}
