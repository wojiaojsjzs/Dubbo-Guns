package com.stylefeng.guns.rest.modular.order;

import com.alibaba.dubbo.config.annotation.Reference;

import com.alibaba.dubbo.rpc.RpcContext;
import com.baomidou.mybatisplus.plugins.Page;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.stylefeng.guns.api.alipay.AlipayAPI;
import com.stylefeng.guns.api.alipay.vo.AliPayInfoVO;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVO;
import com.stylefeng.guns.api.order.OrderAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.TokenBucket;
import com.stylefeng.guns.core.util.ToolUtil;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequestMapping(value = "/order/")
@RestController
public class OrderController {
        @Reference(interfaceClass = OrderAPI.class,check = false,group = "order2018")
    private OrderAPI orderAPI;

        @Reference(interfaceClass = OrderAPI.class,check = false,group = "order2017")
        private OrderAPI orderAPI2017;

        @Reference(interfaceClass = AlipayAPI.class,check = false)
        private AlipayAPI alipayAPI;

        private static  TokenBucket tokenBucket = new TokenBucket();

        private static  String IMG_PRE="http://img.meetingshop.cn/";


            /*
            * 降级
            *
            * */
            public ResponseVO error(Integer fieldId,String soldSeats,String seatsName){
                return ResponseVO.serviceFail("对不起，下单人员太多请稍后重试！");
            }
            //信号量 线程池 线程 线程切换 保证线程安全性
        //购票
            @HystrixCommand(fallbackMethod = "error", commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "4000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")},
            threadPoolProperties = {
            @HystrixProperty(name = "coreSize", value = "1"),
            @HystrixProperty(name = "maxQueueSize", value = "10"),
            @HystrixProperty(name = "keepAliveTimeMinutes", value = "1000"),
            @HystrixProperty(name = "queueSizeRejectionThreshold", value = "8"),
            @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1500")
            })
        @RequestMapping(value = "buyTickets",method = RequestMethod.POST)
        public ResponseVO buyTickets(Integer fieldId,String soldSeats,String seatsName){
                try {
                    if(tokenBucket.getToken()){

                    //验证售出的票是否为true
                    boolean trueSeats = orderAPI.isTrueSeats(fieldId+"",soldSeats);
                    //已经销售的座位里，有没有这个作为
                    boolean notSoldSeats = orderAPI.isNotSoldSeats(fieldId + "", soldSeats);
                    //验证上述两个有一个不为真，则不会创建订单信息
                    if(trueSeats&&notSoldSeats){
                        //创建订单信息,注意获取登录人
                        String userId = CurrentUser.getCurrentUser();
                        if(userId == null||userId.trim().length()==0){
                            return  ResponseVO.serviceFail("用户未登录");
                        }
                        OrderVO orderVO = orderAPI.saveOrderInfo(fieldId,soldSeats,seatsName,Integer.parseInt(userId));
                        if(orderVO==null){
                            log.error("购票失败！");
                            return  ResponseVO.serviceFail("购票业务异常");
                        }else{
                            return  ResponseVO.success(orderVO);
                        }
                    }else{
                        return  ResponseVO.serviceFail("订单中的座位编号有问题!");
                    }
                    }else{
                        return  ResponseVO.serviceFail("桶满了!");
                    }

                }catch (Exception e){
                    log.error("购票业务异常",e);
                    return  ResponseVO.serviceFail("购票业务异常");
                }

        }

        @RequestMapping(value = "getOrderInfo",method = RequestMethod.POST)
        public ResponseVO getOrderInfo(@RequestParam(name="nowPage",required = false,defaultValue = "1") Integer nowPage,
                                       @RequestParam(name="pageSize",required = false,defaultValue = "5")Integer pageSize){
            //获取当前挡路人的信息
            String userId = CurrentUser.getCurrentUser();

            //使用当前登录已经购买的订单
            Page<OrderVO> page = new Page <>(nowPage,pageSize);
            if(userId != null&&userId.trim().length()>0){
                Page <OrderVO> orderByUserId = orderAPI.getOrderByUserId(Integer.parseInt(userId), page);
                Page <OrderVO> orderByUserId2017 = orderAPI2017.getOrderByUserId(Integer.parseInt(userId), page);
                //合并结果集合
                int totalPages = (int) (orderByUserId.getPages()+orderByUserId2017.getPages());
                List<OrderVO> orderVOList = new ArrayList <>();
                orderVOList.addAll(orderByUserId.getRecords());
                orderVOList.addAll(orderByUserId2017.getRecords());
                return ResponseVO.success(nowPage,totalPages,null,orderVOList);
            }else{
                return ResponseVO.serviceFail("用户未登录");
            }


        }



        /**
        * 支付宝获取支付二维码
        * */
        @RequestMapping(value = "getPayInfo",method = RequestMethod.POST)
        public ResponseVO getPayInfo(@RequestParam("orderId") String orderId){
            //获取当前挡路人的信息
            String userId = CurrentUser.getCurrentUser();
            if(userId == null||userId.trim().length()==0){
                return ResponseVO.serviceFail("用户未登录");
            }
            //订单二维码返回结果
            AliPayInfoVO aliPayInfoVO = alipayAPI.getQRCode(orderId);

           return  ResponseVO.success(IMG_PRE,aliPayInfoVO);
        }




         /**
         * {
         *"status":0,
         *"data":{
         *"orderId": "1234123",
         *"orderStatus": 1,
         *"orderMsg":"支付成功"
         * }
         *  }
         * 注： orderStatus状态为1为支付成功，其余均为支付失败
         * */
        @RequestMapping(value = "getPayResult",method = RequestMethod.POST)
        public ResponseVO getPayResult(@RequestParam("orderId")String orderId,@RequestParam(name="tryNums",required = false,defaultValue = "1")Integer tryNums){
            //获取当前挡路人的信息
            String userId = CurrentUser.getCurrentUser();
            if(userId == null||userId.trim().length()==0){
                return ResponseVO.serviceFail("用户未登录");
            }
            //将当前登录人的信息传递给后端
            RpcContext.getContext().setAttachment("userId",userId);
            //判断是否支付超时
            if(tryNums>=4){
                return ResponseVO.serviceFail("订单支付失败,请稍后重试！");
            }else{
                AliPayResultVO aliPayResultVO  = alipayAPI.getOrderStatus(orderId);
                if(aliPayResultVO==null|| ToolUtil.isEmpty(aliPayResultVO.getOrderId())){
                    AliPayResultVO failVo = new AliPayResultVO();
                    failVo.setOrderId(orderId);
                    failVo.setOrderMsg("支付不成功");
                    failVo.setOrderStatus(0);
                    return ResponseVO.success(failVo);
                }
                return ResponseVO.success(aliPayResultVO);
            }

        }


 }
