package com.stylefeng.guns.rest.modular.cinema;

import com.alibaba.dubbo.config.annotation.Reference;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaAPI;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.api.order.OrderAPI;
import com.stylefeng.guns.rest.modular.cinema.vo.CinemaConditionResponseVO;
import com.stylefeng.guns.rest.modular.cinema.vo.CinemaFieldResponseVO;
import com.stylefeng.guns.rest.modular.cinema.vo.CinemaFieldsResponseVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/cinema/")
@RestController
public class CinemaController {
    private static final String IMG_PRE = "http://img.meetingshop.cn/";

    @Reference(interfaceClass = CinemaAPI.class,check = false,cache = "lru",connections = 20)
    private CinemaAPI  cinemaAPI;
    @Reference(interfaceClass = OrderAPI.class,check = false,cache = "lru",connections = 20)
    private OrderAPI orderAPI;

    @RequestMapping(value="getCinemas")
    public ResponseVO getCinemas(CinemaQueryVO cinemaQueryVO){

        try {
            //按照条件筛选
            Page <CinemaVO> cinemas = cinemaAPI.getCinemas(cinemaQueryVO);
            //判断满足条件的筛选
            if(cinemas.getRecords() == null || cinemas.getRecords().size() == 0 ){
                return ResponseVO.success("没有影院可查");
            }else{
                //组织VO
                return ResponseVO.success(cinemas.getCurrent(),(int)cinemas.getPages(),"",cinemas.getRecords());
            }

        }catch (Exception e){
            log.error("获取影院列表异常");
            return ResponseVO.serviceFail("查询影院列表失败");
        }


    }


    @RequestMapping(value="getCondition")
    public ResponseVO getCondition(CinemaQueryVO cinemaQueryVO){
        //获取集合
        try {
            List <BrandVO> brands = cinemaAPI.getBrands(cinemaQueryVO.getBrandId());
            List <AreaVO> areas  = cinemaAPI.getAreas(cinemaQueryVO.getDistrictId());
            List <HallTypeVO> hallTypes = cinemaAPI.getHallTypes(cinemaQueryVO.getHallType());
            CinemaConditionResponseVO cinemaConditionResponseVO = new CinemaConditionResponseVO();
            cinemaConditionResponseVO.setAreaList(areas);
            cinemaConditionResponseVO.setBrandList(brands);
            cinemaConditionResponseVO.setHalltypeList(hallTypes);
            return ResponseVO.success(cinemaConditionResponseVO);
        }catch (Exception e){
            log.error("获取列表异常");
            return ResponseVO.serviceFail("查询失败");
        }

}
    //场次
    @RequestMapping(value="getFields")
    public ResponseVO getFields(Integer cinemaId){
        try {
            CinemaInfoVO  cinemaInfoVO =cinemaAPI.getCinemaInfoById(cinemaId);
            List <FilmInfoVO> filmInfoByCinemaId =cinemaAPI.getFilmInfoByCinemaId(cinemaId);

            CinemaFieldsResponseVO cinemaFieldResponseVO = new CinemaFieldsResponseVO();
            cinemaFieldResponseVO.setCinemaInfo(cinemaInfoVO);
            cinemaFieldResponseVO.setFilmList(filmInfoByCinemaId);

            return ResponseVO.success(IMG_PRE,cinemaFieldResponseVO);
        }catch (Exception e){
            log.error("获取列表异常");
            return ResponseVO.serviceFail("查询失败");
        }


    }

    //场次详细接口

    @RequestMapping(value="getFieldInfo",method = RequestMethod.POST)
    public ResponseVO getFieldInfo(Integer cinemaId,Integer fieldId){
        try {
            CinemaInfoVO cinemaInfoById = cinemaAPI.getCinemaInfoById(cinemaId);
            FilmInfoVO filmInfoByFieldId = cinemaAPI.getFilmInfoByFieldId(fieldId);
            HallInfoVO filmFieldInfo = cinemaAPI.getFilmFieldInfo(fieldId);

            // 造几个销售的假数据，后续会对接订单接口
            filmFieldInfo.setSoldSeats(orderAPI.getSoldSeatsByFieldId(fieldId));


            CinemaFieldResponseVO cinemaFieldResponseVO = new CinemaFieldResponseVO();
            cinemaFieldResponseVO.setCinemaInfo(cinemaInfoById);
            cinemaFieldResponseVO.setFilmInfo(filmInfoByFieldId);
            cinemaFieldResponseVO.setHallInfo(filmFieldInfo);

            return ResponseVO.success(IMG_PRE,cinemaFieldResponseVO);
        }catch (Exception e){
            log.error("获取列表异常");
            return ResponseVO.serviceFail("查询失败");
        }
    }

}
