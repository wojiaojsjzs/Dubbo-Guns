package com.stylefeng.guns.api.film;

import com.stylefeng.guns.api.film.vo.ActorVO;
import com.stylefeng.guns.api.film.vo.FilmDescVO;
import com.stylefeng.guns.api.film.vo.ImgVO;

import java.util.List;

public interface FilmSyncAPI {

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
