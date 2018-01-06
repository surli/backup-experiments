package org.hswebframework.web.dao.mybatis.builder.jpa;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author zhouhao
 * @since 3.0
 */
@Table(name = "s_test")
@Data
public class TestEntity {
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private Integer age;

    @Column(name = "role_id")
    private String roleId;

}
