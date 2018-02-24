package com.ming.shopping.beauty.service.model.definition;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import lombok.Getter;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author CJ
 */
@Getter
public class ManagerModel implements DefinitionModel<Login> {

    private final List<FieldDefinition<Login>> definitions;

    @SuppressWarnings("unchecked")
    public ManagerModel(ConversionService conversionService) {
        super();
        definitions = Arrays.asList(
                FieldBuilder.asName(Login.class, "id")
                        .addSelect(loginRoot -> loginRoot)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            return login.getId();
                        })
                        .addEntityFunction(Function.identity())
                        .build()
                , FieldBuilder.asName(Login.class, "avatar")
                        .addSelect(loginRoot -> null)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            if (login.getWechatUser() == null)
                                return null;
                            return login.getWechatUser().getHeadImageUrl();
                        })
                        .addEntityFunction(Function.identity())
                        .build()
                , FieldBuilder.asName(Login.class, "username")
                        .addSelect(loginRoot -> null)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            return login.getUsername();
                        })
                        .addEntityFunction(Function.identity())
                        .build()
                , FieldBuilder.asName(Login.class, "nickname")
                        .addSelect(loginRoot -> null)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            if (login.getWechatUser() == null)
                                return null;
                            return login.getWechatUser().getNickname();
                        })
                        .addEntityFunction(Function.identity())
                        .build()
                , FieldBuilder.asName(Login.class, "enabled")
                        .addSelect(loginRoot -> null)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            return login.isEnabled();
                        })
                        .addEntityFunction(Function.identity())
                        .build()
                , FieldBuilder.asName(Login.class, "authorities")
                        .addSelect(loginRoot -> null)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            if (login.getLevelSet() == null)
                                return null;
                            return Login.getGrantedAuthorities(login.getLevelSet()).stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.toSet());
                        })
                        .addEntityFunction(Function.identity())
                        .build()
                , FieldBuilder.asName(Login.class, "level")
                        .addSelect(loginRoot -> null)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            if (login.getLevelSet() == null)
                                return null;
                            return login.getLevelSet().stream().map(ManageLevel::title).collect(Collectors.joining(","));
                        })
                        .addEntityFunction(Function.identity())
                        .build()
                , FieldBuilder.asName(Login.class, "merchantId")
                        .addSelect(loginRoot -> null)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            if (login.getMerchant() == null)
                                return null;
                            return login.getMerchant().getId();
                        })
                        .addEntityFunction(Function.identity())
                        .build()
                , FieldBuilder.asName(Login.class, "storeId")
                        .addSelect(loginRoot -> null)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            if (login.getStore() == null)
                                return null;
                            return login.getStore().getId();
                        })
                        .addEntityFunction(Function.identity())
                        .build()
                , FieldBuilder.asName(Login.class, "createTime")
                        .addSelect(loginRoot -> null)
                        .addFormat((data, type) -> {
                            Login login = (Login) data;
                            return conversionService.convert(login.getCreateTime(), String.class);
                        })
                        .addEntityFunction(Function.identity())
                        .build()
        );
    }
}
