package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.item.StoreItem_;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.ItemSearcherBody;
import com.ming.shopping.beauty.service.repository.StoreItemRepository;
import com.ming.shopping.beauty.service.service.ItemService;
import com.ming.shopping.beauty.service.service.StoreItemService;
import com.ming.shopping.beauty.service.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lxf
 */
@Service
public class StoreItemServiceImpl implements StoreItemService {

    @Autowired
    private StoreItemRepository storeItemRepository;
    @Autowired
    private StoreService storeService;
    @Autowired
    private ItemService itemService;

    @Override
    public List<StoreItem> findAllStoreItem(ItemSearcherBody searcher) {
        Specification<StoreItem> specification = (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            if (searcher.getStoreId() != null && searcher.getStoreId() > 0) {
                conditionList.add(cb.equal(root.join(StoreItem_.store, JoinType.LEFT)
                        .get(Store_.id), searcher.getStoreId()));
            }
            if (searcher.getEnabled() != null) {
                conditionList.add(cb.equal(root.get(StoreItem_.enable), searcher.getEnabled()));
            }
            if (searcher.getRecommended() != null) {
                conditionList.add(cb.equal(root.get(StoreItem_.recommended), searcher.getRecommended()));
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
        return storeItemRepository.findAll(specification);
    }


    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public StoreItem addStoreItem(long storeId, long itemId, BigDecimal salesPrice, boolean recommended) {
        StoreItem storeItem = new StoreItem();
        storeItem.setSalesPrice(salesPrice);
        storeItem.setRecommended(recommended);
        return addStoreItem(storeId, itemId, storeItem);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public StoreItem addStoreItem(long storeId, long itemId, StoreItem storeItem) {
        Store store = storeService.findStore(storeId);
        Item item = itemService.findOne(itemId);
        //是否通过审核
        StoreItem newItem = new StoreItem();
        newItem.fromRequest(storeItem);

        if (item.getAuditStatus().equals(AuditStatus.AUDIT_PASS)) {
            newItem.setStore(store);
            newItem.setItem(item);
            if (newItem.getSalesPrice() != null) {
                //这个价格必须大于等于 项目的销售价
                if (newItem.getSalesPrice().compareTo(item.getSalesPrice()) < 0) {
                    throw new ApiResultException(ApiResult.withError(ResultCodeEnum.STORE_ITEM_PRICE_ERROR));
                }
            } else {
                newItem.setSalesPrice(item.getSalesPrice());
            }
            return storeItemRepository.save(newItem);
        } else {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.ITEM_NOT_AUDIT));
        }
    }


    @Override
    public StoreItem findStoreItem(long itemId, Store store) {
        List<StoreItem> storeItems = storeItemRepository.findAll((root, query, cb) ->
                cb.and(
                        cb.equal(root.get(StoreItem_.item).get(Item_.id), itemId)
                        , cb.equal(root.get(StoreItem_.store), store)
                        , cb.isFalse(root.get(StoreItem_.deleted))
                ), new PageRequest(0, 1)).getContent();
        if (storeItems.isEmpty()) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.ITEM_NOT_EXIST));
        }
        return storeItems.get(0);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void freezeOrEnable(boolean enabled, Long storeItemId) {
        StoreItem storeItem = storeItemRepository.findOne(storeItemId);
        storeItem.setEnable(enabled);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void recommended(boolean recommended, Long storeItemId) {
        StoreItem storeItem = storeItemRepository.findOne(storeItemId);
        storeItem.setRecommended(recommended);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateStoreItem(long id, BigDecimal salesPrice) {
        StoreItem storeItem = storeItemRepository.findOne(id);
        Item item = storeItem.getItem();
        if (salesPrice == null) {
            //将价格修改成和item中一样
            storeItem.setSalesPrice(item.getSalesPrice());
            storeItemRepository.save(storeItem);
        } else {
            if (salesPrice.compareTo(item.getSalesPrice()) == -1) {
                throw new ApiResultException(ApiResult.withError(ResultCodeEnum.STORE_ITEM_PRICE_ERROR));
            } else {
                storeItem.setSalesPrice(salesPrice);
                storeItemRepository.save(storeItem);
            }
        }
    }

    @Override
    public List<StoreItem> findByItem(Item item) {
        return storeItemRepository.findByItem(item);
    }
}
