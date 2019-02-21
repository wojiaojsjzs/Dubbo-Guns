package com.stylefeng.guns.api.alipay.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AliPayResultVO implements Serializable {
    //订单ID
    private String orderId;
    //支付结果
    private Integer orderStatus;
    //订单状态
    private String orderMsg;

}
