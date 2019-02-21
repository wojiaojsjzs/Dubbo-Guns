package com.stylefeng.guns.rest.modular.film.vo;

import com.stylefeng.guns.api.film.vo.BannerVo;
import com.stylefeng.guns.api.film.vo.FilmInfo;
import com.stylefeng.guns.api.film.vo.FilmVo;

import java.io.Serializable;
import java.util.List;


public class FilmIndexVo implements Serializable {

    private List<BannerVo> banners;

    private FilmVo hotFilm;

    private FilmVo soonFilms;

    private List<FilmInfo> boxRanking;

    private List<FilmInfo> expectRanking;

    private List<FilmInfo> top100;

    public List <BannerVo> getBanners() {
        return banners;
    }

    public void setBanners(List <BannerVo> banners) {
        this.banners = banners;
    }

    public FilmVo getHotFilm() {
        return hotFilm;
    }

    public void setHotFilm(FilmVo hotFilm) {
        this.hotFilm = hotFilm;
    }

    public FilmVo getSoonFilms() {
        return soonFilms;
    }

    public void setSoonFilms(FilmVo soonFilms) {
        this.soonFilms = soonFilms;
    }

    public List <FilmInfo> getBoxRanking() {
        return boxRanking;
    }

    public void setBoxRanking(List <FilmInfo> boxRanking) {
        this.boxRanking = boxRanking;
    }

    public List <FilmInfo> getExpectRanking() {
        return expectRanking;
    }

    public void setExpectRanking(List <FilmInfo> expectRanking) {
        this.expectRanking = expectRanking;
    }

    public List <FilmInfo> getTop100() {
        return top100;
    }

    public void setTop100(List <FilmInfo> top100) {
        this.top100 = top100;
    }
}
