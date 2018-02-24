package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.lib.sys.service.SystemStringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author helloztt
 */
@Service("systemService")
public class SystemServiceImpl implements SystemService {

    private final Environment environment;
    private final SystemStringService systemStringService;

    @Autowired
    public SystemServiceImpl(Environment environment, SystemStringService systemStringService) {
        this.environment = environment;
        this.systemStringService = systemStringService;
    }

    @Override
    public Integer currentCardAmount() {
        return systemStringService.getCustomSystemString("shopping.service.card.amount", null, true
                , Integer.class, 5000);
    }

    @Override
    public String toUrl(String uri) {
        return environment.getProperty("shopping.url", "http://localhost") + uri;
    }

    @Override
    public String toMobileUrl(String uri) {
        return environment.getProperty("shopping.mobile.url", "http://localhost") + uri;
    }

    @Override
    public String toDesktopUrl(String uri) {
        return environment.getProperty("shopping.desktop.url", "http://localhost") + uri;
    }
}
