package com.ming.shopping.beauty.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ming.shopping.beauty.service.entity.support.OrderStatus;
import com.ming.shopping.beauty.service.utils.Constant;
import lombok.Data;
import me.jiangcai.wx.model.Gender;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author helloztt
 */
@Data
public class OrderResponse {

    private long orderId;
    @JsonProperty("completeTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DATETIME_FORMAT)
    private LocalDateTime createTime;
    private String orderStatus;
    private int orderStatusCode;
    private String store;
    private String payer;
    private String payerMobile;
    private List<OrderItemResponse> items;

    public OrderResponse(long orderId, LocalDateTime createTime, OrderStatus orderStatus, String storeName, String payerName, Gender gender,String payerLoginName){
        this.orderId = orderId;
        this.createTime = createTime;
        this.orderStatus = orderStatus.toString();
        this.orderStatusCode = orderStatus.ordinal();
        this.store = storeName;
        this.payer = payerLoginName;
        this.payerMobile = payerLoginName;
    }
}
