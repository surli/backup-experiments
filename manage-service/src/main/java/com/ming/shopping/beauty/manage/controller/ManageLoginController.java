package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.definition.UserModel;
import com.ming.shopping.beauty.service.repository.MainOrderRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.RowService;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * @author helloztt
 */
@Controller
@RequestMapping("/login")
@PreAuthorize("hasAnyRole('ROOT')")
@RowCustom(dramatizer = AntDesignPaginationDramatizer.class, distinct = true)
public class ManageLoginController extends AbstractCrudController<Login, Long, Login> {

    @Autowired
    private LoginService loginService;
    @Autowired
    private MainOrderRepository mainOrderRepository;
    @Autowired
    private ConversionService conversionService;

    /**
     * 用户详情
     *
     * @param aLong
     * @return
     */
    @Override
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @GetMapping("/{id}")
    @ResponseBody
    public Object getOne(@PathVariable("id") Long aLong) {
        Login login = loginService.findOne(aLong);
        BigDecimal consumption = mainOrderRepository.sumFinalAmountLByPayer(login.getId());
        if (consumption != null) {
            login.setConsumption(consumption);
        } else {
            login.setConsumption(BigDecimal.ZERO);
        }
        return RowService.drawEntityToRow(login, new UserModel(loginService, conversionService).getDefinitions(), null);
    }

    /**
     * 冻结/启用 用户
     *
     * @param loginId 被设置的用户
     * @param putData
     */
    @PutMapping("/{id}/enabled")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setEnable(@PathVariable(value = "id", required = true) long loginId, @RequestBody Boolean putData) {
        if (putData != null) {
            loginService.freezeOrEnable(loginId, putData);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), putData), null));
        }
    }

    /**
     * 设置一个用户是否可以推荐他人
     *
     * @param loginId 被设置的用户
     * @param putData
     */
    @PutMapping("/{id}/guidable")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setGuidable(@PathVariable(value = "id", required = true) long loginId, @RequestBody Boolean putData) {
        if (putData != null) {
            loginService.setGuidable(loginId, putData);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), putData), null));
        }
    }

    /**
     * 根据用户id查询余额
     *
     * @param loginId 用户id
     * @return 余额
     */
    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "'" +
            ",'" + Login.ROLE_PLATFORM_SETTLEMENT + "')")
    @ResponseBody
    public BigDecimal findBalance(@PathVariable("id") Long loginId) {
        return loginService.findBalance(loginId);
    }

    @Override
    protected List<FieldDefinition<Login>> listFields() {
        return new UserModel(loginService, conversionService).getDefinitions();
    }

    @Override
    protected CriteriaQuery<Login> listGroup(CriteriaBuilder cb, CriteriaQuery<Login> query, Root<Login> root) {
        return query.groupBy(root);
    }

    @Override
    protected Specification<Login> listSpecification(Map<String, Object> queryData) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            conditions.add(cb.equal(root.get(Login_.delete), false));
            if (queryData.get("loginId") != null) {
                conditions.add(cb.equal(root.get(Login_.id), queryData.get("loginId")));
            }
            if (queryData.get("enabled") != null) {
                if (BooleanUtils.toBoolean(queryData.get("enabled").toString())) {
                    conditions.add(cb.isTrue(root.get(Login_.enabled)));
                } else {
                    conditions.add(cb.isFalse(root.get(Login_.enabled)));
                }
            }
            if (queryData.get("mobile") != null) {
                if(StringUtils.isNotBlank(queryData.get("mobile").toString())){
                    conditions.add(cb.like(root.get(Login_.loginName), "%" + queryData.get("mobile") + "%"));
                }
            }
            return cb.and(conditions.toArray(new Predicate[conditions.size()]));
        };
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<Login> root) {
        return Arrays.asList(
                criteriaBuilder.desc(root.get(Login_.id))
        );
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Login> getDetail(Long aLong) {
        return null;
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {

    }
}
