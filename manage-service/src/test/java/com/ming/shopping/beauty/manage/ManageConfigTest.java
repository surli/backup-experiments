package com.ming.shopping.beauty.manage;

import com.ming.shopping.beauty.manage.config.ManageConfig;
import com.ming.shopping.beauty.service.CoreServiceTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author helloztt
 */
@ContextConfiguration(classes = ManageConfig.class)
public abstract class ManageConfigTest extends CoreServiceTest {
    protected static final String MANAGE_BASE_URL = "/manage";
}
