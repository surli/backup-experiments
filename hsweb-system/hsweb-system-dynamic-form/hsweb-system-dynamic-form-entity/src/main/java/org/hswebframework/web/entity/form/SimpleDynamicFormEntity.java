package org.hswebframework.web.entity.form;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 动态表单
 *
 * @author hsweb-generator-online
 */
public class SimpleDynamicFormEntity extends SimpleGenericEntity<String> implements DynamicFormEntity {
    //表单名称
    private String  name;
    //数据库表名
    private String  databaseTableName;
    //备注
    private String  describe;
    //版本
    private Long    version;
    //创建人id
    private String  creatorId;
    //创建时间
    private Long    createTime;
    //修改时间
    private Long    updateTime;
    //是否已发布
    private Boolean deployed;
    //别名
    private String  alias;
    //触发器
    private String  triggers;
    //表链接
    private String  correlations;
    //数据源id,为空使用默认数据源
    private String  dataSourceId;
    //表单类型
    private String  type;

    /**
     * @return 表单名称
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @param name 表单名称
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 数据库表名
     */
    @Override
    public String getDatabaseTableName() {
        return this.databaseTableName;
    }

    /**
     * @param databaseTableName 数据库表名
     */
    @Override
    public void setDatabaseTableName(String databaseTableName) {
        this.databaseTableName = databaseTableName;
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

    /**
     * @return 版本
     */
    @Override
    public Long getVersion() {
        return this.version;
    }

    /**
     * @param version 版本
     */
    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * @return 创建人id
     */
    @Override
    public String getCreatorId() {
        return this.creatorId;
    }

    /**
     * @param creatorId 创建人id
     */
    @Override
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * @return 创建时间
     */
    @Override
    public Long getCreateTime() {
        return this.createTime;
    }

    /**
     * @param createTime 创建时间
     */
    @Override
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * @return 修改时间
     */
    @Override
    public Long getUpdateTime() {
        return this.updateTime;
    }

    /**
     * @param updateTime 修改时间
     */
    @Override
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * @return 是否已发布
     */
    @Override
    public Boolean getDeployed() {
        return this.deployed;
    }

    /**
     * @param deployed 是否已发布
     */
    @Override
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    /**
     * @return 别名
     */
    @Override
    public String getAlias() {
        return this.alias;
    }

    /**
     * @param alias 别名
     */
    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return 触发器
     */
    @Override
    public String getTriggers() {
        return this.triggers;
    }

    /**
     * @param triggers 触发器
     */
    @Override
    public void setTriggers(String triggers) {
        this.triggers = triggers;
    }

    /**
     * @return 表链接
     */
    @Override
    public String getCorrelations() {
        return this.correlations;
    }

    /**
     * @param correlations 表链接
     */
    @Override
    public void setCorrelations(String correlations) {
        this.correlations = correlations;
    }

    /**
     * @return 数据源id, 为空使用默认数据源
     */
    @Override
    public String getDataSourceId() {
        return this.dataSourceId;
    }

    /**
     * @param dataSourceId 数据源id,为空使用默认数据源
     */
    @Override
    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    /**
     * @return 表单类型
     */
    @Override
    public String getType() {
        return this.type;
    }

    /**
     * @param type 表单类型
     */
    @Override
    public void setType(String type) {
        this.type = type;
    }
}