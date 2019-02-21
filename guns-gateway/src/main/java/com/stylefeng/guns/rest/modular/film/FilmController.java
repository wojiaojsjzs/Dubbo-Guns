package com.stylefeng.guns.rest.modular.film;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.stylefeng.guns.api.film.FilmAPI;


import com.stylefeng.guns.api.film.FilmSyncAPI;

import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.rest.modular.film.vo.FilmConditionVO;
import com.stylefeng.guns.rest.modular.film.vo.FilmIndexVo;
import com.stylefeng.guns.rest.modular.film.vo.FilmRequestVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;


import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@RestController
@RequestMapping("/film/")
public class FilmController {

       @Reference(interfaceClass = FilmAPI.class,check = false)
       private FilmAPI filmAPI;

        @Reference(interfaceClass = FilmSyncAPI.class,async = true,check = false)
        private FilmSyncAPI filmSyncAPI;

       private static final String IMG_PRE = "http://img.meetingshop.cn/";



    /*
        * 获取首页信息接口
        * api网关：
        * 1、接口功能聚合   【api聚合 】
        * 2、同一个接口对外暴露，降低了前后端分离开发的难度和复杂度
        * 坏处：
        *     模块过多 容易出问题
        * */
        @RequestMapping(value="getIndex",method = RequestMethod.GET)
        public ResponseVO<FilmIndexVo> getIndex(){
            FilmIndexVo filmIndexVo = new FilmIndexVo();

            //获取banner信息
           filmIndexVo.setBanners(filmAPI.getBanners());

            //获取正在热映的电影
            filmIndexVo.setHotFilm(filmAPI.getHotFilm(true,8,1,1,99,99,99));
            //获取即将上映的电影
            filmIndexVo.setSoonFilms(filmAPI.getSoonFilm(true,8,1,1,99,99,99));
            //票房排行榜
            filmIndexVo.setBoxRanking(filmAPI.getBoxRanking());
            //获取受欢迎的榜单
            filmIndexVo.setExpectRanking(filmAPI.getExpectRanking());
            //top100 前100
            filmIndexVo.setTop100(filmAPI.getTop());

            return ResponseVO.success(IMG_PRE,filmIndexVo);
        }

        @RequestMapping(value = "getConditionList",method = RequestMethod.GET)
        public ResponseVO getConditionList(@RequestParam(name="catId",required = false,defaultValue = "99")String catId,
                                           @RequestParam(name="sourceId",required = false,defaultValue = "99")String sourceId,
                                           @RequestParam(name="yearId",required = false,defaultValue = "99")String yearId){
            boolean flag = false;

            FilmConditionVO filmConditionVO = new FilmConditionVO();

           //类型集合
            List<CatVO> cats = filmAPI.getCats();
            List<CatVO>  resultCats = new ArrayList <>();
            CatVO  cat  = new CatVO();
            for (CatVO catVO:cats) {
                if(catVO.getCatId().equals("99")){
                    cat =  catVO;
                    continue;
                }
                //判断类型catid 如果存在 将某实体变成active  ，不存在 全部变成Active状态
                if(catVO.getCatId().equals(catId)){
                    flag = true;
                    catVO.setActive(true);
                }
                resultCats.add(catVO);

            }
            if(!flag){
                cat.setActive(true);
                resultCats.add(cat);
            }else{
                cat.setActive(false);
                resultCats.add(cat);
            }
           //片源集合
            flag = false;
            List<SourceVO> sourceVOS = filmAPI.getSources();
            List<SourceVO>  resultsourceVO = new ArrayList <>();
            SourceVO  source  = new SourceVO();
            for (SourceVO sourceVO:sourceVOS) {
                if(sourceVO.getSourceId().equals("99")){
                    source =  sourceVO;
                    continue;
                }
                //判断类型sourceid 如果存在 将某实体变成active  ，不存在 全部变成Active状态
                if(sourceVO.getSourceId().equals(catId)){
                    flag = true;
                    sourceVO.setActive(true);
                }
                resultsourceVO.add(sourceVO);

            }
            if(!flag){
                source.setActive(true);
                resultsourceVO.add(source);
            }else{
                source.setActive(false);
                resultsourceVO.add(source);
            }
           //年代集合
            flag = false;
            List<YearVO> yearVOS = filmAPI.getYears();
            List<YearVO>  resultyearVO = new ArrayList <>();
            YearVO  year  = new YearVO();
            for (YearVO yearVO:yearVOS) {
                if(yearVO.getYearId().equals("99")){
                    year =  yearVO;
                    continue;
                }
                //判断类型sourceid 如果存在 将某实体变成active  ，不存在 全部变成Active状态
                if(yearVO.getYearId().equals(catId)){
                    flag = true;
                    yearVO.setActive(true);
                }
                resultyearVO.add(yearVO);

            }
            if(!flag){
                year.setActive(true);
                resultyearVO.add(year);
            }else{
                year.setActive(false);
                resultyearVO.add(year);
            }
            filmConditionVO.setCatInfo(cats);
            filmConditionVO.setSourceInfo(sourceVOS);
            filmConditionVO.setYearInfo(yearVOS);

            return ResponseVO.success(filmConditionVO);
        }


        @RequestMapping(value="getFilms",method = RequestMethod.GET)
        public ResponseVO getFilms(FilmRequestVO filmRequestVO){
            FilmVo filmVo = null;
            //根据showType判断影片查询类型
            switch (filmRequestVO.getShowType()){
                case 1 :
                    filmVo=filmAPI.getHotFilm(false,filmRequestVO.getPageSize(),
                            filmRequestVO.getNowPage(),
                            filmRequestVO.getSortId(),
                            filmRequestVO.getSourceId(),
                            filmRequestVO.getYearId(),
                            filmRequestVO.getCatId());
                    break;

                case 2 :
                    filmVo=filmAPI.getSoonFilm(false,filmRequestVO.getPageSize(),
                            filmRequestVO.getNowPage(),
                            filmRequestVO.getSortId(),
                            filmRequestVO.getSourceId(),
                            filmRequestVO.getYearId(),
                            filmRequestVO.getCatId());
                    break;
                case 3 :
                    filmVo=filmAPI.getClassiclFilm(
                            filmRequestVO.getPageSize(),
                            filmRequestVO.getNowPage(),
                            filmRequestVO.getSortId(),
                            filmRequestVO.getSourceId(),
                            filmRequestVO.getYearId(),
                            filmRequestVO.getCatId());
                    break;
                default:
                    filmVo=filmAPI.getHotFilm(false,filmRequestVO.getPageSize(),
                            filmRequestVO.getNowPage(),
                            filmRequestVO.getSortId(),
                            filmRequestVO.getSourceId(),
                            filmRequestVO.getYearId(),
                            filmRequestVO.getCatId());
                    break;

            }
            //根据sortId排序

            //添加各种条件查询

            //判断当前是第几页


            return ResponseVO.success(filmVo.getNowPage(),filmVo.getTotalPage(),IMG_PRE,filmVo.getFilmInfo());
        }


        @RequestMapping(value = "/films/{searchParam}",method = RequestMethod.GET)
        public ResponseVO films(@PathVariable("searchParam")String searchParam,
                                int searchType
                                ) throws ExecutionException, InterruptedException {

            //根据searchType，判断查询类型
            FilmDetailVO filmDetailVO = filmAPI.getFilmDetail(searchType,searchParam);
            String filmId = filmDetailVO.getFilmId();
            //不用的查询类型，传入条件会略有不同

            //获取影片描述信息
            filmSyncAPI.getFilmDesc(filmId);
            Future<FilmDescVO> filmDescVOFuture = RpcContext.getContext().getFuture();
            //获取影片图片
            filmSyncAPI.getImgs(filmId);
            Future<ImgVO> imgVOFuture = RpcContext.getContext().getFuture();
            //获取导演信息
            filmSyncAPI.getDectInfo(filmId);
            Future<ActorVO> actorVOFuture = RpcContext.getContext().getFuture();
            //获取演员信息
            filmSyncAPI.getActors(filmId);
            Future<List<ActorVO>> listFuture = RpcContext.getContext().getFuture();


            //组织Actor属性
            InfoRequstVO infoRequstVO = new InfoRequstVO();
            ActorRequestVO actorRequestVO = new ActorRequestVO();
            actorRequestVO.setActors(listFuture.get());
            actorRequestVO.setDirector(actorVOFuture.get());

            infoRequstVO.setActors(actorRequestVO);
            infoRequstVO.setBiography(filmDescVOFuture.get().getBiography());
            infoRequstVO.setFilmId(filmId);
            infoRequstVO.setImgVO(imgVOFuture.get());





            //查询影片详细信息 --->dubbo异步获取特性
            //返回值
            filmDetailVO.setInfo04(infoRequstVO);



            return  ResponseVO.success(IMG_PRE,filmDetailVO);
        }
}

