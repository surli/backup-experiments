package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.CoreServiceTest;
import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.repository.MerchantRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author lxf
 */
public class ItemServiceTest extends CoreServiceTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    public void go(){
        //添加一个项目
        Item item = itemService.addItem(null,null, "测试添加项目", "测试", new BigDecimal(0.01),
                new BigDecimal(0.01), new BigDecimal(0.01), "测试添加一个项目", "这个项目用来测试", false);
        Item byId = itemService.findOne(item.getId());
        assertThat(byId).isNotNull();
    }
}
