package com.stylefeng.guns.rest.common.persistence.dao;

import com.stylefeng.guns.api.film.vo.FilmDetailVO;
import com.stylefeng.guns.rest.common.persistence.model.MoocFilmT;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 影片主表 Mapper 接口
 * </p>
 *
 * @author zheshen.jin
 * @since 2019-01-25
 */
public interface MoocFilmTMapper extends BaseMapper<MoocFilmT> {
    FilmDetailVO getFilmDetalByName(@Param("filmName") String filmName);

    FilmDetailVO getFilmDetalById(@Param("uuid")String uuid);

}
