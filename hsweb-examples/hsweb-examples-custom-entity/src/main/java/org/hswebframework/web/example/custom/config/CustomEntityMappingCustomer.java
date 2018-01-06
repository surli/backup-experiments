package org.hswebframework.web.example.custom.config;

import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.example.custom.entity.CustomOrganizationalEntity;
import org.hswebframework.web.example.custom.entity.CustomUserEntity;
import org.hswebframework.web.starter.entity.EntityMappingCustomer;
import org.springframework.stereotype.Component;

/**
 * 自定义实体关系
 *
 * @author zhouhao
 * @since 3.0
 */
@Component
public class CustomEntityMappingCustomer implements EntityMappingCustomer {
    @Override
    public void customize(MapperEntityFactory entityFactory) {
        entityFactory.addMapping(OrganizationalEntity.class,
                MapperEntityFactory.defaultMapper(CustomOrganizationalEntity.class));

        entityFactory.addMapping(UserEntity.class,
                MapperEntityFactory.defaultMapper(CustomUserEntity.class));

        entityFactory.addMapping(BindRoleUserEntity.class,
                MapperEntityFactory.defaultMapper(CustomUserEntity.class));
    }
}
