package com.stylefeng.guns.api.alipay;

import com.stylefeng.guns.api.alipay.vo.AliPayInfoVO;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVO;

public interface AlipayAPI {
    //获取二维码接口
    AliPayInfoVO getQRCode(String orderId);
    //获取支付结果
    AliPayResultVO getOrderStatus(String orderId);
}
