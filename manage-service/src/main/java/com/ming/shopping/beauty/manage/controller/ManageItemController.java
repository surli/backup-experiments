package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.modal.ItemCreation;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.ItemService;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.utils.Utils;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.lib.resource.service.ResourceService;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
@RequestMapping("/item")
@Controller
@RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
@PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_READ + "','" + Login.ROLE_PLATFORM_READ + "')")
public class ManageItemController extends AbstractCrudController<Item, Long, ItemCreation> {

    @Autowired
    private ItemService itemService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ResourceService resourceService;

    @Override
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_READ + "','" + Login.ROLE_PLATFORM_READ + "')")
    public RowDefinition<Item> list(HttpServletRequest request) {
        return super.list(request);
    }

    /**
     * 添加项目，只有root或者具备商户项目权限的人
     *
     * @param item    项目
     * @param request 其他信息
     * @return 商户项目列表
     */
    @PostMapping
    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ITEM + "')")
    public ResponseEntity addOne(@RequestBody ItemCreation item, WebRequest request) throws URISyntaxException {
        final String param = "merchantId";

        if (item.getMerchantId() == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        if (StringUtils.isEmpty(item.getName())) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "请求数据"), null));
        }
        if (StringUtils.isEmpty(item.getImagePath())) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), "请求数据"), null));
        }
        Merchant merchant = merchantService.findOne(item.getMerchantId());
        Item responseItem = itemService.addItem(merchant, item, item.getImagePath());
        return ResponseEntity
                .created(new URI("/item/" + responseItem.getId()))
                .build();
    }

    /**
     * 编辑项目
     *
     * @param item 要编辑的项目信息
     * @throws URISyntaxException
     */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ITEM + "')")
    public void updateItem(@RequestBody ItemCreation item, WebRequest request) throws URISyntaxException {
        // TODO 并非什么都可以改
        addOne(item, request);
    }

    /**
     * 项目详情
     *
     * @param id 获取详情的id
     * @return
     */
    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_READ + "','" + Login.ROLE_PLATFORM_READ + "')")
    @Override
    public RowDefinition<Item> getOne(@PathVariable(value = "itemId", required = true) Long id) {
        return new RowDefinition<Item>() {

            @Override
            public Class<Item> entityClass() {
                return Item.class;
            }

            @Override
            public List<FieldDefinition<Item>> fields() {
                return listFields();
            }

            @Override
            public Specification<Item> specification() {
                return (root, query, cb) -> cb.equal(root.get(Item_.id), id);
            }
        };
    }

    /**
     * 项目状态改变/审核
     *
     * @param itemId      项目id
     * @param auditStatus 审核的状态以及备注
     */
    @PutMapping("/{itemId}/auditStatus")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_PLATFORM_AUDIT_ITEM + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setAuditStatus(@PathVariable("itemId") long itemId, @RequestBody Map<String, String> auditStatus) {
        if (auditStatus.get("status") != null && auditStatus.get("comment") != null) {
            itemService.auditItem(itemId, AuditStatus.valueOf(auditStatus.get("status")), auditStatus.get("comment"));
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), auditStatus), null));
        }
    }

    /**
     * 提交项目审核
     *
     * @param itemId      项目id
     * @param auditStatus 审核的状态以及备注
     */
    @PutMapping("/{itemId}/commit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ITEM + "')")
    public void commitItem(@PathVariable("itemId") long itemId, @RequestBody Map<String, String> auditStatus) {
        if (auditStatus.get("status") != null) {
            itemService.auditItem(itemId, AuditStatus.valueOf(auditStatus.get("status")),
                    auditStatus.get("comment"));
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), auditStatus), null));
        }
    }


    @Override
    protected List<FieldDefinition<Item>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(Item.class, "id")
                        .build()
                , FieldBuilder.asName(Item.class, "name")
                        .build()
                , FieldBuilder.asName(Item.class, "itemType")
                        .build()
                , FieldBuilder.asName(Item.class, "thumbnailUrl")
                        .addSelect(itemRoot -> itemRoot.get(Item_.mainImagePath))
                        .addFormat(Utils.formatResourcePathToURL(resourceService))
                        .build()
                , FieldBuilder.asName(Item.class, "merchantName")
                        .addSelect(itemRoot -> itemRoot.join(Item_.merchant).get(Merchant_.name))
                        .build()
                , FieldBuilder.asName(Item.class, "price")
                        .build()
                , FieldBuilder.asName(Item.class, "salesPrice")
                        .build()
                , FieldBuilder.asName(Item.class, "auditStatus")
                        .addFormat((data, type) -> {
                            return data == null ? null : ((AuditStatus) data).getMessage();
                        })
                        .build()
                , FieldBuilder.asName(Item.class, "enabled")
                        .build()
                , FieldBuilder.asName(Item.class, "recommended")
                        .build()
        );
    }

    @Override
    protected Specification<Item> listSpecification(Map<String, Object> queryData) {
        return ((root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            if (queryData.get("itemName") != null) {
                if(StringUtils.isNotBlank(queryData.get("itemName").toString())) {
                    conditions.add(cb.like(root.get(Item_.name), "%" + queryData.get("itemName") + "%"));
                }
            }
            if (queryData.get("itemType") != null) {
                if(StringUtils.isNotBlank(queryData.get("itemType").toString())) {
                    conditions.add(cb.like(root.get(Item_.itemType), "%" + queryData.get("itemType") + "%"));
                }
            }
            if (queryData.get("merchantName") != null) {
                if(StringUtils.isNotBlank(queryData.get("merchantName").toString())){
                    conditions.add(cb.equal(root.join(Item_.merchant, JoinType.LEFT)
                            .get(Merchant_.name), queryData.get("merchantName")));
                }
            }
            if (queryData.get("merchantId") != null) {
                conditions.add(cb.equal(root.join(Item_.merchant).get(Merchant_.id),
                        NumberUtils.parseNumber(queryData.get("merchantId").toString(), Long.class)));
            }
            if (queryData.get("auditStatus") != null) {
                conditions.add(cb.equal(root.get(Item_.auditStatus)
                        , AuditStatus.valueOf(queryData.get("auditStatus").toString())));
            }
            if (queryData.get("enabled") != null) {
                if (BooleanUtils.toBoolean(queryData.get("enabled").toString()))
                    conditions.add(cb.isTrue(root.get(Item_.enabled)));
                else
                    conditions.add(cb.isFalse(root.get(Item_.enabled)));
            }
            if (queryData.get("recommended") != null) {
                if (BooleanUtils.toBoolean(queryData.get("recommended").toString()))
                    conditions.add(cb.isTrue(root.get(Item_.recommended)));
                else
                    conditions.add(cb.isFalse(root.get(Item_.recommended)));
            }

            conditions.add(cb.isFalse(root.get(Item_.deleted)));
            return cb.and(conditions.toArray(new Predicate[conditions.size()]));
        });
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<Item> root) {
        return Arrays.asList(
                criteriaBuilder.desc(root.get(Item_.id))
        );
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Item> getDetail(Long aLong) {
        return null;
    }
}
