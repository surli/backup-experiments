package org.hswebframework.web.datasource.jta;

import org.hswebframework.web.datasource.config.DynamicDataSourceConfigRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author zhouhao
 */
@Configuration
public class AtomikosDataSourceAutoConfiguration {

    //默认数据源
    @Bean(initMethod = "init", destroyMethod = "destroy", value = "defaultDataSource")
    @Primary
    public AtomikosDataSourceBean defaultDataSource() {
        return new AtomikosDataSourceBean();
    }

    @ConditionalOnMissingBean(JtaDataSourceRepository.class)
    @Bean
    public InMemoryAtomikosDataSourceRepository memoryJtaDataSourceStore() {
        return new InMemoryAtomikosDataSourceRepository();
    }

    @Bean
    public JtaDynamicDataSourceService jtaDynamicDataSourceService(DynamicDataSourceConfigRepository<AtomikosDataSourceConfig> repository
            , DataSource dataSource) throws SQLException {
        return new JtaDynamicDataSourceService(repository, dataSource);
    }

}
