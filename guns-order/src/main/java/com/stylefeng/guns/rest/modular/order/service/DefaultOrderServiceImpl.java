package com.stylefeng.guns.rest.modular.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaAPI;
import com.stylefeng.guns.api.cinema.vo.FilmInfoVO;
import com.stylefeng.guns.api.cinema.vo.HallInfoVO;
import com.stylefeng.guns.api.cinema.vo.OrderQueryVO;
import com.stylefeng.guns.api.film.vo.FilmInfo;
import com.stylefeng.guns.api.order.OrderAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.UUIDUtil;
import com.stylefeng.guns.rest.common.persistence.dao.MoocOrderTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocOrderT;
import com.stylefeng.guns.rest.common.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
@Service(interfaceClass = OrderAPI.class,group = "default")
public class DefaultOrderServiceImpl implements OrderAPI {
    @Autowired
    private MoocOrderTMapper moocOrderTMapper;
    @Reference(interfaceClass = CinemaAPI.class,check = false)
    private CinemaAPI cinemaApi;
    @Autowired
    private FTPUtil ftpUtil;

    //验证是否是真实的座位编号
    @Override
    public boolean isTrueSeats(String fieldId, String seats) {
        //根据fieldId找到位置图
        //路径
        String seatPath = moocOrderTMapper.getSeatsByFieldId(fieldId);
        String fileStr =ftpUtil.getFileStrByAddress(seatPath);
        JSONObject jsonObject = JSONObject.parseObject(fileStr);
        //1.2.4.5.6.6.7.5.4.4
        String ids = jsonObject.get("ids").toString();

        String [] seatArray = seats.split(",");
        String [] idArray = ids.split(",");
        int  isTrue = 0;
        for (String id:idArray){
            for (String seat :seatArray){
                if(id.equalsIgnoreCase(seat)){
                    isTrue++;
                }
            }
        }

        //判断seats是否为真
        if(isTrue==seatArray.length){
            return true;
        }

        return false;
    }
    //判断是否为已售的座位
    @Override
    public boolean isNotSoldSeats(String fieldId, String seats) {
        EntityWrapper entityWrapper = new EntityWrapper();
        entityWrapper.eq("field_id",fieldId);
        List<MoocOrderT> list = moocOrderTMapper.selectList(entityWrapper);
        String [] seatArray = seats.split(seats);
        //但凡有任何一个匹配上，则返回失败
        for (MoocOrderT moocOrderT:list) {
                String [] ids = moocOrderT.getSeatsIds().split(",");
            for (String id: ids ) {
                 for (String set : seatArray){
                     if(id.equalsIgnoreCase(set)){
                         return false;
                     }
                 }
            }
        }
        return true;
    }

    @Override
    public OrderVO saveOrderInfo(Integer fieldId, String soldSeats, String seatsName,Integer userId) {
        //编号
        String uuid = UUIDUtil.getUuid();
        FilmInfoVO filmInfoVO = cinemaApi.getFilmInfoByFieldId(fieldId);
        Integer filmId = Integer.parseInt(filmInfoVO.getFilmId());

        //获取影院信息
        OrderQueryVO orderNeeds = cinemaApi.getOrderNeeds(fieldId);
        Integer cinemaId = Integer.parseInt(orderNeeds.getCinemaId());
        double filmPrice = Double.parseDouble(orderNeeds.getFilmPrive());

        //求订单总金额   1.2.3.4.5
        int solds = soldSeats.split(",").length;
        Double sumPrice = getTotalPrice(solds,filmPrice);


        MoocOrderT moocOrderT = new MoocOrderT();
        moocOrderT.setUuid(uuid);
        moocOrderT.setSeatsName(seatsName);
        moocOrderT.setCinemaId(cinemaId);
        moocOrderT.setSeatsIds(soldSeats);
        moocOrderT.setOrderUser(userId);
        moocOrderT.setOrderPrice(sumPrice);
        moocOrderT.setFilmId(filmId);
        moocOrderT.setFieldId(fieldId);
        moocOrderT.setFilmPrice(filmPrice);
        Integer insert = moocOrderTMapper.insert(moocOrderT);
        if(insert>0){
                OrderVO orderVO = moocOrderTMapper.getOrderInfoById(uuid);
                if(orderVO==null||orderVO.getOrderId()==null){
                    log.error("订单信息查询失败,订单编号为{}",uuid);
                    return null;
                }else{
                    return  orderVO;
                }
        }else{
                log.error("订单插入失败");
                return null;
        }



    }


    private double getTotalPrice(int solds,double fieldPrice){
        BigDecimal bsolds = new BigDecimal(solds);
        BigDecimal bfieldPrice = new BigDecimal(fieldPrice);
        BigDecimal result = bsolds.multiply(bfieldPrice);
        //四射五入取小数点后两位
        result.setScale(2, RoundingMode.HALF_UP);
        return result.doubleValue();
    }
    @Override
    public Page<OrderVO> getOrderByUserId(Integer userId,Page<OrderVO> page) {
        Page<OrderVO> result = new Page <>();
        if(userId==null){
            log.error("订单业务查询失败,用户编号获取失败");

        }else{
            List <OrderVO> ordersByUserId = moocOrderTMapper.getOrdersByUserId(userId, page);
            if(ordersByUserId==null||ordersByUserId.size()==0){
                result.setTotal(0);
                result.setRecords(new ArrayList <>());
                return result;
            }else{
                //获取订单总数
                EntityWrapper<MoocOrderT> moocOrderTEntityWrapper = new EntityWrapper <>();
                moocOrderTEntityWrapper.eq("order_user",userId);
                Integer integer = moocOrderTMapper.selectCount(moocOrderTEntityWrapper);

                //将结果放入page
                result.setTotal(integer);
                result.setRecords(ordersByUserId);
                return  result;
            }
        }
        return null;
    }
    //根据放映场次，获取所有的已售座位
    // 1 1,2,3,4,5
    // 2 5,6,7
    @Override
    public String getSoldSeatsByFieldId(Integer fielId) {
        if(fielId==null){
            log.error("为传入任何场次编号");
            return "";
        }else{
            String soldSeatsByFieldId = moocOrderTMapper.getSoldSeatsByFieldId(fielId);
            return soldSeatsByFieldId;
        }
    }

    @Override
    public OrderVO getOrderInfoById(String orderId) {
        OrderVO OrderInfo = moocOrderTMapper.getOrderInfoById(orderId);
        return OrderInfo;
    }

    @Override
    public boolean paySuccess(String orderId) {
        MoocOrderT moocOrderT = new MoocOrderT();
        moocOrderT.setUuid(orderId);
        moocOrderT.setOrderStatus(1);
        Integer integer = moocOrderTMapper.updateById(moocOrderT);
        if(integer>=1){
            return true;
        }else{
            return false;
        }

    }

    @Override
    public boolean payFail(String orderId) {
        MoocOrderT moocOrderT = new MoocOrderT();
        moocOrderT.setUuid(orderId);
        moocOrderT.setOrderStatus(2);
        Integer integer = moocOrderTMapper.updateById(moocOrderT);
        if(integer>=1){
            return true;
        }else{
            return false;
        }
    }
}
