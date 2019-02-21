package com.stylefeng.guns.api.alipay;

import com.stylefeng.guns.api.alipay.vo.AliPayInfoVO;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVO;

public class AlipayAPIMoc implements AlipayAPI{
    @Override
    public AliPayInfoVO getQRCode(String orderId) {
        return null;
    }

    @Override
    public AliPayResultVO getOrderStatus(String orderId) {
        AliPayResultVO  aliPayResultVO = new AliPayResultVO();
        aliPayResultVO.setOrderId(orderId);
        aliPayResultVO.setOrderMsg("尚未支付未成功");
        aliPayResultVO.setOrderStatus(10);
        return aliPayResultVO;
    }
}
