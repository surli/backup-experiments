package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.modal.StoreCreation;
import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.RepresentService;
import com.ming.shopping.beauty.service.service.StoreService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.crud.row.supplier.SingleRowDramatizer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author lxf
 */
@Controller
@RequestMapping("/store")
public class ManageStoreController extends AbstractCrudController<Store, Long, StoreCreation> {

    @Autowired
    private StoreService storeService;
    @Autowired
    private RepresentService representService;
    @Autowired
    private ConversionService conversionService;

    /**
     * 门店列表
     *
     * @param request
     * @return
     */
    @Override
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_PLATFORM_READ + "','" + Login.ROLE_MERCHANT_READ + "')")
    public RowDefinition<Store> list(HttpServletRequest request) {
        return super.list(request);
    }

    /**
     * 新增门店
     *
     * @param postData 门店信息
     * @param request  其他信息
     * @return
     * @throws URISyntaxException
     */
    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity addOne(@RequestBody StoreCreation postData, WebRequest request) throws URISyntaxException {
        final String loginId = "loginId";
        final String merchantId = "merchantId";
        if (postData.getLoginId() == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), loginId), null));
        }
        if (postData.getMerchantId() == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), merchantId), null));
        }
        Store store = storeService.addStore(postData.getLoginId()
                , postData.getMerchantId(),
                postData.getName(), postData.getTelephone(), postData.getContact(), postData.getAddress());
        return ResponseEntity.created(new URI("/store/" + store.getId()))
                .build();
    }

    @Override
    @GetMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_PLATFORM_READ + "','" + Login.ROLE_MERCHANT_READ + "')")
    @RowCustom(distinct = true, dramatizer = SingleRowDramatizer.class)
    public RowDefinition<Store> getOne(@PathVariable("storeId") Long storeId) {
        return new RowDefinition<Store>() {
            @Override
            public Class<Store> entityClass() {
                return Store.class;
            }

            @Override
            public List<FieldDefinition<Store>> fields() {
                return Arrays.asList(
                        FieldBuilder.asName(Store.class, "id")
                                .build()
                        , FieldBuilder.asName(Store.class, "name")
                                .build()
                        , FieldBuilder.asName(Store.class, "telephone")
                                .build()
                        , FieldBuilder.asName(Store.class, "contact")
                                .build()
                        , FieldBuilder.asName(Store.class, "represents")
                                .build()
                );
            }

            @Override
            public Specification<Store> specification() {
                return (root, query, cb) ->
                        cb.equal(root.get(Store_.id), storeId);
            }
        };
    }

    /**
     * 启用/禁用 门店
     *
     * @param loginId
     * @param enable
     */
    @PutMapping("/{storeId}/enabled")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setEnable(@PathVariable("storeId") long loginId, @RequestBody Boolean enable) {
        if (enable != null) {
            storeService.freezeOrEnable(loginId, enable);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), enable), null));
        }
    }

    /**
     * 门店代表列表
     *
     * @param storeId 门店id
     * @return
     */
    @GetMapping("/{storeId}/represent")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "')")
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition<Represent> listForRepresent(@PathVariable long storeId) {
        return new RowDefinition<Represent>() {
            @Override
            public Class<Represent> entityClass() {
                return Represent.class;
            }

            @Override
            public List<Order> defaultOrder(CriteriaBuilder criteriaBuilder, Root<Represent> root) {
                return Arrays.asList(
                        criteriaBuilder.asc(root.get("enable"))
                        , criteriaBuilder.desc(root.get("createTime"))
                );
            }

            @Override
            public List<FieldDefinition<Represent>> fields() {
                return listFieldsForRepresent();
            }

            @Override
            public Specification<Represent> specification() {
                return listSpecificationForRepresent(storeId);
            }
        };
    }

    /**
     * 添加门店代表
     *
     * @param storeId     门店
     * @param representId 用户id
     */
    @PostMapping("/{storeId}/represent/{representId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity addRepresent(@PathVariable(required = true) long storeId, @PathVariable(required = true) long representId) throws URISyntaxException {
        representService.addRepresent(representId, storeId);
        return ResponseEntity
                .created(new URI("/store/" + storeId + "/represent/" + representId))
                .build();
    }

    /**
     * 启用禁用门店代表
     *
     * @param representId 门店代表id
     * @param enable
     */
    @PutMapping("/{storeId}/represent/{representId}/enabled")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enableRepresent(@PathVariable(required = true) long representId, @RequestBody Boolean enable) {
        if (enable != null) {
            representService.freezeOrEnable(representId, enable);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), enable), null));
        }
    }

    /**
     * 移除角色和门店代表的关联
     *
     * @param storeId
     * @param representId
     */
    @DeleteMapping("/{storeId}/represent/{representId}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRepresent(@PathVariable("storeId") long storeId, @PathVariable("representId") long representId) {
        representService.removerRepresent(storeId, representId);
    }

    //门店

    @Override
    protected List<FieldDefinition<Store>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(Store.class, "id")
                        .addSelect(storeRoot -> storeRoot.get(Store_.id))
                        .build()
                , FieldBuilder.asName(Store.class, "username")
                        .addSelect(storeRoot -> storeRoot.join(Store_.login, JoinType.LEFT).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Store.class, "name")
                        .build()
                , FieldBuilder.asName(Store.class, "telephone")
                        .build()
                , FieldBuilder.asName(Store.class, "contact")
                        .build()
//                , FieldBuilder.asName(Store.class, "address")
//                        //TODO 地址问题
//                        .addFormat((data, type) -> data.toString())
//                        .build()
                , FieldBuilder.asName(Store.class, "enabled")
                        .build()
                , FieldBuilder.asName(Store.class, "createTime")
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()

        );
    }

    @Override
    protected Specification<Store> listSpecification(Map<String, Object> queryData) {
        return (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if (queryData.get("merchantId") != null) {
                conditionList.add(cb.equal(root.join(Store_.merchant, JoinType.LEFT).get(Merchant_.id), Long.valueOf(queryData.get("merchantId").toString())));
            }
            if (queryData.get("username") != null) {
                if (StringUtils.isNotBlank(queryData.get("username").toString())) {
                    conditionList.add(cb.like(root.join(Store_.login).get(Login_.loginName), "%" + queryData.get("username") + "%"));
                }
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<Store> root) {
        return Arrays.asList(
                criteriaBuilder.desc(root.get(Store_.createTime))
        );
    }

    //门店代表

    private List<FieldDefinition<Represent>> listFieldsForRepresent() {
        return Arrays.asList(
                FieldBuilder.asName(Represent.class, "enabled")
                        .addSelect(representRoot -> representRoot.get(Represent_.enable))
                        .build()
                , FieldBuilder.asName(Represent.class, "id").build()
                , FieldBuilder.asName(Represent.class, "username")
                        .addSelect(representRoot -> representRoot.join(Represent_.login, JoinType.LEFT).join(Login_.wechatUser).get("nickname"))
                        .build()
                , FieldBuilder.asName(Represent.class, "mobile")
                        .addSelect(representRoot -> representRoot.join(Represent_.login, JoinType.LEFT).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Represent.class, "createTime")
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                //TODO 应该还有一个业绩相关的.
        );
    }

    private Specification<Represent> listSpecificationForRepresent(long storeId) {
        return (root, cq, cb) -> cb.equal(root.join(Represent_.store).get(Store_.id), storeId);
    }


    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Store> getDetail(Long aLong) {
        return null;
    }


}
