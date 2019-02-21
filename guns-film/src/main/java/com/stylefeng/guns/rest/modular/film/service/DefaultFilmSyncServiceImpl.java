package com.stylefeng.guns.rest.modular.film.service;

import com.alibaba.dubbo.config.annotation.Service;

import com.stylefeng.guns.api.film.FilmSyncAPI;

import com.stylefeng.guns.api.film.vo.ActorVO;
import com.stylefeng.guns.api.film.vo.FilmDescVO;
import com.stylefeng.guns.api.film.vo.ImgVO;
import com.stylefeng.guns.rest.common.persistence.dao.MoocActorTMapper;
import com.stylefeng.guns.rest.common.persistence.dao.MoocFilmInfoTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocActorT;
import com.stylefeng.guns.rest.common.persistence.model.MoocFilmInfoT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@Service(interfaceClass = FilmSyncAPI.class)
public class DefaultFilmSyncServiceImpl implements FilmSyncAPI {

    @Autowired
    private MoocFilmInfoTMapper moocFilmInfoTMapper;

    @Autowired
    private MoocActorTMapper moocActorTMapper;

    private MoocFilmInfoT getFilmInfo(String filmId){

        MoocFilmInfoT moocFilmInfoT = new MoocFilmInfoT();
        moocFilmInfoT.setFilmId(filmId);
        moocFilmInfoT = moocFilmInfoTMapper.selectOne(moocFilmInfoT);
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
