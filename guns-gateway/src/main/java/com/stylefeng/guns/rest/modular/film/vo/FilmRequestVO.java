package com.stylefeng.guns.rest.modular.film.vo;


import lombok.Data;

import java.io.Serializable;

@Data
public class FilmRequestVO implements Serializable {

    private Integer showType=1;
    private Integer sortId=1;
    private Integer sourceId=99;
    private Integer catId=99;
    private Integer yearId=99;
    private Integer nowPage=1;
    private Integer pageSize=18;

}
