package com.stylefeng.guns.api.film;

import com.stylefeng.guns.api.film.vo.*;

import java.util.List;

public interface FilmAPI {
    //获取banners
    List<BannerVo> getBanners();
    //获取热映影片   isLimit 是否限制     有的话 numbers 限制多少
    FilmVo getHotFilm(boolean isLimit,int nums,int nowPage,int sortId,int sourceId,int yearId,int catId);
    //获取即将上映影片[受欢迎程度做排序]
    FilmVo getSoonFilm(boolean isLimit,int nums,int nowPage,int sortId,int sourceId,int yearId,int catId);

    //获取经典影片
    FilmVo getClassiclFilm(int nums,int nowPage,int sortId,int sourceId,int yearId,int catId);

    //获取票房排行
    List<FilmInfo> getBoxRanking();
    //获取人气排行
    List<FilmInfo> getExpectRanking();
    //获取top100
    List<FilmInfo> getTop();
    //获取影片条件==
    //分类条件
    List<CatVO> getCats();
    //片源  欧美 大陆
    List<SourceVO> getSources();
    //获取年代
    List<YearVO> getYears();
    //
    //推荐使用做法：重写代码

    //根据影片ID或者名称获取影片信息
    FilmDetailVO getFilmDetail(int searchType,String serachParam);
    //获取影片相关的其他信息[演员表、图片地址]



    //描述信息
    FilmDescVO getFilmDesc(String filmId);
    //获取图片信息
    ImgVO getImgs(String filmId);
    //演员信息
    //导演信息
    ActorVO getDectInfo(String filmId);
    //演员信息
    List<ActorVO> getActors(String filmId);
}
