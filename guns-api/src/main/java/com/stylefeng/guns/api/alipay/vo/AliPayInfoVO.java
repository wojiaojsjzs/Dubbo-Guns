package com.stylefeng.guns.api.alipay.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AliPayInfoVO implements Serializable {
    //订单ID
    private String orderId;
    //二维码地址
    private String QRCodeAddress;

}
