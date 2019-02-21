package com.stylefeng.guns.rest.modular.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.TradeFundBill;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.stylefeng.guns.api.alipay.AlipayAPI;
import com.stylefeng.guns.api.alipay.vo.AliPayInfoVO;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVO;
import com.stylefeng.guns.api.order.OrderAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.rest.common.util.FTPUtil;
import com.stylefeng.guns.rest.modular.alipay.config.Configs;
import com.stylefeng.guns.rest.modular.alipay.model.ExtendParams;
import com.stylefeng.guns.rest.modular.alipay.model.GoodsDetail;
import com.stylefeng.guns.rest.modular.alipay.model.builder.AlipayTradePrecreateRequestBuilder;
import com.stylefeng.guns.rest.modular.alipay.model.builder.AlipayTradeQueryRequestBuilder;
import com.stylefeng.guns.rest.modular.alipay.model.result.AlipayF2FPrecreateResult;
import com.stylefeng.guns.rest.modular.alipay.model.result.AlipayF2FQueryResult;
import com.stylefeng.guns.rest.modular.alipay.service.AlipayMonitorService;
import com.stylefeng.guns.rest.modular.alipay.service.AlipayTradeService;
import com.stylefeng.guns.rest.modular.alipay.service.impl.AlipayMonitorServiceImpl;
import com.stylefeng.guns.rest.modular.alipay.service.impl.AlipayTradeServiceImpl;
import com.stylefeng.guns.rest.modular.alipay.service.impl.AlipayTradeWithHBServiceImpl;
import com.stylefeng.guns.rest.modular.alipay.utils.Utils;
import com.stylefeng.guns.rest.modular.alipay.utils.ZxingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
@Service(interfaceClass = AlipayAPI.class,mock = "com.stylefeng.guns.api.alipay.AlipayAPIMoc" )
public class DefaultAlipayServiceImpl implements AlipayAPI {

    @Reference(interfaceClass = OrderAPI.class,check = false,group = "order2018")
    private OrderAPI orderAPI;
    @Autowired
    private FTPUtil ftpUtil;
    /*
    * alipay初始化代码
    * */
    // 支付宝当面付2.0服务
    private static AlipayTradeService   tradeService;

    // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
    private static AlipayTradeService tradeWithHBService;

    // 支付宝交易保障接口服务，供测试接口api使用，请先阅读readme.txt
    private static AlipayMonitorService monitorService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
        tradeWithHBService = new AlipayTradeWithHBServiceImpl.ClientBuilder().build();

        /** 如果需要在程序中覆盖Configs提供的默认参数, 可以使用ClientBuilder类的setXXX方法修改默认参数 否则使用代码中的默认设置 */
        monitorService = new AlipayMonitorServiceImpl.ClientBuilder()
                .setGatewayUrl("http://mcloudmonitor.com/gateway.do").setCharset("GBK")
                .setFormat("json").build();
    }
    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }
    /*
    * alipay初始化代码结束
    * */

    @Override
    public AliPayInfoVO getQRCode(String orderId) {
        String filePath = trade_precreat(orderId);
        if(filePath.trim().length()==0){
            //获取二维码失败
                return null;
        }else{
            AliPayInfoVO aliPayInfoVO = new AliPayInfoVO();
            aliPayInfoVO.setOrderId(orderId);
            aliPayInfoVO.setQRCodeAddress(filePath);
            return aliPayInfoVO;
        }

    }

    @Override
    public AliPayResultVO getOrderStatus(String orderId) {
        String userId = RpcContext.getContext().getAttachment("userId");


        boolean flag = trade_query(orderId);
        if(flag){
            AliPayResultVO aliPayResultVO = new AliPayResultVO();
            aliPayResultVO.setOrderId(orderId);
            aliPayResultVO.setOrderStatus(1);
            aliPayResultVO.setOrderMsg("支付成功！");
            return aliPayResultVO;
        }else{
            return null;
        }

    }

    /*
    * 传入参数：outTradeNo 订单编号
    * */
    private String trade_precreat(String orderId){
            String filePath = "";
            OrderVO orderInfo = orderAPI.getOrderInfoById(orderId);

            // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
            // 需保证商户系统端不能重复，建议通过数据库sequence生成，
            String outTradeNo = orderId;

            // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
            String subject = "Meeting影院购票业务";

            // (必填) 订单总金额，单位为元，不能超过1亿元
            // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
            String totalAmount = orderInfo.getOrderPrice();

            // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
            // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
            String undiscountableAmount = "0";

            // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
            // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
            String sellerId = "";

            // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
            String body = "购买电影票共花费"+totalAmount+"元";

            // 商户操作员编号，添加此参数可以为商户操作员做销售统计
            String operatorId = "jinzheshen";

            // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
            String storeId = "jinzheshen";

            // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
            ExtendParams extendParams = new ExtendParams();
            extendParams.setSysServiceProviderId("2088100200300400500");

            // 支付超时，定义为120分钟
            String timeoutExpress = "120m";

            // 商品明细列表，需填写购买商品详细信息，
            List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
           // GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx小面包", 1000, 1);
            // 创建好一个商品后添加至商品明细列表
           // goodsDetailList.add(goods1);

            // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件
           // GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx牙刷", 500, 2);
           // goodsDetailList.add(goods2);

            // 创建扫码支付请求builder，设置请求参数
            AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                    .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                    .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                    .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                    .setTimeoutExpress(timeoutExpress)
                    //                .setNotifyUrl("http://www.test-notify-url.com")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                    .setGoodsDetailList(goodsDetailList);

            AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
            switch (result.getTradeStatus()) {
                case SUCCESS:
                    log.info("支付宝预下单成功: )");

                    AlipayTradePrecreateResponse response = result.getResponse();
                   // dumpResponse(response);

                    // 需要修改为运行机器上的路径
                    filePath = String.format("C:/Users/Administrator/Desktop/QRCODE/qr-%s.png",
                            response.getOutTradeNo());
                    String fileName = String.format("qr-%s.png",response.getOutTradeNo());
                    log.info("filePath:" + filePath);
                    File myerweima = ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                    boolean flag = ftpUtil.uploadFile(fileName,myerweima);
                    if(flag){
                        filePath = "";
                    }else{
                        log.error("二维码上传失败!!!");
                    }
                    break;

                case FAILED:
                    log.error("支付宝预下单失败!!!");
                    break;

                case UNKNOWN:
                    log.error("系统异常，预下单状态未知!!!");
                    break;

                default:
                    log.error("不支持的交易状态，交易返回异常!!!");
                    break;
            }
        return filePath;
        }



    private boolean trade_query(String orderId) {
        boolean flag = false;
        // (必填) 商户订单号，通过此商户订单号查询当面付的交易状态
        String outTradeNo = orderId;

        // 创建查询请求builder，设置请求参数
        AlipayTradeQueryRequestBuilder builder = new AlipayTradeQueryRequestBuilder()
                .setOutTradeNo(outTradeNo);

        AlipayF2FQueryResult result = tradeService.queryTradeResult(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("查询返回该订单支付成功: )");
                //当订单支付成功 修改订单状态
                flag = orderAPI.paySuccess(orderId);
                /*AlipayTradeQueryResponse response = result.getResponse();
                //dumpResponse(response);

                log.info(response.getTradeStatus());
                if (Utils.isListNotEmpty(response.getFundBillList())) {
                    for (TradeFundBill bill : response.getFundBillList()) {
                        log.info(bill.getFundChannel() + ":" + bill.getAmount());
                    }
                }*/
                break;

            case FAILED:
                log.error("查询返回该订单支付失败或被关闭!!!");
                break;

            case UNKNOWN:
                log.error("系统异常，订单支付状态未知!!!");
                break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
        return flag;
    }


}
