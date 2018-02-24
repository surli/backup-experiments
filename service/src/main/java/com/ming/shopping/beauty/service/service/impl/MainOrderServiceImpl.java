package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.aop.BusinessSafe;
import com.ming.shopping.beauty.service.entity.item.Item_;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.login.User_;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.entity.order.MainOrder_;
import com.ming.shopping.beauty.service.entity.order.OrderItem;
import com.ming.shopping.beauty.service.entity.order.OrderItem_;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.request.ItemNum;
import com.ming.shopping.beauty.service.model.request.OrderSearcherBody;
import com.ming.shopping.beauty.service.repository.MainOrderRepository;
import com.ming.shopping.beauty.service.repository.OrderItemRepository;
import com.ming.shopping.beauty.service.service.MainOrderService;
import com.ming.shopping.beauty.service.service.StoreItemService;
import com.ming.shopping.beauty.service.utils.Utils;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.IndefiniteFieldDefinition;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.RowService;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.lib.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lxf
 */
@Service
public class MainOrderServiceImpl implements MainOrderService {

    @Autowired
    private MainOrderRepository mainOrderRepository;
    @Autowired
    private RowService rowService;
    @Autowired
    private ConversionService conversionService;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private StoreItemService storeItemService;


    @Override
    public MainOrder findById(long id) {
        MainOrder one = mainOrderRepository.findOne(id);
        if (one == null) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MAINORDER_NOT_EXIST));
        }
        return one;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public MainOrder newEmptyOrder(User user) {
        //先看看这个用户有没有空的订单
        MainOrder mainOrder = mainOrderRepository.findEmptyOrderByPayer(user.getId());
        if (mainOrder != null) {
            mainOrder.setCreateTime(LocalDateTime.now());
            return mainOrder;
        }
        mainOrder = new MainOrder();
        mainOrder.setPayer(user);
        mainOrder.setCreateTime(LocalDateTime.now());
        //空的订单
        mainOrder.setOrderStatus(OrderStatus.EMPTY);
        return mainOrderRepository.saveAndFlush(mainOrder);
    }

    @Override
    @BusinessSafe
    @Transactional(rollbackFor = RuntimeException.class)
    public MainOrder supplementOrder(long orderId, Login login, Store store, ItemNum[] items) {
        Map<StoreItem, Integer> amounts = new HashMap<>(items.length);
        for (ItemNum storeItemNum : items) {
            StoreItem item = storeItemService.findStoreItem(storeItemNum.getStoreItemId(), login.getStore());
            if (!amounts.containsKey(item)) {
                amounts.put(item, storeItemNum.getNum());
            }
        }
        return supplementOrder(orderId, login, store, amounts);
    }

    @Override
    @BusinessSafe
    @Transactional(rollbackFor = RuntimeException.class)
    public MainOrder supplementOrder(long orderId, Login login, Store store, Map<StoreItem, Integer> amounts) {
        //门店代表扫码后，把List<OrderItem>塞到了这个订单里，并修改MainOrder
        MainOrder mainOrder = mainOrderRepository.getOne(orderId);
        //判断订单是不是空的
        if (!OrderStatus.EMPTY.equals(mainOrder.getOrderStatus())) {
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.ORDER_NOT_EMPTY));
        }
        mainOrder.setLogin(login);
        mainOrder.setStore(store);

        BigDecimal costPrice = BigDecimal.ZERO;
        BigDecimal finalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItemList = new ArrayList<>();
        for (StoreItem storeItem : amounts.keySet()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setMainOrder(mainOrder);
            orderItem.setItem(storeItem.getItem());
            //项目的数据随时会改变，所以需要保存在 OrderItem 里面
            orderItem.setName(orderItem.getItem().getName());
            orderItem.setPrice(orderItem.getItem().getPrice());
            //销售价从门店项目中获取
            orderItem.setSalesPrice(storeItem.getSalesPrice());
            orderItem.setCostPrice(orderItem.getItem().getCostPrice());
            orderItem.setNum(amounts.get(storeItem));
            //将orderItem总的成本价统计起来.
            costPrice = costPrice.add(orderItem.getCostPrice().multiply(BigDecimal.valueOf(orderItem.getNum())));
            //统计销售价格
            finalPrice = finalPrice.add(orderItem.getSalesPrice().multiply(BigDecimal.valueOf(orderItem.getNum())));
            orderItemList.add(orderItem);
        }
        //统计成本价格
        mainOrder.setSettlementAmount(costPrice);
        //实付金额
        mainOrder.setFinalAmount(finalPrice);
        mainOrder.setOrderItemList(orderItemList);
        //待付款
        mainOrder.setOrderStatus(OrderStatus.forPay);
        //未结算
        mainOrder.setSettled(false);
        orderItemRepository.save(orderItemList);
        return mainOrderRepository.save(mainOrder);
    }

    @Override
    @BusinessSafe
    @Transactional(rollbackFor = RuntimeException.class)
    public MainOrder supplementOrder(long orderId, Login login, Store store, StoreItem storeItem, int amount) {
        Map<StoreItem, Integer> amounts = new HashMap<>(1);
        amounts.put(storeItem, amount);
        return supplementOrder(orderId, login, store, amounts);
    }

    @Override
    @Transactional
    public void payOrder(long id) {
        MainOrder mainOrder = findById(id);
        mainOrder.setPayTime(LocalDateTime.now());
        mainOrder.setOrderStatus(OrderStatus.success);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Page findAll(OrderSearcherBody orderSearcher) {
        RowDefinition<MainOrder> orderRow = orderRowDefinition(orderSearcher);
        final int startPosition = (orderSearcher.getPage() - 1) * orderSearcher.getPageSize();
        Page<Object[]> page = (Page<Object[]>) rowService.queryFields(orderRow, true, null
                , new PageRequest(startPosition / orderSearcher.getPageSize(), orderSearcher.getPageSize()));
        if (!CollectionUtils.isEmpty(page.getContent())) {
            //根据主键查找关联表所有数据，然后一个个塞进去
            final Long[] orderIdArray = page.getContent().stream().map(p -> Long.valueOf(p[0].toString())).toArray(Long[]::new);
            RowDefinition<OrderItem> itemRow = itemRowDefinition(orderIdArray);
            List<Object[]> itemPage = (List<Object[]>) rowService.queryFields(itemRow, true, null);
            if (!CollectionUtils.isEmpty(itemPage)) {
                page.getContent().forEach(order -> {
                    if (itemPage.stream().anyMatch(item -> item[0].equals(order[0]))) {
                        List<Object[]> orderItems = itemPage.stream().filter(item -> item[0].equals(order[0])).collect(Collectors.toList());
                        order[order.length - 1] = orderItems;
                    } else {
                        order[order.length - 1] = null;
                    }
                });
            }
        }
        return page;
    }

    private RowDefinition<MainOrder> orderRowDefinition(OrderSearcherBody orderSearcher) {
        return new RowDefinition<MainOrder>() {
            @Override
            public Class<MainOrder> entityClass() {
                return MainOrder.class;
            }

            @Override
            public List<Order> defaultOrder(CriteriaBuilder criteriaBuilder, Root<MainOrder> root) {
                return Collections.singletonList(
                        criteriaBuilder.desc(root.get(MainOrder_.orderId))
                );
            }


            @Override
            public List<FieldDefinition<MainOrder>> fields() {
                return orderListField();
            }

            @Override
            public Specification<MainOrder> specification() {
                return (root, cq, cb) -> {
                    List<Predicate> conditions = new ArrayList<>();
                    // 无论如何都不会把根本没下过的订单给发出去
                    if (orderSearcher.getOrderId() != null && orderSearcher.getOrderId() > 0L) {
                        conditions.add(cb.equal(root.get(MainOrder_.orderId), orderSearcher.getOrderId()));
                    } else {
                        conditions.add(cb.notEqual(root.get(MainOrder_.orderStatus), OrderStatus.EMPTY));
                    }
                    if (orderSearcher.getUserId() != null && orderSearcher.getUserId() > 0L) {
                        conditions.add(cb.equal(root.join(MainOrder_.payer, JoinType.LEFT)
                                .get(User_.id), orderSearcher.getUserId()));
                    }
                    if (orderSearcher.getRepresentId() != null && orderSearcher.getRepresentId() > 0L) {
                        conditions.add(cb.equal(root.join(MainOrder_.login, JoinType.LEFT)
                                .get(Login_.id), orderSearcher.getRepresentId()));
                    }
                    if (orderSearcher.getStoreId() != null && orderSearcher.getStoreId() > 0L) {
                        conditions.add(cb.equal(root.join(MainOrder_.store, JoinType.LEFT)
                                .get(Store_.id), orderSearcher.getStoreId()));
                    }
                    return cb.and(conditions.toArray(new Predicate[conditions.size()]));
                };
            }
        };
    }

    private RowDefinition<OrderItem> itemRowDefinition(Long... orderIdArray) {
        return new RowDefinition<OrderItem>() {
            @Override
            public Class<OrderItem> entityClass() {
                return OrderItem.class;
            }

            @Override
            public List<Order> defaultOrder(CriteriaBuilder criteriaBuilder, Root<OrderItem> root) {
                return Arrays.asList(
                        criteriaBuilder.desc(root.get(OrderItem_.itemId))
                );
            }

            @Override
            public List<FieldDefinition<OrderItem>> fields() {
                return orderItemListField();
            }

            @Override
            public Specification<OrderItem> specification() {

                return (root, cq, cb) -> {
                    Path<Long> orderIdExp = root.join(OrderItem_.mainOrder, JoinType.LEFT).get(MainOrder_.orderId);
                    return cb.and(orderIdExp.in(orderIdArray));
                };
            }
        };
    }


    @Override
    public List<FieldDefinition<MainOrder>> orderListField() {
        return Arrays.asList(
                FieldBuilder.asName(MainOrder.class, "orderId")
                        .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.orderId))
                        .build()
                , FieldBuilder.asName(MainOrder.class, "completeTime")
                        .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.payTime))
                        .addFormat((data, type) -> (conversionService.convert(data, String.class)))
                        .build()
                , FieldBuilder.asName(MainOrder.class, "orderStatusCode")
                        .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.orderStatus))
                        .addFormat((data, type) -> ((OrderStatus) data).ordinal())
                        .build()
                , FieldBuilder.asName(MainOrder.class, "orderStatusMsg")
                        .addSelect(mainOrderRoot -> mainOrderRoot.get(MainOrder_.orderStatus))
                        .addFormat((data, type) -> data.toString())
                        .build()
                , FieldBuilder.asName(MainOrder.class, "store")
                        .addSelect(mainOrderRoot -> mainOrderRoot.join(MainOrder_.store, JoinType.LEFT).get(Store_.name))
                        .build()
                , FieldBuilder.asName(MainOrder.class, "payer")
                        .addSelect(mainOrderRoot -> mainOrderRoot.join(MainOrder_.payer, JoinType.LEFT).get(User_.familyName))
                        .build()
                , FieldBuilder.asName(MainOrder.class, "payerAvatar")
                        .addSelect(mainOrderRoot -> mainOrderRoot.join(MainOrder_.payer, JoinType.LEFT)
                                .join(User_.login, JoinType.LEFT).join(Login_.wechatUser, JoinType.LEFT)
                                .get("headImageUrl"))
                        .addFormat((data, type) -> data.toString())
                        .build()
                , FieldBuilder.asName(MainOrder.class, "payerMobile")
                        .addSelect(mainOrderRoot -> mainOrderRoot.join(MainOrder_.payer, JoinType.LEFT)
                                .join(User_.login, JoinType.LEFT).get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(MainOrder.class, "items")
                        .addBiSelect((root, cb) -> cb.nullLiteral(List.class))
                        .addFormat((list, type) -> {
                            //返回数据List 格式化
                            if (list == null) {
                                return null;
                            }
                            List<Object> subRows = new ArrayList<>();
                            List<FieldDefinition<OrderItem>> subRowField = orderItemListField();
                            for (Object data : (List) list) {
                                HashMap<String, Object> outData = new HashMap<>(subRowField.size());
                                for (int i = 0; i < subRowField.size(); i++) {
                                    IndefiniteFieldDefinition fieldDefinition = subRowField.get(i);
                                    outData.put(fieldDefinition.name(), fieldDefinition.export(Array.get(data, i), null, null));
                                }
                                subRows.add(outData);
                            }
                            return subRows;
                        })
                        .build()
        );
    }

    @Autowired
    private ResourceService resourceService;

    private List<FieldDefinition<OrderItem>> orderItemListField() {
        return Arrays.asList(
                FieldBuilder.asName(OrderItem.class, "itemId")
                        .addSelect(orderItemRoot -> orderItemRoot.get(OrderItem_.itemId))
                        .build()
                , FieldBuilder.asName(OrderItem.class, "thumbnail")
                        .addSelect(orderItemRoot -> orderItemRoot.join(OrderItem_.item, JoinType.LEFT).get(Item_.mainImagePath))
                        .addFormat(Utils.formatResourcePathToURL(resourceService))
                        .build()
                , FieldBuilder.asName(OrderItem.class, "title")
                        .addSelect(orderItemRoot -> orderItemRoot.get(OrderItem_.name))
                        .build()
                , FieldBuilder.asName(OrderItem.class, "quantity")
                        .addSelect(orderItemRoot -> orderItemRoot.get(OrderItem_.num))
                        .build()
                , FieldBuilder.asName(OrderItem.class, "vipPrice")
                        .addSelect(orderItemRoot -> orderItemRoot.get(OrderItem_.salesPrice))
                        .build()
                , FieldBuilder.asName(OrderItem.class, "originalPrice")
                        .addSelect(orderItemRoot -> orderItemRoot.get(OrderItem_.price))
                        .build()
                , FieldBuilder.asName(OrderItem.class, "amount")
                        .addBiSelect((orderItemRoot, cb) -> cb.prod(orderItemRoot.get(OrderItem_.salesPrice), orderItemRoot.get(OrderItem_.num)))
                        .build()
        );
    }


}
