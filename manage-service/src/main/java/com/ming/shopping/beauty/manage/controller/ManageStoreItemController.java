package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.modal.StoreItemCreation;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.item.StoreItem_;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.StoreItemService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
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
@RequestMapping("/storeItem")
@PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_READ + "','" + Login.ROLE_PLATFORM_READ + "','" + Login.ROLE_STORE_ROOT + "')")
public class ManageStoreItemController extends AbstractCrudController<StoreItem, Long, StoreItemCreation> {

    @Autowired
    private StoreItemService storeItemService;

    @Override
    @ResponseStatus(HttpStatus.OK)
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition<StoreItem> list(HttpServletRequest request) {
        return super.list(request);
    }

    /**
     * 添加门店项目
     *
     * @param postData 门店项目
     * @param request  其他信息
     * @return
     * @throws URISyntaxException
     */
    @PostMapping
    @Override
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "','" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity addOne(@RequestBody StoreItemCreation postData, WebRequest request) throws URISyntaxException {
        final String storeId = "storeId";
        final String itemId = "itemId";
        if (postData.getStoreId() == null || postData.getItemId() == null) {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), storeId + itemId), null));
        }
        StoreItem storeItem = storeItemService.addStoreItem(postData.getStoreId()
                , postData.getItemId(), postData);
        return ResponseEntity
                .created(new URI("/storeItem/" + storeItem.getId()))
                .build();
    }

    /**
     * 修改门店项目的 销售价/会员价
     *
     * @param salesPrice 新价格
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_STORE + "','" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStoreItem(@PathVariable(value = "id", required = true) long id, @RequestBody BigDecimal salesPrice) {
        storeItemService.updateStoreItem(id, salesPrice);
    }

    @Override
    protected List<FieldDefinition<StoreItem>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(StoreItem.class, "id")
                        .build()
                , FieldBuilder.asName(StoreItem.class, "name")
                        .addSelect(storeItemRoot -> storeItemRoot.join(StoreItem_.item).get(Item_.name))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "storeName")
                        .addSelect(storeItemRoot -> storeItemRoot.join(StoreItem_.store).get(Store_.name))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "price")
                        .addSelect(storeItemRoot -> storeItemRoot.join(StoreItem_.item).get(Item_.price))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "salesPrice")
                        .build()
                , FieldBuilder.asName(StoreItem.class, "enabled")
                        .addSelect(storeItemRoot -> storeItemRoot.get(StoreItem_.enable))
                        .build()
                , FieldBuilder.asName(StoreItem.class, "recommended")
                        .build()
        );
    }

    @Override
    protected Specification<StoreItem> listSpecification(Map<String, Object> queryData) {
        return ((root, query, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if (queryData.get("storeId") != null)
                conditionList.add(cb.equal(root.join(StoreItem_.store).get(Store_.id), Long.parseLong(queryData.get("storeId").toString())));
            if (queryData.get("itemName") != null)
                if(StringUtils.isNotBlank(queryData.get("itemName").toString())){
                    conditionList.add(cb.like(root.join(StoreItem_.item).get(Item_.name), "%" + queryData.get("itemName") + "%"));
                }
            if (queryData.get("storeName") != null)
                if(StringUtils.isNotBlank(queryData.get("storeName").toString())){
                    conditionList.add(cb.like(root.join(StoreItem_.store).get(Store_.name), "%" + queryData.get("storeName") + "%"));
                }
            if (queryData.get("enabled") != null) {
                if (BooleanUtils.toBoolean(queryData.get("enabled").toString()))
                    conditionList.add(cb.isTrue(root.get(StoreItem_.enable)));
                else
                    conditionList.add(cb.isFalse(root.get(StoreItem_.enable)));
            }
            if (queryData.get("recommended") != null) {
                if (BooleanUtils.toBoolean(queryData.get("recommended").toString()))
                    conditionList.add(cb.isTrue(root.get(StoreItem_.recommended)));
                else
                    conditionList.add(cb.isFalse(root.get(StoreItem_.recommended)));
            }
            conditionList.add(cb.isFalse(root.get(StoreItem_.deleted)));
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        });
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<StoreItem> root) {
        return Arrays.asList(criteriaBuilder.desc(root.get(StoreItem_.id)));
    }
}
