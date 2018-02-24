package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author helloztt
 */
public interface StoreItemRepository extends JpaRepository<StoreItem, Long>, JpaSpecificationExecutor<StoreItem> {

    List<StoreItem> findByItem(Item item);
}
