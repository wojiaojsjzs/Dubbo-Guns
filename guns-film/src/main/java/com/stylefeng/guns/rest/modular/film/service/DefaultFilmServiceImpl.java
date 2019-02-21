package com.stylefeng.guns.rest.modular.film.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.film.FilmAPI;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.core.util.DateUtil;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
@Service(interfaceClass = FilmAPI.class)
public class DefaultFilmServiceImpl implements FilmAPI {
    @Autowired
    private MoocBannerTMapper moocBannerTMapper;

    @Autowired
    private MoocFilmTMapper moocFilmTMapper;

    @Autowired
    private MoocCatDictTMapper moocCatDictTMapper;

    @Autowired
    private MoocYearDictTMapper moocYearDictTMapper;

    @Autowired
    private MoocSourceDictTMapper moocSourceDictTMapper;

    @Autowired
    private MoocFilmInfoTMapper moocFilmInfoTMapper;

    @Autowired
    private MoocActorTMapper moocActorTMapper;

    @Override
    public List<BannerVo> getBanners() {
        List<BannerVo> resultList = new ArrayList<BannerVo>();
        List<MoocBannerT> moocBanners = moocBannerTMapper.selectList(null);
        for (MoocBannerT MoocBannerT: moocBanners) {
            BannerVo bannerVo = new BannerVo();
            bannerVo.setBannerId(MoocBannerT.getUuid()+"");
            bannerVo.setBannerUrl(MoocBannerT.getBannerUrl());
            bannerVo.setBannerAddress(MoocBannerT.getBannerAddress());
            resultList.add(bannerVo);
        }
        return resultList;
    }
    private List<FilmInfo> getFilmInfos(List<MoocFilmT> moocFilms){
        List<FilmInfo> filmInfos = new ArrayList<>();
        for(MoocFilmT moocFilmT : moocFilms){
            FilmInfo filmInfo = new FilmInfo();
            filmInfo.setScore(moocFilmT.getFilmScore());
            filmInfo.setImgAddress(moocFilmT.getImgAddress());
            filmInfo.setFilmType(moocFilmT.getFilmType());
            filmInfo.setFilmScore(moocFilmT.getFilmScore());
            filmInfo.setFilmName(moocFilmT.getFilmName());
            filmInfo.setFilmId(moocFilmT.getUuid()+"");
            filmInfo.setExpectNum(moocFilmT.getFilmPresalenum());
            filmInfo.setBoxNum(moocFilmT.getFilmBoxOffice());
            filmInfo.setShowTime(DateUtil.getDay(moocFilmT.getFilmTime()));
            // 将转换的对象放入结果集
            filmInfos.add(filmInfo);
        }

        return filmInfos;
    }
    @Override
    public FilmVo getHotFilm(boolean isLimit, int nums,int nowPage,int sortId,int sourceId,int yearId,int catId) {
        FilmVo filmVo = new FilmVo();
        List<FilmInfo> filmInfos = new ArrayList<FilmInfo>();

        //热映影片限制条件
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","1");
        //判断是否是首页需要的内容
        if(isLimit) { //是   限制内容  限制内容为热映影片
            Page<MoocFilmT> page = new Page<MoocFilmT>(1,8);



            List<MoocFilmT> moocFilmT = moocFilmTMapper.selectPage(page,entityWrapper);
            filmInfos = getFilmInfos(moocFilmT);
            filmVo.setFilmInfo(filmInfos);
            filmVo.setFilmNum(filmInfos.size());

        }else { //否   列表页面  限制内容为热映影片
            Page<MoocFilmT> page = null;//new Page<MoocFilmT>(nowPage,nums);
            //如果yearID ，sourceId，catId不能为99，则表示要按照对应的编号进行查询

            //根据SourceId不同创建page对象
            //影片状态,1-正在热映，2-即将上映，3-经典影片
            switch(sourceId) {
                case 1 :
                    page = new Page<MoocFilmT>(nowPage,nums,"film_box_office");
                    break;
                case 2 :
                    page = new Page<MoocFilmT>(nowPage,nums,"film_time");
                    break;
                case 3 :
                    page = new Page<MoocFilmT>(nowPage,nums,"film_score");
                    break;
                default:
                    page = new Page<MoocFilmT>(nowPage,nums,"film_box_office");
                    break;
            }


            if(sourceId!=99){
                entityWrapper.eq("film_score",sourceId);
            }
            if(yearId!=99){
                entityWrapper.eq("film_date",yearId);
            }
            if(catId!=99){
                //#2#4#22#
                String catStr = "%#"+catId+"#%";
                entityWrapper.like("film_cats",catStr);
            }
            List<MoocFilmT> moocFilmT = moocFilmTMapper.selectPage(page,entityWrapper);
            filmInfos = getFilmInfos(moocFilmT);

            //总页数  totalCounts/nums

            int totalCounts = moocFilmTMapper.selectCount(entityWrapper);
            int totalPages = (totalCounts/nums)+1;
            filmVo.setFilmNum(filmInfos.size());
            filmVo.setFilmInfo(filmInfos);
            filmVo.setNowPage(nowPage);
            filmVo.setTotalPage(totalPages);
        }
        return filmVo;
    }

    @Override
    public FilmVo getSoonFilm(boolean isLimit, int nums,int nowPage,int sortId,int sourceId,int yearId,int catId) {
        FilmVo filmVo = new FilmVo();
        List<FilmInfo> filmInfos = new ArrayList<FilmInfo>();

        //即将上映影片限制条件
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","2");
        //判断是否是首页需要的内容
        if(isLimit) { //是   限制内容  限制内容为即将上映影片
            Page<MoocFilmT> page = new Page<MoocFilmT>(1,8);
            List<MoocFilmT> moocFilmT = moocFilmTMapper.selectPage(page,entityWrapper);
            filmInfos = getFilmInfos(moocFilmT);
            filmVo.setFilmInfo(filmInfos);
            filmVo.setFilmNum(filmInfos.size());

        }else {
            //否   列表页面  限制内容为即将上映影片
            Page<MoocFilmT> page = null;//new Page<MoocFilmT>(nowPage,nums);

            //根据SourceId不同创建page对象
            //影片状态,1-正在热映，2-即将上映，3-经典影片
            switch(sourceId) {
                case 1 :
                    page = new Page<MoocFilmT>(nowPage,nums,"film_preSaleNum");
                    break;
                case 2 :
                    page = new Page<MoocFilmT>(nowPage,nums,"film_time");
                    break;
                case 3 :
                    page = new Page<MoocFilmT>(nowPage,nums,"film_preSaleNum");
                    break;
                default:
                    page = new Page<MoocFilmT>(nowPage,nums,"film_preSaleNum");
                    break;
            }



            //如果yearID ，sourceId，catId不能为99，则表示要按照对应的编号进行查询
            if(sourceId!=99){
                entityWrapper.eq("film_score",sourceId);
            }
            if(yearId!=99){
                entityWrapper.eq("film_date",yearId);
            }
            if(catId!=99){
                //#2#4#22#
                String catStr = "%#"+catId+"#%";
                entityWrapper.like("film_cats",catStr);
            }
            List<MoocFilmT> moocFilmT = moocFilmTMapper.selectPage(page,entityWrapper);
            filmInfos = getFilmInfos(moocFilmT);

            //总页数  totalCounts/nums

            int totalCounts = moocFilmTMapper.selectCount(entityWrapper);
            int totalPages = (totalCounts/nums)+1;
            filmVo.setFilmNum(filmInfos.size());
            filmVo.setFilmInfo(filmInfos);
            filmVo.setNowPage(nowPage);
            filmVo.setTotalPage(totalPages);

        }
        return filmVo;
    }

    @Override
    public FilmVo getClassiclFilm(int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {
        FilmVo filmVo = new FilmVo();
        List<FilmInfo> filmInfos = new ArrayList<FilmInfo>();

        //即将上映影片限制条件
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","3");
        //判断是否是首页需要的内容

            //否   列表页面  限制内容为即将上映影片
            Page<MoocFilmT> page = null;new Page<MoocFilmT>(nowPage,nums);

        //根据SourceId不同创建page对象
        //影片状态,1-正在热映，2-即将上映，3-经典影片
        switch(sourceId) {
            case 1 :
                page = new Page<MoocFilmT>(nowPage,nums,"film_box_office");
                break;
            case 2 :
                page = new Page<MoocFilmT>(nowPage,nums,"film_time");
                break;
            case 3 :
                page = new Page<MoocFilmT>(nowPage,nums,"film_score");
                break;
            default:
                page = new Page<MoocFilmT>(nowPage,nums,"film_box_office");
                break;
        }
            //如果yearID ，sourceId，catId不能为99，则表示要按照对应的编号进行查询
            if(sourceId!=99){
                entityWrapper.eq("film_score",sourceId);
            }
            if(yearId!=99){
                entityWrapper.eq("film_date",yearId);
            }
            if(catId!=99){
                //#2#4#22#
                String catStr = "%#"+catId+"#%";
                entityWrapper.like("film_cats",catStr);
            }
            List<MoocFilmT> moocFilmT = moocFilmTMapper.selectPage(page,entityWrapper);
            filmInfos = getFilmInfos(moocFilmT);

            //总页数  totalCounts/nums

            int totalCounts = moocFilmTMapper.selectCount(entityWrapper);
            int totalPages = (totalCounts/nums)+1;
            filmVo.setFilmNum(filmInfos.size());
            filmVo.setFilmInfo(filmInfos);
            filmVo.setNowPage(nowPage);
            filmVo.setTotalPage(totalPages);


        return filmVo;
    }

    @Override
    public List<FilmInfo> getBoxRanking() {
        //--->条件  正在上映的前10名
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","1");
        Page<MoocFilmT> page = new Page<MoocFilmT>(1,10,"film_box_office");
        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);
        List<FilmInfo> filmInfos = getFilmInfos(moocFilms);
        return filmInfos;
    }

    @Override
    public List <FilmInfo> getExpectRanking() {
        //--->条件  即将上映的前10名
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","2");
        Page<MoocFilmT> page = new Page<MoocFilmT>(1,10,"film_preSaleNum");
        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);
        List<FilmInfo> filmInfos = getFilmInfos(moocFilms);
        return filmInfos;
    }

    @Override
    public List <FilmInfo> getTop() {
        //--->条件  正在上映  评分前10
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","1");
        Page<MoocFilmT> page = new Page<MoocFilmT>(1,10,"film_score");
        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);
        List<FilmInfo> filmInfos = getFilmInfos(moocFilms);
        return filmInfos;
    }

    @Override
    public List <CatVO> getCats() {
        //查询实体对象
        List <CatVO> cats = new ArrayList <>();
        List<MoocCatDictT> moocCats = moocCatDictTMapper.selectList(null);
        //转换实体
        for (MoocCatDictT moocCatDictT:moocCats) {
            CatVO  catVO = new CatVO();
            catVO.setCatId(moocCatDictT.getUuid()+"");
            catVO.setCatName(moocCatDictT.getShowName());
            cats.add(catVO);
        }
        return cats;
    }

    @Override
    public List <SourceVO> getSources() {
        List <SourceVO> sourceVOS = new ArrayList <>();
        //查询实体对象
        List<MoocSourceDictT> moocSourceDictTS = moocSourceDictTMapper.selectList(null);
        for (MoocSourceDictT moocSourceDictT:moocSourceDictTS) {
            SourceVO sourceVO = new SourceVO();
            sourceVO.setSourceId(moocSourceDictT.getUuid()+"");
            sourceVO.setSourceName(moocSourceDictT.getShowName());
            sourceVOS.add(sourceVO);
        }
        return sourceVOS;
    }

    @Override
    public List <YearVO> getYears() {
        List <YearVO> yearVOS = new ArrayList <>();
        //查询实体对象
        List<MoocYearDictT> moocYearDictTS = moocYearDictTMapper.selectList(null);
        for (MoocYearDictT moocYearDictT:moocYearDictTS) {
            YearVO yearVO = new YearVO();
            yearVO.setYearId(moocYearDictT.getUuid()+"");
            yearVO.setYearName(moocYearDictT.getShowName());
            yearVOS.add(yearVO);
        }
        //转换实体
        return yearVOS;
    }

    @Override
    public FilmDetailVO getFilmDetail(int searchType, String serachParam) {
        //searchType  1、按照名称  2、按照ID
        FilmDetailVO filmDetailVO = null;
        if(searchType==1){
            filmDetailVO = moocFilmTMapper.getFilmDetalByName("%"+serachParam+"%");
        }else{
             filmDetailVO = moocFilmTMapper.getFilmDetalById(serachParam);
        }
        return filmDetailVO;
    }

    private  MoocFilmInfoT getFilmInfo(String filmId){
            MoocFilmInfoT moocFilmInfoT = new MoocFilmInfoT();
            moocFilmInfoT.setFilmId(filmId);
            moocFilmInfoT =   moocFilmInfoTMapper.selectOne(moocFilmInfoT);
            return moocFilmInfoT;
    }


    @Override
    public FilmDescVO getFilmDesc(String filmId) {
        MoocFilmInfoT mocFilmInfoT = getFilmInfo(filmId);
        FilmDescVO filmDescVO = new FilmDescVO();
        filmDescVO.setBiography(mocFilmInfoT.getBiography());
        filmDescVO.setFilmId(mocFilmInfoT.getFilmId());
        return filmDescVO;
    }

    @Override
    public ImgVO getImgs(String filmId) {
        //图片地址
        MoocFilmInfoT mocFilmInfoT = getFilmInfo(filmId);
        String filmIms =mocFilmInfoT.getFilmImgs();
        String[] flmImgs = filmIms.split(",");
        ImgVO imgVO = new ImgVO();
        imgVO.setMainImg(flmImgs[0]);
        imgVO.setImg01(flmImgs[1]);
        imgVO.setImg02(flmImgs[2]);
        imgVO.setImg03(flmImgs[3]);
        imgVO.setImg04(flmImgs[4]);
        return imgVO;
    }

    @Override
    public ActorVO getDectInfo(String filmId) {
        MoocFilmInfoT mocFilmInfoT = getFilmInfo(filmId);
        //获取导演编号
        Integer diretId = mocFilmInfoT.getDirectorId();
        MoocActorT moocActorT= moocActorTMapper.selectById(diretId);
        ActorVO actorVO = new ActorVO();
        actorVO.setImgAddress(moocActorT.getActorImg());
        actorVO.setDirectorName(moocActorT.getActorName());
      return actorVO;
    }

    @Override
    public List <ActorVO> getActors(String filmId) {

        List <ActorVO> actorVOS = moocActorTMapper.getActors(filmId);
        return actorVOS;
    }
}
