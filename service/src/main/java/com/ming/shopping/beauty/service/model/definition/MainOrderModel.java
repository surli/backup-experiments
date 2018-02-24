package com.ming.shopping.beauty.service.model.definition;

import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.service.MainOrderService;
import me.jiangcai.crud.row.FieldDefinition;

import java.util.List;

/**
 * 主订单数据Model,对应API中的OrderModel
 *
 * @author CJ
 */
public class MainOrderModel implements DefinitionModel<MainOrder> {

    private final MainOrderService mainOrderService;

    public MainOrderModel(MainOrderService mainOrderService) {
        this.mainOrderService = mainOrderService;
    }

    @Override
    public List<FieldDefinition<MainOrder>> getDefinitions() {
        return mainOrderService.orderListField();
    }
}
