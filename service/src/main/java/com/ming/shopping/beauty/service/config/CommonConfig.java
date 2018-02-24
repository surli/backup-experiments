package com.ming.shopping.beauty.service.config;

import com.huotu.verification.VerificationCodeConfig;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.crud.CrudConfig;
import me.jiangcai.lib.jdbc.JdbcSpringConfig;
import me.jiangcai.lib.misc.WechatVerifyConfig;
import me.jiangcai.lib.resource.web.WebResourceSpringConfig;
import me.jiangcai.lib.spring.logging.LoggingConfig;
import me.jiangcai.lib.sys.SystemStringConfig;
import me.jiangcai.lib.thread.ThreadConfig;
import me.jiangcai.lib.upgrade.UpgradeSpringConfig;
import me.jiangcai.payment.PaymentConfig;
import me.jiangcai.poi.template.POITemplateConfig;
import me.jiangcai.wx.WeixinSpringConfig;
import me.jiangcai.wx.pay.WeixinPayHookConfig;
import me.jiangcai.wx.pay.model.WeixinPayUrl;
import me.jiangcai.wx.standard.StandardWeixinConfig;
import me.jiangcai.wx.web.WeixinWebSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Created by helloztt on 2018/1/5.
 */
@Configuration
@Import({WebResourceSpringConfig.class, JdbcSpringConfig.class
        , POITemplateConfig.class
        , WechatVerifyConfig.class
        , UpgradeSpringConfig.class
        , VerificationCodeConfig.class
        , CrudConfig.class
        , WeixinSpringConfig.class, StandardWeixinConfig.class
        , WeixinPayHookConfig.class, PaymentConfig.class
        , ThreadConfig.class
        , SystemStringConfig.class
        , LoggingConfig.class})
public class CommonConfig extends WeixinWebSpringConfig {
    @Autowired
    private SystemService systemService;

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setDefaultEncoding("UTF-8");
        resourceBundleMessageSource.setBasenames("coreMessage");
        resourceBundleMessageSource.setUseCodeAsDefaultMessage(true);
        return resourceBundleMessageSource;
    }

    @Bean
    public WeixinPayUrl weixinPayUrl() {
        //微信支付异步回调地址
        WeixinPayUrl weixinPayUrl = new WeixinPayUrl();
        // 此处使用移动地址
        weixinPayUrl.setAbsUrl(systemService.toMobileUrl(WeixinPayUrl.relUrl));
        return weixinPayUrl;
    }
}
