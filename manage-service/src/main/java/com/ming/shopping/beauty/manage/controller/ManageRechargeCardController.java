package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import com.ming.shopping.beauty.service.entity.item.RechargeCard_;
import com.ming.shopping.beauty.service.entity.login.User;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.field.Fields;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author CJ
 */
@Controller
@PreAuthorize("hasAnyRole('ROOT')")
@RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
@RequestMapping("/rechargeCard")
public class ManageRechargeCardController extends AbstractCrudController<RechargeCard, Long, RechargeCard> {

    @Autowired
    private ConversionService conversionService;

    @Override
    @PreAuthorize("denyAll()")
    public Object getOne(Long aLong) {
        return super.getOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<RechargeCard> getDetail(Long aLong) {
        return super.getDetail(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public ResponseEntity addOne(RechargeCard postData, WebRequest request) throws URISyntaxException {
        return super.addOne(postData, request);
    }

    @Override
    protected List<FieldDefinition<RechargeCard>> listFields() {
        return Arrays.asList(
                Fields.asBasic("id")
                , FieldBuilder.asName(RechargeCard.class, "code")
                        .addFormat((data, type) -> {
                            // 显示头4和尾4
                            final String str = data.toString();
                            if (str.length() <= 8)
                                return data;
                            StringBuilder rs = new StringBuilder();
                            rs.append(str.substring(0, 4));
                            int x = str.length() - 8;
                            while (x-- > 0)
                                rs.append('*');
                            rs.append(str.substring(str.length() - 4));
                            return rs.toString();
                        })
                        .build()
                , Fields.asBasic("used")
                , Fields.asBasic("amount")
                , FieldBuilder.asName(RechargeCard.class, "user")
                        .addSelect(rechargeCardRoot -> rechargeCardRoot.join(RechargeCard_.user, JoinType.LEFT))
                        .addFormat((data, type) -> {
                            if (data == null)
                                return null;
                            User user = (User) data;
                            return user.getLogin().getHumanReadName();
                        })
                        .build()
                , FieldBuilder.asName(RechargeCard.class, "usedTime")
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
        );
    }

    @Override
    protected Specification<RechargeCard> listSpecification(Map<String, Object> queryData) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            final Object requestCode = queryData.get("code");
            if (requestCode != null) {
                predicate = cb.equal(root.get(RechargeCard_.code), requestCode);
            } else {
                final Object requestUsed = queryData.get("used");
                if (requestUsed != null) {
                    boolean target = BooleanUtils.toBoolean(requestUsed.toString());
                    predicate = cb.and(
                            predicate
                            , cb.equal(root.get(RechargeCard_.used), target)
                    );
                }
                final Object requestUser = queryData.get("user");
                if (requestUser != null) {
                    predicate = cb.and(
                            predicate
                            , User.nameMatch(root.join(RechargeCard_.user, JoinType.LEFT), cb, requestUser.toString())
                    );
                }
            }
            return predicate;
        };
    }
}
