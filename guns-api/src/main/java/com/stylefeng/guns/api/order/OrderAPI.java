package com.stylefeng.guns.api.order;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.order.vo.OrderVO;

import java.util.List;

public interface OrderAPI {

    //验证售出的票是否为true
    //fieldId 影厅类型  imax，情侣等
    boolean isTrueSeats(String fieldId,String seats);
    //已经销售的座位里，有没有这个座位
    boolean isNotSoldSeats(String fieldId,String seats);

    //创建订单信息
    OrderVO saveOrderInfo(Integer fieldId,String soldSeats,String seatsName,Integer userId);
    //使用当前登录已经购买的订单
    Page<OrderVO> getOrderByUserId(Integer userId,Page<OrderVO> page);
    //根据fiedID 查询已经销售的订单编号
    String getSoldSeatsByFieldId(Integer fielId);
    //获取订单状态
    OrderVO getOrderInfoById(String orderId);
    //修改订单状态
    boolean  paySuccess(String orderId);
    boolean  payFail(String orderId);

}
