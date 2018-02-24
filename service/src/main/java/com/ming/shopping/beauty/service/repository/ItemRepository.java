package com.ming.shopping.beauty.service.repository;

import com.ming.shopping.beauty.service.entity.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author lxf
 */
public interface ItemRepository extends JpaRepository<Item,Long>,JpaSpecificationExecutor<Item>{
}
