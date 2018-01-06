package org.hswebframework.web.entity.datasource;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 数据源配置
 *
 * @author hsweb-generator-online
 */
public class SimpleDataSourceConfigEntity extends SimpleGenericEntity<String> implements DataSourceConfigEntity {
    //数据源名称
    private String         name;
    //是否启用
    private Long           enabled;
    //创建日期
    private java.util.Date createDate;
    //备注
    private String         describe;

    /**
     * @return 数据源名称
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @param name 数据源名称
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 是否启用
     */
    @Override
    public Long getEnabled() {
        return this.enabled;
    }

    /**
     * @param enabled 是否启用
     */
    @Override
    public void setEnabled(Long enabled) {
        this.enabled = enabled;
    }

    /**
     * @return 创建日期
     */
    @Override
    public java.util.Date getCreateDate() {
        return this.createDate;
    }

    /**
     * @param createDate 创建日期
     */
    @Override
    public void setCreateDate(java.util.Date createDate) {
        this.createDate = createDate;
    }

    /**
     * @return 备注
     */
    @Override
    public String getDescribe() {
        return this.describe;
    }

    /**
     * @param describe 备注
     */
    @Override
    public void setDescribe(String describe) {
        this.describe = describe;
    }
}