package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.item.StoreItem_;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.model.definition.ClientStoreItemModel;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import me.jiangcai.crud.row.supplier.SingleRowDramatizer;
import me.jiangcai.lib.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lxf
 */
@Controller
public class ClientItemController {

    @Autowired
    private ResourceService resourceService;

    /**
     * 微信端门店项目列表
     *
     * @param storeId
     * @param itemType
     * @param lat
     * @param lon
     * @return
     */
    @GetMapping("/items")
    @RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
    public RowDefinition<StoreItem> itemList(Long storeId, @RequestParam(required = false) String itemType
            , Integer lat, Integer lon) {
        return new RowDefinition<StoreItem>() {
            @Override
            public CriteriaQuery<StoreItem> dataGroup(CriteriaBuilder cb, CriteriaQuery<StoreItem> query, Root<StoreItem> root) {
                if (storeId != null) {
                    return query;
                }
                return query.groupBy(root.get(StoreItem_.item));
            }

            @Override
            public Class<StoreItem> entityClass() {
                return StoreItem.class;
            }

            @Override
            public List<FieldDefinition<StoreItem>> fields() {
                return listField(storeId != null);
            }

            @Override
            public Specification<StoreItem> specification() {
                return (root, query, cb) -> {
                    List<Predicate> conditions = new ArrayList<>();
                    conditions.add(StoreItem.saleable(root, cb));
                    if (storeId != null) {
                        conditions.add(cb.equal(root.join(StoreItem_.store).get(Store_.id), storeId));
                    }
//                    if (StringUtils.isNotBlank(itemType)) {
//                        conditions.add(cb.like(root.join(StoreItem_.item).get(Item_.itemType), "%" + itemType + "%"));
//                    }
                    return cb.and(conditions.toArray(new Predicate[conditions.size()]));
                };
            }
        };
    }

    /**
     * 微信端门店项目详情
     *
     * @param itemId
     * @return
     */
    @GetMapping("/items/{itemId}")
    @RowCustom(distinct = true, dramatizer = SingleRowDramatizer.class)
    public RowDefinition<StoreItem> itemDetail(@PathVariable("itemId") long itemId) {
        return new RowDefinition<StoreItem>() {
            @Override
            public CriteriaQuery<StoreItem> dataGroup(CriteriaBuilder cb, CriteriaQuery<StoreItem> query, Root<StoreItem> root) {
                return query.groupBy(root.get(StoreItem_.item));
            }

            @Override
            public Class<StoreItem> entityClass() {
                return StoreItem.class;
            }

            @Override
            public List<FieldDefinition<StoreItem>> fields() {
                List<FieldDefinition<StoreItem>> fieldDefinitions = new ArrayList<>();
                fieldDefinitions.addAll(listField(false));
                fieldDefinitions.add(
                        FieldBuilder.asName(StoreItem.class, "details")
                                .addSelect(root -> root.join(StoreItem_.item).get(Item_.richDescription))
                                .build()
                );
                return fieldDefinitions;
            }

            @Override
            public Specification<StoreItem> specification() {
                return (root, cq, cb) ->
                        cb.equal(root.get(StoreItem_.item).get(Item_.id), itemId);
            }
        };
    }


    private List<FieldDefinition<StoreItem>> listField(boolean singleStore) {
        return new ClientStoreItemModel(resourceService, singleStore).getDefinitions();
    }
}
