package com.stylefeng.guns.rest.modular.cinema.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaAPI;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;

@Component
@Service(interfaceClass = CinemaAPI.class,executes = 10)
public class CinemaServiceImpl implements CinemaAPI{
    @Autowired
    private MoocCinemaTMapper moocCinemaTMapper;
    @Autowired
    private MoocAreaDictTMapper moocAreaDictTMapper;
    @Autowired
    private MoocBrandDictTMapper moocBrandDictTMapper;
    @Autowired
    private MoocHallDictTMapper moocHallDictTMapper;
    @Autowired
    private MoocHallFilmInfoTMapper moocHallFilmInfoTMapper;
    @Autowired
    private MoocFieldTMapper moocFieldTMapper;

    @Override
    public Page<CinemaVO> getCinemas(CinemaQueryVO cinemaQueryVO) {
        //业务实体集合
        List<CinemaVO> cinemaVOS = new ArrayList <>();

        Page<CinemaVO> cinemaVOPage = new Page <>(cinemaQueryVO.getNowPage(),cinemaQueryVO.getPageSize());
        //判断是否传入查询条件-->brandId，distId，hallType 是否--99
        EntityWrapper<MoocCinemaT> moocCinemaTEntityWrapper = new EntityWrapper <>();
        if(cinemaQueryVO.getBrandId() !=99){
            moocCinemaTEntityWrapper.eq("brand_id",cinemaQueryVO.getBrandId());
        }
        if(cinemaQueryVO.getDistrictId() !=99){
            moocCinemaTEntityWrapper.eq("area_id",cinemaQueryVO.getDistrictId());
        }
        if(cinemaQueryVO.getHallType() !=99){
            //#1#2#3
            moocCinemaTEntityWrapper.like("hall_ids","%#"+cinemaQueryVO.getHallType()+"#%");
        }
        //将数据实体转换为业务实体
        List<MoocCinemaT> moocCinemaTS = moocCinemaTMapper.selectPage(cinemaVOPage,moocCinemaTEntityWrapper);
        for (MoocCinemaT moocCinemaT:moocCinemaTS){
            CinemaVO  cinemaVO = new CinemaVO();
            cinemaVO.setCinemaName(moocCinemaT.getCinemaName());
            cinemaVO.setUuid(moocCinemaT.getUuid()+"");
            cinemaVO.setMinimumPrice(moocCinemaT.getMinimumPrice()+"");
            cinemaVO.setAddress(moocCinemaT.getCinemaAddress());
            cinemaVOS.add(cinemaVO);
        }

        //根据条件判断影院列表总数
        //组织返回对象
        long counts = moocCinemaTMapper.selectCount(moocCinemaTEntityWrapper);
        Page<CinemaVO> result = new Page <>();
        result.setRecords(cinemaVOS);
        result.setSize(cinemaQueryVO.getPageSize());
        result.setTotal(counts);

        return result;
    }

    @Override
    public List<BrandVO> getBrands(int brandId) {
        List<BrandVO> list = new ArrayList <>();
        //判断99
        boolean flag = false;
        //判断传入的ID是否存在
        MoocBrandDictT moocBrandDictT = moocBrandDictTMapper.selectById(brandId);
        //判断ID 是否等于99
        if(brandId == 99 || moocBrandDictT ==null || moocBrandDictT.getUuid() == null){
            flag = true;
        }
        //查询所有列表
        List<MoocBrandDictT> moocBrandDictTS = moocBrandDictTMapper.selectList(null);

        //判断flag如果为true ，将99设置为isActive
        for (MoocBrandDictT moocBrandDict:moocBrandDictTS){
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandName(moocBrandDict.getShowName());
            brandVO.setBrandId(moocBrandDict.getUuid()+"");
            if(flag){
                if(moocBrandDict.getUuid()==99){
                    brandVO.setActive(true);
                }
            }else{
                if(moocBrandDict.getUuid()==brandId){
                    brandVO.setActive(true);
                }
            }
            list.add(brandVO);
        }
        return list;
    }

    @Override
    public List <AreaVO> getAreas(int areaId) {
        List<AreaVO> list = new ArrayList <>();
        //判断99
        boolean flag = false;
        //判断传入的ID是否存在
        MoocAreaDictT moocAreaDictT = moocAreaDictTMapper.selectById(areaId);
        //判断ID 是否等于99
        if(areaId == 99 || moocAreaDictT ==null || moocAreaDictT.getUuid() == null){
            flag = true;
        }
        //查询所有列表
        List<MoocAreaDictT>  moocBrandDictTS= moocAreaDictTMapper.selectList(null);

        //判断flag如果为true ，将99设置为isActive
        for (MoocAreaDictT moocAreaDict:moocBrandDictTS){
            AreaVO areaVO = new AreaVO();
            areaVO.setAreaName(moocAreaDict.getShowName());
            areaVO.setAreaId(moocAreaDict.getUuid()+"");
            if(flag){
                if(moocAreaDict.getUuid()==99){
                    areaVO.setActive(true);
                }
            }else{
                if(moocAreaDict.getUuid()==areaId){
                    areaVO.setActive(true);
                }
            }
            list.add(areaVO);
        }
        return list;
    }

    @Override
    public List <HallTypeVO> getHallTypes(int hallType) {{
        List<HallTypeVO> list = new ArrayList <>();
        //判断99
        boolean flag = false;
        //判断传入的ID是否存在
        MoocHallDictT moocHallDictT = moocHallDictTMapper.selectById(hallType);
        //判断ID 是否等于99
        if(hallType == 99 || moocHallDictT ==null || moocHallDictT.getUuid() == null){
            flag = true;
        }
        //查询所有列表
        List<MoocHallDictT>  moocHallDictTS= moocHallDictTMapper.selectList(null);

        //判断flag如果为true ，将99设置为isActive
        for (MoocHallDictT moocHallDict:moocHallDictTS){
            HallTypeVO hallTypeVO = new HallTypeVO();
            hallTypeVO.setHalltypeName(moocHallDict.getShowName());
            hallTypeVO.setHalltypeId(moocHallDict.getUuid()+"");
            if(flag){
                if(moocHallDict.getUuid()==99){
                    hallTypeVO.setActive(true);
                }
            }else{
                if(moocHallDict.getUuid()==hallType){
                    hallTypeVO.setActive(true);
                }
            }
            list.add(hallTypeVO);
        }
        return list;
    }

    }

    @Override
    public CinemaInfoVO getCinemaInfoById(int cinemaId) {
        //组织数据实体
        MoocCinemaT moocCinemaT = moocCinemaTMapper.selectById(cinemaId);
        if(moocCinemaT == null){
            return new CinemaInfoVO();
        }
        //将数据实体转入
        CinemaInfoVO cinemaInfoVO = new CinemaInfoVO();
        cinemaInfoVO.setImgUrl(moocCinemaT.getImgAddress());
        cinemaInfoVO.setCinemaPhone(moocCinemaT.getCinemaPhone());
        cinemaInfoVO.setCinemaName(moocCinemaT.getCinemaName());
        cinemaInfoVO.setCinemaId(moocCinemaT.getUuid()+"");
        cinemaInfoVO.setCinemaId(moocCinemaT.getCinemaAddress());
        return cinemaInfoVO;
    }

    @Override
    public List <FilmInfoVO> getFilmInfoByCinemaId(int cinemaId) {
        List<FilmInfoVO> filmInfos = moocFieldTMapper.getFilmInfos(cinemaId);
        return filmInfos;
    }

    @Override
    public HallInfoVO getFilmFieldInfo(int fieldId) {
        HallInfoVO hallInfoVO = moocFieldTMapper.getHallInfo(fieldId);
        return hallInfoVO;
    }

    @Override
    public FilmInfoVO getFilmInfoByFieldId(int fieldId) {

        FilmInfoVO filmInfoVO = moocFieldTMapper.getFilmInfoById(fieldId);

        return filmInfoVO;
    }

    @Override
    public OrderQueryVO getOrderNeeds(int fieldId) {
        OrderQueryVO orderQueryVO = new OrderQueryVO();
        MoocFieldT moocFieldT = moocFieldTMapper.selectById(fieldId);
        orderQueryVO.setCinemaId(moocFieldT.getCinemaId()+"");
        orderQueryVO.setFilmPrive(moocFieldT.getPrice()+"");
        return orderQueryVO;
    }
}
