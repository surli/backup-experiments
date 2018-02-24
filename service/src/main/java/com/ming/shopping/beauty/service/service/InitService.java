package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.Version;
import com.ming.shopping.beauty.service.service.impl.support.AbstractLoginService;
import me.jiangcai.lib.jdbc.ConnectionProvider;
import me.jiangcai.lib.jdbc.JdbcService;
import me.jiangcai.lib.upgrade.VersionUpgrade;
import me.jiangcai.lib.upgrade.service.UpgradeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author helloztt
 */
@Service
public class InitService extends AbstractLoginService implements VersionUpgrade<Version> {
    private static final Log log = LogFactory.getLog(InitService.class);
    public static final String cjMobile = "18606509616";

    @Autowired
    private JdbcService jdbcService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UpgradeService upgradeService;

    @PostConstruct
    @Transactional(rollbackFor = RuntimeException.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void init() throws SQLException {
        database();
        upgradeService.systemUpgrade(this);
        initSuperManage();
    }

    /**
     * 定义一些超级管理员
     */
    private void initSuperManage() {
        addRoot(cjMobile, "蒋");
        addRoot("13588049877", "叶");
    }


    private void database() throws SQLException {
        jdbcService.runJdbcWork(connection -> {
            String fileName;
            if (connection.profile().isMySQL()) {
                fileName = "mysql";
            } else if (connection.profile().isH2()) {
                fileName = "h2";
            } else {
                throw new IllegalStateException("not support for:" + connection.getConnection());
            }
            try {
                try (Statement statement = connection.getConnection().createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS `CapitalFlow`");
                    statement.executeUpdate(StreamUtils.copyToString(new ClassPathResource(
                                    "/CapitalFlow." + fileName + ".sql").getInputStream()
                            , Charset.forName("UTF-8")));
                }
            } catch (IOException e) {
                throw new IllegalStateException("读取SQL失败", e);
            }
            //

        });
    }

    private void executeSQLCode(ConnectionProvider connection, String resourceName) throws SQLException {
        try {
            String code = StreamUtils.copyToString(applicationContext.getResource("classpath:/" + resourceName).getInputStream(), Charset.forName("UTF-8"));
            try (Statement statement = connection.getConnection().createStatement()) {
                statement.executeUpdate(code);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void upgradeToVersion(Version version) throws Exception {
        switch (version) {
            case init:
                //never happen
                break;
            default:
        }
    }

}
